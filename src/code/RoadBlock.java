package code;

public class RoadBlock {
    public State s;
    public String action;

    public RoadBlock(State s, String action) {
        this.s = s;
        this.action = action;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RoadBlock)) return false;
        RoadBlock r = (RoadBlock) o;
        return s.equals(r.s) && action.equals(r.action);
    }

    @Override
    public int hashCode() {
        return s.hashCode() + action.hashCode();
    }
}
