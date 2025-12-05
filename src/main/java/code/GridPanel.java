package code;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GridPanel handles drawing of the city grid, walls, edge costs, tunnels,
 * stores, destinations,
 * and dynamic elements such as trucks, trails, and path arrows.
 */
public class GridPanel extends JPanel {

    private final Grid grid; // Grid model
    private State truck; // Current truck position

    private final List<State> truckTrail = new ArrayList<>();
    private String[][] arrows;

    private final Map<State, Color> StoresColors = new HashMap<>();
    private Color truckColor = Color.BLUE;

    public GridPanel(Grid grid) {
        this.grid = grid;

        // Initialize arrows array
        arrows = new String[grid.rows][grid.cols];
        for (int y = 0; y < grid.rows; y++)
            for (int x = 0; x < grid.cols; x++)
                arrows[y][x] = "";

        // Assign random colors to stores
        for (State s : grid.stores) {
            StoresColors.put(s, new Color(
                    (int) (Math.random() * 200 + 30),
                    (int) (Math.random() * 200 + 30),
                    (int) (Math.random() * 200 + 30)));
        }

        setPreferredSize(new Dimension(900, 600));
    }

    /** Move the truck to a new state, update trail, and repaint. */
    public void setTruck(State s) {
        this.truck = s;
        if (s != null) {
            // If truck is at a store, reset trail and update truck color
            if (grid.stores.contains(s)) {
                truckTrail.clear();
                truckColor = StoresColors.getOrDefault(s, Color.BLUE);
            }
            // Add current position to trail if not last
            if (truckTrail.isEmpty() || !truckTrail.get(truckTrail.size() - 1).equals(s)) {
                truckTrail.add(new State(s.x, s.y));
            }
        }
        repaint();
    }

    /**
     * Add an arrow label to a cell for path visualization.
     */
    public void addArrow(State s, String label) {
        if (s == null)
            return;

        if (arrows == null) {
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

        // Draw background grid
        g.setColor(new Color(250, 251, 252));
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(245, 246, 248));

        for (int r = 0; r < grid.rows; r++)
            for (int c = 0; c < grid.cols; c++)
                if ((r + c) % 2 == 0)
                    g.fillRect(gx + c * cellW, gy + r * cellH, cellW, cellH);

        g.setColor(new Color(220, 223, 227));
        for (int i = 0; i <= grid.cols; i++)
            g.drawLine(gx + i * cellW, gy, gx + i * cellW, gy + gh);
        for (int i = 0; i <= grid.rows; i++)
            g.drawLine(gx, gy + i * cellH, gx + gw, gy + i * cellH);

        // Draw edges and walls
        g.setFont(new Font("SansSerif", Font.PLAIN, Math.max(10, cellH / 5)));
        g.setColor(new Color(120, 130, 140));

        for (int y = 0; y < grid.rows; y++) {
            for (int x = 0; x < grid.cols; x++) {
                State s = new State(x, y);

                // Edge costs (right and up)
                if (x + 1 < grid.cols) {
                    int cost = grid.getCost(s, new State(x + 1, y), "right");
                    if (cost > 0)
                        drawEdgeCost(g, s, new State(x + 1, y), cost, cellW, cellH, gx, gy);
                }
                if (y - 1 >= 0) {
                    int cost = grid.getCost(new State(x, y - 1), s, "down");
                    if (cost > 0)
                        drawEdgeCost(g, new State(x, y - 1), s, cost, cellW, cellH, gx, gy);
                }
                // Walls (all four directions)
                g.setColor(Color.BLACK);
                drawWallsForCell(g, s, cellW, cellH, gx, gy);
            }
        }

        // Draw tunnels
        for (Tunnel t : grid.tunnels)
            drawTunnel(g, t, cellW, cellH, gx, gy);
        // Draw stores and destinations
        for (State s : grid.stores)
            drawCell(g, s, cellW, cellH, gx, gy, StoresColors.getOrDefault(s, Color.BLUE),
                    StoresColors.getOrDefault(s, Color.BLUE).darker(), "S");
        for (State d : grid.destinations)
            drawCell(g, d, cellW, cellH, gx, gy, new Color(200, 50, 50),
                    new Color(120, 20, 20), "D");

        // Draw dynamic elements
        drawTruckTrail(g, cellW, cellH, gx, gy);
        drawTruck(g, cellW, cellH, gx, gy);
    }

    // -----------------------Private Helper
    // Methods----------------------------------
    /** Draw a cell */
    private void drawCell(Graphics2D g, State s, int cellW, int cellH, int gx, int gy,
            Color fillColor, Color borderColor, String label) {
        int bx = gx + s.x * cellW;
        int by = gy + s.y * cellH;
        int sizeW = Math.max(10, Math.min(cellW, cellH) / 2);
        int sizeH = Math.max(10, Math.min(cellW, cellH) / 2);
        int rx = bx + (cellW - sizeW) / 2;
        int ry = by + (cellH - sizeH) / 2;

        g.setColor(fillColor);
        g.fillRect(rx, ry, sizeW, sizeH);

        g.setColor(borderColor);
        g.setStroke(new BasicStroke(2f));
        g.drawRect(rx, ry, sizeW, sizeH);

        if (label != null) {
            g.setColor(Color.WHITE);
            FontMetrics fm = g.getFontMetrics();
            int tx = rx + (sizeW - fm.stringWidth(label)) / 2;
            int ty = ry + (sizeH + fm.getAscent()) / 2 - 2;
            g.drawString(label, tx, ty);
        }
    }

    /** Draws all walls for a given cell in four directions. */
    private void drawWallsForCell(Graphics2D g, State s, int cellW, int cellH, int gx, int gy) {
        int x = s.x, y = s.y;

        // Right
        State right = new State(x + 1, y);
        if (x + 1 < grid.cols && grid.isBlockedEdge(s, right))
            drawWall(g, s, right, cellW, cellH, gx, gy);

        // Down
        State down = new State(x, y + 1);
        if (y + 1 < grid.rows && grid.isBlockedEdge(s, down))
            drawWall(g, s, down, cellW, cellH, gx, gy);

        // Left
        State left = new State(x - 1, y);
        if (x - 1 >= 0 && grid.isBlockedEdge(left, s))
            drawWall(g, left, s, cellW, cellH, gx, gy);

        // Up
        State up = new State(x, y - 1);
        if (y - 1 >= 0 && grid.isBlockedEdge(up, s))
            drawWall(g, up, s, cellW, cellH, gx, gy);
    }

    /** Cost between two connected cells. */
    private void drawEdgeCost(Graphics2D g, State a, State b, int cost, int cellW, int cellH, int gx, int gy) {
        int mx = (gx + a.x * cellW + gx + b.x * cellW) / 2 + cellW / 2;
        int my = (gy + a.y * cellH + gy + b.y * cellH) / 2 + cellH / 2;
        g.setColor(new Color(90, 100, 110));
        g.drawString(Integer.toString(cost), mx - 6, my + 4);
    }

    /** Draws a wall (Blocked State). */
    private void drawWall(Graphics2D g, State from, State to, int cellW, int cellH, int gx, int gy) {
        int wallThickness = Math.max(6, Math.min(cellW, cellH) / 10);
        int fx = gx + from.x * cellW;
        int fy = gy + from.y * cellH;
        int tx = gx + to.x * cellW;
        int ty = gy + to.y * cellH;

        if (from.x != to.x) {
            g.fillRect(Math.min(fx, tx) + cellW - wallThickness / 2, fy, wallThickness, cellH);
        } else {
            g.fillRect(fx, Math.min(fy, ty) + cellH - wallThickness / 2, cellW, wallThickness);
        }
    }

    /** Draws the truck trail. */
    private void drawTruckTrail(Graphics2D g, int cellW, int cellH, int gx, int gy) {
        if (truckTrail.size() <= 1)
            return;
        g.setColor(new Color(truckColor.getRed(), truckColor.getGreen(), truckColor.getBlue(), 200));
        g.setStroke(new BasicStroke(Math.max(12f, Math.min(cellW, cellH) / 3f),
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < truckTrail.size() - 1; i++) {
            State a = truckTrail.get(i);
            State b = truckTrail.get(i + 1);
            g.drawLine(cx(a, cellW, gx), cy(a, cellH, gy), cx(b, cellW, gx), cy(b, cellH, gy));
        }
    }

    /** Draws the truck */
    private void drawTruck(Graphics2D g, int cellW, int cellH, int gx, int gy) {
        if (truck == null)
            return;
        int radius = Math.max(12, Math.min(cellW, cellH) / 4);
        int cx = cx(truck, cellW, gx) - radius / 2;
        int cy = cy(truck, cellH, gy) - radius / 2;
        g.setColor(truckColor);
        g.fillOval(cx, cy, radius, radius);
        g.setColor(truckColor.darker());
        g.setStroke(new BasicStroke(2f));
        g.drawOval(cx, cy, radius, radius);
    }

    /** Draws tunnels */
    private void drawTunnel(Graphics2D g, Tunnel t, int cellW, int cellH, int gx, int gy) {
        int cxA = cx(t.A, cellW, gx);
        int cyA = cy(t.A, cellH, gy);
        int cxB = cx(t.B, cellW, gx);
        int cyB = cy(t.B, cellH, gy);

        Stroke old = g.getStroke();
        g.setColor(new Color(46, 139, 87));
        g.setStroke(new BasicStroke(Math.max(2f, Math.min(cellW, cellH) / 12f),
                BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] { 8f, 8f }, 0f));
        g.drawLine(cxA, cyA, cxB, cyB);
        g.setStroke(old);

        int r = Math.max(6, Math.min(cellW, cellH) / 6);
        Color fill = new Color(80, 160, 90), outline = new Color(45, 110, 55);

        g.setColor(fill);
        g.fillOval(cxA - r, cyA - r, r * 2, r * 2);
        g.setColor(outline);
        g.drawOval(cxA - r, cyA - r, r * 2, r * 2);

        g.setColor(fill);
        g.fillOval(cxB - r, cyB - r, r * 2, r * 2);
        g.setColor(outline);
        g.drawOval(cxB - r, cyB - r, r * 2, r * 2);
    }

    // Utility
    private int cx(State s, int cellW, int gx) {
        return gx + s.x * cellW + cellW / 2;
    }

    private int cy(State s, int cellH, int gy) {
        return gy + s.y * cellH + cellH / 2;
    }
}