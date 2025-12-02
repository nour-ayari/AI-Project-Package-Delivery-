#  `Heuristics` → Tests prioritaires

Vérifier que les heuristiques retournent les bonnes valeurs.

**Tests possibles** :

1. `heuristic` avec `heuristicId = 1` (Manhattan) → vérifier la distance entre 2 `State`.
2. `heuristic` avec `heuristicId = 2` (Tunnel-aware) → tester :

   * Pas de tunnel → doit retourner Manhattan.
   * Un tunnel → vérifier que le coût via tunnel est correctement calculé.


---

## `Grid` → Tests prioritaires

Vérifier la logique de mouvements, coûts, tunnels et roadblocks.

**Tests possibles** :

1. `getPossibleActions(State s)` :

   * Test sur un coin, bord, et milieu.
   * Vérifier que les actions bloquées par `RoadBlock` sont exclues.
   * Vérifier que `tunnel` apparaît quand approprié.
2. `applyAction(State s, String action)` :

   * Vérifier que chaque action retourne le bon `State`.
3. `getCost(State s, State next, String action)` :

   * Tester les 4 directions et tunnel.
4. `isTunnelEntrance` et `getTunnelExit` :

   * Vérifier que la sortie du tunnel est correcte.
5. `isBlocked` :

   * Vérifier qu’un `RoadBlock` bloque correctement le mouvement.

---

## `GenericSearch` → Tests prioritaires

Objectif : Vérifier que les algorithmes renvoient un plan correct pour une **grille miniature**.

**Tests possibles** :

1. Préparer une petite grille 3x3 ou 4x4 avec départ et objectif connus.
2. Tester tous les algorithmes :

   * `BFS` → vérifier plan minimal.
   * `DFS` → plan correct mais pas nécessairement minimal.
   * `Greedy` et `AStar` → vérifier que plan mène bien au goal et coût correct avec heuristique 1 et 2.
3. Cas limites :

   * Goal inaccessible → plan vide et coût = -1.
   * Start = goal → plan vide et coût = 0.

---

## `DeliverySearch` → Tests

Objectif : Vérifier l’adaptation du `Grid` aux algorithmes via `SearchProblem`.

**Tests possibles** :

1. `initialState()` → retourne bien le départ.
2. `isGoal(State s)` → vrai pour goal, faux pour les autres.
3. `actions(State s)` → doit correspondre à `Grid.getPossibleActions`.
4. `result(State s, String action)` → doit correspondre à `Grid.applyAction`.
5. `stepCost(...)` → doit correspondre à `Grid.getCost`.

---

## `DeliveryPlanner` → Tests

Objectif : Vérifier la planification globale **sans UI**.

**Tests possibles** :

1. Grille simple avec 1 store et 1 destination → vérifier que plan correct.
2. Multiple destinations → vérifier que toutes sont assignées.
3. Destination impossible → vérifier message de non-reachable.
4. Vérifier que la fonction retourne une chaîne de texte cohérente avec le plan.

---