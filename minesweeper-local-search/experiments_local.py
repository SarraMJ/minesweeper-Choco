# experiments_local.py
import time
from instance import MinesweeperInstance
from local_search import hill_climbing


def run_experiment_on_instance(rows: int,
                               cols: int,
                               mine_prob: float,
                               keep_prob: float,
                               n_runs: int = 20):
    # 1) Générer une instance cohérente (comme en Java)
    full = MinesweeperInstance.random_full(rows=rows, cols=cols,
                                           mine_prob=mine_prob, seed=42)
    inst = full.with_random_hiding(keep_probability=keep_prob, seed=123)

    successes = 0
    total_cost = 0
    total_time = 0.0

    for run in range(n_runs):
        seed = run  # seed différente par run

        t0 = time.time()
        sol, final_cost = hill_climbing(inst, max_iters=20_000, seed=seed)
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
    # Quelques expériences de base
    for keep in [0.3, 0.5, 0.7]:
        run_experiment_on_instance(rows=8, cols=8,
                                   mine_prob=0.18,
                                   keep_prob=keep,
                                   n_runs=20)


if __name__ == "__main__":
    main()
