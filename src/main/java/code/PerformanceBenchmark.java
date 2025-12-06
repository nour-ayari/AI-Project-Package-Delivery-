package code;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.*;

public class PerformanceBenchmark {

    public static class BenchmarkResult {
        public String algorithm;
        public boolean complete;
        public boolean optimal;
        public int pathCost;
        public int nodesExpanded;
        public long memoryUsedKB;
        public long cpuTimeMs;
        public long executionTimeMs;
        
        @Override
        public String toString() {
            return String.format(
                "%-10s | Complete: %-5s | Optimal: %-5s | Cost: %-6d | Nodes: %-8d | RAM: %-10d KB | CPU: %-10d ms | Time: %-10d ms",
                algorithm, complete, optimal, pathCost, nodesExpanded, memoryUsedKB, cpuTimeMs, executionTimeMs
            );
        }
    }

    public static BenchmarkResult runBenchmark(String algorithmName, SearchProblem problem, int heuristicId) {
        BenchmarkResult result = new BenchmarkResult();
        result.algorithm = algorithmName;
        
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        
        // Force garbage collection before test
        System.gc();
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        
        long memoryBefore = memoryBean.getHeapMemoryUsage().getUsed();
        long cpuBefore = threadBean.getCurrentThreadCpuTime();
        long timeBefore = System.currentTimeMillis();
        
        SearchResult searchResult = null;
        
        try {
            switch (algorithmName.toUpperCase()) {
                case "BFS":
                    searchResult = GenericSearch.BFS(problem);
                    break;
                case "DFS":
                    searchResult = GenericSearch.DFS(problem);
                    break;
                case "ID":
                    searchResult = GenericSearch.ID(problem);
                    break;
                case "UCS":
                    searchResult = GenericSearch.UCS(problem);
                    break;
                case "GREEDY1":
                    searchResult = GenericSearch.Greedy(problem, 1);
                    result.algorithm = "Greedy-H1";
                    break;
                case "GREEDY2":
                    searchResult = GenericSearch.Greedy(problem, 2);
                    result.algorithm = "Greedy-H2";
                    break;
                case "ASTAR1":
                    searchResult = GenericSearch.AStar(problem, 1);
                    result.algorithm = "A*-H1";
                    break;
                case "ASTAR2":
                    searchResult = GenericSearch.AStar(problem, 2);
                    result.algorithm = "A*-H2";
                    break;
                default:
                    throw new IllegalArgumentException("Unknown algorithm: " + algorithmName);
            }
        } catch (Exception e) {
            result.complete = false;
            result.optimal = false;
            result.pathCost = -1;
            result.nodesExpanded = 0;
            return result;
        }
        
        long timeAfter = System.currentTimeMillis();
        long cpuAfter = threadBean.getCurrentThreadCpuTime();
        long memoryAfter = memoryBean.getHeapMemoryUsage().getUsed();
        
        result.complete = searchResult != null && searchResult.cost >= 0;
        result.pathCost = searchResult != null ? searchResult.cost : -1;
        result.nodesExpanded = searchResult != null ? searchResult.nodesExpanded : 0;
        result.memoryUsedKB = Math.max(0, (memoryAfter - memoryBefore) / 1024);
        result.cpuTimeMs = (cpuAfter - cpuBefore) / 1_000_000;
        result.executionTimeMs = timeAfter - timeBefore;
        
        return result;
    }

    public static void compareAlgorithms(Grid grid, State start, State goal) {
        System.out.println("\n" + "=".repeat(150));
        System.out.println("PERFORMANCE COMPARISON - AI Delivery Planner");
        System.out.println("=".repeat(150));
        System.out.println("Grid: " + grid.rows + "x" + grid.cols + " | Stores: " + grid.stores.size() + 
                          " | Destinations: " + grid.destinations.size());
        System.out.println("Start: " + start + " | Goal: " + goal);
        System.out.println("=".repeat(150));
        
        DeliverySearch problem = new DeliverySearch(start, goal, grid);
        
        String[] algorithms = {"BFS", "DFS", "ID", "UCS", "GREEDY1", "GREEDY2", "ASTAR1", "ASTAR2"};
        List<BenchmarkResult> results = new ArrayList<>();
        
        // Find optimal cost with UCS first (for comparison)
        BenchmarkResult ucsResult = runBenchmark("UCS", problem, 0);
        int optimalCost = ucsResult.pathCost;
        
        for (String algo : algorithms) {
            System.out.println("\nRunning " + algo + "...");
            BenchmarkResult result = runBenchmark(algo, problem, 0);
            result.optimal = result.complete && result.pathCost == optimalCost;
            results.add(result);
            System.out.println(result);
        }
        
        System.out.println("\n" + "=".repeat(150));
        System.out.println("SUMMARY TABLE");
        System.out.println("=".repeat(150));
        System.out.printf("%-12s | %-10s | %-10s | %-8s | %-10s | %-15s | %-12s | %-12s\n",
                         "Algorithm", "Complete", "Optimal", "Cost", "Nodes Exp", "RAM (KB)", "CPU (ms)", "Time (ms)");
        System.out.println("-".repeat(150));
        
        for (BenchmarkResult r : results) {
            System.out.printf("%-12s | %-10s | %-10s | %-8d | %-10d | %-15d | %-12d | %-12d\n",
                             r.algorithm, r.complete, r.optimal, r.pathCost, 
                             r.nodesExpanded, r.memoryUsedKB, r.cpuTimeMs, r.executionTimeMs);
        }
        System.out.println("=".repeat(150));
        
        // Analysis comments
        System.out.println("\nKEY OBSERVATIONS:");
        System.out.println("─".repeat(150));
        
        // Completeness analysis
        System.out.println("\n1. COMPLETENESS:");
        for (BenchmarkResult r : results) {
            System.out.println("   - " + r.algorithm + ": " + (r.complete ? "✓ Complete" : "✗ Incomplete"));
        }
        
        // Optimality analysis
        System.out.println("\n2. OPTIMALITY:");
        for (BenchmarkResult r : results) {
            System.out.println("   - " + r.algorithm + ": " + (r.optimal ? "✓ Optimal" : "✗ Not Optimal"));
        }
        
        // Nodes expanded comparison
        System.out.println("\n3. NODES EXPANDED (Efficiency):");
        results.sort(Comparator.comparingInt(r -> r.nodesExpanded));
        for (BenchmarkResult r : results) {
            System.out.println("   - " + r.algorithm + ": " + r.nodesExpanded + " nodes");
        }
        
        // Memory comparison
        System.out.println("\n4. MEMORY USAGE:");
        results.sort(Comparator.comparingLong(r -> r.memoryUsedKB));
        for (BenchmarkResult r : results) {
            System.out.println("   - " + r.algorithm + ": " + r.memoryUsedKB + " KB");
        }
        
        // CPU comparison
        System.out.println("\n5. CPU TIME:");
        results.sort(Comparator.comparingLong(r -> r.cpuTimeMs));
        for (BenchmarkResult r : results) {
            System.out.println("   - " + r.algorithm + ": " + r.cpuTimeMs + " ms");
        }
        
        System.out.println("\n" + "=".repeat(150));
    }

    public static void main(String[] args) {
        // Example 1: Small grid
        System.out.println("\n### TEST CASE 1: Small Grid (5x5) ###");
        Grid grid1 = new Grid(5, 5);
        grid1.stores.add(new State(0, 0));
        grid1.destinations.add(new State(4, 4));
        compareAlgorithms(grid1, new State(0, 0), new State(4, 4));
        
        // Example 2: Medium grid with obstacles
        System.out.println("\n\n### TEST CASE 2: Medium Grid (10x10) with obstacles ###");
        Grid grid2 = new Grid(10, 10);
        grid2.stores.add(new State(0, 0));
        grid2.destinations.add(new State(9, 9));
        grid2.blockedRoads.add(new RoadBlock(new State(5, 4), new State(5, 5)));
        grid2.blockedRoads.add(new RoadBlock(new State(4, 5), new State(5, 5)));
        compareAlgorithms(grid2, new State(0, 0), new State(9, 9));
        
        // Example 3: Larger grid
        System.out.println("\n\n### TEST CASE 3: Large Grid (15x15) ###");
        Grid grid3 = new Grid(15, 15);
        grid3.stores.add(new State(0, 0));
        grid3.destinations.add(new State(14, 14));
        compareAlgorithms(grid3, new State(0, 0), new State(14, 14));
    }
}
