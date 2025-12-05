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

        // up
        if (s.y > 0 && !isBlocked(s, "up"))
            actions.add("up");
        // down
        if (s.y < rows - 1 && !isBlocked(s, "down"))
            actions.add("down");
        // left
        if (s.x > 0 && !isBlocked(s, "left"))
            actions.add("left");
        // right
        if (s.x < cols - 1 && !isBlocked(s, "right"))
            actions.add("right");

        // tunnel
        if (isTunnelEntrance(s))
            actions.add("tunnel");

        System.out.println("DEBUG: Possible actions from " + s.x + "," + s.y + ": " + actions);
        return actions;
    }

    // ---------------------------------------
    // APPLY ACTIONS
    // ---------------------------------------
    public State applyAction(State s, String action) {
        switch (action) {
            case "up":
                return new State(s.x, s.y - 1);
            case "down":
                return new State(s.x, s.y + 1);
            case "left":
                return new State(s.x - 1, s.y);
            case "right":
                return new State(s.x + 1, s.y);
            case "tunnel":
                return getTunnelExit(s);
        }
        return s;
    }

    // ---------------------------------------
    // COSTS
    // ---------------------------------------
    public int getCost(State s, State next, String action) {
        if (action.equals("tunnel"))
            return getTunnelCost(s);

        if (action.equals("up"))
            return traffic[s.y][s.x][0];
        if (action.equals("down"))
            return traffic[s.y][s.x][1];
        if (action.equals("left"))
            return traffic[s.y][s.x][2];
        if (action.equals("right"))
            return traffic[s.y][s.x][3];

        return 1; // default
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
    public boolean isBlocked(State s, String action) {
        RoadBlock testBlock = new RoadBlock(s, action);
        System.out.println("DEBUG: isBlocked checking (" + s.x + "," + s.y + ") -> " + action);
        System.out.println("DEBUG: blockedRoads list has " + blockedRoads.size() + " items");
        for (RoadBlock rb : blockedRoads) {
            System.out.println("  - RoadBlock: (" + rb.s.x + "," + rb.s.y + ") -> " + rb.action);
            if (rb.equals(testBlock)) {
                System.out.println("  MATCH FOUND!");
            }
        }
        boolean blocked = blockedRoads.contains(testBlock);
        System.out.println("DEBUG: Result = " + blocked);
        return blocked;
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

            g.blockedRoads.add(new RoadBlock(new State(x, y), dirName(dir)));
            g.traffic[y][x][dir] = 0;
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

    private static String dirName(int d) {
        switch (d) {
            case 0:
                return "up";
            case 1:
                return "down";
            case 2:
                return "left";
            case 3:
                return "right";
            default:
                return "";
        }
    }
}