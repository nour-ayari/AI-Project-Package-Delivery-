
package code;
public class Heuristics {
public static int manhattanDistance(State a, State b) {
    return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
}

    public static int compute(State n , State goal,Grid grid,int id , int minCost) {
        minCost = grid.findMinCost(grid);
        switch (id) {
            case 1: return heuristic1(n,goal,grid,minCost);
            case 2: return heuristic2(n,goal,grid);
            default: return 0;
        }
    }

    // --------------------------------------------------
    // Heuristic 1 : Manhattan × minCost
    // --------------------------------------------------
public static int heuristic1(State n, State goal, Grid grid, int minCost) {
    int manhattan = manhattanDistance(n, goal);

    return manhattan * minCost;
}


    // --------------------------------------------------
    // Heuristic 2 : Tunnel-aware 
    // --------------------------------------------------
public static int heuristic2(State n, State goal, Grid grid) {

        int best = manhattananDistance(s, goal);

    for (Tunnel t : grid.tunnels) {

         int toA = manhattanDistance(s, t.A);
            int toB = manhattanDistance(s, t.B);

            int fromAtoGoal =manhattananDistance(t.A, goal);
            int fromBtoGoal =manhattananDistance(t.B, goal);

            int viaA = toA + t.cost + fromAtoGoal;
            int viaB = toB + t.cost + fromBtoGoal;


            best = Math.min(best, Math.min(viaA, viaB));
    }

    return best;
    }
}
