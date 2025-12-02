# AI Delivery System

## Project Overview
This project implements an **artificial intelligence delivery system** operating on a city modeled as a 2D grid.  
A set of delivery trucks must transport packages from multiple stores to multiple customer destinations while minimizing travel time.  
Roads may contain traffic levels (costs), blocked segments, and optional tunnels connecting distant points.

---
## Compile and Run 
### Note : I have changed project structure so please read this doc carefully before running 
To properly create and manage **unit tests**, we require a **Maven project structure**. Maven simplifies:

* Dependency management (like JUnit)
* Standardized project structure
* Running all tests with a single command
* Future project extensions and builds

If you want to compile and run the project manually:
### Compile and run Without Maven 
**Compile:**
```bash
javac -d target/classes src/main/java/code/*.java
````

**Run:**

```bash
java -cp target/classes code.Main
```


---

### Steps to Install Maven

1. **Download Maven**
   Go to [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi) and download the latest binary zip.

2. **Extract Maven**
   Extract the zip to a directory of your choice:

   * Windows: `C:\Program Files\Apache\Maven`
   * Linux/Mac: `/opt/maven`

3. **Set Environment Variables**
   Add Maven `bin` directory to your `PATH`.


4. **Verify Installation**
   Open a terminal and run:

   ```bash
   mvn -v
   ```

   You should see the Maven version and Java version displayed.

5. **Install Java Maven Extension (VS Code)**
   Install the **Java Extension Pack** and **Maven for Java** extensions in VS Code.

---

### Compile and Run with Maven

Once Maven is installed and the project is converted to the standard Maven structure:

**Compile project:**

```bash
mvn compile
```

**Run the Main class:**

```bash
mvn exec:java -Dexec.mainClass="code.Main"
```

---

### Unit Testing

* Place all test files in:

```
src/test/java/code/
```

* Add JUnit as a dependency in `pom.xml`.
* Run all tests using:

```bash
mvn test
```
