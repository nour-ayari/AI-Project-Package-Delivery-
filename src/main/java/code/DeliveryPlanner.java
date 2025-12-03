package code;

import java.util.*;

public class DeliveryPlanner {

    // ======================================================================
    // MAIN PLANNING FUNCTION (ASSIGN + MULTI-DELIVERIES PER STORE)
    // ======================================================================
    public static String plan(String initialState, String traffic, String strategy, boolean visualize) {

        // 1) Parse Grid from strings
        Grid grid = parseGrid(initialState, traffic);
        // 2) Optional UI
        UIVisualizer ui = visualize ? new UIVisualizer(grid) : null;

        StringBuilder output = new StringBuilder();

        // ==================================================================
        // PHASE 1 : ASSIGN EACH DESTINATION TO THE BEST STORE
        // destination -> store
        // ==================================================================
        Map<State, State> assignment = new HashMap<>();

        for (State dest : grid.destinations) {

            int bestCost = Integer.MAX_VALUE;
            State bestStore = null;

            for (State store : grid.stores) {

                SearchResult r = DeliverySearch.solve(store, dest, grid, strategy);

                if (r != null && r.cost >= 0 && r.cost < bestCost) {
                    bestCost = r.cost;
                    bestStore = store;
                }
            }

            if (bestStore != null) {
                assignment.put(dest, bestStore);
            } else {
                // no store can reach this destination
                output.append("Destination ").append(dest)
                        .append(" is NOT reachable from any store.\n");
            }
        }

        output.append("\n");

        // ==================================================================
        // PHASE 2 : FOR EACH STORE, PLAN A FULL TOUR (Greedy)
        // ==================================================================
        for (State store : grid.stores) {

            output.append("=== TRUCK AT STORE ").append(store).append(" ===\n");

            // collect only destinations assigned to this store
            List<State> myDestinations = new ArrayList<>();
            for (State d : grid.destinations) {
                State assignedStore = assignment.get(d);
                if (assignedStore != null && assignedStore.equals(store)) {
                    myDestinations.add(d);
                }
            }

            if (myDestinations.isEmpty()) {
                output.append("No destinations assigned to this store.\n\n");
                continue;
            }

            State truckPos = store;

            while (!myDestinations.isEmpty()) {

                State bestDest = null;
                SearchResult bestResult = null;

                // ---------------------------------------------------------
                // Greedy choice: next destination with lowest cost
                // (from current truckPos, but truckPos = store each time
                // since it returns after each delivery)
                // ---------------------------------------------------------
                for (State d : myDestinations) {
                    SearchResult r = DeliverySearch.solve(truckPos, d, grid, strategy);

                    if (r != null && r.cost >= 0) {
                        if (bestResult == null || r.cost < bestResult.cost) {
                            bestResult = r;
                            bestDest = d;
                        }
                    }
                }

                if (bestDest == null) {
                    output.append("Some assigned destinations are NOT reachable from store ")
                            .append(store).append(".\n");
                    break;
                }

                // ---------------------------------------------------------
                // Log the delivery
                // ---------------------------------------------------------
                output.append("Deliver to ").append(bestDest)
                        .append(" | plan=").append(bestResult.plan)
                        .append(" | cost=").append(bestResult.cost)
                        .append(" | expanded=").append(bestResult.nodesExpanded)
                        .append("\n");

                // ---------------------------------------------------------
                // Visualize the path if needed
                // ---------------------------------------------------------
                if (visualize) {
                    animatePlan(ui, grid, truckPos, bestResult.plan);
                }

                truckPos = store;

                // Remove the delivered destination from this store's list
                myDestinations.remove(bestDest);
            }

            output.append("\n");
        }

        return output.toString();
    }

    // ======================================================================
    // ANIMATE PLAN IN THE UI
    // ======================================================================
    private static void animatePlan(UIVisualizer ui, Grid grid, State start, String plan) {
        State current = new State(start.x, start.y);
        ui.updateTruck(current);

        if (plan == null || plan.isEmpty())
            return;

        String[] steps = plan.split(",");
        for (String step : steps) {

            ui.drawArrow(current, step.trim());
            current = grid.applyAction(current, step.trim());
            ui.updateTruck(current);
        }
    }

    // ======================================================================
    // PARSE GRID FROM STRINGS
    // Matches Grid.GenGrid() format:
    // m;n;P;S;DESTS;STORES;TUNNELS;
    // ======================================================================
    private static Grid parseGrid(String initialState, String trafficString) {

        String[] parts = initialState.split(";");

        int cols = Integer.parseInt(parts[0]);
        int rows = Integer.parseInt(parts[1]);
        int P = Integer.parseInt(parts[2]);
        int S = Integer.parseInt(parts[3]);

        Grid g = new Grid(rows, cols);

        // -------------------------
        // DESTINATIONS : parts[4]
        // -------------------------
        if (parts.length > 4 && !parts[4].isEmpty()) {
            String[] custData = parts[4].split(",");
            for (int i = 0; i < P; i++) {
                int x = Integer.parseInt(custData[2 * i]);
                int y = Integer.parseInt(custData[2 * i + 1]);
                g.destinations.add(new State(x, y));
            }
        }

        // -------------------------
        // STORES : parts[5]
        // -------------------------
        if (parts.length > 5 && !parts[5].isEmpty()) {
            String[] storeData = parts[5].split(",");
            for (int i = 0; i < S; i++) {
                int x = Integer.parseInt(storeData[2 * i]);
                int y = Integer.parseInt(storeData[2 * i + 1]);
                g.stores.add(new State(x, y));
            }
        }

        // -------------------------
        // TUNNELS : parts[6]
        // -------------------------
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

        // -------------------------
        // TRAFFIC
        // -------------------------
        if (trafficString != null && !trafficString.isEmpty()) {
            String[] segs = trafficString.split(";");
            for (String seg : segs) {
                if (seg.isEmpty())
                    continue;

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