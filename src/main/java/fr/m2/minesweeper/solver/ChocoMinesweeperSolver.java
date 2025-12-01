package fr.m2.minesweeper.solver;

import java.util.ArrayList;
import java.util.List;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;

import fr.m2.minesweeper.model.MinesweeperInstance;
import fr.m2.minesweeper.util.GridPrinter;

/**
 * Construit et résout le modèle CSP du Démineur dans Choco.
 */
public class ChocoMinesweeperSolver {

    /**
     * Résout une instance de Démineur.
     * - Si une solution existe : affiche UNE configuration de mines (0/1)
     *   et vérifie qu'elle respecte bien tous les indices.
     * - Sinon : affiche qu'il n'y a aucune configuration compatible.
     */
    public void solveOnce(MinesweeperInstance instance) {
        int rows = instance.getRows();
        int cols = instance.getCols();
        Integer[][] clues = instance.getClues();
        Integer totalMines = instance.getTotalMines();

        // 1) Création du modèle
        Model model = new Model("Minesweeper CSP");

        // 2) Variables : une booléenne par case (1 = mine, 0 = pas de mine)
        BoolVar[][] mines = model.boolVarMatrix("m", rows, cols);

        // 3) Contraintes : pour chaque case révélée, somme des voisins = indice
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (clues[r][c] != null) {
                    int clue = clues[r][c];
                    BoolVar[] neighbors = getNeighbors(mines, r, c);
                    model.sum(neighbors, "=", clue).post();
                }
            }
        }

        // 4) (Optionnel) contrainte sur le nombre total de mines
        if (totalMines != null) {
            BoolVar[] flat = flatten(mines);
            model.sum(flat, "=", totalMines).post();
        }

        // 5) Résolution
        Solver solver = model.getSolver();
        boolean exists = solver.solve();

        if (!exists) {
            System.out.println("Aucune configuration compatible avec les indices.");
            return;
        }

        // 6) Récupération de la solution
        int[][] sol = new int[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                sol[r][c] = mines[r][c].getValue();
            }
        }

        System.out.println("Une configuration de mines trouvée (1 = mine, 0 = pas de mine) :");
        GridPrinter.printIntGrid(sol);

        // 7) Vérification de la solution par rapport aux indices
        verifySolution(sol, clues);
    }

    /**
     * Vérifie que la solution respecte bien tous les indices fournis.
     */
    private void verifySolution(int[][] sol, Integer[][] clues) {
        int rows = sol.length;
        int cols = sol[0].length;
        boolean ok = true;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (clues[r][c] != null) {
                    int expected = clues[r][c];
                    int actual = countAdjacentMines(sol, r, c);
                    if (expected != actual) {
                        System.out.println("⚠ Incohérence trouvée en (" + r + "," + c + ") : "
                                + "indice = " + expected + ", mines voisines = " + actual);
                        ok = false;
                    }
                }
            }
        }

        if (ok) {
            System.out.println("✅ Solution vérifiée : toutes les contraintes sont satisfaites.");
        } else {
            System.out.println("❌ La solution ne respecte pas tous les indices (voir messages ci-dessus).");
        }
    }

    /**
     * Compte les mines voisines dans la solution (8-connexes) autour de (r,c).
     */
    private int countAdjacentMines(int[][] sol, int r, int c) {
        int rows = sol.length;
        int cols = sol[0].length;
        int count = 0;

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int rr = r + dr;
                int cc = c + dc;
                if (rr >= 0 && rr < rows && cc >= 0 && cc < cols) {
                    if (sol[rr][cc] == 1) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * Renvoie les variables BoolVar des voisins (8-connexes) d'une case (r,c).
     */
    private BoolVar[] getNeighbors(BoolVar[][] mines, int r, int c) {
        int rows = mines.length;
        int cols = mines[0].length;
        List<BoolVar> list = new ArrayList<>(8);

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue; // ignorer la case centrale
                int rr = r + dr;
                int cc = c + dc;
                if (rr >= 0 && rr < rows && cc >= 0 && cc < cols) {
                    list.add(mines[rr][cc]);
                }
            }
        }
        return list.toArray(new BoolVar[0]);
    }

    /**
     * Aplati une matrice 2D de BoolVar en un tableau 1D.
     */
    private BoolVar[] flatten(BoolVar[][] mines) {
        int rows = mines.length;
        int cols = mines[0].length;
        BoolVar[] flat = new BoolVar[rows * cols];
        int k = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                flat[k++] = mines[r][c];
            }
        }
        return flat;
    }
}
