package fr.m2.minesweeper;

import fr.m2.minesweeper.model.MinesweeperInstance;
import fr.m2.minesweeper.solver.BacktrackingMinesweeperSolver;
import fr.m2.minesweeper.solver.ChocoMinesweeperSolver;
import fr.m2.minesweeper.solver.ChocoMinesweeperSolver.ExperimentResult;
import fr.m2.minesweeper.solver.ChocoMinesweeperSolver.Strategy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ExperimentRunner {

    public static void main(String[] args) {
        try {
            runExperiments();
        } catch (IOException e) {
            System.err.println("Erreur IO : " + e.getMessage());
        }
    }

    public static void runExperiments() throws IOException {

        int[] sizes = {8, 12};
        double[] keepProbabilities = {0.3, 0.5, 0.7};
        int nbSeeds = 5;

        int maxSolutions = 50;        // limite d'énumération pour comparer
        double mineProbability = 0.18;

        String timeLimit = "2s";      // même budget temps pour tous les solveurs complets

        Strategy[] chocoStrategies = new Strategy[]{
                Strategy.DEFAULT,
                Strategy.WDEG_MINE_FIRST
        };

        // --- Dossiers clean ---
        File resultsDir = new File("data/results");
        File instancesDir = new File("data/instances");
        resultsDir.mkdirs();
        instancesDir.mkdirs();

        // CSV complet (2 méthodes complètes)
        String outCsvPath = "data/results/results_complete.csv";

        try (PrintWriter out = new PrintWriter(new FileWriter(outCsvPath))) {

            out.println("size,keepProbability,seed,complete_method,variant,solutionsFound,timeMs,status,instance_file");

            ChocoMinesweeperSolver choco = new ChocoMinesweeperSolver();
            BacktrackingMinesweeperSolver bt = new BacktrackingMinesweeperSolver();

            for (int size : sizes) {
                for (double keepProb : keepProbabilities) {
                    for (int s = 0; s < nbSeeds; s++) {

                        long seed = 1000L + s;

                        // 1) Générer instance complète (mines + indices)
                        MinesweeperInstance full = MinesweeperInstance.randomFullInstance(
                                size, size, mineProbability, seed
                        );

                        // 2) Instance partielle (indices masqués)
                        MinesweeperInstance partial = full.withRandomHiding(keepProb, seed + 999);

                        // 3) Export JSON de l'instance partielle
                        String instanceFile = String.format(
                                "instance_%dx%d_keep%.2f_seed%d.json",
                                size, size, keepProb, seed
                        );
                        String instancePath = "data/instances/" + instanceFile;
                        partial.exportToJson(instancePath);

                        
                        // Méthode complète #1 : CHOCO
                    
                        for (Strategy strat : chocoStrategies) {

                            ExperimentResult res = choco.enumerateForExperiment(
                                    partial, maxSolutions, strat, timeLimit
                            );

                            String status = res.timeoutReached ? "TIMEOUT" : "OK";

                            out.printf("%d,%.2f,%d,%s,%s,%d,%d,%s,%s%n",
                                    size, keepProb, seed,
                                    "CHOCO", strat.name(),
                                    res.numberOfSolutions, res.timeMs, status, instanceFile
                            );

                            System.out.println(
                                    "Fait: size=" + size +
                                    " keep=" + keepProb +
                                    " seed=" + seed +
                                    " method=CHOCO" +
                                    " variant=" + strat.name() +
                                    " -> solutions=" + res.numberOfSolutions +
                                    ", time=" + res.timeMs + " ms" +
                                    " (" + status + ")" +
                                    " instance=" + instanceFile
                            );
                        }

                       
                        // Méthode complète #2 : BACKTRACKING + FC
                        
                        BacktrackingMinesweeperSolver.Result btRes =
                                bt.enumerate(partial, maxSolutions, timeLimit);

                        String btStatus = btRes.timeout ? "TIMEOUT" : "OK";

                        out.printf("%d,%.2f,%d,%s,%s,%d,%d,%s,%s%n",
                                size, keepProb, seed,
                                "BACKTRACKING", "FC_BOUNDS",
                                btRes.solutionsFound, btRes.timeMs, btStatus, instanceFile
                        );

                        System.out.println(
                                "Fait: size=" + size +
                                " keep=" + keepProb +
                                " seed=" + seed +
                                " method=BACKTRACKING" +
                                " variant=FC_BOUNDS" +
                                " -> solutions=" + btRes.solutionsFound +
                                ", time=" + btRes.timeMs + " ms" +
                                " (" + btStatus + ")" +
                                " instance=" + instanceFile
                        );
                    }
                }
            }
        }

        System.out.println("Expériences terminées. Résultats : " + outCsvPath);
        System.out.println("Instances exportées dans : data/instances/");
    }
}
