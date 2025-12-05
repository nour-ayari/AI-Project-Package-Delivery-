package code;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class GridTest {

@Test
    void testBlockedRoads() {
        Grid grid = new Grid(5, 5);

        // Add a blocked road from (2,2) → (2,3)
        State from = new State(2, 2);
        State to = new State(2, 3);
        grid.blockedRoads.add(new RoadBlock(from, to));

        // Test getPossibleActions for (2,2)
        List<String> actions = grid.getPossibleActions(from);
        System.out.println("Possible actions from (2,2): " + actions);

        if (actions.contains("down")) {
            System.out.println("ERROR: Blocked road included!");
        } else {
            System.out.println("Blocked road correctly avoided.");
        }

        // Reverse check
        State reverse = new State(2, 3);
        actions = grid.getPossibleActions(reverse);
        System.out.println("Possible actions from (2,3): " + actions);

        if (actions.contains("up")) {
            System.out.println("ERROR: Reverse blocked road included!");
        } else {
            System.out.println("Reverse blocked road correctly avoided.");
        }
    }

    @Test
    void testApplyAction() {
        Grid g = new Grid(3,3);
        State s = new State(1,1);
        assertEquals(new State(1,0), g.applyAction(s, "up"));
        assertEquals(new State(1,2), g.applyAction(s, "down"));
        assertEquals(new State(0,1), g.applyAction(s, "left"));
        assertEquals(new State(2,1), g.applyAction(s, "right"));
    }

    @Test
    void testTunnel() {
        Grid g = new Grid(3,3);
        Tunnel t = new Tunnel(new State(0,0), new State(2,2));
        g.tunnels.add(t);
        assertTrue(g.isTunnelEntrance(new State(0,0)));
        assertEquals(new State(2,2), g.getTunnelExit(new State(0,0)));
        assertEquals(new State(0,0), g.getTunnelExit(new State(2,2)));
    }

    @Test
    void testGetCost() {
        Grid g = new Grid(2,2);
        g.traffic[0][0][0] = 5; // up
        assertEquals(5, g.getCost(new State(0,0), new State(0,-1), "up"));
    }
}
