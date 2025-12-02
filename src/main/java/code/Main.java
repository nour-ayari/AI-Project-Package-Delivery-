package code;

public class Main {
    public static void main(String[] args) {
        // Generate grid and traffic data
        String generated = Grid.GenGrid();
        System.out.println("=== INITIAL STATE ===");
        System.out.println(generated);

        String[] parts = generated.split("\n");
        String init = parts[0];
        String traffic = parts[1];

        System.out.println(init);
        System.out.println(traffic);

        // Test different strategies
        System.out.println("\n=== Testing BFS ===");
        DeliveryPlanner.plan(init, traffic, "BF", true); // BFS Strategy

        System.out.println("\n=== Testing DFS ===");
        DeliveryPlanner.plan(init, traffic, "DF", true); // DFS Strategy

        System.out.println("\n=== Testing Greedy Search (Manhattan) ===");
        DeliveryPlanner.plan(init, traffic, "G1", true); // Greedy (Manhattan)

        System.out.println("\n=== Testing Greedy Search (Tunnel-aware) ===");
        DeliveryPlanner.plan(init, traffic, "G2", true); // Greedy (Tunnel-aware)

        System.out.println("\n=== Testing A* Search (Manhattan) ===");
        DeliveryPlanner.plan(init, traffic, "AS1", true); // A* with Manhattan heuristic

        System.out.println("\n=== Testing A* Search (Tunnel-aware) ===");
        DeliveryPlanner.plan(init, traffic, "AS2", true); // A* with Tunnel-aware heuristic

    }
}
