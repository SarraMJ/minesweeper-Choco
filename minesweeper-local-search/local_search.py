# local_search.py
import random
from typing import List, Optional, Tuple
from instance import MinesweeperInstance, GridInt, GridOptInt


def random_solution(inst: MinesweeperInstance, seed: int) -> GridInt:
    random.seed(seed)
    rows, cols = inst.rows, inst.cols
    grid = [[0 for _ in range(cols)] for _ in range(rows)]

    if inst.total_mines is None:
        # aucune contrainte globale : mines aléatoires indépendantes
        for r in range(rows):
            for c in range(cols):
                grid[r][c] = 1 if random.random() < 0.2 else 0
    else:
        # on force le bon nombre total de mines
        indices = [(r, c) for r in range(rows) for c in range(cols)]
        random.shuffle(indices)
        for i in range(inst.total_mines):
            r, c = indices[i]
            grid[r][c] = 1
    return grid


def count_adjacent_mines_in_solution(sol: GridInt, r: int, c: int) -> int:
    rows = len(sol)
    cols = len(sol[0])
    cnt = 0
    for dr in (-1, 0, 1):
        for dc in (-1, 0, 1):
            if dr == 0 and dc == 0:
                continue
            rr, cc = r + dr, c + dc
            if 0 <= rr < rows and 0 <= cc < cols:
                if sol[rr][cc] == 1:
                    cnt += 1
    return cnt


def cost(inst: MinesweeperInstance, sol: GridInt) -> int:
    """Somme des écarts absolus pour toutes les cases avec un indice connu."""
    total = 0
    for r in range(inst.rows):
        for c in range(inst.cols):
            clue = inst.clues[r][c]
            if clue is not None:
                actual = count_adjacent_mines_in_solution(sol, r, c)
                total += abs(actual - clue)
    return total


def hill_climbing(inst: MinesweeperInstance,
                  max_iters: int = 10_000,
                  seed: int = 0) -> Tuple[GridInt, int]:
    """Hill climbing simple : flip de case si ça améliore le coût."""
    random.seed(seed)
    sol = random_solution(inst, seed)
    current_cost = cost(inst, sol)

    rows, cols = inst.rows, inst.cols

    for _ in range(max_iters):
        # choisir une case au hasard
        r = random.randrange(rows)
        c = random.randrange(cols)

        # essayer de la flipper
        old_val = sol[r][c]
        sol[r][c] = 1 - old_val
        new_cost = cost(inst, sol)

        if new_cost <= current_cost:
            # on garde l'amélioration (ou égal)
            current_cost = new_cost
        else:
            # on annule le flip
            sol[r][c] = old_val

        if current_cost == 0:
            break

    return sol, current_cost
