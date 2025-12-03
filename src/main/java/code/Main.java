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
        frame.setSize(400, 200);
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
                protected Void doInBackground() throws Exception {
                    DeliveryPlanner.plan(init, traffic, selectedAlgo, true);
                    return null;
                }

                @Override
                protected void done() {
                    int choice = JOptionPane.showConfirmDialog(
                            null,
                            "Do you want to try another algorithm?",
                            "Try Again?",
                            JOptionPane.YES_NO_OPTION);

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
        frame.setSize(400, 200);
        frame.setLayout(new GridLayout(3, 1));

        JButton randomBtn = new JButton("Generate Random Grid");
        JButton manualBtn = new JButton("Enter Grid Manually");

        frame.add(new JLabel("Choose grid setup method:", SwingConstants.CENTER));
        frame.add(randomBtn);
        frame.add(manualBtn);

        frame.setVisible(true);

        manualBtn.addActionListener(e -> {
            frame.dispose();

            int rows = Integer.parseInt(JOptionPane.showInputDialog("Number of rows:"));
            int cols = Integer.parseInt(JOptionPane.showInputDialog("Number of columns:"));
            int stores = Integer.parseInt(JOptionPane.showInputDialog("Number of stores:"));
            int destinations = Integer.parseInt(JOptionPane.showInputDialog("Number of destinations:"));

            String generated = Grid.GenGrid(rows, cols, stores, destinations);

            JOptionPane.showMessageDialog(null,
                    "Grid generated!\nRows: " + rows + "\nColumns: " + cols +
                            "\nStores: " + stores + "\nDestinations: " + destinations);

            String[] parts = generated.split("\n");
            String init = parts[0];
            String traffic = parts[1];

            showAlgorithmSelection(init, traffic);
        });

        randomBtn.addActionListener(e -> {
            frame.dispose();

            String generated = Grid.GenGrid();


            String[] parts = generated.split("\n");
            String init = parts[0];
            String traffic = parts[1];
            
            JOptionPane.showMessageDialog(null,
                    "Grid generated!\nRows: " + init);

            showAlgorithmSelection(init, traffic);
            
        });
    }
}
