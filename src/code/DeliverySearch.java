package code;

import java.util.List;

public class DeliverySearch implements SearchProblem {

    private State start;
    private State goal;
    private Grid grid;

    public DeliverySearch(State start, State goal, Grid grid) {
        this.start = start;
        this.goal = goal;
        this.grid = grid;
    }

    // -------------------------
    // État initial
    // -------------------------
    @Override
    public State initialState() {
        return start;
    }

    // -------------------------
    // Test de but
    // -------------------------
    @Override
    public boolean isGoal(State s) {
        return s.x == goal.x && s.y == goal.y;
    }

    // -------------------------
    // Actions possibles (UP/DOWN/LEFT/RIGHT/TUNNEL)
    // -------------------------
    @Override
    public List<String> actions(State s) {
        return grid.getPossibleActions(s);
    }

    // -------------------------
    // Appliquer une action → nouvel état
    // -------------------------
    @Override
    public State result(State s, String action) {
        return grid.applyAction(s, action);
    }

    // -------------------------
    // Coût d’une action
    // -------------------------
    @Override
    public int stepCost(State s, String action, State next) {
        return grid.getCost(s, next, action);
    }

    // --------------------------------------------------------
    // Fonction utilitaire : résoudre un chemin avec un algo
    // --------------------------------------------------------
    public static SearchResult solve(State start, State goal, Grid grid, String strategy) {

        DeliverySearch searchProblem = new DeliverySearch(start, goal, grid);

        switch(strategy) {
            case "BF":  return GenericSearch.BFS(searchProblem);
           case "DF":  return GenericSearch.DFS(searchProblem);
        //    case "ID":  return GenericSearch.ID(searchProblem);
           // case "UC":  return GenericSearch.UCS(searchProblem);
         //   case "GR1": return GenericSearch.Greedy(searchProblem, 1);
       //     case "GR2": return GenericSearch.Greedy(searchProblem, 2);
            //case "AS1": return GenericSearch.AStar(searchProblem, 1);
            //case "AS2": return GenericSearch.AStar(searchProblem, 2);
        }

        return null;
    }
}
