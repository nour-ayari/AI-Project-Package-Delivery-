# Delivery Planner Web UI

An Angular-based web interface for the AI Package Delivery Planner project. This interactive application allows users to visually design delivery networks on a grid-based city layout and compute optimal routes using various search algorithms.

## Features

- **Interactive Canvas Grid**: Visual grid-based interface for planning delivery routes with zoom and pan support
- **Grid Elements**: Add stores (pickup locations), destinations (delivery points), roadblocks (blocked cells), tunnels (shortcuts), and custom traffic costs
- **Traffic Cost Management**: Set movement costs between adjacent cells and visualize traffic patterns
- **Search Algorithm Selection**: Choose from BFS, DFS, UCS, A\*, and Greedy algorithms for route optimization
- **Route Animation**: Watch animated truck movements along calculated delivery routes with adjustable speed
- **Real-time Results**: View detailed route information including costs, expanded nodes, and path sequences
- **Grid Size Control**: Adjust grid dimensions from 3x3 to 20x20 cells
- **Theme Switching**: Toggle between light and dark themes
- **Export/Import**: Save and load grid configurations as JSON files
- **Cost Editing**: Interactive cost modification for cells and edges
- **Random Grid Generation**: Automatically generate random grid configurations for testing

## Prerequisites

- Node.js (v18.19.0 or higher)
- npm (v10.2.3 or higher)
- Angular CLI (v19.2.10 or higher)
- Java 17+ and Maven (for the backend API)

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

Run the development server with proxy configuration:

```bash
npm start
```

The application will start on `http://localhost:4200` with automatic proxying to the backend API at `http://localhost:8080`.

## Build

To build the project for production:

```bash
npm run build
```

The build artifacts will be stored in the `dist/delivery-planner-ui` directory.

## Backend Connection

The UI connects to the Spring Boot backend API through a development proxy configured in `proxy.conf.json`. The proxy forwards all `/api/*` requests to `http://localhost:8080`.

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

1. **Set Grid Size**: Adjust rows and columns (3-20) using the grid size controls
2. **Add Stores**: Click "Add Store" and click on grid cells to place up to 3 pickup locations (green circles with 'S')
3. **Add Destinations**: Click "Add Destination" and click on grid cells to place up to 10 delivery points (red squares with 'D')
4. **Add Tunnels**: Click "Add Tunnel", then click two cells to create shortcuts between distant points
5. **Add Roadblocks**: Click "Add Roadblock" and click on cell edges to block movement
6. **Set Traffic Costs**: Click "Add Cost" and click between cells to modify movement costs
7. **Select Algorithm**: Choose from BFS, DFS, UCS, A\*, or Greedy search algorithms
8. **Plan Routes**: Click "Plan Routes" to calculate optimal delivery paths
9. **View Animation**: Watch the animated truck (🚚) deliver packages along computed routes
10. **Export/Import**: Save your grid configuration or load previously saved setups

## Project Structure

```
delivery-planner-ui/
├── src/
│   ├── app/
│   │   ├── components/
│   │   │   └── delivery-planner/
│   │   │       ├── delivery-planner.component.ts/html/css
│   │   │       └── services/
│   │   │           ├── animation.service.ts
│   │   │           ├── canvas-viewport.service.ts
│   │   │           ├── cost-editing.service.ts
│   │   │           ├── grid-generator.service.ts
│   │   │           ├── grid-interaction.service.ts
│   │   │           ├── grid-renderer.service.ts
│   │   │           ├── grid-state.service.ts
│   │   │           └── results.service.ts
│   │   ├── services/
│   │   │   └── delivery-planner.service.ts
│   │   └── app.component.ts/html/css
│   ├── assets/
│   ├── index.html
│   └── styles.css
├── proxy.conf.json
├── angular.json
├── package.json
└── tsconfig*.json
```

## Components & Services

- **DeliveryPlannerComponent**: Main component managing the grid interface and user interactions
- **GridRendererService**: Handles canvas drawing operations for grid visualization
- **GridInteractionService**: Manages mouse interactions and grid coordinate calculations
- **GridStateService**: Maintains application state including grid configuration and mode
- **AnimationService**: Controls route animation timing and sequencing
- **ResultsService**: Processes and displays algorithm results and route information
- **CanvasViewportService**: Manages zoom, pan, and coordinate transformations
- **CostEditingService**: Handles traffic cost modification interfaces
- **GridGeneratorService**: Creates random grid configurations
- **DeliveryPlannerService**: HTTP client for backend API communication

## Technologies

- **Angular 19.2.0**: Modern web framework with standalone components
- **TypeScript 5.7.2**: Type-safe JavaScript with advanced language features
- **Canvas API**: HTML5 canvas for high-performance grid rendering
- **RxJS 7.8.0**: Reactive programming for async operations
- **Angular Animations**: Smooth UI transitions and effects
- **Zone.js 0.15.0**: Execution context management for Angular

## Development

### Available Scripts

- `npm start`: Start development server with hot reload
- `npm run build`: Build for production
- `npm run watch`: Build in watch mode
- `npm test`: Run unit tests (when implemented)

### Proxy Configuration

The `proxy.conf.json` file configures the development server to proxy API requests:

```json
{
  "/api": {
    "target": "http://localhost:8080",
    "secure": false,
    "logLevel": "debug",
    "changeOrigin": true
  }
}
```

This allows the frontend to make API calls to relative `/api/*` endpoints during development.
