package code ;
import javax.swing.*;
import java.awt.*;

public class UIVisualizer {

    private JFrame frame;
    private GridPanel panel;

    public UIVisualizer(Grid grid) {
        frame = new JFrame("Package Delivery Visualization");

    panel = new GridPanel();
    panel.setGrid(grid);

        // build controls on the right
        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));

        // Algorithms list (compact legend with color square, name and metrics)
        controls.add(new JLabel("Algorithms:"));

        // algorithms and their colors
        String[] algos = new String[]{"BF","DF","G1","G2","AS1","AS2"};
        java.util.Map<String, Color> colorMap = new java.util.HashMap<>();
        colorMap.put("BF", Color.MAGENTA);
        colorMap.put("DF", Color.ORANGE);
        colorMap.put("G1", Color.CYAN);
        colorMap.put("G2", Color.PINK);
        colorMap.put("AS1", Color.GREEN.darker());
        colorMap.put("AS2", Color.BLUE);

        java.util.Map<String, JCheckBox> algoChecks = new java.util.HashMap<>();
        java.util.Map<String, JLabel> nodesLabelMap = new java.util.HashMap<>();
        java.util.Map<String, JLabel> costLabelMap = new java.util.HashMap<>();

        for (String a : algos) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
            // small color square
            JPanel colorDot = new JPanel();
            colorDot.setBackground(colorMap.get(a));
            colorDot.setPreferredSize(new java.awt.Dimension(12,12));
            row.add(colorDot);

            // algorithm name
            JLabel name = new JLabel(a);
            name.setPreferredSize(new java.awt.Dimension(40, 16));
            row.add(name);

            // nodes / cost labels
            JLabel nodesLbl = new JLabel("nodes: -"); nodesLbl.setPreferredSize(new java.awt.Dimension(80, 16));
            JLabel costLbl = new JLabel("cost: -"); costLbl.setPreferredSize(new java.awt.Dimension(80, 16));
            nodesLabelMap.put(a, nodesLbl);
            costLabelMap.put(a, costLbl);
            row.add(nodesLbl);
            row.add(costLbl);

            // checkbox to toggle final path visibility
            JCheckBox cb = new JCheckBox("show");
            cb.setSelected(false);
            cb.addActionListener(ev -> panel.setPathVisible(a, cb.isSelected()));
            algoChecks.put(a, cb);
            // set color on panel now so legend matches even before compute
            panel.setAlgoColor(a, colorMap.getOrDefault(a, Color.MAGENTA));
            row.add(cb);

            controls.add(row);
        }

    controls.add(Box.createVerticalStrut(8));

    JButton computeButton = new JButton("Compute");
    controls.add(Box.createVerticalStrut(6));
    controls.add(computeButton);

        // Action: compute searches for selected algorithms between chosen store & dest
        computeButton.addActionListener(e -> {
            // No manual store/destination selection in this UI variant: use the first store and the first destination
            if (grid.stores == null || grid.stores.isEmpty() || grid.destinations == null || grid.destinations.isEmpty()) return;
            State store = grid.stores.get(0);
            State dest = grid.destinations.get(0);

            // prepare a results collector to compute recommendation when all finish
            java.util.Map<String, SearchResult> results = new java.util.concurrent.ConcurrentHashMap<>();
            java.util.concurrent.atomic.AtomicInteger remaining = new java.util.concurrent.atomic.AtomicInteger(algos.length);

            for (String a : algos) {
                final String strategy = a;
                new SwingWorker<SearchResult, Void>() {
                    @Override
                    protected SearchResult doInBackground() {
                        return DeliverySearch.solve(store, dest, grid, strategy);
                    }

                    @Override
                    protected void done() {
                        try {
                            SearchResult r = get();
                            if (r != null) {
                                panel.setAlgoColor(strategy, colorMap.getOrDefault(strategy, Color.MAGENTA));
                                panel.setAlgoPath(strategy, r.pathStates);
                                panel.setAlgoExpansions(strategy, r.expandedOrder);
                                // visibility follows the "show" checkbox if checked, otherwise hide by default
                                JCheckBox cb = algoChecks.get(strategy);
                                panel.setPathVisible(strategy, cb != null && cb.isSelected());
                                results.put(strategy, r);
                                // update metric labels if present
                                JLabel nodesLbl = nodesLabelMap.get(strategy);
                                JLabel costLbl = costLabelMap.get(strategy);
                                if (nodesLbl != null) nodesLbl.setText("nodes: " + r.nodesExpanded);
                                if (costLbl != null) costLbl.setText("cost: " + r.cost);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        } finally {
                            int left = remaining.decrementAndGet();
                            if (left == 0) {
                                // all done -> compute recommendation
                                SwingUtilities.invokeLater(() -> {
                                    String rec = computeRecommendation(results);
                                    if (rec != null && !rec.isEmpty()) {
                                        JOptionPane.showMessageDialog(frame, rec, "Recommendation", JOptionPane.INFORMATION_MESSAGE);
                                    }
                                });
                            }
                        }
                    }
                }.execute();
            }
        });


        // compute a simple recommendation: prefer lowest cost, then fewest expansions

        // map symbols legend (small)
        controls.add(Box.createVerticalStrut(8));
        controls.add(new JLabel("Map symbols:"));
        JPanel symRow = new JPanel(new FlowLayout(FlowLayout.LEFT,6,2));
        JPanel storeSym = new JPanel(); storeSym.setBackground(new Color(28,115,185)); storeSym.setPreferredSize(new java.awt.Dimension(12,12)); symRow.add(storeSym); symRow.add(new JLabel("Store"));
        JPanel destSym = new JPanel(); destSym.setBackground(new Color(220,20,60)); destSym.setPreferredSize(new java.awt.Dimension(12,12)); symRow.add(destSym); symRow.add(new JLabel("Customer"));
        JPanel blockSym = new JPanel(); blockSym.setBackground(new Color(200,50,50)); blockSym.setPreferredSize(new java.awt.Dimension(12,12)); symRow.add(blockSym); symRow.add(new JLabel("Roadblock"));
        JPanel tunSym = new JPanel(); tunSym.setBackground(new Color(80,160,90)); tunSym.setPreferredSize(new java.awt.Dimension(12,12)); symRow.add(tunSym); symRow.add(new JLabel("Tunnel"));
        controls.add(symRow);

        // put left grid and right controls in split pane
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel, controls);
        split.setResizeWeight(0.8);

        frame.getContentPane().add(split);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    // expose the internal GridPanel so callers can update its grid
    public GridPanel getPanel() {
        return panel;
    }

    // ---------------------------------------------------------
    // Update truck position (animation)
    // ---------------------------------------------------------
    // helper to compute recommendation after searches finish
    private String computeRecommendation(java.util.Map<String, SearchResult> results) {
        if (results == null || results.isEmpty()) return "No results to compare.";
        String bestCostAlgo = null; int bestCost = Integer.MAX_VALUE;
        String bestExpAlgo = null; int bestExp = Integer.MAX_VALUE;
        for (java.util.Map.Entry<String, SearchResult> e : results.entrySet()) {
            String a = e.getKey(); SearchResult r = e.getValue();
            if (r == null) continue;
            if (r.cost < bestCost) { bestCost = r.cost; bestCostAlgo = a; }
            if (r.nodesExpanded < bestExp) { bestExp = r.nodesExpanded; bestExpAlgo = a; }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Recommendation:\n");
        if (bestCostAlgo != null) sb.append(" - Lowest cost: ").append(bestCostAlgo).append(" (cost=").append(bestCost).append(")\n");
        if (bestExpAlgo != null) sb.append(" - Fewest expansions: ").append(bestExpAlgo).append(" (expanded=").append(bestExp).append(")\n");
        if (bestCostAlgo != null && bestExpAlgo != null && bestCostAlgo.equals(bestExpAlgo)) {
            sb.append("\nOverall best: ").append(bestCostAlgo).append(" (balanced: low cost & few expansions)");
        } else {
            sb.append("\nChoose algorithm depending on priority: cost vs speed (expansions).\n");
        }
        return sb.toString();
    }
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
