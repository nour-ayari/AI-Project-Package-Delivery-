import { Injectable } from "@angular/core";
import {
  Position,
  RoadBlockConfig,
} from "../../../services/delivery-planner.service";

@Injectable()
export class CostEditingService {
  // Cost editing state
  isEditingCellCost = false;
  editingCellPosition: Position | null = null;
  editingCellCosts = [1, 1, 1, 1]; // [up, down, left, right]

  isEditingEdgeCost = false;
  editingEdgeFrom: Position | null = null;
  editingEdgeDirection = "";
  editingEdgeCost = 1;

  constructor() {}

  editCellCost(pos: Position, trafficCosts: number[][][]): void {
    this.editingCellPosition = pos;
    this.editingCellCosts = [...trafficCosts[pos.y][pos.x]];
    this.isEditingCellCost = true;
  }

  saveCellCost(trafficCosts: number[][][]): void {
    if (this.editingCellPosition) {
      trafficCosts[this.editingCellPosition.y][this.editingCellPosition.x] = [
        ...this.editingCellCosts,
      ];
    }
    this.cancelCellCostEdit();
  }

  cancelCellCostEdit(): void {
    this.isEditingCellCost = false;
    this.editingCellPosition = null;
  }

  editEdgeCost(
    from: Position,
    direction: string,
    trafficCosts: number[][][],
    roadblocks: RoadBlockConfig[]
  ): string | null {
    // Check if there's a roadblock for this direction - if so, don't allow editing
    const hasRoadblock = roadblocks.some(
      (rb) =>
        rb.from.x === from.x &&
        rb.from.y === from.y &&
        rb.direction === direction
    );

    console.log(`🔍 Roadblock check for (${from.x},${from.y}) ${direction}:`);
    console.log(`   Total roadblocks: ${roadblocks.length}`);
    roadblocks.forEach((rb, index) => {
      console.log(
        `   Roadblock ${index}: (${rb.from.x},${rb.from.y}) ${rb.direction}`
      );
    });
    console.log(`   Exact match found: ${hasRoadblock}`);

    if (hasRoadblock) {
      return "Cannot change cost for blocked roads. Remove the roadblock first.";
    }

    const dirIndex = ["up", "down", "left", "right"].indexOf(direction);
    const currentCost = trafficCosts[from.y][from.x][dirIndex];

    console.log(
      `📝 editEdgeCost called for (${from.x},${from.y}) ${direction}`
    );
    console.log(`   dirIndex: ${dirIndex}`);
    console.log(
      `   Array access: trafficCosts[${from.y}][${from.x}][${dirIndex}] = ${currentCost}`
    );
    console.log(`   Has roadblock: ${hasRoadblock}`);

    this.editingEdgeFrom = from;
    this.editingEdgeDirection = direction;
    this.editingEdgeCost = currentCost;
    this.isEditingEdgeCost = true;

    return null; // No error
  }

  saveEdgeCost(
    trafficCosts: number[][][],
    roadblocks: RoadBlockConfig[]
  ): { error: string | null; roadblocks: RoadBlockConfig[] } {
    if (this.editingEdgeFrom) {
      // Validate cost is between 1-4
      if (this.editingEdgeCost < 1 || this.editingEdgeCost > 4) {
        return { error: "Edge cost must be between 1 and 4", roadblocks };
      }

      const dirIndex = ["up", "down", "left", "right"].indexOf(
        this.editingEdgeDirection
      );
      trafficCosts[this.editingEdgeFrom.y][this.editingEdgeFrom.x][dirIndex] =
        this.editingEdgeCost;

      // If we set a cost > 0, remove any roadblock for this direction
      if (this.editingEdgeCost > 0) {
        roadblocks = roadblocks.filter(
          (rb) =>
            !(
              rb.from.x === this.editingEdgeFrom!.x &&
              rb.from.y === this.editingEdgeFrom!.y &&
              rb.direction === this.editingEdgeDirection
            )
        );
      }
    }
    this.cancelEdgeCostEdit();
    return { error: null, roadblocks };
  }

  cancelEdgeCostEdit(): void {
    this.isEditingEdgeCost = false;
    this.editingEdgeFrom = null;
    this.editingEdgeDirection = "";
  }
}
