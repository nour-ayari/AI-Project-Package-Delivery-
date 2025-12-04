package code;

import javax.swing.*;
import java.awt.*;

public class UIVisualizer {

    private JFrame frame;
    private GridPanel panel;
    private JTextArea debugArea;

    public UIVisualizer(Grid grid) {
        frame = new JFrame("Package Delivery Visualization");

        panel = new GridPanel(grid);

        debugArea = new JTextArea();
        debugArea.setEditable(false);
        debugArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane debugScroll = new JScrollPane(debugArea);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel, debugScroll);
        split.setDividerLocation(1000);
        split.setResizeWeight(1.0);
        frame.add(split);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        frame.setVisible(true);
    }

    public void updateTruck(State s) {
        panel.setTruck(s);
        panel.repaint();
        try {
            Thread.sleep(200); // animation delay
        } catch (Exception ignored) {
        }
    }

    public void drawArrow(State s, String action) {
        panel.addArrow(s, action);
        panel.repaint();
    }

    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            debugArea.append(message + "\n");
            debugArea.setCaretPosition(debugArea.getDocument().getLength());
        });
    }
}
