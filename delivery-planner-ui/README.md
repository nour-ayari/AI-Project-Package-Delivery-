# Delivery Planner Web UI

An Angular-based web interface for the AI Package Delivery Planner project.

## Features

- **Interactive Grid**: Visual grid-based interface for planning delivery routes
- **Multiple Tools**: Add stores, destinations, tunnels, and roadblocks
- **Search Strategies**: Choose from BFS, DFS, UCS, A\*, or Greedy algorithms
- **Route Visualization**: See optimal delivery paths drawn on the canvas
- **Export/Import**: Save and load grid configurations as JSON
- **Responsive Design**: Works on desktop and mobile devices
- **Dark/Light Theme**: Matches the graph coloring UI styling

## Prerequisites

- Node.js (v18 or higher)
- npm (v9 or higher)
- Angular CLI (v19 or higher)

## Installation

1. Navigate to the delivery-planner-ui directory:

```bash
cd delivery-planner-ui
```

2. Install dependencies:

```bash
npm install
```

## Development Server

Run the development server:

```bash
npm start
```

Navigate to `http://localhost:4200/` in your browser. The application will automatically reload if you change any of the source files.

## Build

To build the project for production:

```bash
npm run build
```

The build artifacts will be stored in the `dist/` directory.

## Backend Connection

The UI connects to the backend API at `http://localhost:8080/api/delivery`. Make sure the Spring Boot backend is running before using the UI.

To start the backend:

```bash
# From the project root directory
mvn spring-boot:run
```

Or use the provided PowerShell script:

```powershell
.\start-backend.ps1
```

## How to Use

1. **Add Stores**: Click "Add Store" and click on grid cells to place pickup locations (green circles with 'S')
2. **Add Destinations**: Click "Add Destination" and click on grid cells to place delivery points (red squares with 'D')
3. **Add Tunnels**: Click "Add Tunnel", then click two cells to create a shortcut between them
4. **Add Roadblocks**: Click "Add Roadblock" and click cells to block them
5. **Select Strategy**: Choose a search algorithm (BFS, DFS, UCS, A\*, Greedy)
6. **Plan Routes**: Click "Plan Routes" to calculate optimal delivery paths
7. **Export/Import**: Save your grid configuration or load a saved one

## UI Styling

The UI uses the same styling as the graph coloring component from the front-example folder:

- Consistent color scheme
- Same button styles and animations
- Matching layout and responsive design
- Dark/light theme support

## Components

- **DeliveryPlannerComponent**: Main component with canvas-based grid visualization
- **DeliveryPlannerService**: HTTP service for backend communication

## Technologies

- Angular 19
- TypeScript 5.7
- Canvas API for visualization
- RxJS for reactive programming
- Angular Animations
