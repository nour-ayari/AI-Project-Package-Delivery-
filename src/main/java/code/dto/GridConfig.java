package code.dto;

import java.util.List;

public class GridConfig {
    private int rows;
    private int cols;
    private int[][][] traffic; // [y][x][direction] where direction: 0=up, 1=down, 2=left, 3=right
    private List<Position> stores;
    private List<Position> destinations;
    private List<TunnelConfig> tunnels;
    private List<RoadBlockConfig> roadblocks;
    
    public GridConfig() {}
    
    public int getRows() {
        return rows;
    }
    
    public void setRows(int rows) {
        this.rows = rows;
    }
    
    public int getCols() {
        return cols;
    }
    
    public void setCols(int cols) {
        this.cols = cols;
    }
    
    public int[][][] getTraffic() {
        return traffic;
    }
    
    public void setTraffic(int[][][] traffic) {
        this.traffic = traffic;
    }
    
    public List<Position> getStores() {
        return stores;
    }
    
    public void setStores(List<Position> stores) {
        this.stores = stores;
    }
    
    public List<Position> getDestinations() {
        return destinations;
    }
    
    public void setDestinations(List<Position> destinations) {
        this.destinations = destinations;
    }
    
    public List<TunnelConfig> getTunnels() {
        return tunnels;
    }
    
    public void setTunnels(List<TunnelConfig> tunnels) {
        this.tunnels = tunnels;
    }
    
    public List<RoadBlockConfig> getRoadblocks() {
        return roadblocks;
    }
    
    public void setRoadblocks(List<RoadBlockConfig> roadblocks) {
        this.roadblocks = roadblocks;
    }
    
    public static class Position {
        private int x;
        private int y;
        
        public Position() {}
        
        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        public int getX() { return x; }
        public void setX(int x) { this.x = x; }
        public int getY() { return y; }
        public void setY(int y) { this.y = y; }
    }
    
    public static class TunnelConfig {
        private Position start;
        private Position end;
        private int cost;
        
        public TunnelConfig() {}
        
        public Position getStart() { return start; }
        public void setStart(Position start) { this.start = start; }
        public Position getEnd() { return end; }
        public void setEnd(Position end) { this.end = end; }
        public int getCost() { return cost; }
        public void setCost(int cost) { this.cost = cost; }
    }
    
    public static class RoadBlockConfig {
        private Position from;
        private String direction; // "up", "down", "left", "right"
        
        public RoadBlockConfig() {}
        
        public Position getFrom() { return from; }
        public void setFrom(Position from) { this.from = from; }
        public String getDirection() { return direction; }
        public void setDirection(String direction) { this.direction = direction; }
    }
}
