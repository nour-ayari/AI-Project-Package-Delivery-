import { Injectable } from "@angular/core";

@Injectable()
export class CanvasViewportService {
  private scale: number = 1;
  private offset = { x: 0, y: 0 };
  private lastMousePosition = { x: 0, y: 0 };
  private isDraggingCanvas = false;

  constructor() {}

  getScale(): number {
    return this.scale;
  }

  getOffset(): { x: number; y: number } {
    return this.offset;
  }

  getLastMousePosition(): { x: number; y: number } {
    return this.lastMousePosition;
  }

  isDragging(): boolean {
    return this.isDraggingCanvas;
  }

  setDragging(dragging: boolean): void {
    this.isDraggingCanvas = dragging;
  }

  setLastMousePosition(x: number, y: number): void {
    this.lastMousePosition = { x, y };
  }

  centerGrid(
    canvasWidth: number,
    canvasHeight: number,
    gridWidth: number,
    gridHeight: number
  ): void {
    // Calculate offset to center the grid
    this.offset.x = (canvasWidth / this.scale - gridWidth) / 2;
    this.offset.y = (canvasHeight / this.scale - gridHeight) / 2;
  }

  resetView(): void {
    this.scale = 1;
    this.offset = { x: 0, y: 0 };
  }

  handlePan(dx: number, dy: number): void {
    this.offset.x += dx / this.scale;
    this.offset.y += dy / this.scale;
  }

  handleZoom(mouseX: number, mouseY: number, zoomIn: boolean): void {
    const worldX = mouseX / this.scale - this.offset.x;
    const worldY = mouseY / this.scale - this.offset.y;

    const zoomFactor = zoomIn ? 1.05 : 0.95;
    this.scale *= zoomFactor;

    this.scale = Math.min(Math.max(0.5, this.scale), 3);

    this.offset.x = mouseX / this.scale - worldX;
    this.offset.y = mouseY / this.scale - worldY;
  }

  screenToWorld(screenX: number, screenY: number): { x: number; y: number } {
    return {
      x: screenX / this.scale - this.offset.x,
      y: screenY / this.scale - this.offset.y,
    };
  }
}
