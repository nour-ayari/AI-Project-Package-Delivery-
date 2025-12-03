package code;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DeliveryPlannerTest {

    @Test
    void testSingleDelivery() {
        String initialState = "3;3;1;1;2,2;0,0;;";
        String traffic = "0,0,0,1,1;0,0,1,0,1;1,0,1,1,1;1,0,1,0,1;"; // simplified
        String plan = DeliveryPlanner.plan(initialState, traffic, "BF", false);
        assertTrue(plan.contains("Deliver to State{x=2, y=2}"));
    }

    @Test
    void testMultipleDestinations() {
        String initialState = "3;3;2;1;1,2,2,1;0,0;;";
        String traffic = "";
        String plan = DeliveryPlanner.plan(initialState, traffic, "BF", false);
        assertTrue(plan.contains("Deliver to State{x=1, y=2}"));
        assertTrue(plan.contains("Deliver to State{x=2, y=1}"));
    }
}
