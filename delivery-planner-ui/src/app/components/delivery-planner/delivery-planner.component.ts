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
  Position,
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
import { GridStateService } from "./services/grid-state.service";
import { CanvasViewportService } from "./services/canvas-viewport.service";
import { ResultsService } from "./services/results.service";
import { CostEditingService } from "./services/cost-editing.service";

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
    GridStateService,
    CanvasViewportService,
    ResultsService,
    CostEditingService,
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
    trigger("slideInAnimation", [
      transition(":enter", [
        style({ transform: "translateY(-20px)", opacity: 0 }),
        animate(
          "300ms ease-out",
          style({ transform: "translateY(0)", opacity: 1 })
        ),
      ]),
      transition(":leave", [
        style({ transform: "translateY(0)", opacity: 1 }),
        animate(
          "300ms ease-in",
          style({ transform: "translateY(-20px)", opacity: 0 })
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
  private animationFrameId: number | null = null;

  // Theme
  isDarkTheme = false;

  constructor(
    private deliveryService: DeliveryPlannerService,
    private gridInteraction: GridInteractionService,
    private gridRenderer: GridRendererService,
    private gridGenerator: GridGeneratorService,
    private animationService: AnimationService,
    public gridState: GridStateService,
    public viewport: CanvasViewportService,
    public resultsService: ResultsService,
    public costEditing: CostEditingService
  ) {}

  ngOnInit(): void {
    // Load theme preference
    this.isDarkTheme = localStorage.getItem("deliveryPlannerTheme") === "dark";

    // Update detailed results
    this.updateDetailedResults();

    // Setup animation callbacks
    this.animationService.setCallbacks(
      (route: number, step: number) =>
        this.onAnimationPositionUpdate(route, step),
      () => this.onAnimationComplete(),
      (route: number) => this.onRouteComplete(route)
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
    const gridWidth = this.gridState.gridCols * this.gridState.cellSize;
    const gridHeight = this.gridState.gridRows * this.gridState.cellSize;

    this.viewport.centerGrid(
      canvas.width,
      canvas.height,
      gridWidth,
      gridHeight
    );
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
    const x =
      (e.clientX - rect.left) / this.viewport.getScale() -
      this.viewport.getOffset().x;
    const y =
      (e.clientY - rect.top) / this.viewport.getScale() -
      this.viewport.getOffset().y;

    this.processPointerDown(x, y);
    this.viewport.setLastMousePosition(e.clientX, e.clientY);
  }

  private handleMouseMove(e: MouseEvent): void {
    const clientX = e.clientX;
    const clientY = e.clientY;

    if (this.viewport.isDragging()) {
      const lastPos = this.viewport.getLastMousePosition();
      const dx = clientX - lastPos.x;
      const dy = clientY - lastPos.y;
      this.viewport.handlePan(dx, dy);
      this.viewport.setLastMousePosition(clientX, clientY);
    }
  }

  private handleMouseUp(): void {
    this.viewport.setDragging(false);
  }

  private handleWheel(e: WheelEvent): void {
    e.preventDefault();

    const rect = this.canvasRef.nativeElement.getBoundingClientRect();
    const mouseX = e.clientX - rect.left;
    const mouseY = e.clientY - rect.top;

    this.viewport.handleZoom(mouseX, mouseY, e.deltaY < 0);
  }

  private processPointerDown(x: number, y: number): void {
    const gridX = Math.floor(x / this.gridState.cellSize);
    const gridY = Math.floor(y / this.gridState.cellSize);

    if (
      gridX < 0 ||
      gridX >= this.gridState.gridCols ||
      gridY < 0 ||
      gridY >= this.gridState.gridRows
    ) {
      if (this.gridState.mode === "move") {
        this.viewport.setDragging(true);
      }
      return;
    }

    const pos: Position = { x: gridX, y: gridY };

    // Special handling for roadblock mode - detect which edge/wall was clicked
    if (this.gridState.mode === "add-roadblock") {
      const wallInfo = this.gridInteraction.detectWallClick(
        x,
        y,
        this.gridState.cellSize,
        this.gridState.gridCols,
        this.gridState.gridRows
      );
      if (wallInfo) {
        // Check if roadblock already exists
        const exists = this.gridState.roadblocks.some(
          (rb) =>
            rb.from.x === wallInfo.from.x &&
            rb.from.y === wallInfo.from.y &&
            rb.direction === wallInfo.direction
        );
        if (!exists) {
          this.gridState.roadblocks.push({
            from: wallInfo.from,
            direction: wallInfo.direction,
          });
          this.updateDetailedResults();
          const dirIndex = ["up", "down", "left", "right"].indexOf(
            wallInfo.direction
          );
          if (
            dirIndex >= 0 &&
            this.gridState.trafficCosts[wallInfo.from.y] &&
            this.gridState.trafficCosts[wallInfo.from.y][wallInfo.from.x]
          ) {
            this.gridState.trafficCosts[wallInfo.from.y][wallInfo.from.x][
              dirIndex
            ] = 0;
          }
        }
      }
      this.renderGrid();
      return;
    }

    if (this.gridState.mode === "delete") {
      const deletedRoadblocks = this.gridState.roadblocks.filter(
        (rb) => rb.from.x === pos.x && rb.from.y === pos.y
      );

      const result = this.gridInteraction.deleteAtPosition(
        pos,
        this.gridState.stores,
        this.gridState.destinations,
        this.gridState.tunnels,
        this.gridState.roadblocks
      );
      this.gridState.stores = result.stores;
      this.gridState.destinations = result.destinations;
      this.gridState.tunnels = result.tunnels;
      this.gridState.roadblocks = result.roadblocks;
      this.updateDetailedResults();

      // Reset traffic costs to 1 for deleted roadblocks
      deletedRoadblocks.forEach((rb) => {
        const dirIndex = ["up", "down", "left", "right"].indexOf(rb.direction);
        if (
          dirIndex >= 0 &&
          this.gridState.trafficCosts[rb.from.y] &&
          this.gridState.trafficCosts[rb.from.y][rb.from.x]
        ) {
          this.gridState.trafficCosts[rb.from.y][rb.from.x][dirIndex] = 1;
        }
      });
    } else if (this.gridState.mode === "add-store") {
      if (this.gridState.stores.length >= 3) {
        this.gridState.error = "Maximum 3 stores allowed";
        return;
      }
      if (
        !this.gridInteraction.findItemAtPosition(pos, this.gridState.stores)
      ) {
        this.gridState.stores.push(pos);
        this.updateDetailedResults();
        this.gridState.error = null; // Clear error on successful addition
      }
    } else if (this.gridState.mode === "add-destination") {
      if (this.gridState.destinations.length >= 10) {
        this.gridState.error = "Maximum 10 destinations allowed";
        return;
      }
      if (
        !this.gridInteraction.findItemAtPosition(
          pos,
          this.gridState.destinations
        )
      ) {
        this.gridState.destinations.push(pos);
        this.updateDetailedResults();
        this.gridState.error = null; // Clear error on successful addition
      }
    } else if (this.gridState.mode === "add-tunnel") {
      if (this.gridState.tunnelStart === null) {
        this.gridState.tunnelStart = pos;
      } else {
        if (
          this.gridState.tunnelStart.x !== pos.x ||
          this.gridState.tunnelStart.y !== pos.y
        ) {
          this.gridState.tunnels.push({
            start: this.gridState.tunnelStart,
            end: pos,
          });
          this.updateDetailedResults();
        }
        this.gridState.tunnelStart = null;
      }
    } else if (this.gridState.mode === "add-cost") {
      const wallInfo = this.gridInteraction.detectWallClick(
        x,
        y,
        this.gridState.cellSize,
        this.gridState.gridCols,
        this.gridState.gridRows
      );
      if (wallInfo) {
        this.editEdgeCost(wallInfo.from, wallInfo.direction);
      }
    } else if (this.gridState.mode === "move") {
      this.viewport.setDragging(true);
    }
  }

  private renderGrid(): void {
    if (!this.ctx) return;

    const canvas = this.canvasRef.nativeElement;
    this.ctx.clearRect(0, 0, canvas.width, canvas.height);

    if (!this.gridState.showGrid) {
      return;
    }

    // Save context
    this.ctx.save();

    // Apply transformations
    const offset = this.viewport.getOffset();
    const scale = this.viewport.getScale();
    this.ctx.translate(offset.x * scale, offset.y * scale);
    this.ctx.scale(scale, scale);

    // Draw grid components using renderer service
    this.gridRenderer.drawGridLines(
      this.gridState.gridRows,
      this.gridState.gridCols,
      this.gridState.cellSize
    );
    this.gridRenderer.drawTunnels(
      this.gridState.tunnels,
      this.gridState.cellSize
    );
    this.gridRenderer.drawTrafficCosts(
      this.gridState.trafficCosts,
      this.gridState.gridRows,
      this.gridState.gridCols,
      this.gridState.cellSize
    );

    // Draw routes if any
    if (this.resultsService.routes.length > 0) {
      this.gridRenderer.drawRoutes(
        this.resultsService.routes,
        this.animationService.isAnimationActive(),
        this.animationService.getCurrentRoute(),
        this.animationService.getCurrentStep(),
        this.gridState.cellSize
      );
    }

    this.gridRenderer.drawStores(
      this.gridState.stores,
      this.gridState.cellSize
    );
    this.gridRenderer.drawDestinations(
      this.gridState.destinations,
      this.gridState.cellSize
    );
    this.gridRenderer.drawRoadblocks(
      this.gridState.roadblocks,
      this.gridState.gridRows,
      this.gridState.gridCols,
      this.gridState.cellSize,
      this.gridState.trafficCosts
    );

    // Draw tunnel start indicator if in tunnel mode
    if (this.gridState.mode === "add-tunnel") {
      this.gridRenderer.drawTunnelStartIndicator(
        this.gridState.tunnelStart,
        this.gridState.cellSize
      );
    }

    // Draw animated truck if animation is active
    if (
      this.animationService.isAnimationActive() &&
      this.resultsService.truckPosition
    ) {
      this.gridRenderer.drawTruck(
        this.resultsService.truckPosition,
        this.gridState.cellSize
      );
    }

    // Restore context
    this.ctx.restore();
  }

  generateRandomGrid(): void {
    this.gridState.resetGrid();
    if (!this.gridState.showGrid) {
      this.gridState.showGrid = true;
      this.centerGrid();
    }

    const result = this.gridGenerator.generateRandomGrid(
      this.gridState.gridRows,
      this.gridState.gridCols
    );

    // Verify roadblocks in received data
    let componentInvalidRoadblocks = 0;
    result.roadblocks.forEach((rb, index) => {
      const dirIndex = ["up", "down", "left", "right"].indexOf(rb.direction);
      const actualCost = result.trafficCosts[rb.from.y][rb.from.x][dirIndex];
      if (actualCost !== 0) {
        componentInvalidRoadblocks++;
      }
    });

    this.gridState.stores = result.stores;
    this.gridState.destinations = result.destinations;
    this.gridState.tunnels = result.tunnels;
    this.gridState.roadblocks = result.roadblocks;
    this.gridState.trafficCosts = result.trafficCosts;

    // Ensure all roadblocks have cost 0 in traffic costs
    this.gridState.enforceRoadblockCosts();

    // Update detailed results
    this.updateDetailedResults();
  }

  // Animation methods using service
  startRouteAnimation(): void {
    this.animationService.startRouteAnimation(this.resultsService.routes);
  }

  stopRouteAnimation(): void {
    this.animationService.stopRouteAnimation();
    this.resultsService.setTruckPosition(null);
    this.renderGrid();
  }

  resetAnimation(): void {
    this.animationService.resetAnimation();
    this.resultsService.setTruckPosition(null);
  }

  animateSpecificRoute(routeIndex: number): void {
    this.animationService.animateSpecificRoute(
      routeIndex,
      this.resultsService.routes
    );
  }

  // Animation callbacks
  private onAnimationPositionUpdate(route: number, step: number): void {
    if (
      route < this.resultsService.routes.length &&
      step < this.resultsService.routes[route].path.length
    ) {
      this.resultsService.setTruckPosition(
        this.resultsService.routes[route].path[step]
      );
      this.renderGrid();
    }
  }

  private onAnimationComplete(): void {
    this.resultsService.setTruckPosition(null);
    this.renderGrid();
  }

  private onRouteComplete(routeIndex: number): void {
    // When a route is complete, immediately move truck back to the store
    if (routeIndex < this.resultsService.routes.length) {
      const completedRoute = this.resultsService.routes[routeIndex];
      this.resultsService.setTruckPosition(completedRoute.store);
      this.renderGrid();
    }
  }

  // Grid management methods
  setMode(mode: typeof this.gridState.mode): void {
    this.gridState.setMode(mode);
    this.costEditing.isEditingCellCost = false;
    this.costEditing.isEditingEdgeCost = false;
    if (!this.gridState.showGrid) {
      this.gridState.showGrid = true;
      this.centerGrid();
    }
  }

  resetView(): void {
    this.viewport.resetView();
  }

  resetGrid(): void {
    this.gridState.resetGrid();
    this.resultsService.clearResults();
    this.stopRouteAnimation();
  }

  clearResults(): void {
    this.resultsService.clearResults();
    this.gridState.error = null;
    this.updateDetailedResults();
    this.stopRouteAnimation();
  }

  getCurrentAlgorithmRoutes(): DeliveryRoute[] {
    return this.resultsService.getCurrentAlgorithmRoutes(
      this.gridState.selectedStrategy
    );
  }

  isRouteActive(route: DeliveryRoute): boolean {
    return (
      this.animationService.isAnimationActive() &&
      this.animationService.getCurrentRoute() <
        this.resultsService.routes.length &&
      this.resultsService.routes[this.animationService.getCurrentRoute()] ===
        route
    );
  }

  animateRouteForCurrentAlgorithm(routeIndex: number): void {
    const currentAlgoRoutes = this.getCurrentAlgorithmRoutes();
    if (routeIndex < currentAlgoRoutes.length) {
      const route = currentAlgoRoutes[routeIndex];
      // Find the index of this route in the combined routes array
      const globalIndex = this.resultsService.routes.indexOf(route);
      if (globalIndex >= 0) {
        this.animateSpecificRoute(globalIndex);
      }
    }
  }

  onStrategyChange(): void {
    // Update detailed results when strategy changes
    this.updateDetailedResults();
  }

  onGridSizeChange(): void {
    this.gridState.onGridSizeChange();
    this.resultsService.routes = [];
    this.centerGrid();
    this.resizeCanvas();
    this.updateDetailedResults();
  }

  editCellCost(pos: Position): void {
    this.costEditing.editCellCost(pos, this.gridState.trafficCosts);
  }

  saveCellCost(): void {
    this.costEditing.saveCellCost(this.gridState.trafficCosts);
    this.resultsService.routes = [];
    this.renderGrid();
    this.updateDetailedResults();
  }

  cancelCellCostEdit(): void {
    this.costEditing.cancelCellCostEdit();
  }

  editEdgeCost(from: Position, direction: string): void {
    const error = this.costEditing.editEdgeCost(
      from,
      direction,
      this.gridState.trafficCosts,
      this.gridState.roadblocks
    );
    if (error) {
      this.gridState.error = error;
    }
  }

  saveEdgeCost(): void {
    const result = this.costEditing.saveEdgeCost(
      this.gridState.trafficCosts,
      this.gridState.roadblocks
    );
    if (result.error) {
      this.gridState.error = result.error;
      return;
    }
    this.gridState.roadblocks = result.roadblocks;
    this.resultsService.routes = [];
    this.renderGrid();
    this.updateDetailedResults();
  }

  cancelEdgeCostEdit(): void {
    this.costEditing.cancelEdgeCostEdit();
  }

  // Missing methods for HTML template
  async planDelivery(): Promise<void> {
    if (this.gridState.stores.length === 0) {
      this.gridState.error = "Please add at least one store";
      return;
    }

    if (this.gridState.destinations.length === 0) {
      this.gridState.error = "Please add at least one destination";
      return;
    }

    this.gridState.isLoading = true;
    this.gridState.error = null;
    // Don't clear routes here - we want to accumulate results

    try {
      const gridConfig = {
        rows: this.gridState.gridRows,
        cols: this.gridState.gridCols,
        traffic: this.gridState.trafficCosts,
        stores: this.gridState.stores,
        destinations: this.gridState.destinations,
        tunnels: this.gridState.tunnels,
        roadblocks: this.gridState.roadblocks,
      };

      const backendStrategy = this.resultsService.mapStrategyToBackend(
        this.gridState.selectedStrategy
      );

      const response = await this.deliveryService
        .planDelivery(gridConfig, backendStrategy)
        .toPromise();

      if (response && response.success) {
        const newRoutes = response.routes || [];

        if (newRoutes.length === 0) {
          this.gridState.error =
            "No routes found. Check if destinations are reachable.";
        } else {
          // Store results for this algorithm
          this.resultsService.addAlgorithmResults(
            this.gridState.selectedStrategy,
            newRoutes
          );

          // Update detailed results with planning info
          this.updateDetailedResults();
          // Start animating routes
          this.startRouteAnimation();
        }
      } else {
        this.gridState.error = response?.message || "Planning failed";
      }
    } catch (err: any) {
      this.gridState.error = `Error: ${err.message || "Unknown error"}`;
    } finally {
      this.gridState.isLoading = false;
    }
  }

  private updateDetailedResults(): void {
    this.resultsService.updateDetailedResults(
      this.gridState.gridCols,
      this.gridState.gridRows,
      this.gridState.stores.length,
      this.gridState.destinations.length,
      this.gridState.tunnels.length,
      this.gridState.roadblocks.length
    );
  }

  exportGrid(): void {
    const gridData = this.gridState.exportGrid();
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
        this.gridState.importGrid(data);
        this.resultsService.routes = [];
        if (!this.gridState.showGrid) {
          this.gridState.showGrid = true;
          this.centerGrid();
        }
        this.updateDetailedResults();
      } catch (err) {
        this.gridState.error = "Invalid grid file format";
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
