package code.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import code.DeliverySearch;
import code.Grid;
import code.RoadBlock;
import code.SearchResult;
import code.State;
import code.Tunnel;
import code.dto.GridConfig;
import code.dto.PlanningRequest;
import code.dto.PlanningResponse;

@RestController
@RequestMapping("/api/delivery")
@CrossOrigin(origins = "*")
public class DeliveryPlannerController {

    @PostMapping("/plan")
    public ResponseEntity<PlanningResponse> planDelivery(@RequestBody PlanningRequest request) {
        try {
            // Convert DTO to Grid
            Grid grid = convertToGrid(request.getGrid());

            // Get strategy
            String strategy = request.getStrategy();
            if (strategy == null || strategy.isEmpty()) {
                strategy = "BFS"; // default
            }
            
            // PHASE 1: Assign each destination to the store with the lowest cost
            // destination -> store
            Map<State, State> assignment = new HashMap<>();
            
            for (State dest : grid.destinations) {
                int bestCost = Integer.MAX_VALUE;
                State bestStore = null;
                
                for (State store : grid.stores) {
                    SearchResult result = DeliverySearch.solve(store, dest, grid, strategy);
                    
                    if (result != null && result.cost >= 0 && result.cost < bestCost) {
                        bestCost = result.cost;
                        bestStore = store;
                    }
                }
                
                if (bestStore != null) {
                    assignment.put(dest, bestStore);
                }
            }
            
            // PHASE 2: For each store, create routes to its assigned destinations
            List<PlanningResponse.DeliveryRoute> routes = new ArrayList<>();

            for (State store : grid.stores) {
                
                // Collect destinations assigned to this store
                List<State> assignedDestinations = new ArrayList<>();
                for (State dest : grid.destinations) {
                    State assignedStore = assignment.get(dest);
                    if (assignedStore != null && assignedStore.equals(store)) {
                        assignedDestinations.add(dest);
                    }
                }
                
                // Plan routes to assigned destinations
                for (State dest : assignedDestinations) {
                    SearchResult result = DeliverySearch.solve(store, dest, grid, strategy);

                    if (result != null && result.cost >= 0) {
                        // Convert path to positions
                        List<GridConfig.Position> path = new ArrayList<>();
                        for (State state : result.pathStates) {
                            path.add(new GridConfig.Position(state.x, state.y));
                        }

                        PlanningResponse.DeliveryRoute route = new PlanningResponse.DeliveryRoute(
                                new GridConfig.Position(store.x, store.y),
                                new GridConfig.Position(dest.x, dest.y),
                                path,
                                result.cost,
                                result.nodesExpanded);
                        routes.add(route);
                    }
                }
            }

            PlanningResponse response = new PlanningResponse(true, "Planning completed successfully");
            response.setRoutes(routes);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            PlanningResponse response = new PlanningResponse(false, "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/check")
    public ResponseEntity<String> checkService() {
        return ResponseEntity.ok("Delivery Planner Service is running!");
    }

    private Grid convertToGrid(GridConfig config) {
        Grid grid = new Grid(config.getRows(), config.getCols());

        // Set traffic
        if (config.getTraffic() != null) {
            grid.traffic = config.getTraffic();
        } else {
            // Default traffic cost of 1 for all directions
            for (int y = 0; y < grid.rows; y++) {
                for (int x = 0; x < grid.cols; x++) {
                    for (int d = 0; d < 4; d++) {
                        grid.traffic[y][x][d] = 1;
                    }
                }
            }
        }

        // Add stores
        if (config.getStores() != null) {
            for (GridConfig.Position pos : config.getStores()) {
                grid.stores.add(new State(pos.getX(), pos.getY()));
            }
        }

        // Add destinations
        if (config.getDestinations() != null) {
            for (GridConfig.Position pos : config.getDestinations()) {
                grid.destinations.add(new State(pos.getX(), pos.getY()));
            }
        }

        // Add tunnels
        if (config.getTunnels() != null) {
            for (GridConfig.TunnelConfig tc : config.getTunnels()) {
                State start = new State(tc.getStart().getX(), tc.getStart().getY());
                State end = new State(tc.getEnd().getX(), tc.getEnd().getY());
                grid.tunnels.add(new Tunnel(start, end));
            }
        }

        // Add roadblocks
        if (config.getRoadblocks() != null) {
            for (GridConfig.RoadBlockConfig rb : config.getRoadblocks()) {
                State from = new State(rb.getFrom().getX(), rb.getFrom().getY());
                State to = null;

                switch (rb.getDirection()) {
                    case "up":
                        to = new State(from.x, from.y - 1);
                        // Ensure traffic cost is 0 for blocked direction (index 0 = up)
                        grid.traffic[from.y][from.x][0] = 0;
                        break;
                    case "down":
                        to = new State(from.x, from.y + 1);
                        // Ensure traffic cost is 0 for blocked direction (index 1 = down)
                        grid.traffic[from.y][from.x][1] = 0;
                        break;
                    case "left":
                        to = new State(from.x - 1, from.y);
                        // Ensure traffic cost is 0 for blocked direction (index 2 = left)
                        grid.traffic[from.y][from.x][2] = 0;
                        break;
                    case "right":
                        to = new State(from.x + 1, from.y);
                        // Ensure traffic cost is 0 for blocked direction (index 3 = right)
                        grid.traffic[from.y][from.x][3] = 0;
                        break;
                }

                if (to != null) {
                    grid.blockedRoads.add(new RoadBlock(from, to));
                }
            }
        }

        return grid;
    }
}
