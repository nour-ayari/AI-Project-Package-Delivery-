package code;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Compare_BFS_DFS_Test {

    @Test
    public void testCompareBFSvsDFS() {
        Grid g = new Grid(3, 3);
        for (int y = 0; y < g.rows; y++) {
            for (int x = 0; x < g.cols; x++) {
                if (x + 1 < g.cols)
                    g.traffic[y][x][3] = 1; // right
                if (y + 1 < g.rows)
                    g.traffic[y][x][1] = 1; // down
                if (x - 1 >= 0)
                    g.traffic[y][x][2] = 1; // left
                if (y - 1 >= 0)
                    g.traffic[y][x][0] = 1; // up
            }
        }

        State start = new State(0, 0);
        State goal = new State(2, 2);

        DeliverySearch problem = new DeliverySearch(start, goal, g);

        SearchResult bfs = GenericSearch.BFS(problem);
        SearchResult dfs = GenericSearch.DFS(problem);

        // 1. Check that results are not null
        assertNotNull(bfs, "BFS result should not be null");
        assertNotNull(dfs, "DFS result should not be null");

        // 2. Check that pathStates are non-empty
        assertFalse(bfs.pathStates.isEmpty(), "BFS pathStates should not be empty");
        assertFalse(dfs.pathStates.isEmpty(), "DFS pathStates should not be empty");

        // 3. Start and goal must match
        assertEquals(start, bfs.pathStates.get(0));
        assertEquals(goal, bfs.pathStates.get(bfs.pathStates.size() - 1));

        assertEquals(start, dfs.pathStates.get(0));
        assertEquals(goal, dfs.pathStates.get(dfs.pathStates.size() - 1));

        // 4. BFS and DFS usually give different plans
        boolean differentPlans = !bfs.plan.equals(dfs.plan);
        System.out.println("BFS Plan: " + bfs.plan);
        System.out.println("DFS Plan: " + dfs.plan);
        System.out.println("Are plans different? " + differentPlans);
    }
}
