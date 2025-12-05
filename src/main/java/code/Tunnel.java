package code ;
public class Tunnel {
    public State A;
    public State B;
    public int cost;

    public Tunnel(State a, State b) {
        this(a, b, 1); // default cost
    }
    
    public Tunnel(State a, State b, int cost) {
        this.A = a;
        this.B = b;
        this.cost = cost;
    }
}
