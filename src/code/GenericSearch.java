package code;

import java.util.*;

public class GenericSearch {

    public static SearchResult BFS(SearchProblem problem) {
        State initial = problem.initialState();
        Node root = new Node(initial, null, null, 0, 0);

        Queue<Node> frontier = new LinkedList<>();
        Set<State> visited = new HashSet<>();

        frontier.add(root);
        visited.add(initial);

        int nodesExpanded = 0;

        while (!frontier.isEmpty()) {
            Node current = frontier.poll();
            nodesExpanded++;

            if (problem.isGoal(current.state)) {
                String plan = reconstructPlan(current);
                return new SearchResult(plan, current.pathCost, nodesExpanded);
            }

            for (String action : problem.actions(current.state)) {
                State nextState = problem.result(current.state, action);
                if (!visited.contains(nextState)) {
                    int newCost = current.pathCost + problem.stepCost(current.state, action, nextState);
                    Node child = new Node(nextState, current, action, newCost, current.depth + 1);
                    frontier.add(child);
                    visited.add(nextState);
                }
            }
        }

        return new SearchResult("", -1, nodesExpanded); // no solution found
    }
        // ------------------------------------------------------
        // DEPTH-FIRST SEARCH (DFS)
        // ------------------------------------------------------
        public static SearchResult DFS(SearchProblem problem) {
            State initial = problem.initialState();
            Node root = new Node(initial, null, null, 0, 0);

            Stack<Node> frontier = new Stack<>();
            Set<State> visited = new HashSet<>();

            frontier.push(root);
            int nodesExpanded = 0;

            while (!frontier.isEmpty()) {

                Node current = frontier.pop();
                nodesExpanded++;

                if (problem.isGoal(current.state)) {
                    String plan = reconstructPlan(current);
                    return new SearchResult(plan, current.pathCost, nodesExpanded);
                }

                if (visited.contains(current.state))
                    continue;

                visited.add(current.state);

                for (String action : problem.actions(current.state)) {
                    State next = problem.result(current.state, action);

                    if (!visited.contains(next)) {
                        int newCost = current.pathCost + problem.stepCost(current.state, action, next);
                        Node child = new Node(next, current, action, newCost, current.depth + 1);
                        frontier.push(child);
                    }
                }
            }

            return new SearchResult("", -1, nodesExpanded);
        }

    // ------------------------------------------------------
    // ITERATIVE DEEPENING (ID) — à compléter par votre ami
    // ------------------------------------------------------
    public static SearchResult ID(SearchProblem problem) {
        // TODO: implement later
        return null;
    }

    // ------------------------------------------------------
    // UNIFORM COST SEARCH (UCS) — à compléter par votre ami
    // ------------------------------------------------------
    public static SearchResult UCS(SearchProblem problem) {
        // TODO: implement later
        return null;
    }

    // ------------------------------------------------------
    // GREEDY SEARCH — à compléter
    // ------------------------------------------------------
    public static SearchResult Greedy(SearchProblem problem, int heuristicId) {
        // TODO: implement later
        return null;
    }

        // ------------------------------------------------------
        // A* SEARCH — à compléter
        // ------------------------------------------------------
        public static SearchResult AStar(SearchProblem problem, int heuristicId) {
            // TODO: implement later
            return null;
        }
    private static String reconstructPlan(Node node) {
        List<String> actions = new ArrayList<>();
        while (node.parent != null) {
            actions.add(node.action);
            node = node.parent;
        }
        Collections.reverse(actions);
        return String.join(",", actions);
    }
}
