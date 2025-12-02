// ------------------------------------------------------
// HEURISTICS (h1 / h2) for DeliverySearch
// ------------------------------------------------------
package code ;
public class Heuristics {
private static int manhattan(State a, State b) {
    return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
}

/**
 * heuristicId:
 *  1 => h1 = Manhattan(s, goal)
 *  2 => h2 = Tunnel-aware lower bound:
 *        min(
 *            d(s,goal),
 *            min over tunnels: d(s,A)+d(A,B)+d(B,goal), d(s,B)+d(A,B)+d(A,goal)
 *        )
 */
public static int heuristic(SearchProblem problem, State s, int heuristicId) {
    if (!(problem instanceof DeliverySearch)) {
        return 0;
    }

    DeliverySearch ds = (DeliverySearch) problem;
    State goal = ds.getGoal();
    Grid grid = ds.getGrid();

    int direct = manhattan(s, goal);
    if (heuristicId == 1) return direct;

    int best = direct;

    for (Tunnel t : grid.tunnels) {
        int tunnelCost = manhattan(t.A, t.B);

        int viaAB = manhattan(s, t.A) + tunnelCost + manhattan(t.B, goal);
        int viaBA = manhattan(s, t.B) + tunnelCost + manhattan(t.A, goal);

        best = Math.min(best, Math.min(viaAB, viaBA));
    }

    return best;
}
}