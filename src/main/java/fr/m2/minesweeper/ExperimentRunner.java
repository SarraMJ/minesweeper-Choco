package fr.m2.minesweeper;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import fr.m2.minesweeper.model.MinesweeperInstance;
import fr.m2.minesweeper.solver.ChocoMinesweeperSolver;
import fr.m2.minesweeper.solver.ChocoMinesweeperSolver.ExperimentResult;

public class ExperimentRunner {

    public static void main(String[] args) {
        try {
            runExperiments();
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture du fichier results.csv : " + e.getMessage());
        }
    }

    public static void runExperiments() throws IOException {
        // Tailles de grilles qu'on teste (carrées pour faire simple)
        int[] sizes = {8, 12, 16};

        // Proportions d'indices qu'on garde (grille plus ou moins révélée)
        double[] keepProbabilities = {0.3, 0.5, 0.7};

        // Combien d'instances différentes par combinaison taille / keepProbability
        int nbSeeds = 5;

        // Limite de solutions à énumérer (éviter d'exploser sur des instances avec plein de solutions)
        int maxSolutions = 100;

        // Densité de mines pour la génération initiale (tu peux ajuster)
        double mineProbability = 0.18;

        // Fichier CSV de sortie dans le dossier du projet
        try (PrintWriter out = new PrintWriter(new FileWriter("results.csv"))) {

            // En-tête du CSV
            out.println("size,keepProbability,seed,solutionsFound,timeMs");

            ChocoMinesweeperSolver solver = new ChocoMinesweeperSolver();

            for (int size : sizes) {
                for (double keepProb : keepProbabilities) {
                    for (int s = 0; s < nbSeeds; s++) {

                        long seed = 1000L + s; // juste pour que ce soit reproductible

                        // 1) Générer une instance complète cohérente
                        MinesweeperInstance full = MinesweeperInstance.randomFullInstance(
                                size, size, mineProbability, seed
                        );

                        // 2) Cacher une partie des indices
                        MinesweeperInstance partial = full.withRandomHiding(keepProb, seed + 999);

                        // 3) Lancer la résolution (énumération silencieuse)
                        ExperimentResult res = solver.enumerateForExperiment(partial, maxSolutions);

                        // 4) Écrire une ligne dans le CSV
                        out.printf("%d,%.2f,%d,%d,%d%n",
                                size,
                                keepProb,
                                seed,
                                res.numberOfSolutions,
                                res.timeMs
                        );

                        System.out.println("Fait: size=" + size +
                                " keep=" + keepProb +
                                " seed=" + seed +
                                " -> solutions=" + res.numberOfSolutions +
                                ", time=" + res.timeMs + " ms");
                    }
                }
            }
        }

        System.out.println("Expériences terminées. Résultats dans results.csv");
    }
}
