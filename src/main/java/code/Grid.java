package code;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Grid {

    public int rows, cols;

    // traffic[y][x][direction] : cost
    // direction: 0=up, 1=down, 2=left, 3=right
    public int[][][] traffic;

    public List<State> stores = new ArrayList<>();
    public List<State> destinations = new ArrayList<>();
    public List<Tunnel> tunnels = new ArrayList<>();
    public Set<RoadBlock> blockedRoads = new HashSet<>();

    public Grid(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        traffic = new int[rows][cols][4];
    }

    // ---------------------------------------
    // ACTIONS POSSIBLES
    // ---------------------------------------
    public List<String> getPossibleActions(State s) {
        List<String> actions = new ArrayList<>();

        State up = new State(s.x, s.y - 1);
        if (s.y > 0 && !isBlockedEdge(s, up) && traffic[s.y][s.x][0] > 0)
            actions.add("up");

        State down = new State(s.x, s.y + 1);
        if (s.y < rows - 1 && !isBlockedEdge(s, down) && traffic[s.y][s.x][1] > 0)
            actions.add("down");

        State left = new State(s.x - 1, s.y);
        if (s.x > 0 && !isBlockedEdge(s, left) && traffic[s.y][s.x][2] > 0)
            actions.add("left");

        State right = new State(s.x + 1, s.y);
        if (s.x < cols - 1 && !isBlockedEdge(s, right) && traffic[s.y][s.x][3] > 0)
            actions.add("right");

        if (isTunnelEntrance(s))
            actions.add("tunnel");

        System.out.println("DEBUG: Possible actions from " + s.x + "," + s.y + ": " + actions);
        return actions;
    }

    public int getCost(State s, State next, String action) {
        // Blocked edge returns -1 cost (or Integer.MAX_VALUE)
        if (action.equals("tunnel"))
            return getTunnelCost(s);

        if (isBlockedEdge(s, next))
            return -1; // block completely

        switch (action) {
            case "up":
                return traffic[s.y][s.x][0];
            case "down":
                return traffic[s.y][s.x][1];
            case "left":
                return traffic[s.y][s.x][2];
            case "right":
                return traffic[s.y][s.x][3];
            default:
                return 1;
        }
    }

    // ---------------------------------------
    // APPLY ACTIONS
    // ---------------------------------------
    public State applyAction(State s, String action) {
        State next = switch (action) {
            case "up" -> new State(s.x, s.y - 1);
            case "down" -> new State(s.x, s.y + 1);
            case "left" -> new State(s.x - 1, s.y);
            case "right" -> new State(s.x + 1, s.y);
            case "tunnel" -> getTunnelExit(s);
            default -> s;
        };

        return next;
    }

    // ---------------------------------------
    // TUNNELS
    // ---------------------------------------
    public boolean isTunnelEntrance(State s) {
        for (Tunnel t : tunnels)
            if (t.A.equals(s) || t.B.equals(s))
                return true;
        return false;
    }

    public State getTunnelExit(State s) {
        for (Tunnel t : tunnels) {
            if (t.A.equals(s))
                return t.B;
            if (t.B.equals(s))
                return t.A;
        }
        return s;
    }

    public int getTunnelCost(State s) {
        for (Tunnel t : tunnels) {
            if (t.A.equals(s) || t.B.equals(s))
                return t.cost > 0 ? t.cost : manhattan(t.A, t.B);
        }
        return 0;
    }

    private int manhattan(State a, State b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    // ---------------------------------------
    // BLOCKED ROADS
    // ---------------------------------------

    public boolean isBlockedEdge(State s, State neighbor) {
        // If neighbor is null or same as s, not blocked by an edge
        if (neighbor == null || s.equals(neighbor))
            return false;
        return blockedRoads.contains(new RoadBlock(s, neighbor));
    }

    // Public random grid generator (keeps same API)
    public static String GenGrid() {
        Random rnd = new Random();

        int cols = rnd.nextInt(4) + 7; // 7–10
        int rows = rnd.nextInt(4) + 7;
        int S = rnd.nextInt(3) + 1; // 1–3 stores
        int P = rnd.nextInt(5) + 3; // 3–7 destinations

        return generateGridInternal(rows, cols, S, P);
    }

    // User-defined generator (used by UI)
    public static String GenGrid(int rows, int cols, int numStores, int numDestinations) {
        return generateGridInternal(rows, cols, numStores, numDestinations);
    }

    private static String generateGridInternal(int rows, int cols, int numStores, int numDestinations) {

        Random rnd = new Random();
        Grid g = new Grid(rows, cols);
        HashSet<String> used = new HashSet<>();

        // ----- STORES -----
        for (int i = 0; i < numStores; i++) {
            State s;
            do {
                s = new State(rnd.nextInt(cols), rnd.nextInt(rows));
            } while (!used.add(s.x + "," + s.y));
            g.stores.add(s);
        }

        // ----- DESTINATIONS -----
        for (int i = 0; i < numDestinations; i++) {
            State d;
            do {
                d = new State(rnd.nextInt(cols), rnd.nextInt(rows));
            } while (!used.add(d.x + "," + d.y));
            g.destinations.add(d);
        }

        // ----- TUNNELS (0–2) -----
        int T = rnd.nextInt(3);
        for (int i = 0; i < T; i++) {
            State A = new State(rnd.nextInt(cols), rnd.nextInt(rows));
            State B = new State(rnd.nextInt(cols), rnd.nextInt(rows));
            if (!A.equals(B))
                g.tunnels.add(new Tunnel(A, B));
        }

        // ----- TRAFFIC -----
        for (int y = 0; y < rows; y++)
            for (int x = 0; x < cols; x++)
                for (int d = 0; d < 4; d++)
                    g.traffic[y][x][d] = rnd.nextInt(4) + 1;

        // ----- ROADBLOCKS -----
        int blockCount = (int) (rows * cols * 0.1);
        for (int i = 0; i < blockCount; i++) {
            int x = rnd.nextInt(cols);
            int y = rnd.nextInt(rows);
            int dir = rnd.nextInt(4);

            State from = new State(x, y);
            State to = null;
            switch (dir) {
                case 0:
                    if (y > 0)
                        to = new State(x, y - 1);
                    break; // up
                case 1:
                    if (y < rows - 1)
                        to = new State(x, y + 1);
                    break; // down
                case 2:
                    if (x > 0)
                        to = new State(x - 1, y);
                    break; // left
                case 3:
                    if (x < cols - 1)
                        to = new State(x + 1, y);
                    break; // right
            }

            if (to != null) {
                g.blockedRoads.add(new RoadBlock(from, to));
                g.traffic[y][x][dir] = 0;
            }
        }

        // ----- SERIALIZATION -----
        StringBuilder initial = new StringBuilder();
        StringBuilder trafficSb = new StringBuilder();

        initial.append(cols).append(";").append(rows).append(";")
                .append(numDestinations).append(";").append(numStores).append(";");

        // Destinations
        for (State d : g.destinations)
            initial.append(d.x).append(",").append(d.y).append(",");
        initial.append(";");

        // Stores
        for (State s : g.stores)
            initial.append(s.x).append(",").append(s.y).append(",");
        initial.append(";");

        // Tunnels
        for (Tunnel t : g.tunnels)
            initial.append(t.A.x).append(",").append(t.A.y).append(",")
                    .append(t.B.x).append(",").append(t.B.y).append(",");
        initial.append(";");

        // Traffic
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (y > 0)
                    trafficSb.append(x + "," + y + "," + x + "," + (y - 1) + "," + g.traffic[y][x][0] + ";");
                if (y < rows - 1)
                    trafficSb.append(x + "," + y + "," + x + "," + (y + 1) + "," + g.traffic[y][x][1] + ";");
                if (x > 0)
                    trafficSb.append(x + "," + y + "," + (x - 1) + "," + y + "," + g.traffic[y][x][2] + ";");
                if (x < cols - 1)
                    trafficSb.append(x + "," + y + "," + (x + 1) + "," + y + "," + g.traffic[y][x][3] + ";");
            }
        }

        return initial.toString() + "\n" + trafficSb.toString();
    }
}