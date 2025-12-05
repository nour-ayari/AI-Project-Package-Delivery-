import {
  Component,
  ElementRef,
  OnInit,
  ViewChild,
  AfterViewInit,
  HostListener,
  OnDestroy,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { HttpClientModule } from "@angular/common/http";
import {
  DeliveryPlannerService,
  GridConfig,
  Position,
  TunnelConfig,
  RoadBlockConfig,
  DeliveryRoute,
} from "../../services/delivery-planner.service";
import {
  animate,
  keyframes,
  state,
  style,
  transition,
  trigger,
} from "@angular/animations";

interface GridCell {
  x: number;
  y: number;
  type:
    | "empty"
    | "store"
    | "destination"
    | "tunnel-start"
    | "tunnel-end"
    | "path";
  trafficCosts: number[]; // [up, down, left, right]
}

@Component({
  selector: "app-delivery-planner",
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: "./delivery-planner.component.html",
  styleUrl: "./delivery-planner.component.css",
  providers: [DeliveryPlannerService],
  animations: [
    trigger("fadeAnimation", [
      transition(":enter", [
        style({ opacity: 0 }),
        animate("300ms ease-in", style({ opacity: 1 })),
      ]),
      transition(":leave", [
        style({ opacity: 1 }),
        animate("300ms ease-out", style({ opacity: 0 })),
      ]),
    ]),
    trigger("slideAnimation", [
      transition(":enter", [
        style({ transform: "translateY(20px)", opacity: 0 }),
        animate(
          "300ms ease-out",
          style({ transform: "translateY(0)", opacity: 1 })
        ),
      ]),
      transition(":leave", [
        style({ transform: "translateY(0)", opacity: 1 }),
        animate(
          "300ms ease-in",
          style({ transform: "translateY(20px)", opacity: 0 })
        ),
      ]),
    ]),
    trigger("bounceAnimation", [
      transition(":enter", [
        animate(
          "500ms",
          keyframes([
            style({ transform: "scale(0)", offset: 0 }),
            style({ transform: "scale(1.2)", offset: 0.5 }),
            style({ transform: "scale(1)", offset: 1 }),
          ])
        ),
      ]),
    ]),
    trigger("shakeAnimation", [
      transition(":enter", [
        animate(
          "800ms",
          keyframes([
            style({ transform: "translateX(0)", offset: 0 }),
            style({ transform: "translateX(-10px)", offset: 0.1 }),
            style({ transform: "translateX(10px)", offset: 0.2 }),
            style({ transform: "translateX(-10px)", offset: 0.3 }),
            style({ transform: "translateX(10px)", offset: 0.4 }),
            style({ transform: "translateX(-10px)", offset: 0.5 }),
            style({ transform: "translateX(10px)", offset: 0.6 }),
            style({ transform: "translateX(-10px)", offset: 0.7 }),
            style({ transform: "translateX(10px)", offset: 0.8 }),
            style({ transform: "translateX(-5px)", offset: 0.9 }),
            style({ transform: "translateX(0)", offset: 1 }),
          ])
        ),
      ]),
    ]),
  ],
})
export class DeliveryPlannerComponent
  implements OnInit, AfterViewInit, OnDestroy
{
  @ViewChild("gridCanvas") canvasRef!: ElementRef<HTMLCanvasElement>;

  private ctx!: CanvasRenderingContext2D;
  private scale: number = 1;
  private offset = { x: 0, y: 0 };
  private lastMousePosition = { x: 0, y: 0 };
  private isDraggingCanvas = false;
  private animationFrameId: number | null = null;

  // Theme
  isDarkTheme = false;

  // Grid visibility
  showGrid = false;

  // Grid configuration
  gridRows = 10;
  gridCols = 10;
  cellSize = 50;

  // Grid data
  stores: Position[] = [];
  destinations: Position[] = [];
  tunnels: TunnelConfig[] = [];
  roadblocks: RoadBlockConfig[] = [];
  trafficCosts: number[][][] = []; // [y][x][direction]

  // Operation modes
  mode:
    | "move"
    | "add-store"
    | "add-destination"
    | "add-tunnel"
    | "add-roadblock"
    | "add-cost"
    | "generate-random"
    | "delete" = "move";
  tunnelStart: Position | null = null;

  // Strategy
  selectedStrategy = "BFS";
  strategies = ["BFS", "DFS", "UCS", "AStar", "Greedy"];

  // Results
  routes: DeliveryRoute[] = [];
  isLoading = false;
  error: string | null = null;

  // Animation state
  isAnimating = false;
  currentAnimatingRoute = 0;
  currentAnimatingStep = 0;
  truckPosition: Position | null = null;
  animationSpeed = 200; // milliseconds per step
  animationTimer: any = null;

  constructor(private deliveryService: DeliveryPlannerService) {}

  ngOnInit(): void {
    // Load theme preference
    this.isDarkTheme = localStorage.getItem("deliveryPlannerTheme") === "dark";

    // Initialize traffic costs (default 1 for all directions)
    this.initializeTrafficCosts();
  }

  ngAfterViewInit(): void {
    const canvas = this.canvasRef.nativeElement;
    this.ctx = canvas.getContext("2d")!;

    // Set canvas size
    this.resizeCanvas();

    // Add event listener for window resize
    window.addEventListener("resize", this.resizeCanvas.bind(this));

    // Initialize canvas event listeners
    this.setupCanvasListeners();

    // Start animation loop
    this.startAnimationLoop();
  }

  ngOnDestroy(): void {
    if (this.animationFrameId !== null) {
      cancelAnimationFrame(this.animationFrameId);
    }
    window.removeEventListener("resize", this.resizeCanvas.bind(this));
  }

  @HostListener("window:beforeunload")
  saveThemePreference(): void {
    localStorage.setItem(
      "deliveryPlannerTheme",
      this.isDarkTheme ? "dark" : "light"
    );
  }

  toggleTheme(): void {
    this.isDarkTheme = !this.isDarkTheme;
    localStorage.setItem(
      "deliveryPlannerTheme",
      this.isDarkTheme ? "dark" : "light"
    );
    this.renderGrid();
  }

  private initializeTrafficCosts(): void {
    this.trafficCosts = [];
    for (let y = 0; y < this.gridRows; y++) {
      this.trafficCosts[y] = [];
      for (let x = 0; x < this.gridCols; x++) {
        this.trafficCosts[y][x] = [1, 1, 1, 1]; // [up, down, left, right]
      }
    }
  }

  private resizeCanvas(): void {
    const canvas = this.canvasRef.nativeElement;
    const parentWidth = canvas.parentElement!.clientWidth;
    const parentHeight = canvas.parentElement!.clientHeight;

    canvas.width = parentWidth;
    canvas.height = parentHeight;

    // Center the grid
    this.centerGrid();

    this.renderGrid();
  }

  private centerGrid(): void {
    const canvas = this.canvasRef.nativeElement;
    const gridWidth = this.gridCols * this.cellSize;
    const gridHeight = this.gridRows * this.cellSize;

    // Calculate offset to center the grid
    this.offset.x = (canvas.width / this.scale - gridWidth) / 2;
    this.offset.y = (canvas.height / this.scale - gridHeight) / 2;
  }

  private startAnimationLoop(): void {
    const animate = () => {
      this.renderGrid();
      this.animationFrameId = requestAnimationFrame(animate);
    };

    this.animationFrameId = requestAnimationFrame(animate);
  }

  private setupCanvasListeners(): void {
    const canvas = this.canvasRef.nativeElement;

    canvas.addEventListener("mousedown", this.handleMouseDown.bind(this));
    canvas.addEventListener("mousemove", this.handleMouseMove.bind(this));
    canvas.addEventListener("mouseup", this.handleMouseUp.bind(this));
    canvas.addEventListener("mouseleave", this.handleMouseUp.bind(this));
    canvas.addEventListener("wheel", this.handleWheel.bind(this), {
      passive: false,
    });
  }

  private handleMouseDown(e: MouseEvent): void {
    const rect = this.canvasRef.nativeElement.getBoundingClientRect();
    const x = (e.clientX - rect.left) / this.scale - this.offset.x;
    const y = (e.clientY - rect.top) / this.scale - this.offset.y;

    this.processPointerDown(x, y);
    this.lastMousePosition = { x: e.clientX, y: e.clientY };
  }

  private handleMouseMove(e: MouseEvent): void {
    const rect = this.canvasRef.nativeElement.getBoundingClientRect();
    const clientX = e.clientX;
    const clientY = e.clientY;

    if (this.isDraggingCanvas) {
      const dx = clientX - this.lastMousePosition.x;
      const dy = clientY - this.lastMousePosition.y;
      this.offset.x += dx / this.scale;
      this.offset.y += dy / this.scale;
      this.lastMousePosition = { x: clientX, y: clientY };
    }
  }

  private handleMouseUp(): void {
    this.isDraggingCanvas = false;
  }

  private handleWheel(e: WheelEvent): void {
    e.preventDefault();

    const rect = this.canvasRef.nativeElement.getBoundingClientRect();
    const mouseX = e.clientX - rect.left;
    const mouseY = e.clientY - rect.top;

    const worldX = mouseX / this.scale - this.offset.x;
    const worldY = mouseY / this.scale - this.offset.y;

    const zoomFactor = e.deltaY > 0 ? 0.95 : 1.05;
    this.scale *= zoomFactor;

    this.scale = Math.min(Math.max(0.5, this.scale), 3);

    this.offset.x = mouseX / this.scale - worldX;
    this.offset.y = mouseY / this.scale - worldY;
  }

  private processPointerDown(x: number, y: number): void {
    const gridX = Math.floor(x / this.cellSize);
    const gridY = Math.floor(y / this.cellSize);

    if (
      gridX < 0 ||
      gridX >= this.gridCols ||
      gridY < 0 ||
      gridY >= this.gridRows
    ) {
      if (this.mode === "move") {
        this.isDraggingCanvas = true;
      }
      return;
    }

    const pos: Position = { x: gridX, y: gridY };

    if (this.mode === "delete") {
      this.deleteAtPosition(pos);
    } else if (this.mode === "add-store") {
      if (!this.findItemAtPosition(pos, this.stores)) {
        this.stores.push(pos);
      }
    } else if (this.mode === "add-destination") {
      if (!this.findItemAtPosition(pos, this.destinations)) {
        this.destinations.push(pos);
      }
    } else if (this.mode === "add-tunnel") {
      if (this.tunnelStart === null) {
        this.tunnelStart = pos;
      } else {
        if (this.tunnelStart.x !== pos.x || this.tunnelStart.y !== pos.y) {
          this.tunnels.push({
            start: this.tunnelStart,
            end: pos,
            cost: 5, // default tunnel cost
          });
        }
        this.tunnelStart = null;
      }
    } else if (this.mode === "add-roadblock") {
      // For now, block all directions
      ["up", "down", "left", "right"].forEach((dir) => {
        this.roadblocks.push({ from: pos, direction: dir });
      });
    } else if (this.mode === "add-cost") {
      this.editCellCost(pos);
    } else if (this.mode === "move") {
      this.isDraggingCanvas = true;
    }
  }

  private findItemAtPosition(
    pos: Position,
    list: Position[]
  ): Position | undefined {
    return list.find((item) => item.x === pos.x && item.y === pos.y);
  }

  private deleteAtPosition(pos: Position): void {
    // Remove stores
    this.stores = this.stores.filter((s) => s.x !== pos.x || s.y !== pos.y);

    // Remove destinations
    this.destinations = this.destinations.filter(
      (d) => d.x !== pos.x || d.y !== pos.y
    );

    // Remove tunnels
    this.tunnels = this.tunnels.filter(
      (t) =>
        !(
          (t.start.x === pos.x && t.start.y === pos.y) ||
          (t.end.x === pos.x && t.end.y === pos.y)
        )
    );

    // Remove roadblocks
    this.roadblocks = this.roadblocks.filter(
      (rb) => rb.from.x !== pos.x || rb.from.y !== pos.y
    );
  }

  private renderGrid(): void {
    if (!this.ctx) return;

    const canvas = this.canvasRef.nativeElement;
    this.ctx.clearRect(0, 0, canvas.width, canvas.height);

    // Don't draw anything if grid is not shown yet
    if (!this.showGrid) {
      return;
    }

    // Save context
    this.ctx.save();

    // Apply transformations
    this.ctx.translate(this.offset.x * this.scale, this.offset.y * this.scale);
    this.ctx.scale(this.scale, this.scale);

    // Draw grid
    this.drawGridLines();

    // Draw tunnels first (behind everything)
    this.drawTunnels();

    // Draw traffic costs
    this.drawTrafficCosts();

    // Draw routes if any
    if (this.routes.length > 0) {
      this.drawRoutes();
    }

    // Draw stores
    this.drawStores();

    // Draw destinations
    this.drawDestinations();

    // Draw roadblocks
    this.drawRoadblocks();

    // Draw tunnel start indicator if in tunnel mode
    if (this.mode === "add-tunnel" && this.tunnelStart) {
      this.drawTunnelStartIndicator();
    }

    // Draw animated truck if animation is active
    if (this.isAnimating && this.truckPosition) {
      this.drawTruck();
    }

    // Restore context
    this.ctx.restore();
  }

  private drawGridLines(): void {
    this.ctx.strokeStyle = this.isDarkTheme
      ? "rgba(255, 255, 255, 0.1)"
      : "rgba(0, 0, 0, 0.1)";
    this.ctx.lineWidth = 1;

    // Vertical lines
    for (let x = 0; x <= this.gridCols; x++) {
      this.ctx.beginPath();
      this.ctx.moveTo(x * this.cellSize, 0);
      this.ctx.lineTo(x * this.cellSize, this.gridRows * this.cellSize);
      this.ctx.stroke();
    }

    // Horizontal lines
    for (let y = 0; y <= this.gridRows; y++) {
      this.ctx.beginPath();
      this.ctx.moveTo(0, y * this.cellSize);
      this.ctx.lineTo(this.gridCols * this.cellSize, y * this.cellSize);
      this.ctx.stroke();
    }
  }

  private drawStores(): void {
    for (const store of this.stores) {
      const centerX = store.x * this.cellSize + this.cellSize / 2;
      const centerY = store.y * this.cellSize + this.cellSize / 2;

      // Draw circle background
      this.ctx.fillStyle = "#B19CD9";
      this.ctx.strokeStyle = "#8B7BB8";
      this.ctx.lineWidth = 3;
      this.ctx.beginPath();
      this.ctx.arc(centerX, centerY, this.cellSize * 0.3, 0, 2 * Math.PI);
      this.ctx.fill();
      this.ctx.stroke();

      // Draw 'S' label
      this.ctx.fillStyle = "#FFFFFF";
      this.ctx.font = `bold ${Math.max(12, this.cellSize * 0.4)}px Arial`;
      this.ctx.textAlign = "center";
      this.ctx.textBaseline = "middle";
      this.ctx.fillText("S", centerX, centerY);
    }
  }

  private drawDestinations(): void {
    for (const dest of this.destinations) {
      const centerX = dest.x * this.cellSize + this.cellSize / 2;
      const centerY = dest.y * this.cellSize + this.cellSize / 2;

      // Draw square background
      this.ctx.fillStyle = "#FF5722";
      this.ctx.strokeStyle = "#D84315";
      this.ctx.lineWidth = 3;
      this.ctx.beginPath();
      this.ctx.rect(
        centerX - this.cellSize * 0.3,
        centerY - this.cellSize * 0.3,
        this.cellSize * 0.6,
        this.cellSize * 0.6
      );
      this.ctx.fill();
      this.ctx.stroke();

      // Draw 'D' label
      this.ctx.fillStyle = "#FFFFFF";
      this.ctx.font = `bold ${Math.max(12, this.cellSize * 0.4)}px Arial`;
      this.ctx.textAlign = "center";
      this.ctx.textBaseline = "middle";
      this.ctx.fillText("D", centerX, centerY);
    }
  }

  private drawTunnels(): void {
    this.ctx.strokeStyle = "#9C27B0";
    this.ctx.lineWidth = 4;
    this.ctx.setLineDash([10, 5]);

    for (const tunnel of this.tunnels) {
      const startX = tunnel.start.x * this.cellSize + this.cellSize / 2;
      const startY = tunnel.start.y * this.cellSize + this.cellSize / 2;
      const endX = tunnel.end.x * this.cellSize + this.cellSize / 2;
      const endY = tunnel.end.y * this.cellSize + this.cellSize / 2;

      this.ctx.beginPath();
      this.ctx.moveTo(startX, startY);
      this.ctx.lineTo(endX, endY);
      this.ctx.stroke();

      // Draw tunnel entrance/exit markers
      this.ctx.fillStyle = "#9C27B0";
      this.ctx.beginPath();
      this.ctx.arc(startX, startY, 8, 0, 2 * Math.PI);
      this.ctx.fill();
      this.ctx.beginPath();
      this.ctx.arc(endX, endY, 8, 0, 2 * Math.PI);
      this.ctx.fill();
    }

    this.ctx.setLineDash([]);
  }

  private drawTrafficCosts(): void {
    this.ctx.fillStyle = this.isDarkTheme ? "#AAAAAA" : "#555555";
    this.ctx.font = `${Math.max(8, this.cellSize * 0.12)}px Arial`;
    this.ctx.textAlign = "center";
    this.ctx.textBaseline = "middle";

    for (let y = 0; y < this.gridRows; y++) {
      for (let x = 0; x < this.gridCols; x++) {
        const cellCosts = this.trafficCosts[y][x];

        // Draw right cost (cost to move right from this cell to x+1,y)
        if (x < this.gridCols - 1) {
          const cost = cellCosts[3]; // right cost
          const centerX = x * this.cellSize + this.cellSize;
          const centerY = y * this.cellSize + this.cellSize / 2;
          this.ctx.fillText(cost.toString(), centerX, centerY);
        }

        // Draw down cost (cost to move down from this cell to x,y+1)
        if (y < this.gridRows - 1) {
          const cost = cellCosts[1]; // down cost
          const centerX = x * this.cellSize + this.cellSize / 2;
          const centerY = y * this.cellSize + this.cellSize;
          this.ctx.fillText(cost.toString(), centerX, centerY);
        }
      }
    }
  }

  private drawRoadblocks(): void {
    this.ctx.fillStyle = "#F44336";
    this.ctx.strokeStyle = "#F44336";
    this.ctx.lineWidth = Math.max(4, this.cellSize / 8); // Wall thickness

    for (const rb of this.roadblocks) {
      let fromX = rb.from.x * this.cellSize;
      let fromY = rb.from.y * this.cellSize;
      let toX = rb.from.x * this.cellSize;
      let toY = rb.from.y * this.cellSize;

      // Calculate target position based on direction
      switch (rb.direction) {
        case "right":
          toX = (rb.from.x + 1) * this.cellSize;
          break;
        case "down":
          toY = (rb.from.y + 1) * this.cellSize;
          break;
        case "left":
          fromX = (rb.from.x - 1) * this.cellSize;
          toX = rb.from.x * this.cellSize;
          break;
        case "up":
          fromY = (rb.from.y - 1) * this.cellSize;
          toY = rb.from.y * this.cellSize;
          break;
      }

      // Draw wall between cells
      this.ctx.beginPath();
      if (rb.direction === "right" || rb.direction === "left") {
        // Vertical wall
        const wallX = rb.direction === "right" ? fromX + this.cellSize : fromX;
        this.ctx.fillRect(wallX - this.ctx.lineWidth / 2, fromY, this.ctx.lineWidth, this.cellSize);
      } else {
        // Horizontal wall
        const wallY = rb.direction === "down" ? fromY + this.cellSize : fromY;
        this.ctx.fillRect(fromX, wallY - this.ctx.lineWidth / 2, this.cellSize, this.ctx.lineWidth);
      }
    }
  }

  private drawTunnelStartIndicator(): void {
    if (!this.tunnelStart) return;

    const centerX = this.tunnelStart.x * this.cellSize + this.cellSize / 2;
    const centerY = this.tunnelStart.y * this.cellSize + this.cellSize / 2;

    this.ctx.strokeStyle = "#9C27B0";
    this.ctx.lineWidth = 3;
    this.ctx.setLineDash([5, 5]);
    this.ctx.beginPath();
    this.ctx.arc(centerX, centerY, this.cellSize * 0.4, 0, 2 * Math.PI);
    this.ctx.stroke();
    this.ctx.setLineDash([]);
  }

  private drawRoutes(): void {
    // Only draw routes during animation - show only the currently animating route
    if (!this.isAnimating) {
      return; // Don't draw any routes when not animating
    }

    // Only draw the currently animating route
    const routeIndex = this.currentAnimatingRoute;
    if (routeIndex < 0 || routeIndex >= this.routes.length) {
      return;
    }

    const route = this.routes[routeIndex];
    if (!route.path || route.path.length === 0) return;

    // Highlight the currently animating route with gold color
    this.ctx.strokeStyle = "#FFD700"; // Gold color for active route
    this.ctx.lineWidth = 6; // Thicker line for active route
    this.ctx.shadowColor = "#FFD700";
    this.ctx.shadowBlur = 10;
    this.ctx.lineCap = "round";
    this.ctx.lineJoin = "round";

    this.ctx.beginPath();
    const firstPoint = route.path[0];
    this.ctx.moveTo(
      firstPoint.x * this.cellSize + this.cellSize / 2,
      firstPoint.y * this.cellSize + this.cellSize / 2
    );

    for (let j = 1; j < route.path.length; j++) {
      const point = route.path[j];
      this.ctx.lineTo(
        point.x * this.cellSize + this.cellSize / 2,
        point.y * this.cellSize + this.cellSize / 2
      );
    }

    this.ctx.stroke();

    // Draw arrow at the end
    if (route.path.length > 1) {
      const lastPoint = route.path[route.path.length - 1];
      const secondLastPoint = route.path[route.path.length - 2];
      this.drawArrow(secondLastPoint, lastPoint, "#FFD700");
    }

    // Reset shadow
    this.ctx.shadowBlur = 0;
  }

  private drawTruck(): void {
    if (!this.truckPosition) return;

    const centerX = this.truckPosition.x * this.cellSize + this.cellSize / 2;
    const centerY = this.truckPosition.y * this.cellSize + this.cellSize / 2;

    // Draw truck as emoji
    this.ctx.font = `${Math.max(20, this.cellSize * 0.6)}px Arial`;
    this.ctx.textAlign = "center";
    this.ctx.textBaseline = "middle";
    this.ctx.fillText("🚚", centerX, centerY);
  }

  private drawArrow(from: Position, to: Position, color: string): void {
    const fromX = from.x * this.cellSize + this.cellSize / 2;
    const fromY = from.y * this.cellSize + this.cellSize / 2;
    const toX = to.x * this.cellSize + this.cellSize / 2;
    const toY = to.y * this.cellSize + this.cellSize / 2;

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

  setMode(mode: typeof this.mode): void {
    this.mode = mode;
    this.tunnelStart = null;
    if (!this.showGrid) {
      this.showGrid = true; // Show grid when user selects a tool
      this.centerGrid(); // Center it when first shown
    }
  }

  resetView(): void {
    this.scale = 1;
    this.offset = { x: 0, y: 0 };
  }

  resetGrid(): void {
    this.stores = [];
    this.destinations = [];
    this.tunnels = [];
    this.roadblocks = [];
    this.routes = [];
    this.error = null;
    this.initializeTrafficCosts();
  }

  onGridSizeChange(): void {
    // Validate grid size
    if (this.gridRows < 3 || this.gridRows > 20) {
      this.gridRows = Math.max(3, Math.min(20, this.gridRows));
    }
    if (this.gridCols < 3 || this.gridCols > 20) {
      this.gridCols = Math.max(3, Math.min(20, this.gridCols));
    }

    // Clear existing data that might be outside the new grid bounds
    this.stores = this.stores.filter(
      (s) => s.x < this.gridCols && s.y < this.gridRows
    );
    this.destinations = this.destinations.filter(
      (d) => d.x < this.gridCols && d.y < this.gridRows
    );
    this.tunnels = this.tunnels.filter(
      (t) =>
        t.start.x < this.gridCols &&
        t.start.y < this.gridRows &&
        t.end.x < this.gridCols &&
        t.end.y < this.gridRows
    );
    this.roadblocks = this.roadblocks.filter(
      (rb) => rb.from.x < this.gridCols && rb.from.y < this.gridRows
    );

    // Clear routes when grid changes
    this.routes = [];
    this.error = null;

    // Reinitialize traffic costs with new dimensions
    this.initializeTrafficCosts();

    // Re-center the grid
    this.centerGrid();

    // Update canvas size
    this.resizeCanvas();
  }

  async planDelivery(): Promise<void> {
    console.log("=== Starting planDelivery ===");

    if (this.stores.length === 0) {
      this.error = "Please add at least one store";
      console.error("No stores added");
      return;
    }

    if (this.destinations.length === 0) {
      this.error = "Please add at least one destination";
      console.error("No destinations added");
      return;
    }

    console.log(
      `Stores: ${this.stores.length}, Destinations: ${this.destinations.length}`
    );
    console.log("Stores:", this.stores);
    console.log("Destinations:", this.destinations);
    console.log("Strategy:", this.selectedStrategy);

    this.isLoading = true;
    this.error = null;
    this.routes = [];

    try {
      const gridConfig: GridConfig = {
        rows: this.gridRows,
        cols: this.gridCols,
        traffic: this.trafficCosts,
        stores: this.stores,
        destinations: this.destinations,
        tunnels: this.tunnels,
        roadblocks: this.roadblocks,
      };

      console.log("Sending request to backend with config:", gridConfig);

      const response = await this.deliveryService
        .planDelivery(gridConfig, this.selectedStrategy)
        .toPromise();

      console.log("Received response from backend:", response);

      if (response && response.success) {
        this.routes = response.routes || [];
        console.log(`Found ${this.routes.length} routes`);

        if (this.routes.length === 0) {
          this.error = "No routes found. Check if destinations are reachable.";
        } else {
          // Start animating routes
          this.startRouteAnimation();
        }
      } else {
        this.error = response?.message || "Planning failed";
        console.error("Planning failed:", response?.message);
      }
    } catch (err: any) {
      this.error = `Error: ${err.message || "Unknown error"}`;
      console.error("Planning error:", err);
      console.error("Error details:", err.error);
      console.error("Error status:", err.status);
    } finally {
      this.isLoading = false;
      console.log("=== Finished planDelivery ===");
    }
  }

  exportGrid(): void {
    const gridData = {
      rows: this.gridRows,
      cols: this.gridCols,
      stores: this.stores,
      destinations: this.destinations,
      tunnels: this.tunnels,
      roadblocks: this.roadblocks,
      trafficCosts: this.trafficCosts,
    };

    const dataStr = JSON.stringify(gridData, null, 2);
    const dataBlob = new Blob([dataStr], { type: "application/json" });
    const url = URL.createObjectURL(dataBlob);

    const link = document.createElement("a");
    link.href = url;
    link.download = "delivery-grid.json";
    link.click();

    URL.revokeObjectURL(url);
  }

  importGrid(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const file = input.files[0];
    const reader = new FileReader();

    reader.onload = (e) => {
      try {
        const data = JSON.parse(e.target?.result as string);
        this.gridRows = data.rows || 10;
        this.gridCols = data.cols || 10;
        this.stores = data.stores || [];
        this.destinations = data.destinations || [];
        this.tunnels = data.tunnels || [];
        this.roadblocks = data.roadblocks || [];
        this.trafficCosts = data.trafficCosts || [];

        if (this.trafficCosts.length === 0) {
          this.initializeTrafficCosts();
        }

        this.routes = [];
        this.error = null;
        if (!this.showGrid) {
          this.showGrid = true; // Show grid after import
          this.centerGrid(); // Center it when first shown
        }
      } catch (err) {
        this.error = "Invalid grid file format";
        console.error("Import error:", err);
      }
    };

    reader.readAsText(file);
    input.value = "";
  }

  editCellCost(pos: Position): void {
    const currentCosts = this.trafficCosts[pos.y][pos.x];
    const costInput = prompt(
      `Enter traffic costs for cell (${pos.x}, ${
        pos.y
      }) as: up,down,left,right\nCurrent: ${currentCosts.join(",")}`,
      currentCosts.join(",")
    );

    if (costInput !== null) {
      const costs = costInput.split(",").map((c) => {
        const num = parseInt(c.trim());
        return isNaN(num) || num < 1 ? 1 : num;
      });

      if (costs.length === 4) {
        this.trafficCosts[pos.y][pos.x] = costs;
        this.routes = []; // Clear routes when costs change
      } else {
        this.error =
          "Please enter exactly 4 comma-separated numbers for up, down, left, right costs";
      }
    }
  }

  generateRandomGrid(): void {
    this.resetGrid();
    if (!this.showGrid) {
      this.showGrid = true; // Show grid when generating random grid
      this.centerGrid(); // Center it when first shown
    }

    const numStores = Math.floor(Math.random() * 3) + 2; // 2-4 stores
    const numDestinations = Math.floor(Math.random() * 4) + 3; // 3-6 destinations
    const numTunnels = Math.floor(Math.random() * 3); // 0-2 tunnels
    const numRoadblocks = Math.floor(Math.random() * 5) + 2; // 2-6 roadblocks

    // Generate random stores
    for (let i = 0; i < numStores; i++) {
      let pos: Position;
      do {
        pos = {
          x: Math.floor(Math.random() * this.gridCols),
          y: Math.floor(Math.random() * this.gridRows),
        };
      } while (this.findItemAtPosition(pos, this.stores));
      this.stores.push(pos);
    }

    // Generate random destinations
    for (let i = 0; i < numDestinations; i++) {
      let pos: Position;
      do {
        pos = {
          x: Math.floor(Math.random() * this.gridCols),
          y: Math.floor(Math.random() * this.gridRows),
        };
      } while (
        this.findItemAtPosition(pos, this.stores) ||
        this.findItemAtPosition(pos, this.destinations)
      );
      this.destinations.push(pos);
    }

    // Generate random tunnels
    for (let i = 0; i < numTunnels; i++) {
      let start: Position, end: Position;
      do {
        start = {
          x: Math.floor(Math.random() * this.gridCols),
          y: Math.floor(Math.random() * this.gridRows),
        };
        end = {
          x: Math.floor(Math.random() * this.gridCols),
          y: Math.floor(Math.random() * this.gridRows),
        };
      } while (
        (start.x === end.x && start.y === end.y) ||
        this.findItemAtPosition(start, this.stores) ||
        this.findItemAtPosition(start, this.destinations) ||
        this.findItemAtPosition(end, this.stores) ||
        this.findItemAtPosition(end, this.destinations)
      );

      this.tunnels.push({
        start,
        end,
        cost: Math.floor(Math.random() * 10) + 1, // Random cost 1-10
      });
    }

    // Generate random roadblocks
    for (let i = 0; i < numRoadblocks; i++) {
      let pos: Position;
      const directions = ["up", "down", "left", "right"];
      do {
        pos = {
          x: Math.floor(Math.random() * this.gridCols),
          y: Math.floor(Math.random() * this.gridRows),
        };
      } while (
        this.findItemAtPosition(pos, this.stores) ||
        this.findItemAtPosition(pos, this.destinations)
      );

      // Block random directions
      const numDirections = Math.floor(Math.random() * 3) + 1; // 1-3 directions
      const shuffledDirections = directions.sort(() => 0.5 - Math.random());
      for (let j = 0; j < numDirections; j++) {
        this.roadblocks.push({ from: pos, direction: shuffledDirections[j] });
      }
    }

    // Add some random traffic costs
    for (let y = 0; y < this.gridRows; y++) {
      for (let x = 0; x < this.gridCols; x++) {
        if (Math.random() < 0.3) {
          // 30% chance to have custom costs
          this.trafficCosts[y][x] = [
            Math.floor(Math.random() * 5) + 1, // up
            Math.floor(Math.random() * 5) + 1, // down
            Math.floor(Math.random() * 5) + 1, // left
            Math.floor(Math.random() * 5) + 1, // right
          ];
        }
      }
    }
  }

  // ======================================================================
  // ROUTE ANIMATION (Similar to Swing UI)
  // ======================================================================

  startRouteAnimation(): void {
    console.log("Starting route animation");
    this.isAnimating = true;
    this.currentAnimatingRoute = 0;
    this.currentAnimatingStep = 0;
    this.animateNextStep();
  }

  stopRouteAnimation(): void {
    console.log("Stopping route animation");
    this.isAnimating = false;
    if (this.animationTimer) {
      clearTimeout(this.animationTimer);
      this.animationTimer = null;
    }
    this.truckPosition = null;
    this.renderGrid();
  }

  animateNextStep(): void {
    if (!this.isAnimating || this.routes.length === 0) {
      return;
    }

    // Check if we're done with all routes
    if (this.currentAnimatingRoute >= this.routes.length) {
      console.log("Animation complete for all routes");
      this.isAnimating = false;
      this.truckPosition = null;
      this.renderGrid();
      return;
    }

    const currentRoute = this.routes[this.currentAnimatingRoute];

    // Check if current route is complete
    if (this.currentAnimatingStep >= currentRoute.path.length) {
      console.log(
        `Route ${this.currentAnimatingRoute + 1}/${this.routes.length} complete`
      );

      // Move to next route
      this.currentAnimatingRoute++;
      this.currentAnimatingStep = 0;

      // Pause between routes
      this.animationTimer = setTimeout(() => {
        this.animateNextStep();
      }, 1000); // 1 second pause between routes

      return;
    }

    // Update truck position
    this.truckPosition = currentRoute.path[this.currentAnimatingStep];
    this.currentAnimatingStep++;

    // Render with truck
    this.renderGrid();

    // Schedule next step
    this.animationTimer = setTimeout(() => {
      this.animateNextStep();
    }, this.animationSpeed);
  }

  resetAnimation(): void {
    this.stopRouteAnimation();
    this.currentAnimatingRoute = 0;
    this.currentAnimatingStep = 0;
    this.truckPosition = null;
  }

  animateSpecificRoute(routeIndex: number): void {
    console.log(`Starting animation for route ${routeIndex + 1}`);
    this.stopRouteAnimation(); // Stop any current animation

    if (routeIndex < 0 || routeIndex >= this.routes.length) {
      console.error(`Invalid route index: ${routeIndex}`);
      return;
    }

    this.isAnimating = true;
    this.currentAnimatingRoute = routeIndex;
    this.currentAnimatingStep = 0;
    this.animateSingleRoute();
  }

  animateSingleRoute(): void {
    if (!this.isAnimating || this.currentAnimatingRoute >= this.routes.length) {
      return;
    }

    const currentRoute = this.routes[this.currentAnimatingRoute];

    // Check if current route is complete
    if (this.currentAnimatingStep >= currentRoute.path.length) {
      console.log(`Route ${this.currentAnimatingRoute + 1} animation complete`);
      this.isAnimating = false;
      this.truckPosition = null;
      this.renderGrid();
      return;
    }

    // Update truck position
    this.truckPosition = currentRoute.path[this.currentAnimatingStep];
    this.currentAnimatingStep++;

    // Render with truck
    this.renderGrid();

    // Schedule next step
    this.animationTimer = setTimeout(() => {
      this.animateSingleRoute();
    }, this.animationSpeed);
  }
}
