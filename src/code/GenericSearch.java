package code;

import java.util.*;
import java.awt.Color;

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

        //ID : we will increase limit until solution found or no cutoff occurs
        for (int limit = 0; ; limit++) {
            //Depth-limited search LIFO
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

                //If at depth limit, we don't expand but mark  a cutoff
                if (current.depth == limit) {
                    cutoffOccurred = true;
                    continue;
                }

                //Expand children ( we will not revisit states on the current path )
                for (String action : problem.actions(current.state)) {
                    State next = problem.result(current.state, action);

                    if (isInPath(next, current))
                        continue; //Avoid cycles along current path

                    int newCost = current.pathCost + problem.stepCost(current.state, action, next);
                    Node child = new Node(next, current, action, newCost, current.depth + 1);
                    frontier.push(child);
                }
            }

            totalNodesExpanded += nodesExpandedThisIter;

            //If there was no cutoff at this depth, further increasing limit won't help ( Klawzed )  
            if (!cutoffOccurred) {
                return new SearchResult("", -1, totalNodesExpanded);
            }

            //Else increase limit and continue
        }
    }

    // ------------------------------------------------------
    // UNIFORM COST SEARCH (UCS) 
    // ------------------------------------------------------
    public static SearchResult UCS(SearchProblem problem) {
        State initial = problem.initialState();
        Node root = new Node(initial, null, null, 0, 0);

        //Priority queue ordered by path cost (lowest first)
        PriorityQueue<Node> frontier = new PriorityQueue<>(Comparator.comparingInt(n -> n.pathCost));

        //Best cost found so far for each state
        Map<State, Integer> bestCost = new HashMap<>();

        frontier.add(root);
        bestCost.put(initial, 0);

        int nodesExpanded = 0;

        while (!frontier.isEmpty()) {
            Node current = frontier.poll();

            //If this node is obsolete (we have a better cost recorded), we need to skip it
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

    // reconstruct plan as list of states from root -> node
    private static java.util.List<State> reconstructPlanList(Node node) {
        java.util.LinkedList<State> path = new java.util.LinkedList<>();
        path.addFirst(node.state);
        Node cur = node.parent;
        while (cur != null) {
            path.addFirst(cur.state);
            cur = cur.parent;
        }
        return path;
    }

    // ---------- Trace-producing variants (for visualization) ----------
    public static GridRenderer.ExecutionTrace BFS_trace(SearchProblem problem, String name, Color color) {
        State initial = problem.initialState();
        Node root = new Node(initial, null, null, 0, 0);

        Queue<Node> frontier = new LinkedList<>();
        Set<State> visited = new HashSet<>();

        frontier.add(root);
        visited.add(initial);

        java.util.List<State> expandedOrder = new ArrayList<>();

        int nodesExpanded = 0;

        while (!frontier.isEmpty()) {
            Node current = frontier.poll();
            nodesExpanded++;
            expandedOrder.add(current.state);

            if (problem.isGoal(current.state)) {
                java.util.List<State> finalPath = reconstructPlanList(current);
                return new GridRenderer.ExecutionTrace(name, expandedOrder, new ArrayList<>(), new ArrayList<>(), finalPath, color, current.pathCost, nodesExpanded);
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

        return new GridRenderer.ExecutionTrace(name, expandedOrder, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), color, -1, nodesExpanded);
    }

    public static GridRenderer.ExecutionTrace DFS_trace(SearchProblem problem, String name, Color color) {
        State initial = problem.initialState();
        Node root = new Node(initial, null, null, 0, 0);

        Stack<Node> frontier = new Stack<>();
        Set<State> visited = new HashSet<>();

        frontier.push(root);
        java.util.List<State> expandedOrder = new ArrayList<>();
        int nodesExpanded = 0;

        while (!frontier.isEmpty()) {
            Node current = frontier.pop();

            if (visited.contains(current.state))
                continue;

            nodesExpanded++;
            expandedOrder.add(current.state);

            if (problem.isGoal(current.state)) {
                java.util.List<State> finalPath = reconstructPlanList(current);
                return new GridRenderer.ExecutionTrace(name, expandedOrder, new ArrayList<>(), new ArrayList<>(), finalPath, color, current.pathCost, nodesExpanded);
            }

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

        return new GridRenderer.ExecutionTrace(name, expandedOrder, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), color, -1, nodesExpanded);
    }

    public static GridRenderer.ExecutionTrace ID_trace(SearchProblem problem, String name, Color color) {
        State initial = problem.initialState();

        java.util.List<State> expandedOrder = new ArrayList<>();
        int totalNodesExpanded = 0;

        for (int limit = 0; ; limit++) {
            Stack<Node> frontier = new Stack<>();
            frontier.push(new Node(initial, null, null, 0, 0));

            boolean cutoffOccurred = false;
            int nodesExpandedThisIter = 0;

            while (!frontier.isEmpty()) {
                Node current = frontier.pop();
                nodesExpandedThisIter++;
                expandedOrder.add(current.state);

                if (problem.isGoal(current.state)) {
                    totalNodesExpanded += nodesExpandedThisIter;
                    java.util.List<State> finalPath = reconstructPlanList(current);
                    return new GridRenderer.ExecutionTrace(name, expandedOrder, new ArrayList<>(), new ArrayList<>(), finalPath, color, current.pathCost, totalNodesExpanded);
                }

                if (current.depth == limit) {
                    cutoffOccurred = true;
                    continue;
                }

                for (String action : problem.actions(current.state)) {
                    State next = problem.result(current.state, action);
                    if (isInPath(next, current)) continue;
                    int newCost = current.pathCost + problem.stepCost(current.state, action, next);
                    Node child = new Node(next, current, action, newCost, current.depth + 1);
                    frontier.push(child);
                }
            }

            totalNodesExpanded += nodesExpandedThisIter;
            if (!cutoffOccurred) {
                return new GridRenderer.ExecutionTrace(name, expandedOrder, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), color, -1, totalNodesExpanded);
            }
        }
    }

    public static GridRenderer.ExecutionTrace UCS_trace(SearchProblem problem, String name, Color color) {
        State initial = problem.initialState();
        Node root = new Node(initial, null, null, 0, 0);

        PriorityQueue<Node> frontier = new PriorityQueue<>(Comparator.comparingInt(n -> n.pathCost));
        Map<State, Integer> bestCost = new HashMap<>();
        frontier.add(root);
        bestCost.put(initial, 0);

        java.util.List<State> expandedOrder = new ArrayList<>();
        int nodesExpanded = 0;

        while (!frontier.isEmpty()) {
            Node current = frontier.poll();
            Integer recorded = bestCost.get(current.state);
            if (recorded != null && current.pathCost != recorded) continue;

            nodesExpanded++;
            expandedOrder.add(current.state);

            if (problem.isGoal(current.state)) {
                java.util.List<State> finalPath = reconstructPlanList(current);
                return new GridRenderer.ExecutionTrace(name, expandedOrder, new ArrayList<>(), new ArrayList<>(), finalPath, color, current.pathCost, nodesExpanded);
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

        return new GridRenderer.ExecutionTrace(name, expandedOrder, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), color, -1, nodesExpanded);
    }
}
