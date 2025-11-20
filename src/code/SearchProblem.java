package code;

import java.util.List;

public interface SearchProblem {
    State initialState();
    boolean isGoal(State s);
    List<String> actions(State s);
    State result(State s, String action);
    int stepCost(State s, String action, State next);
}
