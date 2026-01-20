# Résolution Hybride du Démineur (CSP vs Recherche Locale)

Ce projet compare deux approches pour résoudre le jeu du Démineur : une approche **complète** (exacte) via la Programmation par Contraintes en Java et une approche **incomplète** (stochastique) via la Recherche Locale en Python.

## Architecture du Projet

* **Monde Java (`src/`)** : Responsable de la génération des instances, de l'exécution des solveurs exacts (Choco Solver, Backtracking avec Forward Checking) et de l'exportation des données en JSON.
* **Monde Python (`minesweeper-local-search/`)** : Charge les instances JSON et exécute les algorithmes de recherche locale (Hill-Climbing et Recuit Simulé).
* **Analyse (`make_report.py`)** : Fusionne les fichiers CSV de résultats pour générer des graphiques comparatifs dans le dossier `reports/`.

---

## Guide d'exécution

Pour reproduire l'intégralité du benchmark depuis la racine du projet, suivez les étapes ci-dessous.

### 1. Nettoyage des données existantes (Optionnel)
Pour supprimer les anciennes instances, les résultats CSV et les graphiques avant une nouvelle exécution :
```bash
rm -f data/instances/*.json data/results/*.csv reports/*.png reports/*.csv 2>/dev/null
rm -f minesweeper-local-search/*.csv 2>/dev/null

### 2. Génération et Résolution Complète (Java)
Cette étape compile le projet, génère les grilles aléatoires et lance les solveurs Java.

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="fr.m2.minesweeper.ExperimentRunner"

Les instances sont créées dans data/instances/ et les résultats complets dans data/results/results_complete.csv.

### 3. Résolution Locale (Python)
Lance les algorithmes de recherche locale sur les grilles précédemment générées.

```bash
cd minesweeper-local-search
python experiments_local.py
cd ..

Les résultats sont écrits dans minesweeper-local-search/results_local.csv.

### 4. Analyse et Rapport
Génère les graphiques comparatifs basés sur les temps d'exécution et les taux de succès.

```bash
python make_report.py
Les rapports finaux sont consultables dans le dossier reports/.

## Détails des Algorithmes implémentés
Méthodes Complètes (Java)
Choco Solver : Utilise la propagation de contraintes et l'heuristique de choix de variable WDEG (Weighted Degree).
Backtracking avec Forward Checking : Algorithme maison qui anticipe les échecs en vérifiant les bornes locales des mines (S≤K≤S+U) pour chaque indice.
Méthodes Incomplètes (Python)
Hill-Climbing : Recherche locale gloutonne visant à minimiser une fonction de coût basée sur l'écart entre les mines posées et les indices visibles.
Recuit Simulé : Utilise une probabilité d'acceptation de Boltzmann pour autoriser des mouvements dégradant temporairement la solution afin d'échapper aux optima locaux.

## Configuration du Benchmark
Le projet évalue les performances sur deux tailles de grilles (8x8 et 12x12) avec trois niveaux de densité d'information (keepProbability : 0.3, 0.5, 0.7). Un timeout de 10 secondes est appliqué aux solveurs Java pour gérer l'explosion combinatoire sur les instances complexes.
