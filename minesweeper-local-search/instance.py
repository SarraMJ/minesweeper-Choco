# instance.py
import random
import json
from typing import List, Optional

GridInt = List[List[int]]
GridOptInt = List[List[Optional[int]]]


class MinesweeperInstance:
    def __init__(self, rows: int, cols: int,
                 clues: GridOptInt,
                 total_mines: Optional[int] = None):
        self.rows = rows
        self.cols = cols
        self.clues = clues
        self.total_mines = total_mines

    # ------------------------------------------------------------------
    # NOUVEAU : lecture d'une instance exportée par Choco (JSON)
    # ------------------------------------------------------------------
    @staticmethod
    def from_json(path: str) -> "MinesweeperInstance":
        """
        Charge une instance EXACTEMENT telle que générée par Choco.
        """
        with open(path, "r") as f:
            data = json.load(f)

        return MinesweeperInstance(
            rows=data["rows"],
            cols=data["cols"],
            clues=data["clues"],
            total_mines=data["total_mines"]
        )

    # ------------------------------------------------------------------
    # Le reste est conservé (utile pour tests / anciennes expériences)
    # ------------------------------------------------------------------
    @staticmethod
    def _count_adjacent_mines(mines: GridInt, r: int, c: int) -> int:
        rows = len(mines)
        cols = len(mines[0])
        count = 0
        for dr in (-1, 0, 1):
            for dc in (-1, 0, 1):
                if dr == 0 and dc == 0:
                    continue
                rr, cc = r + dr, c + dc
                if 0 <= rr < rows and 0 <= cc < cols:
                    if mines[rr][cc] == 1:
                        count += 1
        return count

    @classmethod
    def random_full(cls, rows: int, cols: int,
                    mine_prob: float,
                    seed: int) -> "MinesweeperInstance":
        random.seed(seed)
        mines = [[0 for _ in range(cols)] for _ in range(rows)]
        total = 0
        for r in range(rows):
            for c in range(cols):
                if random.random() < mine_prob:
                    mines[r][c] = 1
                    total += 1

        clues: GridOptInt = [[None for _ in range(cols)] for _ in range(rows)]
        for r in range(rows):
            for c in range(cols):
                if mines[r][c] == 1:
                    clues[r][c] = None
                else:
                    clues[r][c] = cls._count_adjacent_mines(mines, r, c)

        return cls(rows, cols, clues, total_mines=total)

    def with_random_hiding(self, keep_probability: float, seed: int) -> "MinesweeperInstance":
        random.seed(seed)
        new_clues: GridOptInt = [[None for _ in range(self.cols)] for _ in range(self.rows)]
        for r in range(self.rows):
            for c in range(self.cols):
                clue = self.clues[r][c]
                if clue is None:
                    new_clues[r][c] = None
                else:
                    if random.random() < keep_probability:
                        new_clues[r][c] = clue
                    else:
                        new_clues[r][c] = None
        return MinesweeperInstance(self.rows, self.cols, new_clues, self.total_mines)
