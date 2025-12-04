package code;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DFSTest {

    @Test
    public void DFSTest() {
        Grid g = new Grid(3, 3);

        State store = new State(0, 0);
        State dest  = new State(2, 2);

        DeliverySearch problem = new DeliverySearch(store, dest, g);

        SearchResult result = GenericSearch.DFS(problem);

        assertNotNull(result);

        // DFS trouve une solution mais pas forcément la plus courte
        assertEquals(dest, result.pathStates.get(result.pathStates.size() - 1));

        System.out.println("PLAN = " + result.plan);
        System.out.println("EXPANDED = " + result.expandedOrder);
        System.out.println("PATH = " + result.pathStates);
    }
}
