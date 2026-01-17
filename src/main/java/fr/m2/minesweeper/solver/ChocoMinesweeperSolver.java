package fr.m2.minesweeper.solver;

import fr.m2.minesweeper.model.MinesweeperInstance;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.List;

public class ChocoMinesweeperSolver {

    public enum Strategy {
        DEFAULT,          // ordre d'entrée + LB (0 puis 1)
        WDEG_MINE_FIRST   // domOverWDeg + essayer 1 avant 0
    }

    public static class ExperimentResult {
        public final int numberOfSolutions;
        public final long timeMs;
        public final boolean timeoutReached;

        public ExperimentResult(int numberOfSolutions, long timeMs, boolean timeoutReached) {
            this.numberOfSolutions = numberOfSolutions;
            this.timeMs = timeMs;
            this.timeoutReached = timeoutReached;
        }
    }

    /**
     * Trouver UNE solution (démo).
     */
    public int[][] solveOne(MinesweeperInstance inst, Strategy strategy) {
        Model model = new Model("Minesweeper");
        IntVar[][] mines = buildModel(model, inst, true); // boosters ON

        Solver solver = model.getSolver();
        applyStrategy(solver, mines, strategy);

        Solution sol = solver.findSolution();
        if (sol == null) return null;

        int rows = inst.getRows();
        int cols = inst.getCols();

        int[][] grid = new int[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = sol.getIntVal(mines[r][c]);
            }
        }
        return grid;
    }

    /**
     * Énumération pour expériences (limite maxSolutions + time limit).
     */
    public ExperimentResult enumerateForExperiment(MinesweeperInstance inst,
                                                   int maxSolutions,
                                                   Strategy strategy,
                                                   String timeLimit) {

        long start = System.currentTimeMillis();

        Model model = new Model("Minesweeper");
        IntVar[][] mines = buildModel(model, inst, true); // boosters ON

        Solver solver = model.getSolver();
        if (timeLimit != null && !timeLimit.isBlank()) {
            solver.limitTime(timeLimit);
        }

        applyStrategy(solver, mines, strategy);

        int count = 0;
        while (solver.solve()) {
            count++;
            if (count >= maxSolutions) break;
        }

        long end = System.currentTimeMillis();
        boolean timeoutReached = solver.isStopCriterionMet();

        return new ExperimentResult(count, end - start, timeoutReached);
    }

    // -----------------------------
    //        MODEL BUILDING
    // -----------------------------

    private IntVar[][] buildModel(Model model, MinesweeperInstance inst, boolean addBoosters) {

        int rows = inst.getRows();
        int cols = inst.getCols();
        Integer[][] clues = inst.getClues();
        Integer totalMines = inst.getTotalMines();

        IntVar[][] mine = new IntVar[rows][cols];
        List<IntVar> allVars = new ArrayList<>(rows * cols);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                mine[r][c] = model.intVar("m_" + r + "_" + c, 0, 1);
                allVars.add(mine[r][c]);
            }
        }

        // Contraintes indices révélés
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Integer clue = clues[r][c];
                if (clue == null) continue;

                List<IntVar> neigh = neighbors(mine, rows, cols, r, c);
                IntVar[] neighArr = neigh.toArray(new IntVar[0]);

                model.sum(neighArr, "=", clue).post();

                // ---------------- Boosters propagation ----------------
                if (addBoosters) {
                    // clue==0 => tous voisins = 0
                    if (clue == 0) {
                        for (IntVar v : neighArr) model.arithm(v, "=", 0).post();
                    }

                    // clue==deg => tous voisins = 1
                    if (clue == neighArr.length) {
                        for (IntVar v : neighArr) model.arithm(v, "=", 1).post();
                    }
                }
            }
        }

        // total mines connu ?
        if (totalMines != null) {
            model.sum(allVars.toArray(new IntVar[0]), "=", totalMines).post();
        }

        return mine;
    }

    private List<IntVar> neighbors(IntVar[][] mine, int rows, int cols, int r, int c) {
        List<IntVar> neigh = new ArrayList<>();
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int rr = r + dr;
                int cc = c + dc;
                if (0 <= rr && rr < rows && 0 <= cc && cc < cols) {
                    neigh.add(mine[rr][cc]);
                }
            }
        }
        return neigh;
    }

    // -----------------------------
    //        STRATEGIES
    // -----------------------------

    private void applyStrategy(Solver solver, IntVar[][] mines, Strategy strategy) {
        IntVar[] flat = flatten(mines);
    
        switch (strategy) {
            case DEFAULT:
                // ordre d'entrée, essayer 0 puis 1
                solver.setSearch(Search.inputOrderLBSearch(flat));
                break;
    
            case WDEG_MINE_FIRST:
                // Version COMPATIBLE : "fail-first" (petit domaine d'abord)
                // et essayer 1 avant 0 (UB sur bool)
                solver.setSearch(Search.minDomUBSearch(flat));
                break;
    
            default:
                solver.setSearch(Search.inputOrderLBSearch(flat));
        }
    }
    

    private IntVar[] flatten(IntVar[][] mines) {
        int rows = mines.length;
        int cols = mines[0].length;
        IntVar[] flat = new IntVar[rows * cols];
        int k = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                flat[k++] = mines[r][c];
            }
        }
        return flat;
    }
}
