package code;

public class Main {
    public static void main(String[] args) {

       String generated = Grid.GenGrid();
String[] parts = generated.split("\n");

String init = parts[0];
String traffic = parts[1];

System.out.println(init);
System.out.println(traffic);

DeliveryPlanner.plan(init, traffic, "BF", true);

    }
}
