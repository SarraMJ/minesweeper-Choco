# experiments_local.py

import time
from instance import MinesweeperInstance
from local_search import hill_climbing


def run_experiment_on_instance(rows: int,
                               cols: int,
                               mine_prob: float,
                               keep_prob: float,
                               n_runs: int = 20):
    """
    Lance n_runs de hill-climbing sur UNE instance fixe
    (même grille complète + même masquage des indices),
    et calcule :
      - taux de succès (coût = 0),
      - coût moyen,
      - temps moyen (ms).
    """
    # 1) Générer une instance cohérente (comme en Java / Choco)
    full = MinesweeperInstance.random_full(
        rows=rows,
        cols=cols,
        mine_prob=mine_prob,
        seed=42  # même seed pour avoir toujours la même instance par taille
    )

    inst = full.with_random_hiding(
        keep_probability=keep_prob,
        seed=123  # même masquage pour tous les runs de cette config
    )

    successes = 0
    total_cost = 0
    total_time = 0.0

    for run in range(n_runs):
        # seed différente par run pour varier le point de départ
        seed = run

        t0 = time.time()
        sol, final_cost = hill_climbing(
            inst,
            max_iters=20_000,  # tu peux augmenter si tu veux
            seed=seed
        )
        t1 = time.time()

        if final_cost == 0:
            successes += 1

        total_cost += final_cost
        total_time += (t1 - t0)

    avg_cost = total_cost / n_runs
    avg_time_ms = (total_time / n_runs) * 1000.0
    success_rate = successes / n_runs

    print(f"Instance {rows}x{cols}, keep={keep_prob}")
    print(f"  Runs            : {n_runs}")
    print(f"  Taux de succès  : {success_rate*100:.1f}%")
    print(f"  Coût moyen      : {avg_cost:.2f}")
    print(f"  Temps moyen     : {avg_time_ms:.1f} ms\n")


def main():
    # Tailles de grilles à tester (comme pour Choco)
    sizes = [8, 12, 16]

    # Proportion d'indices révélés (comme keepProbability côté Java)
    keep_list = [0.3, 0.5, 0.7]

    mine_prob = 0.18  # même densité de mines que dans la voie A
    n_runs = 20       # nombre de runs de hill-climbing par config

    for size in sizes:
        for keep in keep_list:
            run_experiment_on_instance(
                rows=size,
                cols=size,
                mine_prob=mine_prob,
                keep_prob=keep,
                n_runs=n_runs
            )


if __name__ == "__main__":
    main()
