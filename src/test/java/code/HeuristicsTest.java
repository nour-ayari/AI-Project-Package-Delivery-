package code;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HeuristicsTest {

    @Test
    void testManhattanHeuristic() {
        State a = new State(1, 2);
        State b = new State(4, 6);
        Grid g = new Grid(10, 10);
        DeliverySearch problem = new DeliverySearch(a, b, g);
        int h = Heuristics.heuristic(problem, a, 1); // h1 = Manhattan
        assertEquals(7, h);
    }

    @Test
    void testTunnelAwareHeuristic() {
        State start = new State(0, 0);
        State goal = new State(4, 0);
        Grid g = new Grid(5, 5);
        Tunnel t = new Tunnel(new State(1, 0), new State(3, 0));
        g.tunnels.add(t);
        DeliverySearch problem = new DeliverySearch(start, goal, g);

        int h1 = Heuristics.heuristic(problem, start, 1); // Manhattan
        int h2 = Heuristics.heuristic(problem, start, 2); // Tunnel-aware
        assertEquals(4, h1);
        assertEquals(4, h2); // via tunnel = 1+2+1 = 4
    }

    @Test
    void testStartEqualsGoal() {
        State s = new State(2, 2);
        Grid g = new Grid(5, 5);
        DeliverySearch problem = new DeliverySearch(s, s, g);
        assertEquals(0, Heuristics.heuristic(problem, s, 1));
        assertEquals(0, Heuristics.heuristic(problem, s, 2));
    }

    @Test
    void testMultipleTunnels() {
        State start = new State(0, 0);
        State goal = new State(5, 0);
        Grid g = new Grid(6, 1);

        // Two tunnels
        Tunnel t1 = new Tunnel(new State(1, 0), new State(3, 0));
        Tunnel t2 = new Tunnel(new State(2, 0), new State(4, 0));
        g.tunnels.add(t1);
        g.tunnels.add(t2);

        DeliverySearch problem = new DeliverySearch(start, goal, g);

        int h1 = Heuristics.heuristic(problem, start, 1); // Manhattan
        int h2 = Heuristics.heuristic(problem, start, 2); // Tunnel-aware

        assertEquals(5, h1); // straight distance
        assertEquals(5, h2); // best path via tunnel t1:0->1 1->3 + 3->5  = 5
    }

    @Test
    void testComplexGrid() {
        State start = new State(0, 0);
        State goal = new State(4, 4);
        Grid g = new Grid(5, 5);

        // Multiple tunnels
        g.tunnels.add(new Tunnel(new State(0, 1), new State(4, 1)));
        g.tunnels.add(new Tunnel(new State(1, 2), new State(3, 3)));

        DeliverySearch problem = new DeliverySearch(start, goal, g);

        int h1 = Heuristics.heuristic(problem, start, 1);
        int h2 = Heuristics.heuristic(problem, start, 2);

        assertEquals(8, h1); // Manhattan distance
        assertTrue(h2 <= 8); // Tunnel-aware should never be more than Manhattan
    }

    @Test
    void testMultipleStatesOneTunnel() {
        // Start and goal
        State start = new State(0, 0);
        State goal = new State(4, 0);

        // Create grid
        Grid g = new Grid(5, 1);

        // Add a tunnel from (1,0) to (3,0)
        Tunnel tunnel = new Tunnel(new State(1, 0), new State(3, 0));
        g.tunnels.add(tunnel);

        // Create DeliverySearch problem
        DeliverySearch problem = new DeliverySearch(start, goal, g);

        // Compute heuristics
        int h1 = Heuristics.heuristic(problem, start, 1); // Manhattan
        int h2 = Heuristics.heuristic(problem, start, 2); // Tunnel-aware

        // Expected:
        // Manhattan: 4
        // Tunnel-aware: path via tunnel = 1 + 2 + 1 = 4 (start->tunnelA + tunnel +
        // tunnelB->goal)
        assertEquals(4, h1);
        assertEquals(4, h2);
    }

}
