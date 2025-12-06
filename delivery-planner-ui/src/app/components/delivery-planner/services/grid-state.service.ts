import { Injectable } from "@angular/core";
import {
  Position,
  TunnelConfig,
  RoadBlockConfig,
} from "../../../services/delivery-planner.service";

@Injectable()
export class GridStateService {
  gridRows = 10;
  gridCols = 10;
  cellSize = 50;

  stores: Position[] = [];
  destinations: Position[] = [];
  tunnels: TunnelConfig[] = [];
  roadblocks: RoadBlockConfig[] = [];
  trafficCosts: number[][][] = []; // [y][x][direction]

  // Grid visibility
  showGrid = false;

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

  // Error handling
  error: string | null = null;
  isLoading = false;

  constructor() {
    this.initializeTrafficCosts();
  }

  initializeTrafficCosts(): void {
    this.trafficCosts = [];
    for (let y = 0; y < this.gridRows; y++) {
      this.trafficCosts[y] = [];
      for (let x = 0; x < this.gridCols; x++) {
        this.trafficCosts[y][x] = [1, 1, 1, 1]; // [up, down, left, right]
      }
    }
  }

  setMode(mode: typeof this.mode): void {
    this.mode = mode;
    this.tunnelStart = null;
    this.error = null;
  }

  resetGrid(): void {
    this.stores = [];
    this.destinations = [];
    this.tunnels = [];
    this.roadblocks = [];
    this.error = null;
    this.initializeTrafficCosts();
  }

  onGridSizeChange(): void {
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
  }

  enforceRoadblockCosts(): void {
    this.normalizeRoadblocks();

    let fixedCount = 0;
    let alreadyCorrectCount = 0;

    this.roadblocks.forEach((rb, index) => {
      const dirIndex = ["up", "down", "left", "right"].indexOf(rb.direction);
      if (
        dirIndex >= 0 &&
        this.trafficCosts[rb.from.y] &&
        this.trafficCosts[rb.from.y][rb.from.x]
      ) {
        const currentCost = this.trafficCosts[rb.from.y][rb.from.x][dirIndex];
        const expectedCost = 0; 

        if (currentCost !== expectedCost) {
          this.trafficCosts[rb.from.y][rb.from.x][dirIndex] = expectedCost;
          fixedCount++;
        } else {
          alreadyCorrectCount++;
        }
      }
    });

    let postEnforceInvalid = 0;
    this.roadblocks.forEach((rb) => {
      const dirIndex = ["up", "down", "left", "right"].indexOf(rb.direction);
      if (
        dirIndex >= 0 &&
        this.trafficCosts[rb.from.y] &&
        this.trafficCosts[rb.from.y][rb.from.x]
      ) {
        const cost = this.trafficCosts[rb.from.y][rb.from.x][dirIndex];
        if (cost !== 0) {
          postEnforceInvalid++;
        }
      }
    });
  }

  private normalizeRoadblocks(): void {
    let normalizedCount = 0;

    this.roadblocks = this.roadblocks.map((rb) => {
      let normalized = { ...rb };

      // For vertical walls (left/right), ensure we use "right" direction from left cell
      if (rb.direction === "left" && rb.from.x > 0) {
        // Convert "left" wall of cell (x,y) to "right" wall of cell (x-1,y)
        normalized = {
          from: { x: rb.from.x - 1, y: rb.from.y },
          direction: "right" as const,
        };
        normalizedCount++;
      }
      // For horizontal walls (up/down), ensure we use "down" direction from top cell
      else if (rb.direction === "up" && rb.from.y > 0) {
        // Convert "up" wall of cell (x,y) to "down" wall of cell (x,y-1)
        normalized = {
          from: { x: rb.from.x, y: rb.from.y - 1 },
          direction: "down" as const,
        };
        normalizedCount++;
      }

      return normalized;
    });

    // Remove duplicates that might have been created by normalization
    const uniqueRoadblocks = this.roadblocks.filter((rb, index, arr) => {
      return (
        arr.findIndex(
          (other) =>
            other.from.x === rb.from.x &&
            other.from.y === rb.from.y &&
            other.direction === rb.direction
        ) === index
      );
    });

    if (uniqueRoadblocks.length !== this.roadblocks.length) {
      this.roadblocks = uniqueRoadblocks;
    }
  }

  exportGrid(): any {
    return {
      rows: this.gridRows,
      cols: this.gridCols,
      stores: this.stores,
      destinations: this.destinations,
      tunnels: this.tunnels,
      roadblocks: this.roadblocks,
      trafficCosts: this.trafficCosts,
    };
  }

  importGrid(data: any): void {
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

    this.error = null;
  }
}
