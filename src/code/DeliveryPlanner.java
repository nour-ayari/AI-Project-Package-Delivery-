package code;

import java.util.*;

public class DeliveryPlanner {

    // -------------------------------------------------------------
    // MAIN PLANNING FUNCTION
    // -------------------------------------------------------------
    public static String plan(String initialState, String traffic, String strategy, boolean visualize) {

        // 1) Parse grid
        Grid grid = parseGrid(initialState, traffic);

        // 2) Setup UI if visualize = true
        UIVisualizer ui = null;
        if (visualize)
            ui = new UIVisualizer(grid);

        StringBuilder output = new StringBuilder();

        // 3) For each destination -> find best store
        for (State dest : grid.destinations) {

            int bestCost = Integer.MAX_VALUE;
            SearchResult bestResult = null;
            State bestStore = null;

            for (State store : grid.stores) {

                SearchResult result = DeliverySearch.solve(store, dest, grid, strategy);

                if (result != null && result.cost >= 0 && result.cost < bestCost) {
                    bestCost = result.cost;
                    bestResult = result;
                    bestStore = store;
                }
            }

            // No path found
            if (bestResult == null) {
                output.append("(NO_STORE_FOUND,Dest)=NO PATH\n");
                continue;
            }

            // 4) Animate plan if visualize = true
            if (visualize) {
                animatePlan(ui, grid, bestStore, bestResult.plan);
            }

            // 5) Append result for this delivery
            output.append("(")
                  .append(bestStore)
                  .append(",")
                  .append(dest)
                  .append(")=")
                  .append(bestResult)
                  .append("\n");
        }

        return output.toString();
    }


    // -------------------------------------------------------------
    // ANIMATE A PLAN IN THE UI
    // -------------------------------------------------------------
    private static void animatePlan(UIVisualizer ui, Grid grid, State start, String plan) {

        State current = new State(start.x, start.y);
        ui.updateTruck(current);

        if (plan == null || plan.isEmpty())
            return;

        String[] steps = plan.split(",");

        for (String step : steps) {
            current = grid.applyAction(current, step.trim());
            ui.updateTruck(current);
        }
    }


    // -------------------------------------------------------------
    // PARSE GRID FROM STRINGS (simple version)
    // -------------------------------------------------------------
    private static Grid parseGrid(String initialState, String trafficString) {

        // Format :
        // m;n;P;S;customerX,customerY,...;tunnelX1,tunnelY1,tunnelX2,tunnelY2,...
        String[] parts = initialState.split(";");

         int cols = Integer.parseInt(parts[0]); // x
    int rows = Integer.parseInt(parts[1]); // y
    int P = Integer.parseInt(parts[2]);
    int S = Integer.parseInt(parts[3]);


    Grid g = new Grid(rows, cols);


        // -------------------------
        // Customers
        // -------------------------
        String[] custData = parts[4].split(",");
        for (int i = 0; i < P; i++) {
            int x = Integer.parseInt(custData[2 * i]);
            int y = Integer.parseInt(custData[2 * i + 1]);
            g.destinations.add(new State(x, y));
        }

        // -------------------------
        // Tunnels
        // -------------------------
        if (parts.length >= 6 && !parts[5].isEmpty()) {
            String[] tunData = parts[5].split(",");
            for (int i = 0; i < tunData.length; i += 4) {
                int x1 = Integer.parseInt(tunData[i]);
                int y1 = Integer.parseInt(tunData[i + 1]);
                int x2 = Integer.parseInt(tunData[i + 2]);
                int y2 = Integer.parseInt(tunData[i + 3]);
                g.tunnels.add(new Tunnel(new State(x1, y1), new State(x2, y2)));
            }
        }

        // -------------------------
        // Stores (we generate S stores at fixed positions for now)
        // Later: you can parse stores from the string if needed.
        // -------------------------
        for (int i = 0; i < S; i++) {
            g.stores.add(new State(i, 0));  // simple example
        }

        // -------------------------
        // Parse traffic
        // -------------------------
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

                // determine direction
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
