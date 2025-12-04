package code.dto;

public class PlanningRequest {
    private GridConfig grid;
    private String strategy; // "BFS", "DFS", "UCS", "AStar", "Greedy"
    
    public PlanningRequest() {}
    
    public PlanningRequest(GridConfig grid, String strategy) {
        this.grid = grid;
        this.strategy = strategy;
    }
    
    public GridConfig getGrid() {
        return grid;
    }
    
    public void setGrid(GridConfig grid) {
        this.grid = grid;
    }
    
    public String getStrategy() {
        return strategy;
    }
    
    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }
}
