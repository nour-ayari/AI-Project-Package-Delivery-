package code;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GenericSearchTest {
    
     private Grid create3x3Grid() {
        Grid g = new Grid(3, 3);
        for (int y = 0; y < 3; y++)
            for (int x = 0; x < 3; x++)
                for (int d = 0; d < 4; d++)
                    g.traffic[y][x][d] = 1;
        return g;
    }

    private State store() { return new State(0, 0); }
    private State dest()  { return new State(2, 2); }

    @Test
    public void UCSTest3x3() {
        Grid g = create3x3Grid();
        DeliverySearch problem = new DeliverySearch(store(), dest(), g);
        SearchResult result = GenericSearch.UCS(problem);

        assertNotNull(result);
        assertTrue(result.cost >= 0);
        assertEquals(dest(), result.pathStates.get(result.pathStates.size() - 1));

        System.out.println("UCS Plan = " + result.plan);
        System.out.println("Nodes Expanded = " + result.nodesExpanded);
    }

    @Test
    public void GreedyTest3x3() {
        Grid g = create3x3Grid();
        DeliverySearch problem = new DeliverySearch(store(), dest(), g);
        SearchResult result = GenericSearch.Greedy(problem, 1); 

        assertNotNull(result);
        assertEquals(dest(), result.pathStates.get(result.pathStates.size() - 1));

        System.out.println("Greedy Plan = " + result.plan);
    }

    @Test
    public void AStarTest3x3() {
        Grid g = create3x3Grid();
        DeliverySearch problem = new DeliverySearch(store(), dest(), g);
        SearchResult result = GenericSearch.AStar(problem, 2);

        assertNotNull(result);
        assertEquals(dest(), result.pathStates.get(result.pathStates.size() - 1));

        System.out.println("A* Plan = " + result.plan);
    }

    @Test
    public void IDTest3x3() {
        Grid g = create3x3Grid();
        DeliverySearch problem = new DeliverySearch(store(), dest(), g);
        SearchResult result = GenericSearch.ID(problem);

        assertNotNull(result);
        assertEquals(dest(), result.pathStates.get(result.pathStates.size() - 1));

        System.out.println("ID Plan = " + result.plan);
    }

    @Test
    public void TestBlockedRoadUCS() {
        Grid g = create3x3Grid();
        State from = new State(1, 1);
        State to   = new State(2, 1);
        g.blockedRoads.add(new RoadBlock(from, to));

        DeliverySearch problem = new DeliverySearch(store(), dest(), g);
        SearchResult result = GenericSearch.UCS(problem);

        assertNotNull(result);
        assertEquals(dest(), result.pathStates.get(result.pathStates.size() - 1));
        System.out.println("UCS with blocked road Plan = " + result.plan);
    }

}
