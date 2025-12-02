package code ;
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
    // ITERATIVE DEEPENING (ID) SEARCH
    // ------------------------------------------------------
    public static SearchResult ID(SearchProblem problem) {
        State initial = problem.initialState();

        int totalNodesExpanded = 0;

        // ID : we will increase limit until solution found or no cutoff occurs
        for (int limit = 0;; limit++) {
            // Depth-limited search LIFO
            Stack<Node> frontier = new Stack<>();
            frontier.push(new Node(initial, null, null, 0, 0));

            boolean cutoffOccurred = false;
            int nodesExpandedThisIter = 0;

            while (!frontier.isEmpty()) {
                Node current = frontier.pop();
                nodesExpandedThisIter++;

                if (problem.isGoal(current.state)) {
                    totalNodesExpanded += nodesExpandedThisIter;
                    String plan = reconstructPlan(current);
                    return new SearchResult(plan, current.pathCost, totalNodesExpanded);
                }

                // If at depth limit, we don't expand but mark a cutoff
                if (current.depth == limit) {
                    cutoffOccurred = true;
                    continue;
                }

                // Expand children ( we will not revisit states on the current path )
                for (String action : problem.actions(current.state)) {
                    State next = problem.result(current.state, action);

                    if (isInPath(next, current))
                        continue; // Avoid cycles along current path

                    int newCost = current.pathCost + problem.stepCost(current.state, action, next);
                    Node child = new Node(next, current, action, newCost, current.depth + 1);
                    frontier.push(child);
                }
            }

            totalNodesExpanded += nodesExpandedThisIter;

            // If there was no cutoff at this depth, further increasing limit won't help (
            // Klawzed )
            if (!cutoffOccurred) {
                return new SearchResult("", -1, totalNodesExpanded);
            }

            // Else increase limit and continue
        }
    }

    // ------------------------------------------------------
    // UNIFORM COST SEARCH (UCS)
    // ------------------------------------------------------
    public static SearchResult UCS(SearchProblem problem) {
        State initial = problem.initialState();
        Node root = new Node(initial, null, null, 0, 0);

        // Priority queue ordered by path cost (lowest first)
        PriorityQueue<Node> frontier = new PriorityQueue<>(Comparator.comparingInt(n -> n.pathCost));

        // Best cost found so far for each state
        Map<State, Integer> bestCost = new HashMap<>();

        frontier.add(root);
        bestCost.put(initial, 0);

        int nodesExpanded = 0;

        while (!frontier.isEmpty()) {
            Node current = frontier.poll();

            // If this node is obsolete (we have a better cost recorded), we need to skip it
            Integer recorded = bestCost.get(current.state);
            if (recorded != null && current.pathCost != recorded)
                continue;

            nodesExpanded++;

            if (problem.isGoal(current.state)) {
                String plan = reconstructPlan(current);
                return new SearchResult(plan, current.pathCost, nodesExpanded);
            }

            for (String action : problem.actions(current.state)) {
                State next = problem.result(current.state, action);
                int newCost = current.pathCost + problem.stepCost(current.state, action, next);

                Integer prev = bestCost.get(next);
                if (prev == null || newCost < prev) {
                    bestCost.put(next, newCost);
                    Node child = new Node(next, current, action, newCost, current.depth + 1);
                    frontier.add(child);
                }
            }
        }

        return new SearchResult("", -1, nodesExpanded);
    }

    // ------------------------------------------------------
    // GREEDY SEARCH (GS)
    // priority = h(state)
    // ------------------------------------------------------
 public static SearchResult Greedy(SearchProblem problem, int heuristicId) {

    State initial = problem.initialState();
    Node root = new Node(initial, null, null, 0, 0);

    // PriorityQueue ordered by heuristic value (lowest h first)
    PriorityQueue<Node> frontier = new PriorityQueue<>(
            Comparator.comparingInt(n -> Heuristics.heuristic(problem, n.state, heuristicId))
    );

    // prevent re-expanding the same state
    Set<State> closed = new HashSet<>();

    frontier.add(root);

    int nodesExpanded = 0;

    // Debug: Initial state information
    System.out.println("Starting Greedy Search with initial state: " + root.state);

    while (!frontier.isEmpty()) {
        Node current = frontier.poll();

        // Debug: Show current node being processed and its heuristic
        System.out.println("Expanding Node: " + current.state);
        System.out.println("Heuristic for " + current.state + ": " + Heuristics.heuristic(problem, current.state, heuristicId));

        // Skip if already expanded
        if (closed.contains(current.state))
            continue;

        closed.add(current.state);
        nodesExpanded++;

        if (problem.isGoal(current.state)) {
            String plan = reconstructPlan(current);
            // Debug: Goal state found, show plan
            System.out.println("Goal reached: " + current.state);
            System.out.println("Plan to goal: " + plan);
            return new SearchResult(plan, current.pathCost, nodesExpanded);
        }

        for (String action : problem.actions(current.state)) {
            State next = problem.result(current.state, action);

            // Debug: Log next state to be expanded
            System.out.println("Next state from " + current.state + " with action " + action + ": " + next);

            if (closed.contains(next))
                continue;

            int newCost = current.pathCost + problem.stepCost(current.state, action, next);
            Node child = new Node(next, current, action, newCost, current.depth + 1);
            frontier.add(child);
        }
    }

    // If we reach here, no solution found
    return new SearchResult("", -1, nodesExpanded);
}

// ------------------------------------------------------
// A* SEARCH — Uses both g(n) and h(n)
// ------------------------------------------------------
public static SearchResult AStar(SearchProblem problem, int heuristicId) {

    State initial = problem.initialState();
    Node root = new Node(initial, null, null, 0, 0);

    // PriorityQueue ordered by f(n) = g(n) + h(n)
    PriorityQueue<Node> frontier = new PriorityQueue<>(
            Comparator.comparingInt(n -> n.pathCost + Heuristics.heuristic(problem, n.state, heuristicId))
    );

    // Best cost found so far for each state (g(n))
    Map<State, Integer> bestG = new HashMap<>();
    bestG.put(initial, 0);

    frontier.add(root);

    int nodesExpanded = 0;

    // Debug: Initial state information
    System.out.println("Starting A* Search with initial state: " + root.state);

    while (!frontier.isEmpty()) {
        Node current = frontier.poll();

        // Debug: Show current node being processed and its f(n) = g(n) + h(n)
        int fCurrent = current.pathCost + Heuristics.heuristic(problem, current.state, heuristicId);
        System.out.println("Expanding Node: " + current.state + " | f(n) = " + fCurrent);
        System.out.println("Heuristic for " + current.state + ": " + Heuristics.heuristic(problem, current.state, heuristicId));

        // Skip if already expanded with a better g(n)
        Integer recorded = bestG.get(current.state);
        if (recorded != null && current.pathCost != recorded)
            continue;

        nodesExpanded++;

        if (problem.isGoal(current.state)) {
            String plan = reconstructPlan(current);
            // Debug: Goal state found, show plan
            System.out.println("Goal reached: " + current.state);
            System.out.println("Plan to goal: " + plan);
            return new SearchResult(plan, current.pathCost, nodesExpanded);
        }

        for (String action : problem.actions(current.state)) {
            State next = problem.result(current.state, action);

            // Debug: Log next state to be expanded
            System.out.println("Next state from " + current.state + " with action " + action + ": " + next);

            int newCost = current.pathCost + problem.stepCost(current.state, action, next);
            Integer prevBest = bestG.get(next);

            // If we find a better g(n), update and add the node to frontier
            if (prevBest == null || newCost < prevBest) {
                bestG.put(next, newCost);
                Node child = new Node(next, current, action, newCost, current.depth + 1);
                frontier.add(child);
            }
        }
    }

    // If we reach here, no solution found
    return new SearchResult("", -1, nodesExpanded);
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

    // helper to check if a given state appears on the path from node to root
    private static boolean isInPath(State state, Node node) {
        Node cur = node;
        while (cur != null) {
            if (cur.state.equals(state))
                return true;
            cur = cur.parent;
        }
        return false;
    }
}
