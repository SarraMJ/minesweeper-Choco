package fr.m2.minesweeper.model;

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
     * Petite instance d’exemple codée en dur :
     *
     * ? 2 ?
     * 1 ? ?
     * ? ? ?
     *
     * On connaît :
     *  - clues[0][1] = 2
     *  - clues[1][0] = 1
     */
    public static MinesweeperInstance smallExample() {
        int rows = 3;
        int cols = 3;
        Integer[][] clues = new Integer[rows][cols];

        // Tout est null par défaut (cases inconnues).
        // On ajoute les deux chiffres connus :
        clues[0][1] = 2;  // ligne 0, colonne 1
        clues[1][0] = 1;  // ligne 1, colonne 0

        Integer totalMines = null; // on ne fixe pas le nombre total de mines
        return new MinesweeperInstance(rows, cols, clues, totalMines);
    }
}
