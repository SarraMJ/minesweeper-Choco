# main.py
from instance import MinesweeperInstance
from local_search import hill_climbing, cost


def print_grid(grid):
    for row in grid:
        print(" ".join(str(x) for x in row))


def main():
    # même logique que côté Java : on génère une instance cohérente, puis partielle
    full = MinesweeperInstance.random_full(rows=8, cols=8, mine_prob=0.18, seed=42)
    inst = full.with_random_hiding(keep_probability=0.5, seed=123)

    print("Clues (None = inconnu) :")
    for r in range(inst.rows):
        line = []
        for c in range(inst.cols):
            val = inst.clues[r][c]
            line.append("." if val is None else str(val))
        print(" ".join(line))

    sol, final_cost = hill_climbing(inst, max_iters=20_000, seed=0)

    print("\nSolution trouvée (1 = mine) :")
    print_grid(sol)
    print(f"\nCoût final = {final_cost}")

    if final_cost == 0:
        print("✅ configuration cohérente trouvée")
    else:
        print("⚠ pas de solution parfaite trouvée (mais peut-être proche)")


if __name__ == "__main__":
    main()
