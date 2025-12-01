package fr.m2.minesweeper.util;

public class GridPrinter {

    /**
     * Affiche une grille d'entiers (0/1 ou chiffres) de façon lisible.
     */
    public static void printIntGrid(int[][] grid) {
        int rows = grid.length;
        int cols = grid[0].length;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                System.out.print(grid[r][c] + " ");
            }
            System.out.println();
        }
    }

    /**
     * Version pour des booléens (true/false).
     */
    public static void printBoolGrid(boolean[][] grid) {
        int rows = grid.length;
        int cols = grid[0].length;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                System.out.print((grid[r][c] ? "1" : "0") + " ");
            }
            System.out.println();
        }
    }
}
