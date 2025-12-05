package code;

public class Heuristics {

    private static int manhattan(State a, State b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    /**
     * Heuristic 1: Manhattan distance (admissible)
     * Heuristic 2: Traffic-aware admissible heuristic
     * Uses Manhattan distance weighted by minimal traffic along x and y axes
     */
public static int heuristic(SearchProblem problem, State s, int heuristicId) {
    if (!(problem instanceof DeliverySearch)) return 0;

    DeliverySearch ds = (DeliverySearch) problem;
    State goal = ds.getGoal();
    Grid grid = ds.getGrid();

    if (s.equals(goal)) return 0;

    if (heuristicId == 1) {
        return manhattan(s, goal);
    } else {
        int dx = Math.abs(s.x - goal.x);
        int dy = Math.abs(s.y - goal.y);

        int minXCost = Integer.MAX_VALUE;
        if (dx > 0) {
            int startX = Math.min(s.x, goal.x);
            int endX = Math.max(s.x, goal.x);
            int y = s.y;
            if (goal.x > s.x) {
                for (int x = startX; x < endX; x++) {
                    minXCost = Math.min(minXCost, grid.traffic[y][x][3]);
                }
            } else {
                for (int x = startX + 1; x <= endX; x++) {
                    minXCost = Math.min(minXCost, grid.traffic[y][x][2]);
                }
            }
        }
        int minYCost = Integer.MAX_VALUE;
        if (dy > 0) {
            int startY = Math.min(s.y, goal.y);
            int endY = Math.max(s.y, goal.y);
            int x = s.x;
            if (goal.y > s.y) {
                for (int y = startY; y < endY; y++) {
                    minYCost = Math.min(minYCost, grid.traffic[y][x][1]);
                }
            } else {
                for (int y = startY + 1; y <= endY; y++) {
                    minYCost = Math.min(minYCost, grid.traffic[y][x][0]);
                }
            }
        }
        if (minXCost == Integer.MAX_VALUE || minXCost == 0 ) minXCost = 1;
        if (minYCost == Integer.MAX_VALUE || minYCost == 0) minYCost = 1;

        return dx * minXCost + dy * minYCost;
    }
}

}