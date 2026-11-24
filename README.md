# NetFlow: Network Traffic Monitoring & Management System

## 📌 Project Overview

NetFlow is a robust, Java-based console application designed to simulate, monitor, and manage network devices and traffic flows. This project was developed in phases (Milestones 1 to 5) to demonstrate core and advanced Java programming concepts, ranging from Object-Oriented Programming (OOP) to concurrent execution and relational database persistence.

## 🚀 Features (Milestones Covered)

- **Milestone 1 (OOP & Interfaces):** Custom domain models (`NetworkDevice`, `TrafficFlow`), Enums, Interface implementation (`Monitorable`), and custom checked exceptions (`InvalidDeviceException`, `InvalidFlowException`).

- **Milestone 2 (Collections & Streams):** In-memory data management using `HashMap` and `HashSet` for O(1) lookups and duplicate IP prevention, along with Java 8 Streams for sorting and filtering flows.

- **Milestone 3 (Multithreading & Synchronization):** Thread-safe concurrent processing of network flows using the `synchronized` keyword to prevent race conditions on shared statistics.

- **Milestone 4 (Producer-Consumer & Executors):** Custom bounded queue implementation using `wait()` and `notifyAll()` for thread coordination, plus `ExecutorService` and `Callable/Future` for asynchronous task execution.

- **Milestone 5 (JDBC & MySQL):** Permanent data storage using the DAO design pattern, `PreparedStatement` to prevent SQL injection, and Transaction Management (`commit`/`rollback`) to ensure atomic operations.

## 🛠️ Prerequisites

- **Java Development Kit (JDK) 17 or higher** (the code uses pattern-matching `instanceof`, which needs Java 16+).
- **MySQL Server 8.x** running locally on the default port `3306`.
- **MySQL JDBC driver:** the jar file `mysql-connector-j-*.jar` must be placed in the `lib/` folder (see below).
- **IDE (optional):** VS Code, Eclipse or IntelliJ IDEA.

## 📦 JDBC Driver Setup

1. Download **MySQL Connector/J** (Platform Independent) from <https://dev.mysql.com/downloads/connector/j/>.
2. Extract the downloaded archive and copy **only the `.jar` file** (for example `mysql-connector-j-26.10.0.jar`) into the `lib/` folder of this project.
3. The `lib/` folder is committed with the project, so `.gitignore` does not exclude `*.jar`.

VS Code detects the jar automatically through `.vscode/settings.json` (`lib/**/*.jar`).

## 🗄️ Database Setup

1. Open MySQL Workbench (or the MySQL command line).
2. Run the script `sql/netflow_schema.sql`. It creates the `netflow_db` database and the tables `network_devices` and `traffic_flows`.
3. Configure the connection in `src/netflow/database/DBConnection.java`. The default values are:

   | Setting | Default | Environment variable override |
   | :--- | :--- | :--- |
   | URL | `jdbc:mysql://localhost:3306/netflow_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` | `NETFLOW_DB_URL` |
   | User | `root` | `NETFLOW_DB_USER` |
   | Password | set in `DEFAULT_PASSWORD` | `NETFLOW_DB_PASSWORD` |

   **Recommended:** keep the placeholder password in the code and set your real password as an environment variable so it never gets committed:

   ```
   # Windows PowerShell
   $env:NETFLOW_DB_PASSWORD = "your_mysql_password"

   # Windows cmd
   set NETFLOW_DB_PASSWORD=your_mysql_password

   # Linux / macOS
   export NETFLOW_DB_PASSWORD="your_mysql_password"
   ```

   The variable lasts only for the terminal window where you set it.

## 💻 How to Run the Application

The main file is `src/netflow/main/NetFlowApp.java`.

### Option A: IDE (VS Code / Eclipse / IntelliJ)
1. Open the project folder (the jar in `lib/` is picked up automatically in VS Code).
2. Run `NetFlowApp.java`.

### Option B: Command line

The JUnit tests in `src/netflow/test/` need a JUnit jar, so the commands below compile everything **except** the test folder.

**Windows PowerShell**
```
Get-ChildItem -Recurse -Filter *.java src | Where-Object { $_.FullName -notmatch '\\test\\' } | ForEach-Object { $_.FullName.Replace('\','/') } > sources.txt
javac -cp "lib/*" -d bin "@sources.txt"
java -cp "bin;lib/*" netflow.main.NetFlowApp
```

**Linux / macOS**
```
find src -name "*.java" -not -path "*/test/*" > sources.txt
javac -cp "lib/*" -d bin @sources.txt
java -cp "bin:lib/*" netflow.main.NetFlowApp
```

### The console menu
```
1. Register New Network Device
2. View a Network Device by ID
3. Simulate & Save Traffic Flow
4. View All Saved Traffic Flows
5. Run Multithreading Demo
0. Exit Application
```

## 🧪 Milestone Demo Programs

Each milestone has a runnable console demo in `src/netflow/main/`. Milestones 2-4 need no database; Milestone 5 needs MySQL.

| Class | Demonstrates | Needs MySQL |
| :--- | :--- | :---: |
| `Milestone2App` | Collections, duplicate prevention, Streams | No |
| `Milestone3App` | Threads, synchronization, interruption | No |
| `Milestone4App` | Producer-Consumer, Executor/Callable/Future | No |
| `Milestone5App` | JDBC CRUD, nullable ports, transaction rollback | **Yes** |

Example (Windows): `java -cp "bin;lib/*" netflow.main.Milestone5App`
(on Linux/macOS use `:` instead of `;`).

Test cases and recorded output are documented in [`docs/test_cases.md`](docs/test_cases.md).

## ✅ Unit Tests

JUnit 5 tests are in `src/netflow/test/`. To run them, add a JUnit 5 jar (for example `junit-platform-console-standalone`) to your IDE's libraries; most IDEs can run them directly from the test folder.

## 📁 Project Structure

```
NetFlow/
├── docs/
│   └── test_cases.md           # Testing evidence and traceability matrix
├── lib/
│   └── mysql-connector-j-*.jar # MySQL JDBC driver
├── sql/
│   └── netflow_schema.sql      # Database creation script
├── src/netflow/
│   ├── concurrency/            # Producer-Consumer queue and thread tasks
│   ├── contract/               # Interfaces (Monitorable)
│   ├── dao/                    # Data Access Objects (JDBC logic)
│   ├── database/               # DBConnection manager
│   ├── exception/              # Custom Exceptions
│   ├── main/                   # NetFlowApp (main menu) and Milestone2-5 demos
│   ├── model/                  # POJOs (NetworkDevice, TrafficFlow, Enums)
│   ├── service/                # Business logic and Collection management
│   ├── test/                   # JUnit 5 unit tests
│   └── util/                   # ValidationUtils
├── .gitignore
└── README.md
```

## 🔧 Troubleshooting

| Error | Cause and fix |
| :--- | :--- |
| `No suitable driver found` | The jar is missing from `lib/`, or it is a `.tar.gz` instead of a `.jar`. Extract it and copy the `.jar`. |
| `Access denied for user` | Wrong MySQL password. Set `NETFLOW_DB_PASSWORD` or edit `DBConnection.java`. |
| `Unknown database 'netflow_db'` | Run `sql/netflow_schema.sql` first. |
| `Communications link failure` | MySQL is not running or not on port 3306. |

## 👥 Contributors

- **JAYANT (Team Lead):** Milestone 1 (OOP & Core Architecture) & Milestone 5 (JDBC Database & Persistence)
- **RAKESH:** Milestone 2 (Collections, Streams & In-Memory Data)
- **AMRITA:** Milestone 3 (Multithreading & Synchronization)
- **ARSHIA:** Milestone 4 (Producer-Consumer Queue & Executor Services)

_Developed for Java Programming Evaluation / Viva Submission._