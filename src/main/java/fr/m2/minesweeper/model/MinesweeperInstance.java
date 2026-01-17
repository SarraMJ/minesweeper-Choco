package fr.m2.minesweeper.model;

import java.util.Random;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Représente une instance de Démineur.
 * - rows, cols : dimensions de la grille.
 * - clues[r][c] : valeur 0..8 si la case est révélée, sinon null (case inconnue).
 * - totalMines : (optionnel) nombre total de mines si on veut le fixer (sinon null).
 */
public class MinesweeperInstance {

    public int rows;
    public int cols;
    public Integer[][] clues;   // null si pas de chiffre connu
    public Integer totalMines;  // peut être null

    public MinesweeperInstance(int rows, int cols, Integer[][] clues, Integer totalMines) {
        this.rows = rows;
        this.cols = cols;
        this.clues = clues;
        this.totalMines = totalMines;
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public Integer[][] getClues() { return clues; }
    public Integer getTotalMines() { return totalMines; }

    /**
     * Petite instance 3x3 d'exemple.
     *
     * ? 3 ?
     * 2 ? ?
     * ? ? ?
     */
    public static MinesweeperInstance smallExample() {
        int rows = 3;
        int cols = 3;
        Integer[][] clues = new Integer[rows][cols];

        clues[0][1] = 3;
        clues[1][0] = 2;

        Integer totalMines = null;
        return new MinesweeperInstance(rows, cols, clues, totalMines);
    }

    /**
     * Instance aléatoire "complètement révélée".
     */
    public static MinesweeperInstance randomFullInstance(int rows, int cols,
                                                         double mineProbability,
                                                         long seed) {
        boolean[][] mines = new boolean[rows][cols];
        Random rng = new Random(seed);
        int totalMines = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (rng.nextDouble() < mineProbability) {
                    mines[r][c] = true;
                    totalMines++;
                } else {
                    mines[r][c] = false;
                }
            }
        }

        Integer[][] clues = new Integer[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (mines[r][c]) {
                    clues[r][c] = null;
                } else {
                    clues[r][c] = countAdjacentMines(mines, r, c);
                }
            }
        }

        return new MinesweeperInstance(rows, cols, clues, totalMines);
    }

    private static int countAdjacentMines(boolean[][] mines, int r, int c) {
        int rows = mines.length;
        int cols = mines[0].length;
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int rr = r + dr;
                int cc = c + dc;
                if (rr >= 0 && rr < rows && cc >= 0 && cc < cols) {
                    if (mines[rr][cc]) count++;
                }
            }
        }
        return count;
    }

    public static MinesweeperInstance random8x8(long seed) {
        return randomFullInstance(8, 8, 0.18, seed);
    }

    public static MinesweeperInstance random16x16(long seed) {
        return randomFullInstance(16, 16, 0.18, seed);
    }

    /**
     * Masquage partiel des indices.
     */
    public MinesweeperInstance withRandomHiding(double keepProbability, long seed) {
        Integer[][] newClues = new Integer[rows][cols];
        Random rng = new Random(seed);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Integer clue = clues[r][c];
                if (clue == null) {
                    newClues[r][c] = null;
                } else {
                    newClues[r][c] = (rng.nextDouble() < keepProbability) ? clue : null;
                }
            }
        }

        return new MinesweeperInstance(rows, cols, newClues, totalMines);
    }

    public void exportToJson(String filename) throws IOException {
        FileWriter fw = new FileWriter(filename);
        fw.write("{\n");
        fw.write("  \"rows\": " + rows + ",\n");
        fw.write("  \"cols\": " + cols + ",\n");
        fw.write("  \"clues\": [\n");

        for (int r = 0; r < rows; r++) {
            fw.write("    [");
            for (int c = 0; c < cols; c++) {
                if (clues[r][c] == null) fw.write("null");
                else fw.write(clues[r][c].toString());
                if (c < cols - 1) fw.write(", ");
            }
            fw.write("]");
            if (r < rows - 1) fw.write(",");
            fw.write("\n");
        }

        fw.write("  ],\n");
        fw.write("  \"total_mines\": ");
        fw.write(totalMines == null ? "null\n" : totalMines + "\n");
        fw.write("}\n");
        fw.close();
    }
}
