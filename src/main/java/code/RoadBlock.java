package code;

public class RoadBlock {
    public final State A, B;

    public RoadBlock(State A, State B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoadBlock)) return false;
        RoadBlock rb = (RoadBlock) o;
        // Blocked in either direction
        return (A.equals(rb.A) && B.equals(rb.B)) || (A.equals(rb.B) && B.equals(rb.A));
    }
   @Override
    public int hashCode() {
        return A.hashCode() + B.hashCode(); // order-independent
    }
}
