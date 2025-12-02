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

        // Store / Destination selectors
        controls.add(new JLabel("Select store:"));
        JComboBox<State> storeBox = new JComboBox<>();
        for (State s : grid.stores) storeBox.addItem(s);
        controls.add(storeBox);

        controls.add(new JLabel("Select destination:"));
        JComboBox<State> destBox = new JComboBox<>();
        for (State d : grid.destinations) destBox.addItem(d);
        controls.add(destBox);

        controls.add(Box.createVerticalStrut(10));

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

        for (String a : algos) {
            JPanel row = new JPanel();
            row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
            JCheckBox cb = new JCheckBox(a);
            cb.setSelected(false);
            algoChecks.put(a, cb);
            // when checkbox toggled, show/hide the algorithm path overlay
            cb.addActionListener(ev -> panel.setPathVisible(a, cb.isSelected()));
            // set color on panel now so legend matches even before compute
            panel.setAlgoColor(a, colorMap.getOrDefault(a, Color.MAGENTA));
            row.add(cb);

            JButton play = new JButton("Play");
            // when play pressed, animate expansions for this algo
            play.addActionListener(ev -> panel.animateExpansions(a));
            row.add(Box.createHorizontalStrut(6));
            row.add(play);

            // legend color square
            JPanel colorDot = new JPanel();
            colorDot.setBackground(colorMap.get(a));
            colorDot.setPreferredSize(new java.awt.Dimension(14,14));
            row.add(Box.createHorizontalStrut(6));
            row.add(colorDot);

            controls.add(row);
        }

    controls.add(Box.createVerticalStrut(8));

    // --- Animation controls (select algorithm to control, play/pause/step, speed) ---
    controls.add(new JLabel("Animation controls:"));
    JPanel animPanel = new JPanel();
    animPanel.setLayout(new BoxLayout(animPanel, BoxLayout.X_AXIS));
    JComboBox<String> algoSelect = new JComboBox<>();
    for (String a : algos) algoSelect.addItem(a);
    animPanel.add(algoSelect);
    animPanel.add(Box.createHorizontalStrut(6));
    JButton playAll = new JButton("Play");
    JButton pause = new JButton("Pause");
    JButton stepBack = new JButton("<");
    JButton stepFwd = new JButton(">");
    animPanel.add(playAll); animPanel.add(Box.createHorizontalStrut(4)); animPanel.add(pause);
    animPanel.add(Box.createHorizontalStrut(4)); animPanel.add(stepBack); animPanel.add(stepFwd);
    animPanel.add(Box.createHorizontalStrut(6));
    JSlider speed = new JSlider(20, 500, 80);
    speed.setToolTipText("Animation delay ms");
    speed.setPreferredSize(new java.awt.Dimension(120, 24));
    animPanel.add(speed);
    controls.add(animPanel);

    JButton computeButton = new JButton("Compute selected");
    controls.add(Box.createVerticalStrut(6));
    controls.add(computeButton);

        // Action: compute searches for selected algorithms between chosen store & dest
        computeButton.addActionListener(e -> {
            State store = (State) storeBox.getSelectedItem();
            State dest = (State) destBox.getSelectedItem();
            if (store == null || dest == null) return;
            // prepare a results collector to compute recommendation when all finish
            java.util.Map<String, SearchResult> results = new java.util.concurrent.ConcurrentHashMap<>();
            java.util.concurrent.atomic.AtomicInteger remaining = new java.util.concurrent.atomic.AtomicInteger(0);

            // count selected
            for (String a : algos) if (algoChecks.get(a) != null && algoChecks.get(a).isSelected()) remaining.incrementAndGet();
            if (remaining.get() == 0) return;

            for (String a : algos) {
                JCheckBox cb = algoChecks.get(a);
                if (cb == null || !cb.isSelected()) {
                    panel.setAlgoPath(a, null);
                    panel.setAlgoExpansions(a, null);
                    continue;
                }

                final String strategy = a;
                // run search in background to avoid freezing UI
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
                                panel.setPathVisible(strategy, true);
                                results.put(strategy, r);
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
        // wire animation controls
        playAll.addActionListener(ev -> {
            String a = (String) algoSelect.getSelectedItem();
            if (a != null) {
                panel.setAnimationDelay(speed.getValue());
                panel.animateExpansions(a);
            }
        });
        pause.addActionListener(ev -> panel.stopAnimation());
        stepBack.addActionListener(ev -> {
            String a = (String) algoSelect.getSelectedItem(); if (a != null) panel.stepExpansion(a, -1);
        });
        stepFwd.addActionListener(ev -> {
            String a = (String) algoSelect.getSelectedItem(); if (a != null) panel.stepExpansion(a, +1);
        });
        speed.addChangeListener(ev -> panel.setAnimationDelay(speed.getValue()));

        // --- Legend (colors + map symbols)
        controls.add(Box.createVerticalStrut(8));
        controls.add(new JLabel("Legend:"));
        JPanel legend = new JPanel();
        legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS));
        for (String a : algos) {
            JPanel lrow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
            JPanel col = new JPanel(); col.setBackground(colorMap.get(a)); col.setPreferredSize(new java.awt.Dimension(14,14));
            lrow.add(col); lrow.add(new JLabel(a));
            legend.add(lrow);
        }
        // map symbols
    JPanel symRow = new JPanel(new FlowLayout(FlowLayout.LEFT,6,2));
    JPanel storeSym = new JPanel(); storeSym.setBackground(new Color(28,115,185)); storeSym.setPreferredSize(new java.awt.Dimension(14,14)); symRow.add(storeSym); symRow.add(new JLabel("Store"));
    JPanel destSym = new JPanel(); destSym.setBackground(new Color(220,20,60)); destSym.setPreferredSize(new java.awt.Dimension(14,14)); symRow.add(destSym); symRow.add(new JLabel("Customer"));
    JPanel blockSym = new JPanel(); blockSym.setBackground(new Color(200,50,50)); blockSym.setPreferredSize(new java.awt.Dimension(14,14)); symRow.add(blockSym); symRow.add(new JLabel("Roadblock"));
    JPanel tunSym = new JPanel(); tunSym.setBackground(new Color(80,160,90)); tunSym.setPreferredSize(new java.awt.Dimension(14,14)); symRow.add(tunSym); symRow.add(new JLabel("Tunnel"));
        legend.add(symRow);
        controls.add(legend);

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
