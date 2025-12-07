import { Injectable } from "@angular/core";
import {
  Position,
  RoadBlockConfig,
  TunnelConfig,
} from "../../../services/delivery-planner.service";
import { WallInfo } from "../interfaces/grid-cell.interface";

@Injectable()
export class GridInteractionService {
  /**
   * Detects which wall was clicked by finding the nearest edge/boundary between cells.
   * This method handles clicks near cell boundaries more accurately by considering
   * which actual wall (between two cells) the user intended to click.
   *
   * Normalizes wall coordinates to ensure the same physical wall always has the same representation:
   * - Vertical walls: always represented from the left cell's "right" direction
   * - Horizontal walls: always represented from the top cell's "down" direction
   */
  detectWallClick(
    x: number,
    y: number,
    cellSize: number,
    gridCols: number,
    gridRows: number
  ): WallInfo | null {
    // Find the nearest vertical grid line (x coordinate)
    const nearestVerticalLine = Math.round(x / cellSize);
    const distToVerticalLine = Math.abs(x - nearestVerticalLine * cellSize);

    // Find the nearest horizontal grid line (y coordinate)
    const nearestHorizontalLine = Math.round(y / cellSize);
    const distToHorizontalLine = Math.abs(y - nearestHorizontalLine * cellSize);

    // Threshold for edge detection (in pixels)
    const edgeThreshold = cellSize * 0.3; // 30% of cell size

    // Determine if click is close to a vertical or horizontal wall
    if (
      distToVerticalLine < edgeThreshold &&
      distToVerticalLine <= distToHorizontalLine
    ) {
      // Clicked near a vertical wall
      const leftCellX = nearestVerticalLine - 1;
      const rightCellX = nearestVerticalLine;
      const cellY = Math.floor(y / cellSize);

      // Normalize: always represent vertical walls from the left cell's "right" direction
      if (
        leftCellX >= 0 &&
        leftCellX < gridCols &&
        cellY >= 0 &&
        cellY < gridRows
      ) {
        // Wall is to the right of the left cell
        return { from: { x: leftCellX, y: cellY }, direction: "right" };
      } else if (
        rightCellX >= 0 &&
        rightCellX < gridCols &&
        cellY >= 0 &&
        cellY < gridRows
      ) {
        // This shouldn't happen with normalization, but fallback
        return { from: { x: rightCellX, y: cellY }, direction: "left" };
      }
    } else if (distToHorizontalLine < edgeThreshold) {
      // Clicked near a horizontal wall
      const topCellY = nearestHorizontalLine - 1;
      const bottomCellY = nearestHorizontalLine;
      const cellX = Math.floor(x / cellSize);

      // Normalize: always represent horizontal walls from the top cell's "down" direction
      if (
        topCellY >= 0 &&
        topCellY < gridRows &&
        cellX >= 0 &&
        cellX < gridCols
      ) {
        // Wall is below the top cell
        return { from: { x: cellX, y: topCellY }, direction: "down" };
      } else if (
        bottomCellY >= 0 &&
        bottomCellY < gridRows &&
        cellX >= 0 &&
        cellX < gridCols
      ) {
        // This shouldn't happen with normalization, but fallback
        return { from: { x: cellX, y: bottomCellY }, direction: "up" };
      }
    } else {
      // Click is not close to any edge, use fallback: closest edge of clicked cell
      const gridX = Math.floor(x / cellSize);
      const gridY = Math.floor(y / cellSize);

      if (gridX >= 0 && gridX < gridCols && gridY >= 0 && gridY < gridRows) {
        // Get position within the cell (0 to 1 range)
        const cellX = (x - gridX * cellSize) / cellSize;
        const cellY = (y - gridY * cellSize) / cellSize;

        // Determine which edge is closest
        const distToLeft = cellX;
        const distToRight = 1 - cellX;
        const distToTop = cellY;
        const distToBottom = 1 - cellY;

        const minDist = Math.min(
          distToLeft,
          distToRight,
          distToTop,
          distToBottom
        );

        let direction: "up" | "down" | "left" | "right";
        if (minDist === distToLeft) {
          // Normalize: represent left wall as right wall of cell to the left
          if (gridX > 0) {
            return { from: { x: gridX - 1, y: gridY }, direction: "right" };
          } else {
            direction = "left";
          }
        } else if (minDist === distToRight) {
          direction = "right";
        } else if (minDist === distToTop) {
          // Normalize: represent top wall as down wall of cell above
          if (gridY > 0) {
            return { from: { x: gridX, y: gridY - 1 }, direction: "down" };
          } else {
            direction = "up";
          }
        } else {
          direction = "down";
        }

        return { from: { x: gridX, y: gridY }, direction };
      }
    }

    return null;
  }

  findItemAtPosition(pos: Position, list: Position[]): Position | undefined {
    return list.find((item) => item.x === pos.x && item.y === pos.y);
  }

  deleteAtPosition(
    pos: Position,
    stores: Position[],
    destinations: Position[],
    tunnels: TunnelConfig[],
    roadblocks: RoadBlockConfig[]
  ): {
    stores: Position[];
    destinations: Position[];
    tunnels: TunnelConfig[];
    roadblocks: RoadBlockConfig[];
  } {
    // Remove stores
    const newStores = stores.filter((s) => s.x !== pos.x || s.y !== pos.y);

    // Remove destinations
    const newDestinations = destinations.filter(
      (d) => d.x !== pos.x || d.y !== pos.y
    );

    // Remove tunnels
    const newTunnels = tunnels.filter(
      (t) =>
        !(
          (t.start.x === pos.x && t.start.y === pos.y) ||
          (t.end.x === pos.x && t.end.y === pos.y)
        )
    );

    // Remove roadblocks
    const newRoadblocks = roadblocks.filter(
      (rb) => rb.from.x !== pos.x || rb.from.y !== pos.y
    );

    return {
      stores: newStores,
      destinations: newDestinations,
      tunnels: newTunnels,
      roadblocks: newRoadblocks,
    };
  }
}
