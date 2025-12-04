import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";

export interface Position {
  x: number;
  y: number;
}

export interface TunnelConfig {
  start: Position;
  end: Position;
  cost: number;
}

export interface RoadBlockConfig {
  from: Position;
  direction: string;
}

export interface GridConfig {
  rows: number;
  cols: number;
  traffic: number[][][]; // [y][x][direction] where direction: 0=up, 1=down, 2=left, 3=right
  stores: Position[];
  destinations: Position[];
  tunnels: TunnelConfig[];
  roadblocks: RoadBlockConfig[];
}

export interface DeliveryRoute {
  store: Position;
  destination: Position;
  path: Position[];
  cost: number;
}

export interface PlanningResponse {
  success: boolean;
  message: string;
  routes: DeliveryRoute[];
}

@Injectable({
  providedIn: "root",
})
export class DeliveryPlannerService {
  private apiUrl = "http://localhost:8080/api/delivery"; // Backend URL

  constructor(private http: HttpClient) {}

  planDelivery(
    grid: GridConfig,
    strategy: string
  ): Observable<PlanningResponse> {
    console.log("Planning delivery with strategy:", strategy);
    console.log("Grid config:", grid);
    return this.http.post<PlanningResponse>(`${this.apiUrl}/plan`, {
      grid,
      strategy,
    });
  }

  checkService(): Observable<string> {
    return this.http.get(`${this.apiUrl}/check`, { responseType: "text" });
  }
}
