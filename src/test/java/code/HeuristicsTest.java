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
        assertEquals(7, h);
    }

    // Traffic-aware heuristic
    @Test
    void testTrafficAwareHeuristicRightDown() {
        State start = new State(1, 1);
        State goal = new State(3, 4);
        Grid g = new Grid(5, 5);

        // Set traffic costs
        g.traffic[1][1][3] = 1;
        g.traffic[1][1][1] = 2;

        DeliverySearch problem = new DeliverySearch(start, goal, g);

        int h = Heuristics.heuristic(problem, start, 2);

        
        int expected = 3 + 5;
        assertEquals(expected, h);
    }

    @Test
    void testTrafficAwareHeuristicLeftUp() {
        State start = new State(3, 4);
        State goal = new State(1, 1);
        Grid g = new Grid(5, 5);

        g.traffic[4][3][2] = 3;
        g.traffic[4][3][0] = 4;

        DeliverySearch problem = new DeliverySearch(start, goal, g);

        int h = Heuristics.heuristic(problem, start, 2);

        // Manhattan = 2+3=5
        // Traffic = 3+4=7
        int expected = 5 + 7; // 12
        assertEquals(expected, h);
    }

    @Test
    void testStartEqualsGoal() {
        State s = new State(2, 2);
        Grid g = new Grid(5, 5);
        DeliverySearch problem = new DeliverySearch(s, s, g);
        assertEquals(0, Heuristics.heuristic(problem, s, 1));
        assertEquals(0, Heuristics.heuristic(problem, s, 2));
    }

}