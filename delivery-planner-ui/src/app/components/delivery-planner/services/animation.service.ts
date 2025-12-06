import { Injectable } from "@angular/core";
import { DeliveryRoute } from "../../../services/delivery-planner.service";

@Injectable()
export class AnimationService {
  private animationTimer: any = null;
  public isAnimating = false;
  public currentAnimatingRoute = 0;
  private currentAnimatingStep = 0;
  public animationSpeed = 200; // milliseconds per step

  // Callbacks
  private onPositionUpdate?: (route: number, step: number) => void;
  private onAnimationComplete?: () => void;

  setCallbacks(
    onPositionUpdate: (route: number, step: number) => void,
    onAnimationComplete: () => void
  ): void {
    this.onPositionUpdate = onPositionUpdate;
    this.onAnimationComplete = onAnimationComplete;
  }

  setAnimationSpeed(speed: number): void {
    this.animationSpeed = speed;
  }

  isAnimationActive(): boolean {
    return this.isAnimating;
  }

  getCurrentRoute(): number {
    return this.currentAnimatingRoute;
  }

  getCurrentStep(): number {
    return this.currentAnimatingStep;
  }

  startRouteAnimation(routes: DeliveryRoute[]): void {
    console.log("Starting route animation");
    this.isAnimating = true;
    this.currentAnimatingRoute = 0;
    this.currentAnimatingStep = 0;
    this.animateNextStep(routes);
  }

  stopRouteAnimation(): void {
    console.log("Stopping route animation");
    this.isAnimating = false;
    if (this.animationTimer) {
      clearTimeout(this.animationTimer);
      this.animationTimer = null;
    }
    if (this.onAnimationComplete) {
      this.onAnimationComplete();
    }
  }

  resetAnimation(): void {
    this.stopRouteAnimation();
    this.currentAnimatingRoute = 0;
    this.currentAnimatingStep = 0;
  }

  animateSpecificRoute(routeIndex: number, routes: DeliveryRoute[]): void {
    console.log(`Starting animation for route ${routeIndex + 1}`);
    this.stopRouteAnimation(); // Stop any current animation

    if (routeIndex < 0 || routeIndex >= routes.length) {
      console.error(`Invalid route index: ${routeIndex}`);
      return;
    }

    this.isAnimating = true;
    this.currentAnimatingRoute = routeIndex;
    this.currentAnimatingStep = 0;
    this.animateSingleRoute(routes);
  }

  private animateNextStep(routes: DeliveryRoute[]): void {
    if (!this.isAnimating || routes.length === 0) {
      return;
    }

    // Check if we're done with all routes
    if (this.currentAnimatingRoute >= routes.length) {
      console.log("Animation complete for all routes");
      this.stopRouteAnimation();
      return;
    }

    const currentRoute = routes[this.currentAnimatingRoute];

    // Check if current route is complete
    if (this.currentAnimatingStep >= currentRoute.path.length) {
      console.log(
        `Route ${this.currentAnimatingRoute + 1}/${routes.length} complete`
      );

      // Move to next route
      this.currentAnimatingRoute++;
      this.currentAnimatingStep = 0;

      // Pause between routes
      this.animationTimer = setTimeout(() => {
        this.animateNextStep(routes);
      }, 1000); // 1 second pause between routes

      return;
    }

    // Update position
    if (this.onPositionUpdate) {
      this.onPositionUpdate(
        this.currentAnimatingRoute,
        this.currentAnimatingStep
      );
    }

    this.currentAnimatingStep++;

    // Schedule next step
    this.animationTimer = setTimeout(() => {
      this.animateNextStep(routes);
    }, this.animationSpeed);
  }

  private animateSingleRoute(routes: DeliveryRoute[]): void {
    if (!this.isAnimating || this.currentAnimatingRoute >= routes.length) {
      return;
    }

    const currentRoute = routes[this.currentAnimatingRoute];

    // Check if current route is complete
    if (this.currentAnimatingStep >= currentRoute.path.length) {
      console.log(`Route ${this.currentAnimatingRoute + 1} animation complete`);
      this.stopRouteAnimation();
      return;
    }

    // Update position
    if (this.onPositionUpdate) {
      this.onPositionUpdate(
        this.currentAnimatingRoute,
        this.currentAnimatingStep
      );
    }

    this.currentAnimatingStep++;

    // Schedule next step
    this.animationTimer = setTimeout(() => {
      this.animateSingleRoute(routes);
    }, this.animationSpeed);
  }
}
