package code;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GridPanel extends JPanel {

    private Grid grid;
    private State truck;

    // ARROWS FOR PATH VISUALIZATION
    private String[][] arrows;
    private List<State> truckTrail = new ArrayList<>();

    public GridPanel(Grid grid) {
        this.grid = grid;

        // initialize array of arrows
        arrows = new String[grid.rows][grid.cols];
        for (int y = 0; y < grid.rows; y++)
            for (int x = 0; x < grid.cols; x++)
                arrows[y][x] = "";
 StoresColors = new HashMap<>();
    for (State s : grid.stores) {
        StoresColors.put(s, new Color(
                (int) (Math.random() * 200 + 30),
                (int) (Math.random() * 200 + 30),
                (int) (Math.random() * 200 + 30)));
    }
        setPreferredSize(new Dimension(900, 600));
    }

    private Map<State, Color> StoresColors = new HashMap<>();
    private Color truckColor = Color.BLUE;

    // move truck to new position
    public void setTruck(State s) {
        this.truck = s;
        if (s != null) {
            // if truck is at a store, reset the trail and set color
            if (grid.stores.contains(s)) {
                truckTrail.clear();
                truckColor = StoresColors.getOrDefault(s, Color.BLUE);
            }

            // add current position to trail if not already last
            if (truckTrail.isEmpty() || !truckTrail.get(truckTrail.size() - 1).equals(s)) {
                truckTrail.add(new State(s.x, s.y));
            }
        }
        repaint();
    }

    // ---------------------------------------------------------
    // ADD ARROW TO A CELL
    // ---------------------------------------------------------
    /** add a single arrow label at a state's position (overwrites existing) */
    public void addArrow(State s, String label) {
        if (s == null)
            return;
        if (arrows == null) {
            if (grid == null)
                return;
            arrows = new String[grid.rows][grid.cols];
            for (int r = 0; r < grid.rows; r++)
                for (int c = 0; c < grid.cols; c++)
                    arrows[r][c] = "";
        }
        if (s.y >= 0 && s.y < arrows.length && s.x >= 0 && s.x < arrows[0].length) {
            arrows[s.y][s.x] = label == null ? "" : label;
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        
        Graphics2D g = (Graphics2D) g0.create();
        int w = getWidth();
        int h = getHeight();
        int padding = Math.max(16, Math.min(w, h) / 12);
        int gx = padding;
        int gy = padding;
        int gw = Math.max(100, w - padding * 2);
        int gh = Math.max(100, h - padding * 2);

        int cellW = gw / grid.cols;
        int cellH = gh / grid.rows;

        g.setColor(new Color(250, 251, 252));
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(245, 246, 248));

        // ------------- DRAW GRID -------------
        for (int r = 0; r < grid.rows; r++) {
            for (int c = 0; c < grid.cols; c++) {
                if ((r + c) % 2 == 0)
                    g.fillRect(gx + c * cellW, gy + r * cellH, cellW, cellH);
            }
        }

        g.setColor(new Color(220, 223, 227));
        for (int i = 0; i <= grid.cols; i++)
            g.drawLine(gx + i * cellW, gy, gx + i * cellW, gy + gh);
        for (int i = 0; i <= grid.rows; i++)
            g.drawLine(gx, gy + i * cellH, gx + gw, gy + i * cellH);

        // draw edge costs
        g.setFont(new Font("SansSerif", Font.PLAIN, Math.max(10, cellH / 5)));
        g.setColor(new Color(120, 130, 140));
        for (int y = 0; y < grid.rows; y++) {
            for (int x = 0; x < grid.cols; x++) {
                State p = new State(x, y);
                if (x + 1 < grid.cols) {
                    State q = new State(x + 1, y);
                    int cost = grid.getCost(p, q, "right");
                    if (cost > 0) {
                        int mx = gx + (p.x + q.x) * cellW / 2 + cellW / 2;
                        int my = gy + (p.y + q.y) * cellH / 2 + cellH / 2;
                        g.setColor(new Color(90, 100, 110));
                        g.drawString(Integer.toString(cost), mx - 6, my + 4);
                    }
                }
                if (y - 1 >= 0) {
                    State q = new State(x, y - 1);
                    int cost = grid.getCost(q, p, "down");
                    if (cost > 0) {
                        int mx = gx + (p.x + q.x) * cellW / 2 + cellW / 2;
                        int my = gy + (p.y + q.y) * cellH / 2 + cellH / 2;
                        g.setColor(new Color(90, 100, 110));
                        g.drawString(Integer.toString(cost), mx - 6, my + 4);
                    }
                }
            }

        }
        // --------------------- Blocked Roads ---------------
        int wallThickness = Math.max(6, Math.min(cellW, cellH) / 10);

        g.setColor(Color.BLACK);
        for (int y = 0; y < grid.rows; y++) {
            for (int x = 0; x < grid.cols; x++) {
                State s = new State(x, y);

                // check right edge
                if (x + 1 < grid.cols && grid.getCost(s, new State(x + 1, y), "right") == 0) {
                    int rx = gx + (x + 1) * cellW - wallThickness / 2;
                    int ry = gy + y * cellH;
                    g.fillRect(rx, ry, wallThickness, cellH);
                }

                // check down edge
                if (y + 1 < grid.rows && grid.getCost(s, new State(x, y + 1), "down") == 0) {
                    int rx = gx + x * cellW; // horizontally centered
                    int ry = gy + (y + 1) * cellH - wallThickness / 2; // bottom edge
                    g.fillRect(rx, ry, cellW, wallThickness);
                }

                // check left edge (optional if traffic table has left)
                if (x - 1 >= 0 && grid.getCost(new State(x - 1, y), s, "right") == 0) {
                    int rx = gx + x * cellW - wallThickness / 2; // left edge
                    int ry = gy + y * cellH;
                    g.fillRect(rx, ry, wallThickness, cellH);
                }

                // check up edge (optional if traffic table has up)
                if (y - 1 >= 0 && grid.getCost(new State(x, y - 1), s, "down") == 0) {
                    int rx = gx + x * cellW;
                    int ry = gy + y * cellH - wallThickness / 2; // top edge
                    g.fillRect(rx, ry, cellW, wallThickness);
                }
            }
        }

        // --------------------Tunnels------------------------
        // tunnels: dashed connecting line and small green circles at each entrance
        for (Tunnel t : grid.tunnels) {
            int cxA = gx + t.A.x * cellW + cellW / 2;
            int cyA = gy + t.A.y * cellH + cellH / 2;
            int cxB = gx + t.B.x * cellW + cellW / 2;
            int cyB = gy + t.B.y * cellH + cellH / 2;

            // dashed line (sea green)
            Stroke old = g.getStroke();
            g.setColor(new Color(46, 139, 87));
            g.setStroke(new BasicStroke(Math.max(2f, Math.min(cellW, cellH) / 12f), BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 10f, new float[] { 8f, 8f }, 0f));
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

        // ------------------------Stores ---------------
        g.setFont(new Font("SansSerif", Font.BOLD, Math.max(10, Math.min(cellW, cellH) / 4)));
        for (State s : grid.stores) {
            int bx = gx + s.x * cellW;
            int by = gy + s.y * cellH;

            int sizeW = Math.max(10, Math.min(cellW, cellH) / 2);
            int sizeH = Math.max(10, Math.min(cellW, cellH) / 2);
            int sx = bx + (cellW - sizeW) / 2;
            int sy = by + (cellH - sizeH) / 2;

            // use unique color for each store
            Color storeColor = StoresColors.getOrDefault(s, new Color(28, 115, 185));
            g.setColor(storeColor);
            g.fillRect(sx, sy, sizeW, sizeH);

            // rectangle outline (darker version of the store color)
            g.setColor(storeColor.darker());
            g.setStroke(new BasicStroke(2f));
            g.drawRect(sx, sy, sizeW, sizeH);

            // store label
            g.setColor(Color.WHITE);
            FontMetrics fm = g.getFontMetrics();
            String sLabel = "S";
            int tx = sx + (sizeW - fm.stringWidth(sLabel)) / 2;
            int ty = sy + (sizeH + fm.getAscent()) / 2 - 2;
            g.drawString(sLabel, tx, ty);
        }

        // ---------------------------------Destinations ----------------------
        g.setFont(new Font("SansSerif", Font.BOLD, Math.max(10, Math.min(cellW, cellH) / 4)));
        for (State d : grid.destinations) {
            int bx = gx + d.x * cellW;
            int by = gy + d.y * cellH;
            int sizeW = Math.max(10, Math.min(cellW, cellH) / 2);
            int sizeH = Math.max(10, Math.min(cellW, cellH) / 2);
            int rx = bx + (cellW - sizeW) / 2;
            int ry = by + (cellH - sizeH) / 2;

            // filled red rectangle
            g.setColor(new Color(200, 50, 50));
            g.fillRect(rx, ry, sizeW, sizeH);

            // rectangle outline
            g.setColor(new Color(120, 20, 20));
            g.setStroke(new BasicStroke(2f));
            g.drawRect(rx, ry, sizeW, sizeH);

            g.setColor(Color.WHITE);
            String cLabel = "D";
            FontMetrics fm = g.getFontMetrics();
            int tx = rx + (sizeW - fm.stringWidth(cLabel)) / 2;
            int ty = ry + (sizeH + fm.getAscent()) / 2 - 2;
            g.drawString(cLabel, tx, ty);
        }

        // draw truck trail
        if (truckTrail.size() > 1) {
            g.setColor(new Color(truckColor.getRed(), truckColor.getGreen(), truckColor.getBlue(), 200));
            g.setStroke(new BasicStroke(Math.max(12f, Math.min(cellW, cellH) / 3f),
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < truckTrail.size() - 1; i++) {
                State a = truckTrail.get(i);
                State b = truckTrail.get(i + 1);
                int x1 = gx + a.x * cellW + cellW / 2;
                int y1 = gy + a.y * cellH + cellH / 2;
                int x2 = gx + b.x * cellW + cellW / 2;
                int y2 = gy + b.y * cellH + cellH / 2;
                g.drawLine(x1, y1, x2, y2);
            }
        }

        // draw truck
        if (truck != null) {
            int radius = Math.max(12, Math.min(cellW, cellH) / 4);
            g.setColor(truckColor);
            g.fillOval(gx + truck.x * cellW + cellW / 2 - radius / 2,
                    gy + truck.y * cellH + cellH / 2 - radius / 2,
                    radius, radius);
            g.setColor(truckColor.darker());
            g.setStroke(new BasicStroke(2f));
            g.drawOval(gx + truck.x * cellW + cellW / 2 - radius / 2,
                    gy + truck.y * cellH + cellH / 2 - radius / 2,
                    radius, radius);
        }

    }
}