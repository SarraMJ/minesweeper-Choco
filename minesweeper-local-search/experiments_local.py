# experiments_local.py
import os
import time
import csv

from instance import MinesweeperInstance
from local_search import (
    hill_climbing,
    hill_climbing_with_restarts,
    simulated_annealing
)

N_RUNS = 10
OUTPUT_CSV = os.path.join("..", "data", "results", "results_local.csv")
os.makedirs(os.path.dirname(OUTPUT_CSV), exist_ok=True)



def run_solvers_on_instance(inst: MinesweeperInstance, instance_name: str, writer):
    methods = [
        ("HC", lambda seed: hill_climbing(inst, seed=seed)),
        ("HC+restarts", lambda seed: hill_climbing_with_restarts(
            inst, seed=seed, max_iters=5000, restarts=5, walk_prob=0.05
        )),
        ("SA", lambda seed: simulated_annealing(
            inst, seed=seed, max_iters=40000, t0=5.0, cooling=0.9995
        )),
    ]

    print(f"\n=== Instance {instance_name} ===")

    for name, solver in methods:
        successes = 0
        total_cost = 0
        total_time = 0.0

        for run in range(N_RUNS):
            t0 = time.time()
            _, final_cost = solver(run)
            t1 = time.time()

            if final_cost == 0:
                successes += 1

            total_cost += final_cost
            total_time += (t1 - t0)

        avg_cost = total_cost / N_RUNS
        avg_time_ms = (total_time / N_RUNS) * 1000.0
        success_rate = successes / N_RUNS

        writer.writerow([
            instance_name,
            name,
            success_rate,
            avg_cost,
            avg_time_ms
        ])

        print(
            f"{name:12s} | "
            f"success={success_rate*100:5.1f}% | "
            f"avg_cost={avg_cost:6.2f} | "
            f"avg_time={avg_time_ms:7.1f} ms"
        )


def main():
    instance_files = sorted(
        f for f in os.listdir("../data/instances")
        if f.startswith("instance_") and f.endswith(".json")
    )

    if not instance_files:
        print(" Aucun fichier instance_*.json trouvé.")
        return

    with open(OUTPUT_CSV, "w", newline="") as f:
        writer = csv.writer(f)
        writer.writerow([
            "instance",
            "solver",
            "success_rate",
            "avg_cost",
            "avg_time_ms"
        ])

        for filename in instance_files:
            inst = MinesweeperInstance.from_json(os.path.join("..", "data", "instances", filename))

            run_solvers_on_instance(inst, filename, writer)

    print("\n results_local.csv généré")


if __name__ == "__main__":
    main()
