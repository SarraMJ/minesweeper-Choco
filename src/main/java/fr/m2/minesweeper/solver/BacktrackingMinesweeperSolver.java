package fr.m2.minesweeper.solver;

import fr.m2.minesweeper.model.MinesweeperInstance;

import java.util.*;

/**
 * Solveur COMPLET (backtracking) avec propagation par bornes (forward-checking):
 */
public class BacktrackingMinesweeperSolver {

    public static class Result {
        public final int solutionsFound;
        public final long timeMs;
        public final boolean timeout;

        public Result(int solutionsFound, long timeMs, boolean timeout) {
            this.solutionsFound = solutionsFound;
            this.timeMs = timeMs;
            this.timeout = timeout;
        }
    }

    private MinesweeperInstance inst;
    private int rows, cols;
    private Integer[][] clues;
    private Integer totalMines;

    private int[][] assign;      // -1 unassigned, 0/1 assigned
    private int assignedCount;
    private int assignedMines;

    private long deadlineMs;     // 0 => no deadline
    private int maxSolutions;    // <=0 => no limit
    private int solutions;

    // For heuristic: precompute "degree" of each cell = how many clue-constraints it appears in
    private int[][] degree;

    public Result enumerate(MinesweeperInstance inst, int maxSolutions, String timeLimit) {
        this.inst = inst;
        this.rows = inst.getRows();
        this.cols = inst.getCols();
        this.clues = inst.getClues();
        this.totalMines = inst.getTotalMines();

        this.assign = new int[rows][cols];
        for (int r = 0; r < rows; r++) Arrays.fill(assign[r], -1);

        this.assignedCount = 0;
        this.assignedMines = 0;

        this.maxSolutions = maxSolutions;
        this.solutions = 0;

        long start = System.currentTimeMillis();
        this.deadlineMs = computeDeadline(start, timeLimit);

        buildDegrees();

        boolean timeout = backtrack();

        long end = System.currentTimeMillis();
        return new Result(solutions, end - start, timeout);
    }

    private long computeDeadline(long startMs, String timeLimit) {
        if (timeLimit == null || timeLimit.isBlank()) return 0;
        String s = timeLimit.trim().toLowerCase(Locale.ROOT);

        // formats: "2000ms", "2s"
        try {
            if (s.endsWith("ms")) {
                long ms = Long.parseLong(s.substring(0, s.length() - 2));
                return startMs + ms;
            }
            if (s.endsWith("s")) {
                long sec = Long.parseLong(s.substring(0, s.length() - 1));
                return startMs + sec * 1000L;
            }
        } catch (Exception ignored) {}
        // fallback: no deadline
        return 0;
    }

    private void buildDegrees() {
        degree = new int[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Integer clue = clues[r][c];
                if (clue == null) continue;
                for (int[] nb : neighbors(r, c)) {
                    degree[nb[0]][nb[1]]++;
                }
            }
        }
    }

    private boolean backtrack() {
        
        if (deadlineMs != 0 && System.currentTimeMillis() > deadlineMs) return true; // timeout
        if (maxSolutions > 0 && solutions >= maxSolutions) return false;

        
        if (assignedCount == rows * cols) {
            // final check global mines
            if (totalMines == null || assignedMines == totalMines) {
                // also all local constraints must be satisfied by pruning guarantees
                solutions++;
            }
            return false;
        }

        // choose next var 
        int[] cell = selectUnassignedCell();
        int r = cell[0], c = cell[1];

        // value ordering: try 1 then 0 (mine-first) often helps
        for (int val : new int[]{1, 0}) {
            if (!canAssignGlobal(val)) continue;

            assign[r][c] = val;
            assignedCount++;
            assignedMines += val;

            if (isConsistentAfterAssign(r, c)) {
                boolean timeout = backtrack();
                if (timeout) return true;
            }

            // undo
            assignedMines -= val;
            assignedCount--;
            assign[r][c] = -1;

            if (maxSolutions > 0 && solutions >= maxSolutions) return false;
        }

        return false;
    }

    private boolean canAssignGlobal(int val) {
        if (totalMines == null) return true;

        // if set val=1 we must not exceed totalMines
        if (assignedMines + val > totalMines) return false;

        // also must still be possible to reach totalMines with remaining cells
        int remaining = rows * cols - (assignedCount + 1);
        int minPossible = assignedMines + val;            // all remaining 0
        int maxPossible = assignedMines + val + remaining; // all remaining 1
        return (minPossible <= totalMines && totalMines <= maxPossible);
    }

    /**
     * Check all clue constraints affected: for each revealed clue cell,
     */
    private boolean isConsistentAfterAssign(int rr, int cc) {
        
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Integer clue = clues[r][c];
                if (clue == null) continue;

                int assignedSum = 0;
                int unassigned = 0;

                for (int[] nb : neighbors(r, c)) {
                    int v = assign[nb[0]][nb[1]];
                    if (v == -1) unassigned++;
                    else assignedSum += v;
                }

                // lower bound
                if (assignedSum > clue) return false;
                // upper bound
                if (assignedSum + unassigned < clue) return false;

            
            }
        }

    
        if (totalMines != null) {
            int remaining = rows * cols - assignedCount;
            int minPossible = assignedMines;
            int maxPossible = assignedMines + remaining;
            if (!(minPossible <= totalMines && totalMines <= maxPossible)) return false;
        }

        return true;
    }

    private int[] selectUnassignedCell() {
        int bestR = -1, bestC = -1;
        int bestDeg = -1;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (assign[r][c] != -1) continue;
                int deg = degree[r][c];
                if (deg > bestDeg) {
                    bestDeg = deg;
                    bestR = r;
                    bestC = c;
                }
            }
        }

        // fallback (should not happen)
        if (bestR == -1) return new int[]{0, 0};
        return new int[]{bestR, bestC};
    }

    private List<int[]> neighbors(int r, int c) {
        List<int[]> out = new ArrayList<>(8);
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int rr = r + dr;
                int cc = c + dc;
                if (0 <= rr && rr < rows && 0 <= cc && cc < cols) {
                    out.add(new int[]{rr, cc});
                }
            }
        }
        return out;
    }
}
