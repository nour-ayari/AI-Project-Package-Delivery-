# AI Package Delivery Planner - Complete Guide

This project includes both a **Swing UI** and a new **Angular Web UI** for the AI-powered package delivery planning system.

## Project Structure

```
AI-Project-Package-Delivery-/
├── src/                          # Java backend source code
│   ├── main/java/code/
│   │   ├── api/                  # REST API controllers
│   │   ├── dto/                  # Data Transfer Objects
│   │   ├── DeliveryPlanner.java  # Main delivery planning logic
│   │   ├── Grid.java             # Grid data structure
│   │   └── ...                   # Other Java classes
│   └── main/resources/
│       └── application.properties
├── delivery-planner-ui/          # Angular Web UI (NEW)
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/
│   │   │   │   └── delivery-planner/
│   │   │   └── services/
│   │   └── styles.css
│   ├── package.json
│   └── angular.json
├── pom.xml                       # Maven configuration
├── start-backend.ps1             # Script to start backend
└── start-frontend.ps1            # Script to start frontend
```

## Features

### Backend (Spring Boot + Java)

- **Search Algorithms**: BFS, DFS, UCS, A\*, Greedy
- **Grid-based Routing**: Navigate through a customizable grid
- **Obstacles**: Tunnels (shortcuts) and roadblocks
- **Traffic Costs**: Variable costs for each direction per cell
- **REST API**: HTTP endpoints for planning delivery routes

### Web UI (Angular)

- **Interactive Canvas**: Visual grid editor with zoom and pan
- **Grid Tools**: Add stores (purple circles with "S"), destinations (red squares with "D"), tunnels, and roadblocks
- **Strategy Selection**: Choose different search algorithms
- **Route Visualization**: See optimal paths drawn on the canvas one at a time during animation
- **Animated Delivery**: Watch truck move along routes sequentially, showing one route at a time
- **Individual Route Playback**: Click any route result to replay its specific animation
- **Responsive Design**: Works on desktop and mobile
- **Themed UI**: Matches the graph coloring example styling

### Original Swing UI (Desktop)

- **Desktop Application**: Standalone Java GUI
- **Real-time Visualization**: Watch the search algorithm in action
- **Step-by-step Execution**: See how nodes are explored

## Prerequisites

- **Java**: JDK 17 or higher
- **Maven**: 3.6 or higher
- **Node.js**: v18 or higher (for Web UI)
- **npm**: v9 or higher (for Web UI)

## Quick Start

### Option 1: Use PowerShell Scripts (Easiest)

#### Start Backend:

```powershell
.\start-backend.ps1
```

The backend will run on `http://localhost:8080`

#### Start Frontend:

```powershell
.\start-frontend.ps1
```

The web UI will open on `http://localhost:4200`

### Option 2: Manual Commands

#### Start Backend:

```bash
# Option A: Spring Boot (for Web UI)
mvn spring-boot:run

# Option B: Swing UI (desktop)
mvn exec:java
```

#### Start Frontend:

```bash
cd delivery-planner-ui
npm install
npm start
```

## Using the Web UI

1. **Open Browser**: Navigate to `http://localhost:4200`

2. **Build Your Grid**:

   - Click "Add Store" and place pickup locations (green circles with 'S')
   - Click "Add Destination" and place delivery points (red squares with 'D')
   - Optionally add tunnels (shortcuts) or roadblocks

3. **Choose Strategy**: Select from BFS, DFS, UCS, A\*, or Greedy

4. **Plan Routes**: Click "Plan Routes" to calculate optimal delivery paths

5. **View Results**: Routes will be drawn on the canvas with different colors

6. **Export/Import**: Save your grid configuration or load a saved one

## Using the Swing UI

Run the desktop application:

```bash
mvn exec:java
```

The Swing UI provides similar functionality but runs as a desktop application with real-time visualization of the search algorithms.

## API Endpoints

### Backend REST API

- **POST** `/api/delivery/plan`

  - Body: `{ grid: GridConfig, strategy: string }`
  - Returns: List of delivery routes with paths and costs

- **GET** `/api/delivery/check`
  - Returns: Service status message

## Configuration

### Backend Configuration

Edit `src/main/resources/application.properties`:

```properties
server.port=8080
spring.application.name=delivery-planner
```

### Frontend Configuration

Edit `delivery-planner-ui/src/app/services/delivery-planner.service.ts`:

```typescript
private apiUrl = 'http://localhost:8080/api/delivery';
```

## Grid Configuration Format

When exporting, the grid is saved as JSON:

```json
{
  "rows": 10,
  "cols": 10,
  "stores": [{"x": 0, "y": 0}],
  "destinations": [{"x": 9, "y": 9}],
  "tunnels": [{"start": {"x": 2, "y": 2}, "end": {"x": 7, "y": 7}, "cost": 5}],
  "roadblocks": [{"from": {"x": 5, "y": 5}, "direction": "up"}],
  "trafficCosts": [[[1,1,1,1], ...]]
}
```

## Search Algorithms

- **BFS (Breadth-First Search)**: Explores level by level, finds shortest path
- **DFS (Depth-First Search)**: Explores deeply before backtracking
- **UCS (Uniform Cost Search)**: Considers edge costs, finds lowest-cost path
- **A\* (A-Star)**: Uses heuristics for efficient pathfinding
- **Greedy**: Always moves toward goal, may not find optimal path

## Building for Production

### Backend:

```bash
mvn clean package
java -jar target/ai-delivery-1.0-SNAPSHOT.jar
```

### Frontend:

```bash
cd delivery-planner-ui
npm run build
# Deploy the dist/ folder to a web server
```

## Testing

Run backend tests:

```bash
mvn test
```

Run specific test:

```bash
mvn test -Dtest=DeliveryPlannerTest
```

## UI Styling

The Web UI uses the same styling as the graph coloring example:

- **Colors**: Matching color scheme with CSS variables
- **Animations**: Fade, slide, bounce, and shake effects
- **Responsive**: Mobile-friendly layout
- **Dark/Light Theme**: Theme preference saved in localStorage

## Troubleshooting

### Backend won't start:

- Check if Java 17+ is installed: `java -version`
- Check if Maven is installed: `mvn -version`
- Check if port 8080 is available

### Frontend won't start:

- Check if Node.js is installed: `node -version`
- Check if npm is installed: `npm -version`
- Delete `node_modules` and run `npm install` again
- Check if port 4200 is available

### CORS Errors:

- The backend has `@CrossOrigin(origins = "*")` enabled
- If issues persist, check browser console for specific errors

### Routes not displaying:

- Ensure backend is running on port 8080
- Check browser console for API errors
- Verify grid has both stores and destinations

## Development

### Adding New Features:

1. Backend: Add endpoints in `DeliveryPlannerController.java`
2. Frontend: Update `delivery-planner.service.ts` and component
3. Test changes locally before committing
