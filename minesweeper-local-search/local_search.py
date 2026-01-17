# local_search.py
import math
import random
from typing import List, Tuple, Optional

from instance import MinesweeperInstance

GridInt = List[List[int]]


def random_solution(inst: MinesweeperInstance, seed: int) -> GridInt:
    """Génère une solution aléatoire, en respectant inst.total_mines si renseigné."""
    random.seed(seed)
    rows, cols = inst.rows, inst.cols
    grid = [[0 for _ in range(cols)] for _ in range(rows)]

    if inst.total_mines is None:
        # pas de contrainte globale : mines indépendantes
        for r in range(rows):
            for c in range(cols):
                grid[r][c] = 1 if random.random() < 0.2 else 0
    else:
        # respecter le nombre total de mines
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
    """
    Somme des écarts absolus pour toutes les cases dont l'indice est connu.
    coût = 0 <=> toutes les contraintes sont satisfaites.
    """
    total = 0
    for r in range(inst.rows):
        for c in range(inst.cols):
            clue = inst.clues[r][c]
            if clue is not None:
                actual = count_adjacent_mines_in_solution(sol, r, c)
                total += abs(actual - clue)
    return total


def flip_cell(sol: GridInt, r: int, c: int) -> None:
    sol[r][c] = 1 - sol[r][c]


# ---------------------------------------------------------------------
# Baseline : Hill-Climbing simple (ta version)
# ---------------------------------------------------------------------
def hill_climbing(inst: MinesweeperInstance,
                  max_iters: int = 20_000,
                  seed: int = 0) -> Tuple[GridInt, int]:
    """
    Hill-climbing simple : flip aléatoire, on accepte si améliore (ou égal).
    Incomplet : peut se bloquer dans un optimum local.
    """
    random.seed(seed)
    sol = random_solution(inst, seed)
    current_cost = cost(inst, sol)

    rows, cols = inst.rows, inst.cols

    for _ in range(max_iters):
        r = random.randrange(rows)
        c = random.randrange(cols)

        old_val = sol[r][c]
        sol[r][c] = 1 - old_val
        new_cost = cost(inst, sol)

        if new_cost <= current_cost:
            current_cost = new_cost
        else:
            sol[r][c] = old_val

        if current_cost == 0:
            break

    return sol, current_cost


# ---------------------------------------------------------------------
# Amélioration 1 : Hill-Climbing + random-walk + random restarts
# ---------------------------------------------------------------------
def hill_climbing_with_restarts(inst: MinesweeperInstance,
                                max_iters: int = 20_000,
                                seed: int = 0,
                                restarts: int = 20,
                                walk_prob: float = 0.05) -> Tuple[GridInt, int]:
    """
    Version améliorée (toujours incomplète) :
    - Random restarts : on relance plusieurs fois depuis des états init différents.
    - Random walk : avec probabilité walk_prob, on accepte un flip même s'il dégrade.
      => aide à sortir des optima locaux.

    Retourne la meilleure solution trouvée (coût minimal).
    """
    random.seed(seed)

    best_sol: Optional[GridInt] = None
    best_cost = 10**18

    for k in range(restarts):
        # seed différente par restart
        local_seed = seed * 10_000 + k
        sol = random_solution(inst, local_seed)
        current_cost = cost(inst, sol)

        rows, cols = inst.rows, inst.cols

        for _ in range(max_iters):
            r = random.randrange(rows)
            c = random.randrange(cols)

            old_val = sol[r][c]
            sol[r][c] = 1 - old_val
            new_cost = cost(inst, sol)

            # random-walk : parfois accepter même si pire
            if new_cost <= current_cost or random.random() < walk_prob:
                current_cost = new_cost
            else:
                sol[r][c] = old_val

            if current_cost == 0:
                break

        if current_cost < best_cost:
            best_cost = current_cost
            best_sol = [row[:] for row in sol]  # copie profonde

        if best_cost == 0:
            break

    assert best_sol is not None
    return best_sol, best_cost


# ---------------------------------------------------------------------
# Amélioration 2 : Simulated Annealing (recuit simulé)
# ---------------------------------------------------------------------
def simulated_annealing(inst: MinesweeperInstance,
                        max_iters: int = 100_000,
                        seed: int = 0,
                        t0: float = 5.0,
                        cooling: float = 0.9995) -> Tuple[GridInt, int]:
    """
    Recuit simulé :
    - accepte parfois des détériorations selon exp(-(Δ)/T)
    - T diminue progressivement (cooling < 1)

    Très classique pour les CSP en approche incomplète.
    """
    random.seed(seed)
    sol = random_solution(inst, seed)
    current_cost = cost(inst, sol)

    best_sol = [row[:] for row in sol]
    best_cost = current_cost

    T = t0
    rows, cols = inst.rows, inst.cols

    for _ in range(max_iters):
        if current_cost == 0:
            break

        r = random.randrange(rows)
        c = random.randrange(cols)

        old_val = sol[r][c]
        sol[r][c] = 1 - old_val
        new_cost = cost(inst, sol)

        delta = new_cost - current_cost

        accept = False
        if delta <= 0:
            accept = True
        else:
            # accepter parfois une détérioration
            if T > 1e-12:
                p = math.exp(-delta / T)
                accept = (random.random() < p)

        if accept:
            current_cost = new_cost
            if current_cost < best_cost:
                best_cost = current_cost
                best_sol = [row[:] for row in sol]
        else:
            sol[r][c] = old_val

        # refroidissement
        T *= cooling

    return best_sol, best_cost
