# AI Package Delivery System

## Project Overview

The **AI Package Delivery System** is a simulation of a delivery service operating in a city modeled as a 2D grid.  
The system uses **search algorithms** to plan optimal routes for delivery trucks that transport packages from multiple stores to multiple customer destinations.  

Key features:  

- Roads have **traffic costs** affecting travel time.  
- Some road segments can be **blocked**.  
- **Tunnels** allow shortcuts between distant points.  
- Supports multiple search strategies: BFS, DFS, Uniform Cost Search (UCS), Greedy, and A*.  
- Visualization and performance metrics to compare algorithms.  

This project can be used in **two ways**:  

1. **Desktop/Java Application**: Run simulations locally with a Java-based GUI.  
2. **Web Interface**: Angular-based interactive application for designing grids and visualizing delivery plans in the browser.  

---

## Desktop Application (Java GUI)

### Prerequisites

- Java 17+  
- Maven (for dependency management and running unit tests)  

### Installation & Setup

1. **Install Maven**: [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi)  
2. Extract Maven and add its `bin` folder to your system PATH.  
3. Verify installation:  

```bash
mvn -v
````

### Compile & Run

```bash
# Compile project
mvn compile

# Run the main application
mvn exec:java
```

### Unit Testing

* Place all test files under `src/test/java/code/`.
* Ensure JUnit dependency is in `pom.xml`.
* Run all tests:

```bash
mvn test
```

### Manual Compile & Run (Without Maven)

```bash
# Compile
javac -d target/classes src/main/java/code/*.java

# Run
java -cp target/classes code.Main
```

---

## Web Interface (Angular UI)

The web interface allows **interactive visualization and planning** of delivery routes in a browser.

### Features

* Interactive grid-based canvas for designing delivery networks
* Add stores, destinations, tunnels, and roadblocks
* Set and visualize traffic costs
* Choose and run search algorithms (BFS, DFS, UCS, A*, Greedy)
* Animate trucks delivering packages
* Export and import grid configurations (JSON)
* Grid sizes: 3x3 to 20x20 cells

### Prerequisites

* Node.js v18+
* npm v10+
* Angular CLI v19+
* Java 17+ & Maven (backend API)

### Setup

```bash
cd delivery-planner-ui
npm install
```

### Run Development Server

```bash
npm start
```

* Application available at `http://localhost:4200`
* Automatically proxies API requests to backend at `http://localhost:8080`

### Build for Production

```bash
npm run build
```

* Build artifacts stored in `dist/delivery-planner-ui`

### Backend

Start Spring Boot backend API:

```bash
mvn spring-boot:run
```

Or use the provided PowerShell script:

```powershell
.\start-backend.ps1
```

---

## How to Use (Web UI)

1. Set grid size (3–20 rows/columns)
2. Add stores (pickup points)
3. Add destinations (delivery points)
4. Add tunnels (shortcuts)
5. Add roadblocks (blocked edges)
6. Set traffic costs
7. Select a search algorithm
8. Click **Plan Routes**
9. Watch animated trucks deliver packages
10. Export or import grid configurations

---

## Technologies Used

* **Java 17** & **Maven** – backend logic & algorithms
* **Angular 19** & **TypeScript** – interactive web UI
* **Canvas API** – grid visualization
* **RxJS** – reactive programming
* **JUnit 5** – unit testing
* **Spring Boot** – backend API

