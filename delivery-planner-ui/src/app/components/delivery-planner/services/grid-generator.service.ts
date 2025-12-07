import { Injectable } from "@angular/core";
import {
  Position,
  TunnelConfig,
  RoadBlockConfig,
} from "../../../services/delivery-planner.service";

@Injectable()
export class GridGeneratorService {
  generateRandomGrid(
    gridRows: number,
    gridCols: number
  ): {
    stores: Position[];
    destinations: Position[];
    tunnels: TunnelConfig[];
    roadblocks: RoadBlockConfig[];
    trafficCosts: number[][][];
  } {
    const stores: Position[] = [];
    const destinations: Position[] = [];
    const tunnels: TunnelConfig[] = [];
    const roadblocks: RoadBlockConfig[] = [];
    const trafficCosts: number[][][] = [];

    // Initialize traffic costs
    for (let y = 0; y < gridRows; y++) {
      trafficCosts[y] = [];
      for (let x = 0; x < gridCols; x++) {
        trafficCosts[y][x] = [1, 1, 1, 1]; // [up, down, left, right]
      }
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
          x: Math.floor(Math.random() * gridCols),
          y: Math.floor(Math.random() * gridRows),
        };
      } while (this.findItemAtPosition(pos, stores));
      stores.push(pos);
    }

    // Generate random destinations
    for (let i = 0; i < numDestinations; i++) {
      let pos: Position;
      do {
        pos = {
          x: Math.floor(Math.random() * gridCols),
          y: Math.floor(Math.random() * gridRows),
        };
      } while (
        this.findItemAtPosition(pos, stores) ||
        this.findItemAtPosition(pos, destinations)
      );
      destinations.push(pos);
    }

    // Generate random tunnels
    for (let i = 0; i < numTunnels; i++) {
      let start: Position, end: Position;
      do {
        start = {
          x: Math.floor(Math.random() * gridCols),
          y: Math.floor(Math.random() * gridRows),
        };
        end = {
          x: Math.floor(Math.random() * gridCols),
          y: Math.floor(Math.random() * gridRows),
        };
      } while (
        (start.x === end.x && start.y === end.y) ||
        this.findItemAtPosition(start, stores) ||
        this.findItemAtPosition(start, destinations) ||
        this.findItemAtPosition(end, stores) ||
        this.findItemAtPosition(end, destinations)
      );

      tunnels.push({
        start,
        end,
      });
    }

    // Generate random roadblocks FIRST
    for (let i = 0; i < numRoadblocks; i++) {
      let pos: Position;
      const directions = ["up", "down", "left", "right"];
      do {
        pos = {
          x: Math.floor(Math.random() * gridCols),
          y: Math.floor(Math.random() * gridRows),
        };
      } while (
        this.findItemAtPosition(pos, stores) ||
        this.findItemAtPosition(pos, destinations)
      );

      // Block random directions
      const numDirections = Math.floor(Math.random() * 3) + 1; // 1-3 directions
      const shuffledDirections = directions.sort(() => 0.5 - Math.random());
      for (let j = 0; j < numDirections; j++) {
        const direction = shuffledDirections[j];
        roadblocks.push({ from: pos, direction });

        // IMPORTANT: Set traffic cost to 0 for blocked direction
        const dirIndex = ["up", "down", "left", "right"].indexOf(direction);
        trafficCosts[pos.y][pos.x][dirIndex] = 0;
      }
    }

    // Add some random traffic costs (skip directions with roadblocks)
    for (let y = 0; y < gridRows; y++) {
      for (let x = 0; x < gridCols; x++) {
        if (Math.random() < 0.3) {
          // 30% chance to have custom costs
          const newCosts = [
            Math.floor(Math.random() * 4) + 1, // up 1-4
            Math.floor(Math.random() * 4) + 1, // down 1-4
            Math.floor(Math.random() * 4) + 1, // left 1-4
            Math.floor(Math.random() * 4) + 1, // right 1-4
          ];

          // Don't override roadblock costs (which are 0)
          for (let dir = 0; dir < 4; dir++) {
            const directionName = ["up", "down", "left", "right"][dir];
            const hasRoadblock = roadblocks.some(
              (rb) =>
                rb.from.x === x &&
                rb.from.y === y &&
                rb.direction === directionName
            );
            if (!hasRoadblock) {
              trafficCosts[y][x][dir] = newCosts[dir];
            }
          }
        }
      }
    }

    return { stores, destinations, tunnels, roadblocks, trafficCosts };
  }

  private findItemAtPosition(
    pos: Position,
    list: Position[]
  ): Position | undefined {
    return list.find((item) => item.x === pos.x && item.y === pos.y);
  }
}
