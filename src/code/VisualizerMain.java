package code;

import java.awt.Color;
import java.util.*;

public class VisualizerMain {
    public static void main(String[] args) {
        String generated = Grid.GenGrid();
        System.out.println("=== INITIAL STATE ===");
        System.out.println(generated);

        String[] parts = generated.split("\n");
        String init = parts[0];
        String traffic = parts[1];

        Grid grid = parseGrid(init, traffic);

        if (grid.stores.isEmpty() || grid.destinations.isEmpty()) {
            System.out.println("No stores or destinations generated.");
            return;
        }

        State start = grid.stores.get(0);
        State goal = grid.destinations.get(0);

        System.out.println("Start: " + start + " Goal: " + goal);

        DeliverySearch problem = new DeliverySearch(start, goal, grid);

        java.util.List<GridRenderer.ExecutionTrace> traces = new ArrayList<>();
        traces.add(GenericSearch.BFS_trace(problem, "BFS", Color.ORANGE));
        traces.add(GenericSearch.DFS_trace(problem, "DFS", Color.CYAN));
        traces.add(GenericSearch.ID_trace(problem, "IDS", Color.MAGENTA));
        traces.add(GenericSearch.UCS_trace(problem, "UCS", Color.GREEN.darker()));

    // show visualization: slower, sequential playback so you can watch each algorithm in turn
    GridRenderer.show(grid, new ArrayList<>(), traces, 400, true);
    }

    // Duplicate of DeliveryPlanner.parseGrid (small helper) — keeps Visualizer independent
    private static Grid parseGrid(String initialState, String trafficString) {
        String[] parts = initialState.split(";");

        int cols = Integer.parseInt(parts[0]);
        int rows = Integer.parseInt(parts[1]);
        int P = Integer.parseInt(parts[2]);
        int S = Integer.parseInt(parts[3]);

        Grid g = new Grid(rows, cols);

        // DESTINATIONS : parts[4]
        if (parts.length > 4 && !parts[4].isEmpty()) {
            String[] custData = parts[4].split(",");
            for (int i = 0; i < P; i++) {
                int x = Integer.parseInt(custData[2 * i]);
                int y = Integer.parseInt(custData[2 * i + 1]);
                g.destinations.add(new State(x, y));
            }
        }

        // STORES : parts[5]
        if (parts.length > 5 && !parts[5].isEmpty()) {
            String[] storeData = parts[5].split(",");
            for (int i = 0; i < S; i++) {
                int x = Integer.parseInt(storeData[2 * i]);
                int y = Integer.parseInt(storeData[2 * i + 1]);
                g.stores.add(new State(x, y));
            }
        }

        // TUNNELS : parts[6]
        if (parts.length > 6 && !parts[6].isEmpty()) {
            String[] tunData = parts[6].split(",");
            for (int i = 0; i + 3 < tunData.length; i += 4) {
                int x1 = Integer.parseInt(tunData[i]);
                int y1 = Integer.parseInt(tunData[i + 1]);
                int x2 = Integer.parseInt(tunData[i + 2]);
                int y2 = Integer.parseInt(tunData[i + 3]);
                g.tunnels.add(new Tunnel(new State(x1, y1), new State(x2, y2)));
            }
        }

        // TRAFFIC
        if (trafficString != null && !trafficString.isEmpty()) {
            String[] segs = trafficString.split(";");
            for (String seg : segs) {
                if (seg.isEmpty()) continue;

                String[] t = seg.split(",");
                int sx = Integer.parseInt(t[0]);
                int sy = Integer.parseInt(t[1]);
                int dx = Integer.parseInt(t[2]);
                int dy = Integer.parseInt(t[3]);
                int cost = Integer.parseInt(t[4]);

                if (dx == sx && dy == sy - 1)
                    g.traffic[sy][sx][0] = cost; // up
                if (dx == sx && dy == sy + 1)
                    g.traffic[sy][sx][1] = cost; // down
                if (dx == sx - 1 && dy == sy)
                    g.traffic[sy][sx][2] = cost; // left
                if (dx == sx + 1 && dy == sy)
                    g.traffic[sy][sx][3] = cost; // right
            }
        }

        return g;
    }
}
