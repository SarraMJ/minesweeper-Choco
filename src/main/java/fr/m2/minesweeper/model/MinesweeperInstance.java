package fr.m2.minesweeper.model;

import java.util.Random;

/**
 * Représente une instance de Démineur.
 * - rows, cols : dimensions de la grille.
 * - clues[r][c] : valeur 0..8 si la case est révélée, sinon null (case inconnue).
 * - totalMines : (optionnel) nombre total de mines si on veut le fixer (sinon null).
 */
public class MinesweeperInstance {

    private final int rows;
    private final int cols;
    private final Integer[][] clues;   // null si pas de chiffre connu
    private final Integer totalMines;  // peut être null

    public MinesweeperInstance(int rows, int cols, Integer[][] clues, Integer totalMines) {
        this.rows = rows;
        this.cols = cols;
        this.clues = clues;
        this.totalMines = totalMines;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public Integer[][] getClues() {
        return clues;
    }

    public Integer getTotalMines() {
        return totalMines;
    }

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

        clues[0][1] = 3;  // ligne 0, colonne 1
        clues[1][0] = 2;  // ligne 1, colonne 0

        Integer totalMines = null; // on ne fixe pas le nombre total de mines
        return new MinesweeperInstance(rows, cols, clues, totalMines);
    }

    /**
     * Instance aléatoire "complètement révélée" :
     * - On place des mines aléatoirement avec une probabilité donnée.
     * - On calcule les chiffres autour de chaque case sans mine.
     * - On connaît également le nombre total de mines.
     */
    public static MinesweeperInstance randomFullInstance(int rows, int cols,
                                                         double mineProbability,
                                                         long seed) {
        boolean[][] mines = new boolean[rows][cols];
        Random rng = new Random(seed);
        int totalMines = 0;

        // 1) Placement aléatoire des mines
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

        // 2) Calcul des chiffres (clues) pour les cases sans mine
        Integer[][] clues = new Integer[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (mines[r][c]) {
                    // Case avec mine => on peut mettre null (pas de chiffre révélé)
                    clues[r][c] = null;
                } else {
                    int count = countAdjacentMines(mines, r, c);
                    clues[r][c] = count;
                }
            }
        }

        return new MinesweeperInstance(rows, cols, clues, totalMines);
    }

    /**
     * Compte le nombre de mines dans les cases voisines (8-connexes) de (r,c).
     */
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
                    if (mines[rr][cc]) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * Raccourcis pratiques pour 8x8 et 16x16.
     */
    public static MinesweeperInstance random8x8(long seed) {
        // densité de mines ~ 18%
        return randomFullInstance(8, 8, 0.18, seed);
    }

    public static MinesweeperInstance random16x16(long seed) {
        // densité de mines ~ 18% aussi (tu peux ajuster)
        return randomFullInstance(16, 16, 0.18, seed);
    }
    /**
     * Crée une nouvelle instance à partir de celle-ci
     * où certains indices (clues) sont "cachés" aléatoirement.
     *
     * keepProbability = probabilité de garder un indice connu.
     * Ex: 0.5 => ~50% des indices restent visibles, le reste devient null.
     *
     * Les cases déjà null restent null.
     * Le nombre total de mines est conservé tel quel.
     */
    public MinesweeperInstance withRandomHiding(double keepProbability, long seed) {
        Integer[][] newClues = new Integer[rows][cols];
        Random rng = new Random(seed);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Integer clue = clues[r][c];
                if (clue == null) {
                    // déjà inconnu (ou mine non révélée) -> on laisse null
                    newClues[r][c] = null;
                } else {
                    // on décide aléatoirement de le garder ou pas
                    if (rng.nextDouble() < keepProbability) {
                        newClues[r][c] = clue; // on garde l'indice
                    } else {
                        newClues[r][c] = null; // on le cache
                    }
                }
            }
        }

        // On garde le même totalMines (comme dans beaucoup de versions du jeu : nombre de mines connu)
        return new MinesweeperInstance(rows, cols, newClues, totalMines);
    }
}
