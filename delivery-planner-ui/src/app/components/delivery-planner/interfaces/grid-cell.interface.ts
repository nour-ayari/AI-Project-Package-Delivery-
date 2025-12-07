export interface GridCell {
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

export interface WallInfo {
  from: { x: number; y: number };
  direction: "up" | "down" | "left" | "right";
}
