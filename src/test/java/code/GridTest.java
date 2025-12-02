package code;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class GridTest {

    @Test
    void testPossibleActions() {
        Grid g = new Grid(3,3);
        State s = new State(1,1);
        List<String> actions = g.getPossibleActions(s);
        assertTrue(actions.contains("up"));
        assertTrue(actions.contains("down"));
        assertTrue(actions.contains("left"));
        assertTrue(actions.contains("right"));
        assertFalse(actions.contains("tunnel"));
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
    void testBlockedRoad() {
        Grid g = new Grid(3,3);
        g.blockedRoads.add(new RoadBlock(new State(1,1), "up"));
        assertTrue(g.isBlocked(new State(1,1), "up"));
        assertFalse(g.isBlocked(new State(1,1), "down"));
    }

    @Test
    void testGetCost() {
        Grid g = new Grid(2,2);
        g.traffic[0][0][0] = 5; // up
        assertEquals(5, g.getCost(new State(0,0), new State(0,-1), "up"));
    }
}
