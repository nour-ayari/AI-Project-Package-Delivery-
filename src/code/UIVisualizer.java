package code;

import javax.swing.*;

public class UIVisualizer {

    private JFrame frame;
    private GridPanel panel;

    public UIVisualizer(Grid grid) {
        frame = new JFrame("Package Delivery Visualization");
        panel = new GridPanel(grid);
        frame.add(panel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public void updateTruck(State s) {
        panel.setTruck(s);
        try { Thread.sleep(200); } catch (Exception e) { }
    }
}
