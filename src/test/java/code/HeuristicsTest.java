package code;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HeuristicsTest {

    // Manhattan heuristic
    @Test
    void testManhattanHeuristic() {
        State a = new State(1, 2);
        State b = new State(4, 6);
        Grid g = new Grid(10, 10);
        DeliverySearch problem = new DeliverySearch(a, b, g);

        int h = Heuristics.heuristic(problem, a, 1); // h1 = Manhattan
        assertEquals(7, h); // |4-1| + |6-2| = 3+4=7
    }

    @Test
    void testGoalHeuristicIsZero() {
        State goal = new State(2, 2);
        Grid g = new Grid(5, 5);
        DeliverySearch problem = new DeliverySearch(goal, goal, g);

        assertEquals(0, Heuristics.heuristic(problem, goal, 1)); // Manhattan
        assertEquals(0, Heuristics.heuristic(problem, goal, 2)); // Traffic-aware
    }

   
    @Test
    void testTrafficAwareHeuristic() {
        State start = new State(0, 0);
        State goal = new State(2, 3);
        Grid g = new Grid(5, 5);

        // Horizontal traffic
        g.traffic[0][0][3] = 3;
        g.traffic[0][1][3] = 2;

        // Vertical traffic
        g.traffic[0][0][1] = 4;
        g.traffic[1][0][1] = 1;
        g.traffic[2][0][1] = 2;

        DeliverySearch problem = new DeliverySearch(start, goal, g);
        int h = Heuristics.heuristic(problem, start, 2);

        // dx=2, minXCost=min(3,2)=2; dy=3, minYCost=min(4,1,2)=1
        int expected = 2*2 + 3*1; // 4+3=7
        assertEquals(expected, h);
    }

    @Test
    void testTrafficAwareHeuristicAllOnes() {
        State start = new State(1, 1);
        State goal = new State(4, 3);
        Grid g = new Grid(5, 5);

        DeliverySearch problem = new DeliverySearch(start, goal, g);
        int h = Heuristics.heuristic(problem, start, 2);

        int expected = Heuristics.heuristic(problem, start, 1);
        // dx=3, dy=2, min costs=1
        assertEquals(expected, h);
    }
}
