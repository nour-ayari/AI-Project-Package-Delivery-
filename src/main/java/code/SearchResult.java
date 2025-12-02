package code ;
public class SearchResult {
    public String plan;       // ex: "right,down,down,left"
    public int cost;          // coût total
    public int nodesExpanded; // nœuds expandés
    // Order in which states were expanded (for visualization)
    public java.util.List<State> expandedOrder;
    // The sequence of states along the final plan (from start to goal)
    public java.util.List<State> pathStates;

    public SearchResult(String plan, int cost, int nodesExpanded) {
        this(plan, cost, nodesExpanded, new java.util.ArrayList<>(), new java.util.ArrayList<>());
    }

    public SearchResult(String plan, int cost, int nodesExpanded,
                        java.util.List<State> expandedOrder,
                        java.util.List<State> pathStates) {
        this.plan = plan;
        this.cost = cost;
        this.nodesExpanded = nodesExpanded;
        this.expandedOrder = expandedOrder == null ? new java.util.ArrayList<>() : expandedOrder;
        this.pathStates = pathStates == null ? new java.util.ArrayList<>() : pathStates;
    }

    @Override
    public String toString() {
        return plan + ";" + cost + ";" + nodesExpanded;
    }
}
