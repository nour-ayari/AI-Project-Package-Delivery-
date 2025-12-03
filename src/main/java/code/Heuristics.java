package code;

public class Heuristics {

    private static int manhattan(State a, State b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    /**
     * Heuristic 1: Manhattan distance (admissible)
     * Heuristic 2: Traffic-aware heuristic
     * Combines Manhattan distance + traffic in the direction to goal
     */   
    public static int heuristic(SearchProblem problem, State s, int heuristicId) {
        if (!(problem instanceof DeliverySearch)) return 0;
        
        DeliverySearch ds = (DeliverySearch) problem;
        State goal = ds.getGoal();
        Grid grid = ds.getGrid();

        if (heuristicId==1){
        int direct = manhattan(s, goal);

            return direct;
        } 
        else{

        int x = s.x;
        int y = s.y;
        int cost = 0;

        // Horizontal traffic
        if (goal.x > x) {
            cost += grid.traffic[y][x][3];   // RIGHT
        } else if (goal.x < x) {
            cost += grid.traffic[y][x][2];   // LEFT
        }

        // Vertical traffic
        if (goal.y > y) {
            cost += grid.traffic[y][x][1];   // DOWN
        } else if (goal.y < y) {
            cost += grid.traffic[y][x][0];   // UP
        }

       //added Manhattan to keep admissible behavior
        cost += manhattan(s, goal);

        return cost;
    
    }
}
}
