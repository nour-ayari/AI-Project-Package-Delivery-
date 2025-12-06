package code;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DFSTest {

    @Test
    public void DFSTest3x3() {
        Grid g = new Grid(3, 3);
        State store = new State(0, 0);
        State dest = new State(2, 2);
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                g.traffic[y][x][0] = 1;
                g.traffic[y][x][1] = 1;
                g.traffic[y][x][2] = 1;
                g.traffic[y][x][3] = 1;
            }
        }
        DeliverySearch problem = new DeliverySearch(store, dest, g);
        SearchResult result = GenericSearch.DFS(problem);

        // Check DFS found something
        assertNotNull(result);
        assertFalse(result.pathStates.isEmpty(), "DFS did not find any path");

        // Now safe to check destination
        assertEquals(dest, result.pathStates.get(result.pathStates.size() - 1));

        System.out.println("PLAN = " + result.plan);
        System.out.println("EXPANDED = " + result.expandedOrder);
        System.out.println("PATH = " + result.pathStates);
    }
}
