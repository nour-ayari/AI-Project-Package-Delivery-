package code;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BFSTest{

    @Test
    public void BFSTest3x3() {


        // -----------------------------------------------------
        // 1) Construire une grille contrôlée 3×3
        // -----------------------------------------------------
        Grid g = new Grid(3, 3);

        // 1 store et 1 destination
        State store = new State(0, 0);
        State goal  = new State(2, 0);

        g.stores.add(store);
        g.destinations.add(goal);

        // trafic = 1 partout (aucune route bloquée)
        for (int y = 0; y < 3; y++)
            for (int x = 0; x < 3; x++)
                for (int d = 0; d < 4; d++)
                    g.traffic[y][x][d] = 1;

        SearchResult result = DeliverySearch.solve(store, goal, g, "BF");

        assertNotNull(result);
        assertTrue(result.cost > 0, "Le coût doit être positif.");

        // -----------------------------------------------------
        // 3) Vérifications principales
        // -----------------------------------------------------
        System.out.println("=== BFS DeliverySearch ===");
        System.out.println("Plan = " + result.plan);
        System.out.println("Cost = " + result.cost);
        System.out.println("Nodes expanded = " + result.nodesExpanded);
        System.out.println("Order = " + result.expandedOrder);
        System.out.println("Path = " + result.pathStates);

        // Le dernier état du chemin doit être le goal
        assertEquals(goal, result.pathStates.get(result.pathStates.size() - 1));
    }
@Test
public void testBFS_WithBlockedRoad() {
    Grid g = new Grid(3, 3);

    // store → destination
    State store = new State(0, 0);
    State dest  = new State(2, 0);

    g.stores.add(store);
    g.destinations.add(dest);

    // traffic = 1 everywhere
    for (int y = 0; y < 3; y++)
        for (int x = 0; x < 3; x++)
            for (int d = 0; d < 4; d++)
                g.traffic[y][x][d] = 1;

    // ----- BLOCK A ROAD: (1,0) -> (2,0) -----
    State from = new State(1, 0);
    State to   = new State(2, 0);
    g.blockedRoads.add(new RoadBlock(from, to)); // now edge-based

    DeliverySearch problem = new DeliverySearch(store, dest, g);

    SearchResult result = GenericSearch.BFS(problem);

    assertNotNull(result);
    assertTrue(result.cost >= 0);

    // BFS must avoid blocked road: go down, then right, then up
    assertEquals(dest, result.pathStates.get(result.pathStates.size() - 1));

    System.out.println("PLAN = " + result.plan);
    System.out.println("EXPANDED = " + result.expandedOrder);
    System.out.println("PATH = " + result.pathStates);
}

}
