package code ;
import javax.swing.*;
import java.awt.*;

public class GridPanel extends JPanel {

    private Grid grid;
    private State truck;

    // ARROWS FOR PATH VISUALIZATION
    private String[][] arrows;

    public GridPanel(Grid grid) {
        this.grid = grid;

        // initialize array of arrows
        arrows = new String[grid.rows][grid.cols];
        for (int y = 0; y < grid.rows; y++)
            for (int x = 0; x < grid.cols; x++)
                arrows[y][x] = "";

        setPreferredSize(new Dimension(900, 600));
    }

    public void setTruck(State s) {
        this.truck = s;
        repaint();
    }

    // ---------------------------------------------------------
    // ADD ARROW TO A CELL
    // ---------------------------------------------------------
    public void addArrow(State s, String action) {
        switch (action) {
            case "up": arrows[s.y][s.x] = "↑"; break;
            case "down": arrows[s.y][s.x] = "↓"; break;
            case "left": arrows[s.y][s.x] = "<-"; break;
            case "right": arrows[s.y][s.x] = "→"; break;
            case "tunnel": arrows[s.y][s.x] = "⟿"; break;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int w = getWidth();
        int h = getHeight();

        int cellW = w / grid.cols;
        int cellH = h / grid.rows;

        //------------- DRAW GRID -------------
        for (int y = 0; y < grid.rows; y++) {
            for (int x = 0; x < grid.cols; x++) {

                g.setColor(Color.black);
                g.drawRect(x * cellW, y * cellH, cellW, cellH);

                int up    = grid.traffic[y][x][0];
                int down  = grid.traffic[y][x][1];
                int left  = grid.traffic[y][x][2];
                int right = grid.traffic[y][x][3];

                g.drawString("" + up,    x * cellW + cellW/2 - 2, y * cellH + 12);
                g.drawString("" + left,  x * cellW + 2,           y * cellH + cellH/2);
                g.drawString("" + right, x * cellW + cellW - 10,  y * cellH + cellH/2);
                g.drawString("" + down,  x * cellW + cellW/2 - 2, y * cellH + cellH - 4);
            }
        }

        //------------- STORES -------------
        g.setColor(Color.BLACK);
        for (State s : grid.stores) {
            g.fillRect(s.x * cellW + 3, s.y * cellH + 3, cellW - 6, cellH - 6);
        }

        //------------- DESTINATIONS -------------
        g.setColor(Color.BLUE);
        int dIndex = 1;
        for (State d : grid.destinations) {
            g.drawString("D" + dIndex, d.x * cellW + cellW/3, d.y * cellH + cellH/2);
            dIndex++;
        }

        //------------- TUNNELS -------------
        g.setColor(Color.BLACK);
        for (Tunnel t : grid.tunnels) {
            g.drawOval(t.A.x * cellW + 10, t.A.y * cellH + 10, cellW - 20, cellH - 20);
            g.drawOval(t.B.x * cellW + 10, t.B.y * cellH + 10, cellW - 20, cellH - 20);
        }

        //------------- BLOCKED ROADS (TRIANGLES) -------------
        g.setColor(Color.RED);
        for (RoadBlock rb : grid.blockedRoads) {
            State s = rb.s;
            int x = s.x;
            int y = s.y;

            int cx = x * cellW;
            int cy = y * cellH;

            int[] xs = new int[3];
            int[] ys = new int[3];

            switch (rb.action) {
                case "up":
                    xs[0] = cx + cellW / 2; ys[0] = cy + 2;
                    xs[1] = cx + cellW / 2 - 6; ys[1] = cy + 12;
                    xs[2] = cx + cellW / 2 + 6; ys[2] = cy + 12;
                    break;
                case "down":
                    xs[0] = cx + cellW / 2; ys[0] = cy + cellH - 2;
                    xs[1] = cx + cellW / 2 - 6; ys[1] = cy + cellH - 12;
                    xs[2] = cx + cellW / 2 + 6; ys[2] = cy + cellH - 12;
                    break;
                case "left":
                    xs[0] = cx + 2; ys[0] = cy + cellH / 2;
                    xs[1] = cx + 12; ys[1] = cy + cellH / 2 - 6;
                    xs[2] = cx + 12; ys[2] = cy + cellH / 2 + 6;
                    break;
                case "right":
                    xs[0] = cx + cellW - 2; ys[0] = cy + cellH / 2;
                    xs[1] = cx + cellW - 12; ys[1] = cy + cellH / 2 - 6;
                    xs[2] = cx + cellW - 12; ys[2] = cy + cellH / 2 + 6;
                    break;
            }

            g.fillPolygon(xs, ys, 3);
        }

        //------------- DRAW ARROWS ON GRID -------------
        g.setColor(Color.BLUE);
        g.setFont(new Font("Arial", Font.BOLD, 20));

        for (int y = 0; y < grid.rows; y++) {
            for (int x = 0; x < grid.cols; x++) {
                if (!arrows[y][x].isEmpty()) {
                    g.drawString(arrows[y][x],
                            x * cellW + cellW/2 - 6,
                            y * cellH + cellH/2 + 6
                    );
                }
            }
        }

        //------------- TRUCK -------------
        if (truck != null) {
            g.setColor(Color.GREEN);
            g.fillOval(truck.x * cellW + 8, truck.y * cellH + 8, cellW - 16, cellH - 16);
        }
    }
}
