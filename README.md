RentCluster - Distributed Rental Management System

**Authors:** Ahmet Yasin Karaman - Ahmed Boudokhane   
**Institution:** Poznań University of Technology (PUT) - Faculty of Computing and Telecommunications

## 📌 Project Overview
RentCluster is a booking system specifically designed to manage short-term rental operations utilizing a **distributed database** architecture.

In real-world scenarios, multiple clients attempting to rent the same property simultaneously (Race Condition) can lead to severe data inconsistencies. This project demonstrates how to solve high availability, write-heavy throughput, and distributed concurrency issues by implementing a NoSQL distributed architecture instead of a traditional Relational Database Management System (RDBMS).

## 🛠️ Tech Stack
* **Language:** Java (JDK 17+)
* **GUI:** Java Swing & JCalendar (for dynamic UI updates and date picking)
* **Database:** Apache Cassandra (3-Node Cluster via Docker)
* **Driver:** DataStax Java Driver for Apache Cassandra
* **Build Tool:** Maven

## ⚙️ Core Concepts & Architecture Proved
This system proves fundamental distributed engineering concepts through the following implementations:

1. **Denormalization & Write-Heavy Architecture:** Instead of storing date ranges, reservations are inserted day-by-day to exploit Cassandra's massive write throughput capabilities.
2. **Double-Booking Prevention:** Cassandra's **LWT (Lightweight Transactions)** and the **Paxos Consensus Protocol** (`IF NOT EXISTS` clauses) are utilized to prevent the same date from being sold to two different users.
3. **Concurrency & Data Consistency:** Java Multithreading is employed to prevent UI freezing and ensure that multiple background threads can safely execute database operations simultaneously.

## 🚀 Setup & Execution Guide

Follow these steps to run the project on your local machine:

### 1. Start the Cassandra Cluster
Ensure Docker Desktop is running on your system. Open your terminal, navigate to the project's root directory (where the `docker-compose.yml` is located), and execute:
```bash
docker-compose up -d
Note: This command spins up 3 independent Cassandra nodes in the background. Please allow 1-2 minutes for the cluster to fully initialize and communicate.

2. Run the Java Application (Auto-Initialization)
Open the project in your preferred IDE (e.g., IntelliJ IDEA), reload the Maven dependencies, and run the com.rentcluster.Main class.

Plug and Play: The system features an Auto-Initialization protocol. Upon launch, the Java application will automatically detect if the database is empty. It will seamlessly construct the rent_cluster keyspace, build the required tables, and insert default properties (like 'Suite_5', 'Villa_1') without requiring any manual CQL terminal inputs.

📊 Stress Tests & System Evaluation
The embedded Stress Tests module is specifically engineered to push the limits of the distributed system:

Test 1: Sequential Load

Scenario: A single client fires 100 rapid, continuous reservation requests.

Proves: System Throughput. Demonstrates how quickly the database can process data without memory overflow, despite the overhead of LWT and Paxos voting.

Test 2: Random Requests

Scenario: Two independent bots simultaneously send 50 requests each for random properties and random dates.

Proves: Concurrency. Confirms that the database does not encounter deadlocks and can safely handle multiple simultaneous connections.

Test 3: Occupy All Seats (Race Condition)

Scenario: Two clients (Client_A and Client_B) are unleashed at the exact same millisecond (with simulated network latency) to fight over a continuous 100-day block for a single property.

Proves: Fairness and Consistency. Paxos consensus ensures zero double-bookings, while the results demonstrate that the system fairly distributes resources between both clients without causing starvation to either party.

Test 4: Constant Cancellations (State Toggling)

Scenario: A bot rapidly books and immediately cancels a reservation for a single property and date, repeating this aggressive cycle 50 times.

Proves: State Resilience. Demonstrates the database's ability to handle rapid "insert" and "tombstone" (delete) commands on the exact same row without confusing the final state or crashing.

Test 5: Batch Cancellations (Mass Deletion)

Scenario: The system simulates a travel agency booking a continuous 50-day block, followed by an instant, massive batch cancellation of all 50 records simultaneously.

Proves: Deletion Throughput. Confirms the distributed architecture can efficiently generate and distribute wide-scale tombstones across multiple nodes without timeouts or dropping connections.