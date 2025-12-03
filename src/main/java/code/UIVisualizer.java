package code ;
import javax.swing.*;
import java.awt.*;


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

    // ---------------------------------------------------------
    // Update truck position (animation)
    // ---------------------------------------------------------
    public void updateTruck(State s) {
        panel.setTruck(s);
        panel.repaint();
        try {
            Thread.sleep(200); // animation delay
        } catch (Exception ignored) {}
    }

    // ---------------------------------------------------------
    // Draw arrow at a specific step
    // ---------------------------------------------------------
    public void drawArrow(State s, String action) {
        panel.addArrow(s, action);
        panel.repaint();
    }
}