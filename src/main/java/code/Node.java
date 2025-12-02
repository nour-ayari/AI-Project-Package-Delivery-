package code ;
public class Node {
    public State state;
    public Node parent;
    public String action;
    public int pathCost;
    public int depth;

    public Node(State state, Node parent, String action, int pathCost, int depth) {
        this.state = state;
        this.parent = parent;
        this.action = action;
        this.pathCost = pathCost;
        this.depth = depth;
    }
}
