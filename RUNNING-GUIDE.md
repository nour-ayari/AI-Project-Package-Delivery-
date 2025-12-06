# Running the Application - Frontend & Backend Connection Guide

## 🚀 Quick Start

### Step 1: Start the Backend (Spring Boot)

Open a **PowerShell terminal** in the project root directory and run:

```powershell
.\start-backend.ps1
```

**Or manually:**

```powershell
mvn spring-boot:run
```

✅ **Backend should start on:** `http://localhost:8080`

### Step 2: Start the Frontend (Angular)

Open a **NEW PowerShell terminal** in the project root directory and run:

```powershell
.\start-frontend.ps1
```

**Or manually:**

```powershell
cd delivery-planner-ui
npm start
```

✅ **Frontend should start on:** `http://localhost:4200`

### Step 3: Access the Application

Open your browser and navigate to:

```
http://localhost:4200
```

---

## 🔧 Configuration Details

### Backend Configuration

**File:** `src/main/resources/application.properties`

```properties
server.port=8080
spring.application.name=delivery-planner
```

**API Endpoints:**

- `POST /api/delivery/plan` - Plan delivery routes
- `GET /api/delivery/health` - Health check

**CORS:** Enabled for all origins (`@CrossOrigin(origins = "*")`)

### Frontend Configuration

**Proxy Configuration:** `delivery-planner-ui/proxy.conf.json`

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

**Service Configuration:** `delivery-planner-ui/src/app/services/delivery-planner.service.ts`

```typescript
private apiUrl = "/api/delivery"; 
```

**Start Script:** `delivery-planner-ui/package.json`

```json
"start": "ng serve --proxy-config proxy.conf.json"
```

---

##  Troubleshooting

### Issue: Backend won't start

**Error:** `Port 8080 is already in use`

**Solution:**

```powershell
# Find process using port 8080
netstat -ano | findstr :8080

# Kill the process (replace PID with actual process ID)
taskkill /PID <PID> /F
```

### Issue: Frontend can't connect to backend

**Checklist:**

1. Backend is running on port 8080
2. Frontend is using proxy configuration
3. Browser console shows no CORS errors
4. Check browser DevTools Network tab for API calls

**Test Backend Directly:**

```powershell
# Test health endpoint
curl http://localhost:8080/api/delivery/health
```

### Issue: CORS errors in browser

**Verify:** Backend controller has `@CrossOrigin` annotation:

```java
@RestController
@RequestMapping("/api/delivery")
@CrossOrigin(origins = "*")
public class DeliveryPlannerController { ... }
```

### Issue: npm install fails

**Solution:**

```powershell
cd delivery-planner-ui
rm -r node_modules
rm package-lock.json
npm install
```

### Issue: Angular build errors

**Solution:**

```powershell
cd delivery-planner-ui
rm -r .angular
npm run build
```

---

## Prerequisites

### Required Software

1. **Java 17 or higher**

   ```powershell
   java -version
   ```

2. **Maven 3.6+**

   ```powershell
   mvn -version
   ```

3. **Node.js 20+ and npm**

   ```powershell
   node -version
   npm -version
   ```

4. **Angular CLI 19+**
   ```powershell
   npm install -g @angular/cli
   ng version
   ```

---

##  Verify Connection

### Test 1: Backend Health Check

```powershell
curl http://localhost:8080/api/delivery/health
```

**Expected Response:**

```json
{
  "status": "UP",
  "message": "Delivery Planner API is running"
}
```

### Test 2: Plan Delivery from Frontend

1. Open `http://localhost:4200`
2. Add stores (lavender circles)
3. Add destinations (red squares)
4. Click "Plan Routes"
5. Check browser console for API calls
6. Routes should appear on the grid

### Test 3: Check Proxy Logs

Look for these messages in the frontend terminal:

```
[HPM] Proxy created: /api  ->  http://localhost:8080
[HPM] Proxy rewrite rule created: "^/api" ~> ""
```
