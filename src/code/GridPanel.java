package code;

import javax.swing.*;
import java.awt.*;

public class GridPanel extends JPanel {

    private Grid grid;
    private State truck;

    public GridPanel(Grid grid) {
        this.grid = grid;
        setPreferredSize(new Dimension(900, 600));
    }

    public void setTruck(State s) {
        this.truck = s;
        repaint();
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

                // cell border
                g.setColor(Color.black);
                g.drawRect(x * cellW, y * cellH, cellW, cellH);

                // traffic numbers
                g.setColor(Color.black);
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

        //------------- BLOCKED ROADS -------------
        g.setColor(Color.RED);
        for (RoadBlock r : grid.blockedRoads) {
            int x = r.s.x;
            int y = r.s.y;

            if (r.action.equals("up"))
                g.drawLine(x*cellW, y*cellH, x*cellW + cellW, y*cellH);
            if (r.action.equals("down"))
                g.drawLine(x*cellW, (y+1)*cellH, x*cellW + cellW, (y+1)*cellH);
            if (r.action.equals("left"))
                g.drawLine(x*cellW, y*cellH, x*cellW, y*cellH + cellH);
            if (r.action.equals("right"))
                g.drawLine((x+1)*cellW, y*cellH, (x+1)*cellW, y*cellH + cellH);
        }

        //------------- TRUCK -------------
        if (truck != null) {
            g.setColor(Color.GREEN);
            g.fillOval(truck.x * cellW + 8, truck.y * cellH + 8, cellW - 16, cellH - 16);
        }
        // ----- DESSINER LES ROADBLOCKS (TRIANGLES ROUGES) -----
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
            xs[0] = cx + cellW / 2;
            ys[0] = cy + 2;
            xs[1] = cx + cellW / 2 - 6;
            ys[1] = cy + 12;
            xs[2] = cx + cellW / 2 + 6;
            ys[2] = cy + 12;
            break;
        case "down":
            xs[0] = cx + cellW / 2;
            ys[0] = cy + cellH - 2;
            xs[1] = cx + cellW / 2 - 6;
            ys[1] = cy + cellH - 12;
            xs[2] = cx + cellW / 2 + 6;
            ys[2] = cy + cellH - 12;
            break;
        case "left":
            xs[0] = cx + 2;
            ys[0] = cy + cellH / 2;
            xs[1] = cx + 12;
            ys[1] = cy + cellH / 2 - 6;
            xs[2] = cx + 12;
            ys[2] = cy + cellH / 2 + 6;
            break;
        case "right":
            xs[0] = cx + cellW - 2;
            ys[0] = cy + cellH / 2;
            xs[1] = cx + cellW - 12;
            ys[1] = cy + cellH / 2 - 6;
            xs[2] = cx + cellW - 12;
            ys[2] = cy + cellH / 2 + 6;
            break;
    }

    g.fillPolygon(xs, ys, 3);
}

    }
}
