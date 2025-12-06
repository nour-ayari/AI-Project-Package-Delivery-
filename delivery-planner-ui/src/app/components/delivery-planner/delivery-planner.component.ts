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
  style,
  transition,
  trigger,
} from "@angular/animations";
import { GridInteractionService } from "./services/grid-interaction.service";
import { GridRendererService } from "./services/grid-renderer.service";
import { GridGeneratorService } from "./services/grid-generator.service";
import { AnimationService } from "./services/animation.service";

@Component({
  selector: "app-delivery-planner",
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: "./delivery-planner.component.html",
  styleUrl: "./delivery-planner.component.css",
  providers: [
    DeliveryPlannerService,
    GridInteractionService,
    GridRendererService,
    GridGeneratorService,
    AnimationService,
  ],
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
  truckPosition: Position | null = null;

  constructor(
    private deliveryService: DeliveryPlannerService,
    private gridInteraction: GridInteractionService,
    private gridRenderer: GridRendererService,
    private gridGenerator: GridGeneratorService,
    private animationService: AnimationService
  ) {}

  ngOnInit(): void {
    // Load theme preference
    this.isDarkTheme = localStorage.getItem("deliveryPlannerTheme") === "dark";

    // Initialize traffic costs (default 1 for all directions)
    this.initializeTrafficCosts();

    // Setup animation callbacks
    this.animationService.setCallbacks(
      (route: number, step: number) =>
        this.onAnimationPositionUpdate(route, step),
      () => this.onAnimationComplete()
    );
  }

  ngAfterViewInit(): void {
    const canvas = this.canvasRef.nativeElement;
    this.ctx = canvas.getContext("2d")!;

    // Setup renderer
    this.gridRenderer.setContext(this.ctx);
    this.gridRenderer.setTheme(this.isDarkTheme);

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
    this.animationService.stopRouteAnimation();
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
    this.gridRenderer.setTheme(this.isDarkTheme);
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

    // Special handling for roadblock mode - detect which edge/wall was clicked
    if (this.mode === "add-roadblock") {
      const wallInfo = this.gridInteraction.detectWallClick(
        x,
        y,
        this.cellSize,
        this.gridCols,
        this.gridRows
      );
      if (wallInfo) {
        // Check if roadblock already exists
        const exists = this.roadblocks.some(
          (rb) =>
            rb.from.x === wallInfo.from.x &&
            rb.from.y === wallInfo.from.y &&
            rb.direction === wallInfo.direction
        );
        if (!exists) {
          this.roadblocks.push({
            from: wallInfo.from,
            direction: wallInfo.direction,
          });
          // Set traffic cost to 0 for blocked direction
          const dirIndex = ["up", "down", "left", "right"].indexOf(
            wallInfo.direction
          );
          this.trafficCosts[wallInfo.from.y][wallInfo.from.x][dirIndex] = 0;
        }
      }
      this.renderGrid();
      return;
    }

    if (this.mode === "delete") {
      const result = this.gridInteraction.deleteAtPosition(
        pos,
        this.stores,
        this.destinations,
        this.tunnels,
        this.roadblocks
      );
      this.stores = result.stores;
      this.destinations = result.destinations;
      this.tunnels = result.tunnels;
      this.roadblocks = result.roadblocks;
    } else if (this.mode === "add-store") {
      if (this.stores.length >= 3) {
        this.error = "Maximum 3 stores allowed";
        return;
      }
      if (!this.gridInteraction.findItemAtPosition(pos, this.stores)) {
        this.stores.push(pos);
        this.error = null; // Clear error on successful addition
      }
    } else if (this.mode === "add-destination") {
      if (this.destinations.length >= 10) {
        this.error = "Maximum 10 destinations allowed";
        return;
      }
      if (!this.gridInteraction.findItemAtPosition(pos, this.destinations)) {
        this.destinations.push(pos);
        this.error = null; // Clear error on successful addition
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
    } else if (this.mode === "add-cost") {
      const wallInfo = this.gridInteraction.detectWallClick(
        x,
        y,
        this.cellSize,
        this.gridCols,
        this.gridRows
      );
      if (wallInfo) {
        this.editEdgeCost(wallInfo.from, wallInfo.direction);
      }
    } else if (this.mode === "move") {
      this.isDraggingCanvas = true;
    }
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

    // Draw grid components using renderer service
    this.gridRenderer.drawGridLines(
      this.gridRows,
      this.gridCols,
      this.cellSize
    );
    this.gridRenderer.drawTunnels(this.tunnels, this.cellSize);
    this.gridRenderer.drawTrafficCosts(
      this.trafficCosts,
      this.gridRows,
      this.gridCols,
      this.cellSize
    );

    // Draw routes if any
    if (this.routes.length > 0) {
      this.gridRenderer.drawRoutes(
        this.routes,
        this.animationService.isAnimationActive(),
        this.animationService.getCurrentRoute(),
        this.cellSize
      );
    }

    this.gridRenderer.drawStores(this.stores, this.cellSize);
    this.gridRenderer.drawDestinations(this.destinations, this.cellSize);
    this.gridRenderer.drawRoadblocks(
      this.roadblocks,
      this.gridRows,
      this.gridCols,
      this.cellSize
    );

    // Draw tunnel start indicator if in tunnel mode
    if (this.mode === "add-tunnel") {
      this.gridRenderer.drawTunnelStartIndicator(
        this.tunnelStart,
        this.cellSize
      );
    }

    // Draw animated truck if animation is active
    if (this.animationService.isAnimationActive() && this.truckPosition) {
      this.gridRenderer.drawTruck(this.truckPosition, this.cellSize);
    }

    // Restore context
    this.ctx.restore();
  }

  generateRandomGrid(): void {
    this.resetGrid();
    if (!this.showGrid) {
      this.showGrid = true;
      this.centerGrid();
    }

    const result = this.gridGenerator.generateRandomGrid(
      this.gridRows,
      this.gridCols
    );
    this.stores = result.stores;
    this.destinations = result.destinations;
    this.tunnels = result.tunnels;
    this.roadblocks = result.roadblocks;
    this.trafficCosts = result.trafficCosts;
  }

  // Animation methods using service
  startRouteAnimation(): void {
    this.animationService.startRouteAnimation(this.routes);
  }

  stopRouteAnimation(): void {
    this.animationService.stopRouteAnimation();
    this.truckPosition = null;
    this.renderGrid();
  }

  resetAnimation(): void {
    this.animationService.resetAnimation();
    this.truckPosition = null;
  }

  animateSpecificRoute(routeIndex: number): void {
    this.animationService.animateSpecificRoute(routeIndex, this.routes);
  }

  // Animation callbacks
  private onAnimationPositionUpdate(route: number, step: number): void {
    if (route < this.routes.length && step < this.routes[route].path.length) {
      this.truckPosition = this.routes[route].path[step];
      this.renderGrid();
    }
  }

  private onAnimationComplete(): void {
    this.truckPosition = null;
    this.renderGrid();
  }

  // Grid management methods
  setMode(mode: typeof this.mode): void {
    this.mode = mode;
    this.tunnelStart = null;
    this.error = null; // Clear any previous error messages
    if (!this.showGrid) {
      this.showGrid = true;
      this.centerGrid();
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

    // Ensure roadblocks have cost 0 after resizing
    this.roadblocks.forEach((rb) => {
      const dirIndex = ["up", "down", "left", "right"].indexOf(rb.direction);
      if (
        dirIndex >= 0 &&
        this.trafficCosts[rb.from.y] &&
        this.trafficCosts[rb.from.y][rb.from.x]
      ) {
        this.trafficCosts[rb.from.y][rb.from.x][dirIndex] = 0;
      }
    });

    // Re-center the grid
    this.centerGrid();

    // Update canvas size
    this.resizeCanvas();
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
        return isNaN(num) || num < 0 ? 0 : num;
      });

      if (costs.length === 4) {
        this.trafficCosts[pos.y][pos.x] = costs;
        this.routes = [];
        this.renderGrid();
      } else {
        this.error =
          "Please enter exactly 4 comma-separated numbers for up, down, left, right costs";
      }
    }
  }

  editEdgeCost(from: Position, direction: string): void {
    const dirIndex = ["up", "down", "left", "right"].indexOf(direction);
    const currentCost = this.trafficCosts[from.y][from.x][dirIndex];
    const costInput = prompt(
      `Enter cost for ${direction} from cell (${from.x}, ${from.y}):\nCurrent: ${currentCost}`,
      currentCost.toString()
    );

    if (costInput !== null) {
      const num = parseInt(costInput.trim());
      if (!isNaN(num) && num >= 0) {
        this.trafficCosts[from.y][from.x][dirIndex] = num;
        this.routes = [];
        this.renderGrid();
      } else {
        this.error = "Please enter a valid non-negative number";
      }
    }
  }

  // Missing methods for HTML template
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
      const gridConfig = {
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

        // Ensure roadblocks have cost 0
        this.roadblocks.forEach((rb) => {
          const dirIndex = ["up", "down", "left", "right"].indexOf(
            rb.direction
          );
          if (
            dirIndex >= 0 &&
            this.trafficCosts[rb.from.y] &&
            this.trafficCosts[rb.from.y][rb.from.x]
          ) {
            this.trafficCosts[rb.from.y][rb.from.x][dirIndex] = 0;
          }
        });

        this.routes = [];
        this.error = null;
        if (!this.showGrid) {
          this.showGrid = true;
          this.centerGrid();
        }
      } catch (err) {
        this.error = "Invalid grid file format";
        console.error("Import error:", err);
      }
    };

    reader.readAsText(file);
    input.value = "";
  }

  // Animation property getters that delegate to service
  get isAnimating(): boolean {
    return this.animationService.isAnimating;
  }

  get currentAnimatingRoute(): number {
    return this.animationService.currentAnimatingRoute;
  }

  get animationSpeed(): number {
    return this.animationService.animationSpeed;
  }

  set animationSpeed(value: number) {
    this.animationService.animationSpeed = value;
  }
}
