import { Injectable } from "@angular/core";
import {
  Position,
  TunnelConfig,
  RoadBlockConfig,
  DeliveryRoute,
} from "../../../services/delivery-planner.service";

@Injectable()
export class GridRendererService {
  private ctx!: CanvasRenderingContext2D;
  private isDarkTheme = false;

  setContext(ctx: CanvasRenderingContext2D): void {
    this.ctx = ctx;
  }

  setTheme(isDarkTheme: boolean): void {
    this.isDarkTheme = isDarkTheme;
  }

  private getCssVariable(variableName: string): string {
    // Get the computed style from document body to access CSS variables
    const root = document.documentElement;
    const computedStyle = getComputedStyle(root);
    return computedStyle.getPropertyValue(variableName).trim();
  }

  drawGridLines(gridRows: number, gridCols: number, cellSize: number): void {
    this.ctx.strokeStyle = this.isDarkTheme
      ? "rgba(255, 255, 255, 0.1)"
      : "rgba(0, 0, 0, 0.1)";
    this.ctx.lineWidth = 1;

    // Vertical lines
    for (let x = 0; x <= gridCols; x++) {
      this.ctx.beginPath();
      this.ctx.moveTo(x * cellSize, 0);
      this.ctx.lineTo(x * cellSize, gridRows * cellSize);
      this.ctx.stroke();
    }

    // Horizontal lines
    for (let y = 0; y <= gridRows; y++) {
      this.ctx.beginPath();
      this.ctx.moveTo(0, y * cellSize);
      this.ctx.lineTo(gridCols * cellSize, y * cellSize);
      this.ctx.stroke();
    }
  }

  drawStores(stores: Position[], cellSize: number): void {
    for (const store of stores) {
      const centerX = store.x * cellSize + cellSize / 2;
      const centerY = store.y * cellSize + cellSize / 2;

      // Draw circle background
      this.ctx.fillStyle = this.getCssVariable("--store-color");
      this.ctx.strokeStyle = this.getCssVariable("--secondary-color");
      this.ctx.lineWidth = 3;
      this.ctx.beginPath();
      this.ctx.arc(centerX, centerY, cellSize * 0.3, 0, 2 * Math.PI);
      this.ctx.fill();
      this.ctx.stroke();

      // Draw 'S' label
      this.ctx.fillStyle = "#FFFFFF";
      this.ctx.font = `bold ${Math.max(12, cellSize * 0.4)}px Arial`;
      this.ctx.textAlign = "center";
      this.ctx.textBaseline = "middle";
      this.ctx.fillText("S", centerX, centerY);
    }
  }

  drawDestinations(destinations: Position[], cellSize: number): void {
    for (const dest of destinations) {
      const centerX = dest.x * cellSize + cellSize / 2;
      const centerY = dest.y * cellSize + cellSize / 2;

      // Draw square background
      this.ctx.fillStyle = "#ff6131ff";
      this.ctx.strokeStyle = "#e74b1bff";
      this.ctx.lineWidth = 3;
      this.ctx.beginPath();
      this.ctx.rect(
        centerX - cellSize * 0.3,
        centerY - cellSize * 0.3,
        cellSize * 0.6,
        cellSize * 0.6
      );
      this.ctx.fill();
      this.ctx.stroke();

      // Draw 'D' label
      this.ctx.fillStyle = "#FFFFFF";
      this.ctx.font = `bold ${Math.max(12, cellSize * 0.4)}px Arial`;
      this.ctx.textAlign = "center";
      this.ctx.textBaseline = "middle";
      this.ctx.fillText("D", centerX, centerY);
    }
  }

  drawTunnels(tunnels: TunnelConfig[], cellSize: number): void {
    this.ctx.strokeStyle = this.getCssVariable("--tunnel-color");
    this.ctx.lineWidth = 4;
    this.ctx.setLineDash([10, 5]);

    for (const tunnel of tunnels) {
      const startX = tunnel.start.x * cellSize + cellSize / 2;
      const startY = tunnel.start.y * cellSize + cellSize / 2;
      const endX = tunnel.end.x * cellSize + cellSize / 2;
      const endY = tunnel.end.y * cellSize + cellSize / 2;

      this.ctx.beginPath();
      this.ctx.moveTo(startX, startY);
      this.ctx.lineTo(endX, endY);
      this.ctx.stroke();

      // Draw tunnel entrance/exit markers
      this.ctx.fillStyle = this.getCssVariable("--tunnel-color");
      this.ctx.beginPath();
      this.ctx.arc(startX, startY, 8, 0, 2 * Math.PI);
      this.ctx.fill();
      this.ctx.beginPath();
      this.ctx.arc(endX, endY, 8, 0, 2 * Math.PI);
      this.ctx.fill();
    }

    this.ctx.setLineDash([]);
  }

  drawTrafficCosts(
    trafficCosts: number[][][],
    gridRows: number,
    gridCols: number,
    cellSize: number
  ): void {
    this.ctx.fillStyle = this.getCssVariable("--text-secondary");
    this.ctx.font = `${Math.max(8, cellSize * 0.12)}px Arial`;
    this.ctx.textAlign = "center";
    this.ctx.textBaseline = "middle";

    for (let y = 0; y < gridRows; y++) {
      for (let x = 0; x < gridCols; x++) {
        const cellCosts = trafficCosts[y][x];

        // Draw right cost (cost to move right from this cell to x+1,y)
        if (x < gridCols - 1) {
          const cost = cellCosts[3]; // right cost
          if (cost > 0) {
            // Only display non-zero costs (0 means blocked)
            const centerX = x * cellSize + cellSize;
            const centerY = y * cellSize + cellSize / 2;
            this.ctx.fillText(cost.toString(), centerX, centerY);
          }
        }

        // Draw down cost (cost to move down from this cell to x,y+1)
        if (y < gridRows - 1) {
          const cost = cellCosts[1]; // down cost
          if (cost > 0) {
            // Only display non-zero costs (0 means blocked)
            const centerX = x * cellSize + cellSize / 2;
            const centerY = y * cellSize + cellSize;
            this.ctx.fillText(cost.toString(), centerX, centerY);
          }
        }
      }
    }
  }

  drawRoadblocks(
    roadblocks: RoadBlockConfig[],
    gridRows: number,
    gridCols: number,
    cellSize: number,
    trafficCosts: number[][][]
  ): void {
    this.ctx.fillStyle = this.getCssVariable("--roadblock-color");
    this.ctx.strokeStyle = this.getCssVariable("--roadblock-color");
    this.ctx.lineWidth = Math.max(4, cellSize / 8); // Wall thickness

    for (const rb of roadblocks) {
      // Boundary checks to prevent drawing outside grid
      if (
        (rb.direction === "right" && rb.from.x + 1 >= gridCols) ||
        (rb.direction === "down" && rb.from.y + 1 >= gridRows) ||
        (rb.direction === "left" && rb.from.x - 1 < 0) ||
        (rb.direction === "up" && rb.from.y - 1 < 0)
      ) {
        continue;
      }

      const dirIndex = ["up", "down", "left", "right"].indexOf(rb.direction);
      if (
        dirIndex < 0 ||
        !trafficCosts[rb.from.y] ||
        !trafficCosts[rb.from.y][rb.from.x]
      ) {
        continue;
      }

      // Draw wall for every roadblock in the array, regardless of cost value

      let fromX = rb.from.x * cellSize;
      let fromY = rb.from.y * cellSize;

      // Draw wall between cells
      this.ctx.beginPath();
      if (rb.direction === "right" || rb.direction === "left") {
        // Vertical wall
        const wallX = rb.direction === "right" ? fromX + cellSize : fromX;
        this.ctx.fillRect(
          wallX - this.ctx.lineWidth / 2,
          fromY,
          this.ctx.lineWidth,
          cellSize
        );
      } else {
        // Horizontal wall
        const wallY = rb.direction === "down" ? fromY + cellSize : fromY;
        this.ctx.fillRect(
          fromX,
          wallY - this.ctx.lineWidth / 2,
          cellSize,
          this.ctx.lineWidth
        );
      }
    }
  }

  drawTunnelStartIndicator(
    tunnelStart: Position | null,
    cellSize: number
  ): void {
    if (!tunnelStart) return;

    const centerX = tunnelStart.x * cellSize + cellSize / 2;
    const centerY = tunnelStart.y * cellSize + cellSize / 2;

    this.ctx.strokeStyle = this.getCssVariable("--tunnel-color");
    this.ctx.lineWidth = 3;
    this.ctx.setLineDash([5, 5]);
    this.ctx.beginPath();
    this.ctx.arc(centerX, centerY, cellSize * 0.4, 0, 2 * Math.PI);
    this.ctx.stroke();
    this.ctx.setLineDash([]);
  }

  drawRoutes(
    routes: DeliveryRoute[],
    isAnimating: boolean,
    currentAnimatingRoute: number,
    currentAnimatingStep: number,
    cellSize: number
  ): void {
    // Only draw routes during animation - show only the currently animating route up to current step
    if (!isAnimating) {
      return;
    }

    // Only draw the currently animating route
    const routeIndex = currentAnimatingRoute;
    if (routeIndex < 0 || routeIndex >= routes.length) {
      return;
    }

    const route = routes[routeIndex];
    if (!route.path || route.path.length === 0) return;

    // Draw the path up to the current animating step (inclusive)
    const stepsToDraw = Math.min(currentAnimatingStep + 1, route.path.length);
    if (stepsToDraw < 2) return; // Need at least 2 points to draw a line

    // Highlight the currently animating route with gold color
    this.ctx.strokeStyle = this.getCssVariable("--path-color");
    this.ctx.lineWidth = 6; // Thicker line for active route
    this.ctx.shadowColor = this.getCssVariable("--path-color");
    this.ctx.shadowBlur = 10;
    this.ctx.lineCap = "round";
    this.ctx.lineJoin = "round";

    this.ctx.beginPath();
    const firstPoint = route.path[0];
    this.ctx.moveTo(
      firstPoint.x * cellSize + cellSize / 2,
      firstPoint.y * cellSize + cellSize / 2
    );

    for (let j = 1; j < stepsToDraw; j++) {
      const point = route.path[j];
      this.ctx.lineTo(
        point.x * cellSize + cellSize / 2,
        point.y * cellSize + cellSize / 2
      );
    }

    this.ctx.stroke();

    // Draw arrow at the current position (end of drawn path)
    if (stepsToDraw > 1) {
      const lastDrawnPoint = route.path[stepsToDraw - 1];
      const secondLastDrawnPoint = route.path[stepsToDraw - 2];
      this.drawArrow(
        secondLastDrawnPoint,
        lastDrawnPoint,
        this.getCssVariable("--path-color"),
        cellSize
      );
    }

    // Reset shadow
    this.ctx.shadowBlur = 0;
  }

  drawTruck(truckPosition: Position | null, cellSize: number): void {
    if (!truckPosition) return;

    const centerX = truckPosition.x * cellSize + cellSize / 2;
    const centerY = truckPosition.y * cellSize + cellSize / 2;

    // Draw truck as emoji
    this.ctx.font = `${Math.max(20, cellSize * 0.6)}px Arial`;
    this.ctx.textAlign = "center";
    this.ctx.textBaseline = "middle";
    this.ctx.fillText("🚚", centerX, centerY);
  }

  private drawArrow(
    from: Position,
    to: Position,
    color: string,
    cellSize: number
  ): void {
    const fromX = from.x * cellSize + cellSize / 2;
    const fromY = from.y * cellSize + cellSize / 2;
    const toX = to.x * cellSize + cellSize / 2;
    const toY = to.y * cellSize + cellSize / 2;

    const angle = Math.atan2(toY - fromY, toX - fromX);
    const arrowLength = 15;
    const arrowAngle = Math.PI / 6;

    this.ctx.fillStyle = color;
    this.ctx.beginPath();
    this.ctx.moveTo(toX, toY);
    this.ctx.lineTo(
      toX - arrowLength * Math.cos(angle - arrowAngle),
      toY - arrowLength * Math.sin(angle - arrowAngle)
    );
    this.ctx.lineTo(
      toX - arrowLength * Math.cos(angle + arrowAngle),
      toY - arrowLength * Math.sin(angle + arrowAngle)
    );
    this.ctx.closePath();
    this.ctx.fill();
  }
}
