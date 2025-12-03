package code;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        showGridSetup();
    }
    private static void showAlgorithmSelection(String init, String traffic) {
        JFrame frame = new JFrame("Package Delivery Visualizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 300);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        JPanel selectionPanel = new JPanel(new FlowLayout());
        JLabel label = new JLabel("Select Algorithm: ");
        String[] algorithms = { "BF", "DF", "UC", "ID", "G1", "G2", "AS1", "AS2" };
        JComboBox<String> algoBox = new JComboBox<>(algorithms);
        JButton startButton = new JButton("Start Visualization");

        selectionPanel.add(label);
        selectionPanel.add(algoBox);
        selectionPanel.add(startButton);
        frame.add(selectionPanel, BorderLayout.CENTER);
        frame.setVisible(true);

        startButton.addActionListener(e -> {
            String selectedAlgo = (String) algoBox.getSelectedItem();
            frame.dispose();

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    DeliveryPlanner.plan(init, traffic, selectedAlgo, true);
                    return null;
                }

                @Override
                protected void done() {
                    int choice = JOptionPane.showConfirmDialog(null, "Do you want to try another algorithm?",
                            "Try Again?", JOptionPane.YES_NO_OPTION);
                    if (choice == JOptionPane.YES_OPTION) {
                        showAlgorithmSelection(init, traffic);
                    } else {
                        System.exit(0);
                    }
                }
            }.execute();
        });
    }

    private static void showGridSetup() {
        JFrame frame = new JFrame("Grid Setup");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 300);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("Choose grid setup method:");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JButton randomBtn = new JButton("Generate Random Grid");
        JButton manualBtn = new JButton("Enter Grid Manually");

        for (JButton btn : new JButton[] { randomBtn, manualBtn }) {
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(200, 40));
            btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        }

        panel.add(label);
        panel.add(randomBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(manualBtn);
        frame.add(panel);
        frame.setVisible(true);

        // Manual grid input
        manualBtn.addActionListener(e -> {
            frame.dispose();
            int rows = Integer.parseInt(JOptionPane.showInputDialog("Number of rows:"));
            int cols = Integer.parseInt(JOptionPane.showInputDialog("Number of columns:"));
            int stores = Integer.parseInt(JOptionPane.showInputDialog("Number of stores:"));
            int destinations = Integer.parseInt(JOptionPane.showInputDialog("Number of destinations:"));

            String generated = Grid.GenGrid(rows, cols, stores, destinations);
            String[] parts = generated.split("\n");
            String init = parts[0];
            String traffic = parts[1];

            JOptionPane.showMessageDialog(null,
                    "Grid generated!\nRows: " + rows +
                            "\nColumns: " + cols +
                            "\nStores: " + stores +
                            "\nDestinations: " + destinations);

            showAlgorithmSelection(init, traffic);
        });

        randomBtn.addActionListener(e -> {
            frame.dispose();
            String generated = Grid.GenGrid();
            String[] parts = generated.split("\n");
            String init = parts[0];
            String traffic = parts[1];

            String[] fields = init.split(";");
            int cols = Integer.parseInt(fields[0]);
            int rows = Integer.parseInt(fields[1]);
            int numDestinations = Integer.parseInt(fields[2]);
            int numStores = Integer.parseInt(fields[3]);

            JOptionPane.showMessageDialog(null,
                    "Grid generated!\nRows: " + rows +
                            "\nColumns: " + cols +
                            "\nStores: " + numStores +
                            "\nDestinations: " + numDestinations);

            showAlgorithmSelection(init, traffic);
        });
    }
}
