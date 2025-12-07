package code.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PlanningResponse {
    private boolean success;
    private String message;
    private List<DeliveryRoute> routes;
    
    public PlanningResponse() {}
    
    public PlanningResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public List<DeliveryRoute> getRoutes() {
        return routes;
    }
    
    public void setRoutes(List<DeliveryRoute> routes) {
        this.routes = routes;
    }
    
    public static class DeliveryRoute {
        private GridConfig.Position store;
        private GridConfig.Position destination;
        private List<GridConfig.Position> path;
        private int cost;
        @JsonProperty("expanded")
        private int expanded;
        
        public DeliveryRoute() {}
        
        public DeliveryRoute(GridConfig.Position store, GridConfig.Position destination, 
                           List<GridConfig.Position> path, int cost, int expanded) {
            this.store = store;
            this.destination = destination;
            this.path = path;
            this.cost = cost;
            this.expanded = expanded;
        }
        
        public GridConfig.Position getStore() { return store; }
        public void setStore(GridConfig.Position store) { this.store = store; }
        public GridConfig.Position getDestination() { return destination; }
        public void setDestination(GridConfig.Position destination) { this.destination = destination; }
        public List<GridConfig.Position> getPath() { return path; }
        public void setPath(List<GridConfig.Position> path) { this.path = path; }
        public int getCost() { return cost; }
        public void setCost(int cost) { this.cost = cost; }
        public int getExpanded() { return expanded; }
        public void setExpanded(int expanded) { this.expanded = expanded; }
    }
}
