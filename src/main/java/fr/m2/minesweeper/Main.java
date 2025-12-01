package fr.m2.minesweeper;

import fr.m2.minesweeper.model.MinesweeperInstance;
import fr.m2.minesweeper.solver.ChocoMinesweeperSolver;

public class Main {

    public static void main(String[] args) {
        // === Choisis ici la taille de la grille ===

        // 3x3 d'exemple simple :
        //MinesweeperInstance instance = MinesweeperInstance.smallExample();

        // 1) Créer une instance complète 16x16
       // MinesweeperInstance full = MinesweeperInstance.random16x16(42L);
        MinesweeperInstance full = MinesweeperInstance.random8x8(42L);

        // 2) Créer une version partiellement révélée
        //    ici on garde 40% des indices (tu peux jouer avec ce paramètre)
        MinesweeperInstance partial = full.withRandomHiding(0.5, 123L);

        // 3) Afficher la grille d'indices visibles pour comprendre ce qu'on envoie au solveur
        System.out.println("Grille d'indices visibles ('.' = rien / inconnu) :");
        printClues(partial);

        // 4) Lancer la résolution avec Choco sur la grille partielle
        ChocoMinesweeperSolver solver = new ChocoMinesweeperSolver();
        //solver.solveOnce(partial);
        solver.enumerateSolutions(partial, 20);  // par ex. max 20 solutions

    }

    private static void printClues(MinesweeperInstance instance) {
        Integer[][] clues = instance.getClues();
        int rows = instance.getRows();
        int cols = instance.getCols();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (clues[r][c] == null) {
                    System.out.print(". ");
                } else {
                    System.out.print(clues[r][c] + " ");
                }
            }
            System.out.println();
        }
    }
    
}
