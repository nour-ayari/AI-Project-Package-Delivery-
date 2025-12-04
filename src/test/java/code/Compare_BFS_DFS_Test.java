package code;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Compare_BFS_DFS_Test {

    @Test
    public void testCompare() {
        Grid g = new Grid(3, 3);

        State store = new State(0, 0);
        State dest  = new State(2, 2);

        DeliverySearch problem = new DeliverySearch(store, dest, g);

        SearchResult bfs = GenericSearch.BFS(problem);
        SearchResult dfs = GenericSearch.DFS(problem);

        assertNotNull(bfs);
        assertNotNull(dfs);

        assertNotEquals(bfs.plan, dfs.plan, "BFS and DFS should find different solutions");

        System.out.println("BFS : " + bfs.plan);
        System.out.println("DFS : " + dfs.plan);
    }
}
