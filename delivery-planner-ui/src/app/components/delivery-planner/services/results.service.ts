import { Injectable } from "@angular/core";
import {
  DeliveryRoute,
  Position,
} from "../../../services/delivery-planner.service";

@Injectable()
export class ResultsService {
  // Results - store results from multiple algorithms
  routes: DeliveryRoute[] = [];
  algorithmResults: Map<string, { routes: DeliveryRoute[]; timestamp: Date }> =
    new Map();
  detailedResults: string = "";

  // Animation state
  truckPosition: Position | null = null;

  constructor() {}

  clearResults(): void {
    this.routes = [];
    this.algorithmResults.clear();
    this.truckPosition = null;
  }

  addAlgorithmResults(strategyName: string, routes: DeliveryRoute[]): void {
    this.algorithmResults.set(strategyName, {
      routes: routes,
      timestamp: new Date(),
    });

    // Combine all routes from all algorithms
    this.routes = [];
    this.algorithmResults.forEach((result) => {
      this.routes.push(...result.routes);
    });
  }

  getCurrentAlgorithmRoutes(selectedStrategy: string): DeliveryRoute[] {
    const currentAlgoResult = this.algorithmResults.get(selectedStrategy);
    return currentAlgoResult ? currentAlgoResult.routes : [];
  }

  updateDetailedResults(
    gridCols: number,
    gridRows: number,
    storesCount: number,
    destinationsCount: number,
    tunnelsCount: number,
    roadblocksCount: number
  ): void {
    let result = `Grid created: ${gridCols}x${gridRows}, Stores=${storesCount}, Destinations=${destinationsCount}, Tunnels=${tunnelsCount}, `;
    result += `Blocked roads: ${roadblocksCount}\n\n`;

    // Display results for each algorithm that has been run
    if (this.algorithmResults.size > 0) {
      // Sort algorithms in the order they were run
      const sortedAlgorithms = Array.from(this.algorithmResults.entries()).sort(
        (a, b) => a[1].timestamp.getTime() - b[1].timestamp.getTime()
      );

      sortedAlgorithms.forEach(([strategyName, algoResult]) => {
        const routes = algoResult.routes;

        result += `\n========================================\n`;
        result += `Algorithm: ${this.getAlgorithmDisplayName(strategyName)}\n`;
        result += `========================================\n\n`;

        // Group routes by store
        const routesByStore = new Map<string, DeliveryRoute[]>();
        routes.forEach((route) => {
          const storeKey = `(${route.store.x},${route.store.y})`;
          if (!routesByStore.has(storeKey)) {
            routesByStore.set(storeKey, []);
          }
          routesByStore.get(storeKey)!.push(route);
        });

        // Sort stores
        const sortedStores = Array.from(routesByStore.keys()).sort();

        sortedStores.forEach((storeKey) => {
          result += `------------------------------------ \n`;
          result += `TRUCK AT STORE ${storeKey} \n`;
          result += `------------------------------------ \n`;

          const storeRoutes = routesByStore.get(storeKey)!;
          storeRoutes.forEach((route) => {
            const plan = this.pathToDirections(route.path);
            result += `Delivers to :\n`;
            const expandedValue =
              route.expanded !== undefined &&
              route.expanded !== null &&
              route.expanded >= 0
                ? route.expanded
                : "N/A";
            result += `(${route.destination.x},${route.destination.y}) plan=${plan} | cost=${route.cost} | expanded=${expandedValue}\n`;
          });
          result += `\n`;
        });
      });
    }

    this.detailedResults = result;
  }

  private getAlgorithmDisplayName(strategy: string): string {
    switch (strategy) {
      case "BFS":
        return "BF Search";
      case "DFS":
        return "DF Search";
      case "UCS":
        return "UC Search";
      case "AStar":
        return "A* Search";
      case "Greedy":
        return "Greedy Search";
      default:
        return strategy + " Search";
    }
  }

  private pathToDirections(path: Position[]): string {
    if (path.length < 2) return "";

    const directions: string[] = [];
    for (let i = 1; i < path.length; i++) {
      const prev = path[i - 1];
      const curr = path[i];

      if (curr.x > prev.x) {
        directions.push("right");
      } else if (curr.x < prev.x) {
        directions.push("left");
      } else if (curr.y > prev.y) {
        directions.push("down");
      } else if (curr.y < prev.y) {
        directions.push("up");
      }
    }

    return directions.join(",");
  }

  setTruckPosition(position: Position | null): void {
    this.truckPosition = position;
  }

  mapStrategyToBackend(displayStrategy: string): string {
    switch (displayStrategy) {
      case "BFS":
        return "BF";
      case "DFS":
        return "DF";
      case "UCS":
        return "UC";
      case "AStar":
        return "AS1";
      case "Greedy":
        return "G1";
      default:
        return "BF"; // default fallback
    }
  }
}
