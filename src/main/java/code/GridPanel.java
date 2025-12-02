package code;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * GridPanel: draws the grid, stores, destinations, tunnels, roadblocks,
 * and supports multiple algorithm overlays (expansions + final paths)
 * with per-algorithm colors and a simple expansion animation API.
 */
public class GridPanel extends JPanel {

    private Grid grid;
    private State truck;
    private String[][] arrows;

    // overlay data structures
    private final Map<String, java.util.List<State>> algoPaths = new HashMap<>();
    private final Map<String, java.util.List<State>> algoExpansions = new HashMap<>();
    private final Map<String, Color> algoColors = new HashMap<>();
    private final Map<String, Integer> expansionProgress = new HashMap<>();
    private final Set<String> visiblePaths = new HashSet<>();

    // timer used for animations (shared)
    private final javax.swing.Timer animationTimer;

    public GridPanel() {
        this.setBackground(new Color(245, 247, 250));
    this.animationTimer = new javax.swing.Timer(80, null);
    this.animationTimer.setRepeats(true);
    }

    public void setGrid(Grid g) {
        this.grid = g;
        if (g != null) {
            this.arrows = new String[g.rows][g.cols];
            for (int r = 0; r < g.rows; r++) for (int c = 0; c < g.cols; c++) arrows[r][c] = "";
        }
        repaint();
    }

    public void setTruck(State t) {
        this.truck = t;
        repaint();
    }

    public void setArrows(String[][] arr) {
        this.arrows = arr;
        repaint();
    }

    /** add a single arrow label at a state's position (overwrites existing) */
    public void addArrow(State s, String label) {
        if (s == null) return;
        if (arrows == null) {
            if (grid == null) return;
            arrows = new String[grid.rows][grid.cols];
            for (int r = 0; r < grid.rows; r++) for (int c = 0; c < grid.cols; c++) arrows[r][c] = "";
        }
        if (s.y >= 0 && s.y < arrows.length && s.x >= 0 && s.x < arrows[0].length) {
            arrows[s.y][s.x] = label == null ? "" : label;
            repaint();
        }
    }

    // --- overlay API ---
    public void setAlgoColor(String algo, Color c) { algoColors.put(algo, c); }

    public void setAlgoPath(String algo, java.util.List<State> path) {
        if (path == null) algoPaths.remove(algo); else algoPaths.put(algo, new ArrayList<>(path));
        repaint();
    }

    public void setAlgoExpansions(String algo, java.util.List<State> exps) {
        if (exps == null) algoExpansions.remove(algo); else algoExpansions.put(algo, new ArrayList<>(exps));
        expansionProgress.put(algo, 0);
        repaint();
    }

    public void setPathVisible(String algo, boolean visible) {
        if (visible) visiblePaths.add(algo); else visiblePaths.remove(algo);
        repaint();
    }

    /**
     * Animate expansions for a given algorithm. This will incrementally reveal
     * expansion cells until the algorithm's expansion list is exhausted.
     */
    public void animateExpansions(final String algo) {
        java.util.List<State> ex = algoExpansions.get(algo);
        if (ex == null || ex.isEmpty()) return;

        // reset progress for this algo
        expansionProgress.put(algo, 0);

        // stop any running animation and remove listeners
        stopAnimation();

        // add a lambda listener that increments the current algo step
        animationTimer.addActionListener(e -> {
            int cur = expansionProgress.getOrDefault(algo, 0);
            cur++;
            expansionProgress.put(algo, cur);
            repaint();
            if (cur >= ex.size()) {
                stopAnimation();
            }
        });

        animationTimer.start();
    }

    /**
     * Stop the shared animation timer and clear its listeners.
     */
    public void stopAnimation() {
        if (animationTimer.isRunning()) animationTimer.stop();
        for (ActionListener al : animationTimer.getActionListeners()) {
            animationTimer.removeActionListener(al);
        }
    }

    /**
     * Change the animation delay (ms) used by the shared timer.
     */
    public void setAnimationDelay(int ms) {
        if (ms < 10) ms = 10;
        animationTimer.setDelay(ms);
    }

    /**
     * Step the expansion progress for an algorithm by delta (can be negative).
     */
    public void stepExpansion(String algo, int delta) {
        java.util.List<State> ex = algoExpansions.get(algo);
        if (ex == null) return;
        int cur = expansionProgress.getOrDefault(algo, 0) + delta;
        cur = Math.max(0, Math.min(cur, ex.size()));
        expansionProgress.put(algo, cur);
        repaint();
    }

    public int getExpansionProgress(String algo) {
        return expansionProgress.getOrDefault(algo, 0);
    }

    public int getExpansionSize(String algo) {
        java.util.List<State> ex = algoExpansions.get(algo);
        return ex == null ? 0 : ex.size();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        if (grid == null) return;

        Graphics2D g = (Graphics2D) g0.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            int cols = Math.max(1, grid.cols);
            int rows = Math.max(1, grid.rows);
            // leave padding around the grid so it doesn't fill the whole window
            int padding = Math.max(16, Math.min(w, h) / 12);
            int gx = padding;
            int gy = padding;
            int gw = Math.max(100, w - padding * 2);
            int gh = Math.max(100, h - padding * 2);
            int cellW = gw / cols;
            int cellH = gh / rows;

            // subtle checkered background
            g.setColor(new Color(250, 251, 252));
            g.fillRect(0, 0, w, h);
            g.setColor(new Color(245, 246, 248));
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if ((r + c) % 2 == 0) g.fillRect(gx + c * cellW, gy + r * cellH, cellW, cellH);
                }
            }

            // thin grid lines
            g.setColor(new Color(220, 223, 227));
            for (int i = 0; i <= cols; i++) g.drawLine(gx + i * cellW, gy, gx + i * cellW, gy + gh);
            for (int i = 0; i <= rows; i++) g.drawLine(gx, gy + i * cellH, gx + gw, gy + i * cellH);

            // draw edge costs for right and up edges (centered between cells)
            if (grid.traffic != null) {
                g.setFont(new Font("SansSerif", Font.PLAIN, Math.max(10, cellH/5)));
                g.setColor(new Color(120, 130, 140));
                for (int y = 0; y < rows; y++) {
                    for (int x = 0; x < cols; x++) {
                        State p = new State(x, y);
                        // right edge
                        if (x + 1 < cols) {
                            State q = new State(x + 1, y);
                            int cost = grid.getCost(p, q, "right");
                            if (cost > 0) {
                                int ax = gx + x * cellW + cellW / 2;
                                int ay = gy + y * cellH + cellH / 2;
                                int bx = gx + q.x * cellW + cellW / 2;
                                int by = gy + q.y * cellH + cellH / 2;
                                int mx = (ax + bx) / 2;
                                int my = (ay + by) / 2;
                                g.setColor(new Color(90, 100, 110));
                                g.drawString(Integer.toString(cost), mx - 6, my + 4);
                            }
                        }
                        // up edge (from p to y-1 / display as upward connection)
                        if (y - 1 >= 0) {
                            State q = new State(x, y - 1);
                            // moving from q to p is a "down" action in Grid's cost model
                            int cost = grid.getCost(q, p, "down");
                            if (cost > 0) {
                                int ax = gx + x * cellW + cellW / 2;
                                int ay = gy + y * cellH + cellH / 2;
                                int bx = gx + q.x * cellW + cellW / 2;
                                int by = gy + q.y * cellH + cellH / 2;
                                int mx = (ax + bx) / 2;
                                int my = (ay + by) / 2;
                                g.setColor(new Color(90, 100, 110));
                                g.drawString(Integer.toString(cost), mx - 6, my + 4);
                            }
                        }
                    }
                }
            }

            // tunnels: dashed connecting line and small green circles at each entrance
            for (Tunnel t : grid.tunnels) {
                int cxA = gx + t.A.x * cellW + cellW/2;
                int cyA = gy + t.A.y * cellH + cellH/2;
                int cxB = gx + t.B.x * cellW + cellW/2;
                int cyB = gy + t.B.y * cellH + cellH/2;

                // dashed line (sea green)
                Stroke old = g.getStroke();
                g.setColor(new Color(46, 139, 87));
                g.setStroke(new BasicStroke(Math.max(2f, Math.min(cellW, cellH) / 12f), BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER, 10f, new float[]{8f, 8f}, 0f));
                g.drawLine(cxA, cyA, cxB, cyB);
                g.setStroke(old);

                // small filled green circles at entrances
                int r = Math.max(6, Math.min(cellW, cellH) / 6);
                Color fill = new Color(80, 160, 90);
                Color outline = new Color(45, 110, 55);

                g.setColor(fill);
                g.fillOval(cxA - r, cyA - r, r * 2, r * 2);
                g.setColor(outline);
                g.setStroke(new BasicStroke(2f));
                g.drawOval(cxA - r, cyA - r, r * 2, r * 2);

                g.setColor(fill);
                g.fillOval(cxB - r, cyB - r, r * 2, r * 2);
                g.setColor(outline);
                g.drawOval(cxB - r, cyB - r, r * 2, r * 2);
            }

            // blocked roads
            for (RoadBlock rb : grid.blockedRoads) {
                State s = rb.s; int x = s.x; int y = s.y; int cx = gx + x * cellW; int cy = gy + y * cellH;
                int[] xs = new int[3], ys = new int[3];
                switch (rb.action) {
                    case "up": xs[0]=cx+cellW/2; ys[0]=cy+4; xs[1]=cx+cellW/2-8; ys[1]=cy+18; xs[2]=cx+cellW/2+8; ys[2]=cy+18; break;
                    case "down": xs[0]=cx+cellW/2; ys[0]=cy+cellH-4; xs[1]=cx+cellW/2-8; ys[1]=cy+cellH-18; xs[2]=cx+cellW/2+8; ys[2]=cy+cellH-18; break;
                    case "left": xs[0]=cx+4; ys[0]=cy+cellH/2; xs[1]=cx+18; ys[1]=cy+cellH/2-8; xs[2]=cx+18; ys[2]=cy+cellH/2+8; break;
                    default: xs[0]=cx+cellW-4; ys[0]=cy+cellH/2; xs[1]=cx+cellW-18; ys[1]=cy+cellH/2-8; xs[2]=cx+cellW-18; ys[2]=cy+cellH/2+8; break;
                }
                // shadow
                g.setColor(new Color(160, 40, 40, 90));
                int[] xsS = new int[]{xs[0]+2, xs[1]+2, xs[2]+2};
                int[] ysS = new int[]{ys[0]+2, ys[1]+2, ys[2]+2};
                g.fillPolygon(xsS, ysS, 3);
                g.setColor(new Color(200, 50, 50));
                g.fillPolygon(xs, ys, 3);
            }

            // stores
            // stores as small blue square with 'S'
            g.setFont(new Font("SansSerif", Font.BOLD, Math.max(10, Math.min(cellW, cellH)/4)));
            for (State s : grid.stores) {
                int bx = gx + s.x * cellW; int by = gy + s.y * cellH;
                int size = Math.max(8, Math.min(cellW, cellH) / 3);
                int sx = bx + (cellW - size) / 2;
                int sy = by + (cellH - size) / 2;
                g.setColor(new Color(28, 115, 185));
                g.fillRect(sx, sy, size, size);
                g.setColor(new Color(12, 60, 100)); g.setStroke(new BasicStroke(2f)); g.drawRect(sx, sy, size, size);
                g.setColor(Color.WHITE);
                FontMetrics fm = g.getFontMetrics(); String sLabel = "S";
                int tx = sx + (size - fm.stringWidth(sLabel)) / 2;
                int ty = sy + (size + fm.getAscent()) / 2 - fm.getDescent()/2;
                g.drawString(sLabel, tx, ty);
            }

            // destinations / customers as red triangle with 'C'
            g.setFont(new Font("SansSerif", Font.BOLD, Math.max(10, Math.min(cellW, cellH)/4)));
            for (State d : grid.destinations) {
                int bx = gx + d.x * cellW; int by = gy + d.y * cellH;
                int size = Math.max(10, Math.min(cellW, cellH) / 3);
                int cx = bx + cellW/2; int cy = by + cellH/2;
                int[] xs = new int[]{cx, cx - size/2, cx + size/2};
                int[] ys = new int[]{cy - size/2, cy + size/2, cy + size/2};
                // shadow
                g.setColor(new Color(160, 40, 40, 90));
                int[] xsS = new int[]{xs[0]+2, xs[1]+2, xs[2]+2};
                int[] ysS = new int[]{ys[0]+2, ys[1]+2, ys[2]+2};
                g.fillPolygon(xsS, ysS, 3);
                g.setColor(new Color(200, 50, 50));
                g.fillPolygon(xs, ys, 3);
                // label 'C' near triangle
                g.setColor(Color.WHITE);
                String cLabel = "C";
                FontMetrics fm = g.getFontMetrics();
                g.drawString(cLabel, cx - fm.stringWidth(cLabel)/2, cy + fm.getAscent()/2 - 2);
            }

            // algorithm expansions (semi-transparent overlays)
            for (String algo : algoExpansions.keySet()) {
                java.util.List<State> exList = algoExpansions.get(algo);
                if (exList == null) continue;
                Color c = algoColors.getOrDefault(algo, Color.MAGENTA);
                Color semi = new Color(c.getRed(), c.getGreen(), c.getBlue(), 80);
                g.setColor(semi);
                int show = expansionProgress.getOrDefault(algo, 0);
                show = Math.max(0, Math.min(show, exList.size()));
                for (int i = 0; i < show; i++) {
                    State s = exList.get(i);
                    g.fillRoundRect(gx + s.x * cellW + 2, gy + s.y * cellH + 2, cellW - 4, cellH - 4, 6, 6);
                }
            }

            // algorithm paths (colored lines / dots)
            for (String algo : algoPaths.keySet()) {
                if (!visiblePaths.contains(algo)) continue;
                java.util.List<State> path = algoPaths.get(algo);
                if (path == null || path.isEmpty()) continue;
                Color c = algoColors.getOrDefault(algo, Color.MAGENTA);
                g.setColor(c); g.setStroke(new BasicStroke(Math.max(2f, Math.min(cellW, cellH)/8f)));
                for (int i = 0; i < path.size(); i++) {
                    State s = path.get(i);
                    int cx = gx + s.x * cellW + cellW/2; int cy = gy + s.y * cellH + cellH/2;
                    int dot = Math.max(4, Math.min(cellW, cellH)/8);
                    g.fillOval(cx - dot, cy - dot, dot*2, dot*2);
                    if (i + 1 < path.size()) {
                        State s2 = path.get(i+1);
                        int cx2 = gx + s2.x * cellW + cellW/2; int cy2 = gy + s2.y * cellH + cellH/2;
                        g.drawLine(cx, cy, cx2, cy2);
                    }
                }
            }

            // (movement action text suppressed) do not draw per-step action strings

            // truck
            // truck: smaller moving circle
            if (truck != null) {
                int cx = gx + truck.x * cellW + cellW/2; int cy = gy + truck.y * cellH + cellH/2;
                int r = Math.max(6, Math.min(cellW, cellH) / 5);
                g.setColor(new Color(15, 130, 55)); g.fillOval(cx - r, cy - r, r*2, r*2);
                g.setColor(new Color(10, 80, 35)); g.setStroke(new BasicStroke(2f)); g.drawOval(cx - r, cy - r, r*2, r*2);
            }

        } finally {
            g.dispose();
        }
    }
}
