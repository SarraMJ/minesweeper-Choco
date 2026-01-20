import os
import re
import textwrap
import pandas as pd
import matplotlib.pyplot as plt

REPORT_DIR = "reports"
KEEP_ORDER = [0.30, 0.50, 0.70]
SIZES_KEEP = [8, 12] 

def ensure_dir(path: str):
    os.makedirs(path, exist_ok=True)

def find_first_existing(paths):
    for p in paths:
        if os.path.exists(p):
            return p
    return None

def parse_instance_name(instance_name: str):
    # Format: instance_12x12_keep0.30_seed1000.json
    m = re.match(r"instance_(\d+)x(\d+)_keep([0-9.]+)_seed(\d+)\.json", instance_name.strip())
    if not m:
        return None
    size = int(m.group(1))
    keep = float(m.group(3))
    seed = int(m.group(4))
    return size, keep, seed

def normalize_local(local_raw: pd.DataFrame) -> pd.DataFrame:
    """Normalise les résultats de la recherche locale (Python)."""
    rows = []
    for _, r in local_raw.iterrows():
        parsed = parse_instance_name(str(r["instance"]))
        if not parsed:
            continue
        size, keep, seed = parsed
        rows.append({
            "size": size,
            "keepProbability": keep,
            "seed": seed,
            "approach": f"Incomplete ({r['solver']})",
            "solver": str(r["solver"]),
            "success_rate": float(r["success_rate"]),
            "avg_cost": float(r["avg_cost"]),
            "avg_time_ms": float(r["avg_time_ms"]),
            "solutionsFound": None
        })
    return pd.DataFrame(rows)

def normalize_choco(choco_raw: pd.DataFrame) -> pd.DataFrame:
    """
    Normalise les résultats Java (Choco & Backtracking).
    Correction : utilise 'variant' au lieu de 'strategy'.
    """
    # On vérifie si la colonne s'appelle 'variant' (ton Java) ou 'strategy'
    col_strat = "variant" if "variant" in choco_raw.columns else "strategy"
    
    required = {"size", "keepProbability", "seed", col_strat, "solutionsFound", "timeMs"}
    missing = required - set(choco_raw.columns)
    if missing:
        raise ValueError(f"Complete results missing columns: {missing}. Found: {list(choco_raw.columns)}")

    status_col = "status" if "status" in choco_raw.columns else None

    rows = []
    for _, r in choco_raw.iterrows():
        ok = True
        if status_col is not None:
            ok = str(r[status_col]).strip().upper() == "OK"

        sol = int(r["solutionsFound"])
        # Un succès pour une méthode complète = pas de timeout ET au moins 1 solution
        success = 1.0 if (ok and sol >= 1) else 0.0

        rows.append({
            "size": int(r["size"]),
            "keepProbability": float(r["keepProbability"]),
            "seed": int(r["seed"]),
            "approach": f"Complete ({r['complete_method']})",
            "solver": str(r[col_strat]),
            "success_rate": success,
            "avg_cost": 0.0, 
            "avg_time_ms": float(r["timeMs"]),
            "solutionsFound": sol
        })
    return pd.DataFrame(rows)

def wrap_text(s: str, width: int = 18) -> str:
    return "\n".join(textwrap.wrap(str(s), width=width))

def save_table_png(df: pd.DataFrame, out_path: str):
    # On crée une copie pour l'affichage
    display_df = df.copy()

    # Nettoyage des noms pour l'affichage
    display_df["solver"] = display_df["solver"].astype(str)
    # On réduit un peu la largeur du wrap pour forcer le passage à la ligne
    display_df["solver"] = display_df["solver"].apply(lambda x: wrap_text(x, 12))
    display_df["approach"] = display_df["approach"].apply(lambda x: wrap_text(x, 15))

    # Formatage des nombres
    display_df["success_rate"] = display_df["success_rate"].apply(lambda v: f"{float(v)*100:.0f}%")
    display_df["avg_time_ms"] = display_df["avg_time_ms"].apply(lambda v: f"{float(v):.0f}")
    display_df["avg_cost"] = display_df["avg_cost"].apply(lambda v: f"{float(v):.2f}")
    display_df["solutionsFound"] = display_df["solutionsFound"].apply(
        lambda v: f"{float(v):.1f}" if pd.notna(v) and v != "" else "-"
    )

    # Calcul dynamique de la taille de la figure
    nrows = len(display_df) + 1
    fig_h = max(8, 0.6 * nrows) # On augmente le facteur de hauteur par ligne
    fig, ax = plt.subplots(figsize=(12, fig_h))
    ax.axis("off")

    table = ax.table(
        cellText=display_df.values,
        colLabels=display_df.columns,
        cellLoc="center",
        loc="center"
    )

    table.auto_set_font_size(False)
    table.set_fontsize(10)
    
    # Ajustement manuel des largeurs de colonnes (valeurs entre 0 et 1)
    col_widths = {
        0: 0.05, # size
        1: 0.08, # keepProb
        2: 0.20, # approach (plus large)
        3: 0.20, # solver (plus large)
        4: 0.10, # success
        5: 0.10, # cost
        6: 0.12, # time
        7: 0.12  # solutions
    }

    for (row, col), cell in table.get_celld().items():
        # Appliquer les largeurs définies
        if col in col_widths:
            cell.set_width(col_widths[col])
        
        # Augmenter la hauteur de TOUTES les cellules pour éviter le débordement vertical
        cell.set_height(0.06) 
        
        # Style pour l'en-tête
        if row == 0:
            cell.set_text_props(weight="bold", color="white")
            cell.set_facecolor("#404040") # Gris foncé pour l'en-tête
            cell.set_height(0.08)

    plt.savefig(out_path, dpi=200, bbox_inches="tight", pad_inches=0.1)
    plt.close(fig)

def plot_metric(all_df: pd.DataFrame, metric: str, out_path: str, title: str, ylabel: str):
    plt.figure(figsize=(10, 6))
    # On groupe par approche et solveur pour tracer une ligne par méthode
    for (approach, solver, size), g in all_df.groupby(["approach", "solver", "size"]):
        g = g.sort_values("keepProbability")
        plt.plot(g["keepProbability"], g[metric], marker="o", label=f"{solver} ({approach}) - Size {size}")

    plt.title(title)
    plt.xlabel("keepProbability")
    plt.ylabel(ylabel)
    plt.legend(bbox_to_anchor=(1.05, 1), loc='upper left', fontsize=8)
    plt.grid(True, alpha=0.3)
    plt.tight_layout()
    plt.savefig(out_path, dpi=200)
    plt.close()

def main():
    ensure_dir(REPORT_DIR)

    # Chemins des fichiers
    choco_path = find_first_existing(["data/results/results_complete.csv", "results_complete.csv"])
    local_path = find_first_existing(["data/results/results_local.csv", "results_local.csv"])

    if not choco_path or not local_path:
        print(f"Erreur : fichiers manquants. Choco: {choco_path}, Local: {local_path}")
        return

    # Chargement et Normalisation
    choco_norm = normalize_choco(pd.read_csv(choco_path))
    local_norm = normalize_local(pd.read_csv(local_path))

    # Agrégation (Moyenne sur les seeds)
    combined = pd.concat([choco_norm, local_norm], ignore_index=True)
    final_df = combined.groupby(["size", "keepProbability", "approach", "solver"], as_index=False).mean()
    
    # Filtrage et tri
    final_df = final_df[final_df["size"].isin(SIZES_KEEP)].sort_values(["size", "keepProbability"])

    # Génération des rapports
    final_df.to_csv(os.path.join(REPORT_DIR, "comparison_final.csv"), index=False)
    save_table_png(final_df, os.path.join(REPORT_DIR, "comparison_table.png"))
    
    plot_metric(final_df, "success_rate", os.path.join(REPORT_DIR, "plot_success.png"), "Taux de Succès", "Success Rate (0-1)")
    plot_metric(final_df, "avg_time_ms", os.path.join(REPORT_DIR, "plot_time.png"), "Temps d'exécution", "Time (ms)")
    
    print(f"Rapport généré avec succès dans le dossier '{REPORT_DIR}'")

if __name__ == "__main__":
    main()