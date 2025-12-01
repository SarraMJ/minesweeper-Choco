package fr.m2.minesweeper;

import fr.m2.minesweeper.model.MinesweeperInstance;
import fr.m2.minesweeper.solver.ChocoMinesweeperSolver;

public class Main {

    public static void main(String[] args) {
        // 1) Crée une petite instance d'exemple (codée en dur pour commencer)
        MinesweeperInstance instance = MinesweeperInstance.smallExample();

        // 2) Crée le solveur Choco et lance la résolution
        ChocoMinesweeperSolver solver = new ChocoMinesweeperSolver();
        solver.solveOnce(instance);
    }
}
