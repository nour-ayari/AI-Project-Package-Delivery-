package code;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Simple Swing renderer to visualize the grid, traffic, tunnels, stores, customers and routes.
 * Extended to display exploration order (expanded nodes), frontier, final paths and an overlay
 * with metrics per-algorithm. Also supports saving images.
 */
public class GridRenderer extends JPanel {

    public static class Route {
        public final State store;
        public final State customer;
        public final java.util.List<State> path;
        public final Color color;
        public Route(State store, State customer, java.util.List<State> path, Color color) {
            this.store = store; this.customer = customer; this.path = path; this.color = color;
        }
    }

    // Execution trace for an algorithm: order of expanded nodes, current frontier snapshots (optional)
    public static class ExecutionTrace {
        public final String name; // algorithm name
    public final java.util.List<State> expandedOrder; // order nodes were expanded
    public final java.util.List<State> frontierSnapshot; // optional later snapshot or last frontier
    public final java.util.List<State> closedSet; // optional
    public final java.util.List<State> finalPath; // sequence from start to goal
        public final Color color; // color to render path
        public final int finalCost; // -1 if no solution
        public final int nodesExpanded;

    public ExecutionTrace(String name, java.util.List<State> expandedOrder, java.util.List<State> frontierSnapshot,
                  java.util.List<State> closedSet, java.util.List<State> finalPath, Color color,
                              int finalCost, int nodesExpanded) {
            this.name = name;
            this.expandedOrder = expandedOrder != null ? expandedOrder : new ArrayList<>();
            this.frontierSnapshot = frontierSnapshot != null ? frontierSnapshot : new ArrayList<>();
            this.closedSet = closedSet != null ? closedSet : new ArrayList<>();
            this.finalPath = finalPath != null ? finalPath : new ArrayList<>();
            this.color = color;
            this.finalCost = finalCost;
            this.nodesExpanded = nodesExpanded;
        }
    }

    private final Grid grid;
    private final java.util.List<Route> routes;
    private final java.util.List<ExecutionTrace> traces;
    private final int cellSize = 60;
    private final int margin = 40;

    // animation state: how many expanded nodes to show for each trace
    private final Map<String, Integer> animateStep = new HashMap<>();
    private javax.swing.Timer animator;
    // visibility per trace (controlled by checkboxes)
    private final Map<String, Boolean> traceVisible = new HashMap<>();

    public GridRenderer(Grid grid, java.util.List<Route> routes, java.util.List<ExecutionTrace> traces) {
        this.grid = grid;
        this.routes = routes != null ? routes : new ArrayList<>();
        this.traces = traces != null ? traces : new ArrayList<>();
    int w = margin * 2 + grid.cols * cellSize;
    int h = margin * 2 + grid.rows * cellSize + 120; // extra space for overlay
        setPreferredSize(new Dimension(w, h));

        for (ExecutionTrace t : this.traces) {
            animateStep.put(t.name, 0);
            traceVisible.put(t.name, true);
        }
    }

    /**
     * Build a small control panel containing one checkbox per algorithm and global Select/Deselect buttons.
     */
    public JPanel createControlPanel() {
        JPanel controls = new JPanel();
        controls.setLayout(new FlowLayout(FlowLayout.LEFT));

        java.util.List<JCheckBox> boxes = new ArrayList<>();
        for (ExecutionTrace t : traces) {
            JCheckBox cb = new JCheckBox(t.name, true);
            cb.setForeground(t.color != null ? t.color.darker() : Color.BLACK);
            cb.addItemListener(e -> {
                traceVisible.put(t.name, cb.isSelected());
                repaint();
            });
            boxes.add(cb);
            controls.add(cb);
        }

        JButton selectAll = new JButton("Select All");
        selectAll.addActionListener(e -> {
            for (JCheckBox cb : boxes) { cb.setSelected(true); }
        });
        JButton deselectAll = new JButton("Deselect All");
        deselectAll.addActionListener(e -> {
            for (JCheckBox cb : boxes) { cb.setSelected(false); }
        });
        controls.add(selectAll);
        controls.add(deselectAll);

        return controls;
    }

    private Point toPixel(State p) {
        int px = margin + p.x * cellSize + cellSize/2;
        // flip y so y=0 at bottom
        int py = margin + (grid.rows - 1 - p.y) * cellSize + cellSize/2;
        return new Point(px, py);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        // draw grid cells
        for (int x = 0; x < grid.cols; x++) {
            for (int y = 0; y < grid.rows; y++) {
                int rx = margin + x * cellSize;
                int ry = margin + (grid.rows - 1 - y) * cellSize;
                g.setColor(Color.WHITE);
                g.fillRect(rx, ry, cellSize, cellSize);
                g.setColor(new Color(0xCCCCCC));
                g.drawRect(rx, ry, cellSize, cellSize);
            }
        }

        // draw edge costs for right and up edges
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        for (int x = 0; x < grid.cols; x++) {
            for (int y = 0; y < grid.rows; y++) {
                State p = new State(x, y);
                // right
                if (x + 1 < grid.cols) {
                    State q = new State(x+1, y);
                    int cost = grid.getCost(p, q, "right");
                    if (cost > 0) {
                        Point a = toPixel(p);
                        Point b = toPixel(q);
                        int mx = (a.x + b.x) / 2;
                        int my = (a.y + b.y) / 2;
                        g.setColor(Color.DARK_GRAY);
                        g.drawString(Integer.toString(cost), mx - 6, my + 4);
                    }
                }
                // up
                if (y + 1 < grid.rows) {
                    State q = new State(x, y+1);
                    // moving from p=(x,y) to q=(x,y+1) is a "down" action (y increases)
                    int cost = grid.getCost(p, q, "down");
                    if (cost > 0) {
                        Point a = toPixel(p);
                        Point b = toPixel(q);
                        int mx = (a.x + b.x) / 2;
                        int my = (a.y + b.y) / 2;
                        g.setColor(Color.DARK_GRAY);
                        g.drawString(Integer.toString(cost), mx - 6, my + 4);
                    }
                }
            }
        }

        // draw tunnels
        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(0x2E8B57)); // sea green
        for (Tunnel t : grid.tunnels) {
            Point a = toPixel(t.A);
            Point b = toPixel(t.B);
            // dashed line
            Stroke old = g.getStroke();
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{8f,8f}, 0f));
            g.drawLine(a.x, a.y, b.x, b.y);
            g.setStroke(old);
            g.fillOval(a.x-6, a.y-6, 12, 12);
            g.fillOval(b.x-6, b.y-6, 12, 12);
        }

        // draw expanded nodes (per trace) with semi-transparent colors and labels
        for (ExecutionTrace t : traces) {
            boolean visible = traceVisible.getOrDefault(t.name, true);
            if (!visible) continue;
            Color base = t.color != null ? t.color : Color.MAGENTA;
            Color fill = new Color(base.getRed(), base.getGreen(), base.getBlue(), 120);
            int steps = animateStep.getOrDefault(t.name, t.expandedOrder.size());

            for (int i = 0; i < steps && i < t.expandedOrder.size(); i++) {
                State pos = t.expandedOrder.get(i);
                Point p = toPixel(pos);
                int s = cellSize/3;
                g.setColor(fill);
                g.fillOval(p.x - s/2, p.y - s/2, s, s);
                g.setColor(base.darker());
                g.drawOval(p.x - s/2, p.y - s/2, s, s);
                g.setColor(Color.BLACK);
                g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
                String label = Integer.toString(i+1);
                g.drawString(label, p.x - 6, p.y + 4);
            }

            // optionally draw frontier snapshot as squares
            for (State fpos : t.frontierSnapshot) {
                Point pf = toPixel(fpos);
                g.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 180));
                g.fillRect(pf.x - 6, pf.y - 6, 12, 12);
                g.setColor(base.darker());
                g.drawRect(pf.x - 6, pf.y - 6, 12, 12);
            }
            
        }

        // draw routes (final planned routes)
        // If no explicit routes were provided, generate routes from traces' finalPath so
        // we can show all algorithm paths together with their colors.
        java.util.List<Route> displayRoutes = routes;
        if ((displayRoutes == null || displayRoutes.isEmpty()) && traces != null) {
            displayRoutes = new ArrayList<>();
            for (ExecutionTrace t : traces) {
                boolean visible = traceVisible.getOrDefault(t.name, true);
                if (!visible) continue;
                if (t.finalPath != null && t.finalPath.size() > 1) {
                    displayRoutes.add(new Route(t.finalPath.get(0), t.finalPath.get(t.finalPath.size() - 1), t.finalPath, t.color != null ? t.color : Color.MAGENTA));
                }
            }
        }

        if (displayRoutes != null) for (Route r : displayRoutes) {
            g.setColor(r.color);
            g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i + 1 < r.path.size(); i++) {
                Point p1 = toPixel(r.path.get(i));
                Point p2 = toPixel(r.path.get(i+1));
                g.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
            // draw arrows
            for (int i = 0; i + 1 < r.path.size(); i++) {
                Point from = toPixel(r.path.get(i));
                Point to = toPixel(r.path.get(i+1));
                drawArrow(g, from.x, from.y, to.x, to.y, r.color);
            }
        }

        // draw stores and customers
        for (State s : grid.stores) {
            Point p = toPixel(s);
            g.setColor(new Color(0x1E90FF)); // dodger blue
            g.fillRect(p.x-12, p.y-12, 24, 24);
            g.setColor(Color.BLACK);
            g.drawRect(p.x-12, p.y-12, 24, 24);
            g.setColor(Color.WHITE);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            g.drawString("S", p.x - 5, p.y + 5);
        }
        for (State c : grid.destinations) {
            Point p = toPixel(c);
            g.setColor(new Color(0xDC143C)); // crimson
            Polygon tri = new Polygon();
            tri.addPoint(p.x, p.y-14);
            tri.addPoint(p.x-12, p.y+10);
            tri.addPoint(p.x+12, p.y+10);
            g.fillPolygon(tri);
            g.setColor(Color.BLACK);
            g.drawPolygon(tri);
            g.setColor(Color.WHITE);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            g.drawString("C", p.x - 5, p.y + 5);
        }

        // draw legend and metrics overlay at bottom
        drawOverlay(g);
    }

    private void drawOverlay(Graphics2D g) {
    int overlayY = margin + grid.rows * cellSize + 10;
        int x = margin;
        int y = overlayY;

        g.setColor(new Color(255,255,255,220));
        g.fillRoundRect(x - 8, y - 6, getWidth() - margin*2 + 16, 100, 8, 8);
        g.setColor(Color.BLACK);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g.drawString("Algorithms execution summary:", x, y + 14);

        // draw a legend: one colored square + algorithm name + cost + pathlen
        int lineY = y + 34;
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        // gracefully handle null traces
        java.util.List<ExecutionTrace> tlist = traces != null ? traces : new ArrayList<>();

        // compute best algorithm (lowest cost among solved)
        ExecutionTrace best = pickBest(tlist);

        for (ExecutionTrace t : tlist) {
            String solved = t.finalCost >= 0 ? String.format("cost=%d", t.finalCost) : "no-solution";
            String line = String.format("%s — %s, nodes=%d, pathLen=%d", t.name, solved, t.nodesExpanded, t.finalPath != null ? t.finalPath.size() : 0);

            // color swatch
            Color sw = t.color != null ? t.color : Color.MAGENTA;
            g.setColor(sw);
            g.fillRect(x, lineY - 14, 18, 14);
            g.setColor(Color.BLACK);
            g.drawRect(x, lineY - 14, 18, 14);

            // text
            if (best != null && t == best) {
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
                g.drawString(line + "  <-- BEST", x + 24, lineY - 2);
                g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            } else {
                g.drawString(line, x + 24, lineY - 2);
            }

            lineY += 20;
        }
    }

    private ExecutionTrace pickBest(java.util.List<ExecutionTrace> traces) {
        // prefer solved with lowest cost; if none solved, pick lowest nodesExpanded
        ExecutionTrace bestCost = null;
        for (ExecutionTrace t : traces) {
            if (t.finalCost >= 0) {
                if (bestCost == null || t.finalCost < bestCost.finalCost) bestCost = t;
            }
        }
        if (bestCost != null) return bestCost;
        // pick by nodesExpanded
        ExecutionTrace bestNodes = null;
        for (ExecutionTrace t : traces) {
            if (bestNodes == null || t.nodesExpanded < bestNodes.nodesExpanded) bestNodes = t;
        }
        return bestNodes;
    }

    private void drawArrow(Graphics2D g, int x1, int y1, int x2, int y2, Color color) {
        g.setColor(color);
        double dx = x2 - x1;
        double dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        // draw arrow head
        int ax = x2;
        int ay = y2;
        int ah = 8; // size
        Polygon p = new Polygon();
        p.addPoint(ax, ay);
        p.addPoint((int)(ax - ah * Math.cos(angle - Math.PI / 6)), (int)(ay - ah * Math.sin(angle - Math.PI / 6)));
        p.addPoint((int)(ax - ah * Math.cos(angle + Math.PI / 6)), (int)(ay - ah * Math.sin(angle + Math.PI / 6)));
        g.fillPolygon(p);
    }

    /**
     * Start animation with given step delay in ms. Default mode animates all traces in parallel.
     */
    public void startAnimation(int msStep) {
        startAnimation(msStep, false);
    }

    /**
     * Start animation with given step delay in ms.
     * If sequential==true, traces are animated one after another (fully) instead of in parallel.
     */
    public void startAnimation(int msStep, boolean sequential) {
        if (animator != null && animator.isRunning()) animator.stop();
        // reset steps
        for (ExecutionTrace t : traces) animateStep.put(t.name, 0);

        if (!sequential) {
            animator = new javax.swing.Timer(msStep, e -> {
                boolean changed = false;
                for (ExecutionTrace t : traces) {
                    int cur = animateStep.getOrDefault(t.name, 0);
                    if (cur < t.expandedOrder.size()) {
                        animateStep.put(t.name, cur + 1);
                        changed = true;
                    }
                }
                if (!changed) {
                    animator.stop();
                }
                repaint();
            });
            animator.start();
            return;
        }

        // sequential mode: animate traces one by one
        animator = new javax.swing.Timer(msStep, new java.awt.event.ActionListener() {
            int currentIndex = 0;

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (currentIndex >= traces.size()) {
                    animator.stop();
                    return;
                }

                ExecutionTrace t = traces.get(currentIndex);
                int cur = animateStep.getOrDefault(t.name, 0);
                if (cur < t.expandedOrder.size()) {
                    animateStep.put(t.name, cur + 1);
                } else {
                    // finished current trace, move to next after a short pause
                    currentIndex++;
                }

                // if we've animated all traces, stop
                boolean allDone = true;
                for (ExecutionTrace tx : traces) {
                    if (animateStep.getOrDefault(tx.name, 0) < tx.expandedOrder.size()) { allDone = false; break; }
                }
                if (allDone) {
                    animator.stop();
                }

                repaint();
            }
        });
        animator.start();
    }

    public void stopAnimation() {
        if (animator != null) animator.stop();
    }

    public void resetAnimation() {
        for (ExecutionTrace t : traces) animateStep.put(t.name, 0);
        repaint();
    }

    public static void show(Grid grid, java.util.List<Route> routes, java.util.List<ExecutionTrace> traces) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Delivery Planner Visualization");
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            GridRenderer panel = new GridRenderer(grid, routes, traces);
            JPanel main = new JPanel(new BorderLayout());
            main.add(new JScrollPane(panel), BorderLayout.CENTER);
            main.add(panel.createControlPanel(), BorderLayout.SOUTH);
            f.setContentPane(main);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
            panel.startAnimation(120); // default animate (parallel, 120ms)
        });
    }

    /**
     * Show with custom animation delay and sequential mode.
     */
    public static void show(Grid grid, java.util.List<Route> routes, java.util.List<ExecutionTrace> traces, int msStep, boolean sequential) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Delivery Planner Visualization");
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            GridRenderer panel = new GridRenderer(grid, routes, traces);
            JPanel main = new JPanel(new BorderLayout());
            main.add(new JScrollPane(panel), BorderLayout.CENTER);
            main.add(panel.createControlPanel(), BorderLayout.SOUTH);
            f.setContentPane(main);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
            panel.startAnimation(msStep, sequential);
        });
    }

    // Save visualization to PNG file (useful if GUI cannot be displayed)
    public static void saveImage(Grid grid, java.util.List<Route> routes, java.util.List<ExecutionTrace> traces, String filename) throws IOException {
        GridRenderer panel = new GridRenderer(grid, routes, traces);
        int w = panel.getPreferredSize().width;
        int h = panel.getPreferredSize().height;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        panel.setSize(w, h);
        panel.paint(g);
        g.dispose();
        ImageIO.write(img, "png", new File(filename));
    }
}
