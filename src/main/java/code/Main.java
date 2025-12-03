package code;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {

    public static void main(String[] args) {
        // Generate grid and traffic data
        String generated = Grid.GenGrid();
        String[] parts = generated.split("\n");
        String init = parts[0];
        String traffic = parts[1];

        // Create main frame
        JFrame frame = new JFrame("Package Delivery Visualizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLayout(new BorderLayout());

        // Panel for algorithm selection
        JPanel selectionPanel = new JPanel();
        selectionPanel.setLayout(new FlowLayout());

        JLabel label = new JLabel("Select Algorithm: ");
        String[] algorithms = { "BF", "DF", "UC", "ID", "G1", "G2", "A1", "A2" };
        JComboBox<String> algoBox = new JComboBox<>(algorithms);

        JButton startButton = new JButton("Start Visualization");

        selectionPanel.add(label);
        selectionPanel.add(algoBox);
        selectionPanel.add(startButton);

        frame.add(selectionPanel, BorderLayout.CENTER);

        // Show frame
        frame.setVisible(true);

        // Action listener for start button
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedAlgo = (String) algoBox.getSelectedItem();
                System.out.println("Selected Algorithm: " + selectedAlgo);

                // Close selection UI
                frame.dispose();

                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        DeliveryPlanner.plan(init, traffic, selectedAlgo, true);
                        return null;
                    }
                }.execute();
            }
        });
    }
}
