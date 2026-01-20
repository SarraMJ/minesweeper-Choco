package fr.m2.minesweeper;

import fr.m2.minesweeper.model.MinesweeperInstance;
import fr.m2.minesweeper.solver.ChocoMinesweeperSolver;
import fr.m2.minesweeper.solver.ChocoMinesweeperSolver.Strategy;
import fr.m2.minesweeper.solver.ChocoMinesweeperSolver.ExperimentResult;

public class Main {

    public static void main(String[] args) {

        // Générer une instance complète puis masquer
        MinesweeperInstance full = MinesweeperInstance.random8x8(42L);
        MinesweeperInstance partial = full.withRandomHiding(0.5, 123L);

        // Afficher la grille d'indices visibles
        System.out.println("Grille d'indices visibles ('.' = rien / inconnu) :");
        printClues(partial);

        // Solveur Choco
        ChocoMinesweeperSolver solver = new ChocoMinesweeperSolver();

        
        System.out.println("\n--- SolveOne avec stratégie DEFAULT ---");
        int[][] sol = solver.solveOne(partial, Strategy.DEFAULT);
        if (sol == null) {
            System.out.println("Aucune solution trouvée.");
        } else {
            printMines(sol);
        }

        
        System.out.println("\n--- Énumération (max 20 solutions) ---");
        ExperimentResult res = solver.enumerateForExperiment(
                partial,
                20,                      // maxSolutions
                Strategy.WDEG_MINE_FIRST, // stratégie
                "1s"                     // time limit (ex: "500ms", "2s", ou "" pour none)
        );

        System.out.println("Solutions trouvées: " + res.numberOfSolutions);
        System.out.println("Temps (ms): " + res.timeMs);
        System.out.println("Timeout atteint ? " + res.timeoutReached);
    }

    private static void printClues(MinesweeperInstance instance) {
        Integer[][] clues = instance.getClues();
        int rows = instance.getRows();
        int cols = instance.getCols();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (clues[r][c] == null) System.out.print(". ");
                else System.out.print(clues[r][c] + " ");
            }
            System.out.println();
        }
    }

    private static void printMines(int[][] mines) {
        System.out.println("Une configuration de mines (1=mine, 0=pas mine) :");
        for (int r = 0; r < mines.length; r++) {
            for (int c = 0; c < mines[0].length; c++) {
                System.out.print(mines[r][c] + " ");
            }
            System.out.println();
        }
    }
}
