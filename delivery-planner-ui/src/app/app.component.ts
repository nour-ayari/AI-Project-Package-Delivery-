import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import { DeliveryPlannerComponent } from "./components/delivery-planner/delivery-planner.component";

@Component({
  selector: "app-root",
  standalone: true,
  imports: [CommonModule, DeliveryPlannerComponent],
  templateUrl: "./app.component.html",
  styleUrl: "./app.component.css",
})
export class AppComponent {
  title = "Delivery Planner";
}
