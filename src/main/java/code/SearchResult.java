package code ;
public class SearchResult {
    public String plan;       // ex: "right,down,down,left"
    public int cost;          // coût total
    public int nodesExpanded; // nœuds expandés

    public SearchResult(String plan, int cost, int nodesExpanded) {
        this.plan = plan;
        this.cost = cost;
        this.nodesExpanded = nodesExpanded;
    }

    @Override
    public String toString() {
        return plan + ";" + cost + ";" + nodesExpanded;
    }
}
