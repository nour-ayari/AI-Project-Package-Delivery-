package code;

import java.util.*;

public class GenericSearch {

    // ------------------ BFS ------------------
    public static SearchResult BFS(SearchProblem problem) {
        State initial = problem.initialState();
        Node root = new Node(initial, null, null, 0, 0);
        Deque<Node> frontier = new ArrayDeque<>();
        Set<State> visited = new HashSet<>();

        frontier.add(root);
        visited.add(initial);

        int nodesExpanded = 0;
        List<State> expandedOrder = new ArrayList<>();

        while (!frontier.isEmpty()) {
            Node current = frontier.poll();
            nodesExpanded++;
            expandedOrder.add(current.state);

            if (problem.isGoal(current.state)) {
                String plan = reconstructPlan(current);
                java.util.List<State> pathStates = reconstructPathStates(current);
                return new SearchResult(plan, current.pathCost, nodesExpanded, expandedOrder, pathStates);
            }

            for (String action : problem.actions(current.state)) {
                State next = problem.result(current.state, action);
                if (visited.add(next)) {
                    Node child = new Node(next, current, action,
                            current.pathCost + problem.stepCost(current.state, action, next),
                            current.depth + 1);
                    frontier.add(child);
                }
            }
        }
        return new SearchResult("", -1, nodesExpanded, expandedOrder, new java.util.ArrayList<>()); 

    }

    // ------------------ DFS ------------------
    public static SearchResult DFS(SearchProblem problem) {
        State initial = problem.initialState();
        Node root = new Node(initial, null, null, 0, 0);
        Deque<Node> frontier = new ArrayDeque<>();
        Set<State> visited = new HashSet<>();

        frontier.push(root);
        int nodesExpanded = 0;
        List<State> expandedOrder = new ArrayList<>();

        while (!frontier.isEmpty()) {
            Node current = frontier.pop();
            if (!visited.add(current.state)) continue;

            nodesExpanded++;
            expandedOrder.add(current.state);

            if (problem.isGoal(current.state))
                return resultFromNode(current, nodesExpanded, expandedOrder);

            for (String action : problem.actions(current.state)) {
                State next = problem.result(current.state, action);
                if (!visited.contains(next)) {
                    Node child = new Node(next, current, action,
                            current.pathCost + problem.stepCost(current.state, action, next),
                            current.depth + 1);
                    frontier.push(child);
                }
            }
        }
        return emptyResult(nodesExpanded, expandedOrder);
    }

    // ------------------ Iterative Deepening ------------------
    public static SearchResult ID(SearchProblem problem) {
        State initial = problem.initialState();
        int totalNodesExpanded = 0;
        List<State> expandedOrder = new ArrayList<>();

        for (int limit = 0;; limit++) {
            Deque<Node> frontier = new ArrayDeque<>();
            frontier.push(new Node(initial, null, null, 0, 0));
            boolean cutoffOccurred = false;
            int nodesExpandedThisIter = 0;

            while (!frontier.isEmpty()) {
                Node current = frontier.pop();
                nodesExpandedThisIter++;
                expandedOrder.add(current.state);

                if (problem.isGoal(current.state))
                    return resultFromNode(current, totalNodesExpanded + nodesExpandedThisIter, expandedOrder);

                if (current.depth == limit) {
                    cutoffOccurred = true;
                    continue;
                }

                for (String action : problem.actions(current.state)) {
                    State next = problem.result(current.state, action);
                    if (isInPath(next, current)) continue;

                    Node child = new Node(next, current, action,
                            current.pathCost + problem.stepCost(current.state, action, next),
                            current.depth + 1);
                    frontier.push(child);
                }
            }

            totalNodesExpanded += nodesExpandedThisIter;
            if (!cutoffOccurred)
                return emptyResult(totalNodesExpanded, expandedOrder);
        }
    }

    // ------------------ UCS ------------------
    public static SearchResult UCS(SearchProblem problem) {
        return uniformCost(problem, false);
    }

    // ------------------ Greedy Search ------------------
    public static SearchResult Greedy(SearchProblem problem, int heuristicId) {
        return uniformCost(problem, true, heuristicId);
    }

    // ------------------ A* Search ------------------
    public static SearchResult AStar(SearchProblem problem, int heuristicId) {
        return uniformCost(problem, true, heuristicId, true);
    }

    // ------------------ Unified UCS / Greedy / A* ------------------
    private static SearchResult uniformCost(SearchProblem problem, boolean useHeuristic) {
        return uniformCost(problem, useHeuristic, 0, false);
    }

    private static SearchResult uniformCost(SearchProblem problem, boolean useHeuristic, int heuristicId) {
        return uniformCost(problem, useHeuristic, heuristicId, false);
    }

    private static SearchResult uniformCost(SearchProblem problem, boolean useHeuristic, int heuristicId, boolean isAStar) {
        State initial = problem.initialState();
        Node root = new Node(initial, null, null, 0, 0);

        PriorityQueue<Node> frontier = new PriorityQueue<>(Comparator.comparingInt(n -> {
            int h = useHeuristic ? Heuristics.heuristic(problem, n.state, heuristicId) : 0;
            return isAStar ? n.pathCost + h : h != 0 ? h : n.pathCost;
        }));

        Map<State, Integer> bestG = new HashMap<>();
        frontier.add(root);
        bestG.put(initial, 0);

        int nodesExpanded = 0;
        List<State> expandedOrder = new ArrayList<>();

        while (!frontier.isEmpty()) {
            Node current = frontier.poll();
            Integer recorded = bestG.get(current.state);
            if (recorded != null && current.pathCost != recorded) continue;

            nodesExpanded++;
            expandedOrder.add(current.state);

            if (problem.isGoal(current.state))
                return resultFromNode(current, nodesExpanded, expandedOrder);

            for (String action : problem.actions(current.state)) {
                State next = problem.result(current.state, action);
                int newCost = current.pathCost + problem.stepCost(current.state, action, next);
                Integer prev = bestG.get(next);
                if (prev == null || newCost < prev) {
                    bestG.put(next, newCost);
                    Node child = new Node(next, current, action, newCost, current.depth + 1);
                    frontier.add(child);
                }
            }
        }
        return emptyResult(nodesExpanded, expandedOrder);
    }

    // ------------------ Utilities ------------------
    private static String reconstructPlan(Node node) {
        List<String> actions = new ArrayList<>();
        while (node.parent != null) {
            actions.add(node.action);
            node = node.parent;
        }
        Collections.reverse(actions);
        return String.join(",", actions);
    }

    private static List<State> reconstructPathStates(Node node) {
        List<State> states = new ArrayList<>();
        Node cur = node;
        while (cur != null) {
            states.add(cur.state);
            cur = cur.parent;
        }
        Collections.reverse(states);
        return states;
    }

    private static boolean isInPath(State state, Node node) {
        while (node != null) {
            if (node.state.equals(state)) return true;
            node = node.parent;
        }
        return false;
    }

    private static SearchResult resultFromNode(Node node, int nodesExpanded, List<State> expandedOrder) {
        return new SearchResult(reconstructPlan(node), node.pathCost, nodesExpanded, expandedOrder, reconstructPathStates(node));
    }

    private static SearchResult emptyResult(int nodesExpanded, List<State> expandedOrder) {
        return new SearchResult("", -1, nodesExpanded, expandedOrder, new ArrayList<>());
    }
}
