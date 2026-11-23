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

Before running this project, ensure you have the following installed:

- **Java Development Kit (JDK):** Version 8 or higher (Java 17+ recommended).

- **MySQL Server:** Running locally (default port `3306`).

- **MySQL JDBC Connector:** `mysql-connector-java.jar` (Version 8.x.x) added to the project's classpath / referenced libraries.

- **IDE:** VS Code, Eclipse, or IntelliJ IDEA.

## 🗄️ Database Setup

1. Open your MySQL client (e.g., MySQL Workbench or Command Line).

2. Execute the provided SQL script located at: `sql/netflow_schema.sql`. This will create the `netflow_db` database and the required tables (`network_devices` and `traffic_flows`).

3. Open `src/main/java/netflow/database/DBConnection.java` in your IDE.

4. Update the `USER` and `PASSWORD` constants to match your local MySQL database credentials.

## 💻 How to Run the Application

1. Open the project folder in your IDE.

2. Ensure the `mysql-connector-java.jar` is successfully added to the project libraries to avoid `ClassNotFoundException`.

3. Navigate to the main application file: `src/main/java/netflow/main/NetFlowApp.java`.

4. Run the `NetFlowApp.java` file.

5. An interactive console menu will appear in the terminal, allowing you to Register Devices, Simulate Traffic Flows, and Run Concurrency Demos.

## 📁 Project Structure

```
NetFlow/
├── docs/
│   └── test_cases.md           # Testing evidence and traceability matrix
├── sql/
│   └── netflow_schema.sql      # Database creation script
└── src/main/java/netflow/
    ├── concurrency/            # Producer-Consumer queue and thread tasks
    ├── contract/               # Interfaces (Monitorable)
    ├── dao/                    # Data Access Objects (JDBC logic)
    ├── database/               # DBConnection manager
    ├── exception/              # Custom Exceptions
    ├── main/                   # Test Harnesses and Final NetFlowApp (Main Menu)
    ├── model/                  # POJOs (NetworkDevice, TrafficFlow, Enums)
    └── service/                # Business logic and Collection management

```

## 👥 Contributors

- **JAYANT (Team Lead):** Milestone 1 (OOP & Core Architecture) & Milestone 5 (JDBC Database & Persistence)
- **RAKESH:** Milestone 2 (Collections, Streams & In-Memory Data)
- **AMRITA:** Milestone 3 (Multithreading & Synchronization)
- **ARSHIA:** Milestone 4 (Producer-Consumer Queue & Executor Services)

_Developed for Java Programming Evaluation / Viva Submission._
