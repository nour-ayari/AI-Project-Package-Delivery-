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

        switch (strategy) {
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
                return null;
        }
    }
}

