package code;

public class State {
    public int x, y; // position dans la grille

    public State(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof State)) return false;
        State s = (State) o;
        return this.x == s.x && this.y == s.y;
    }

    @Override
    public int hashCode() {
        return x * 31 + y;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
