package code;

import java.util.List;

public class DeliverySearch implements SearchProblem {

    private final State start;
    private final State goal;
    private final Grid grid;

    public DeliverySearch(State start, State goal, Grid grid) {
        this.start = start;
        this.goal = goal;
        this.grid = grid;
    }

    public State getGoal() {
        return goal;
    }

    public Grid getGrid() {
        return grid;
    }

    // ---------------------------------------------------
    // SearchProblem implementation
    // ---------------------------------------------------

    @Override
    public State initialState() {
        return start;
    }

    @Override
    public boolean isGoal(State s) {
        return s.equals(goal);
    }

    @Override
    public List<String> actions(State s) {
        return grid.getPossibleActions(s);
    }

    @Override
    public State result(State s, String action) {
        return grid.applyAction(s, action);
    }

    @Override
    public int stepCost(State s, String action, State next) {
        return grid.getCost(s, next, action);
    }

    // ---------------------------------------------------
    // SOLVE (called by DeliveryPlanner)
    // ---------------------------------------------------
    public static SearchResult solve(State start, State goal, Grid grid, String strategy) {

        DeliverySearch problem = new DeliverySearch(start, goal, grid);

        // Normalize strategy name (support both short codes and full names)
        String normalizedStrategy = normalizeStrategy(strategy);

        switch (normalizedStrategy) {
            case "BF":
                return GenericSearch.BFS(problem);
            case "DF":
                return GenericSearch.DFS(problem);
            case "UC":
                return GenericSearch.UCS(problem);
            case "ID":
                return GenericSearch.ID(problem);
            case "G1":
                return GenericSearch.Greedy(problem, 1);
            case "G2":
                return GenericSearch.Greedy(problem, 2);
            case "AS1":
                return GenericSearch.AStar(problem, 1);
            case "AS2":
                return GenericSearch.AStar(problem, 2);
            default:
                System.out.println("Unknown strategy " + strategy);
                return null;
        }
    }

    /**
     * Normalize strategy names from Angular UI to backend codes
     */
    private static String normalizeStrategy(String strategy) {
        if (strategy == null) {
            return "BF"; // default
        }
        
        // Convert to uppercase for case-insensitive matching
        String upper = strategy.toUpperCase();
        
        // Map full names to short codes
        switch (upper) {
            case "BFS":
            case "BREADTH-FIRST":
            case "BF":
                return "BF";
            case "DFS":
            case "DEPTH-FIRST":
            case "DF":
                return "DF";
            case "UCS":
            case "UNIFORM-COST":
            case "UC":
                return "UC";
            case "ID":
            case "ITERATIVE-DEEPENING":
                return "ID";
            case "GREEDY":
            case "GREEDY1":
            case "G1":
                return "G1";
            case "GREEDY2":
            case "G2":
                return "G2";
            case "ASTAR":
            case "A*":
            case "A-STAR":
            case "AS1":
                return "AS1";
            case "ASTAR2":
            case "A*2":
            case "AS2":
                return "AS2";
            default:
                // If already in short form, return as-is
                if (upper.matches("(BF|DF|UC|ID|G1|G2|AS1|AS2)")) {
                    return upper;
                }
                System.out.println("Unknown strategy '" + strategy + "', defaulting to BFS");
                return "BF";
        }
    }
}

