# Financial CSV Parser - Architecture Documentation

A sophisticated multi-threaded Java application that parses financial transaction data from CSV files using the **Producer-Consumer architectural pattern** with thread-safe synchronization mechanisms and real-time analytics processing.

---

<img width="3240" height="2441" alt="image" src="https://github.com/user-attachments/assets/d8eba98e-5872-4183-90b5-ee1e37bff7ce" />


## 📐 Architecture Overview

This project demonstrates an **enterprise-grade distributed systems architecture** pattern suitable for real-world scenarios involving concurrent data processing, I/O operations, analytics, and resource management.

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                   APPLICATION ENTRY POINT                           │
│                    (Application.java)                                │
│      - Thread Orchestration & Lifecycle Management                  │
│      - Producer/Consumer Coordination                               │
│      - Analytics Engine Integration                                 │
└──────────────────────┬──────────────────────────────────────────────┘
                       │
        ┌──────────────┼─────────────────┬─────────────────┐
        │              │                 │                 │
        ▼              ▼                 ▼                 ▼
   ┌─────────┐  ┌───────────────┐  ┌─────────────┐  ┌──────────────┐
   │PRODUCER │  │SHARED BUFFER  │  │  CONSUMER   │  │   ANALYTICS  │
   │ THREAD  │──┤   (Queue)     │◄─┤ THREAD POOL │  │    ENGINE    │
   └─────────┘  └───────────────┘  │ (4 threads) │  └──────────────┘
        │              ▲             └─────────────┘          ▲
        │ CSV Input    │                   │                  │
        │              │ Thread Safe       │ Process          │
        │         Wait/Notify              │ & Analyze        │
        │      Synchronization             │                  │
        ▼                                   └──────────────────┘
   ┌────────────┐                          (ConcurrentHashMap)
   │FILE SYSTEM │
   │ (CSV Data) │
   └────────────┘
```

---

## 🏛️ Architectural Layers

The application is organized into **6 distinct architectural layers**:

### 1. **Application/Orchestration Layer** (`Application.java`)

**Responsibility:** Thread lifecycle management, thread pooling, and system coordination

```
Application.java
├── Creates SharedBuffer with capacity
├── Instantiates single Producer thread
├── Creates ExecutorService for Consumer thread pool
├── Initializes AnalyticsEngine
├── Manages thread start/stop/shutdown
└── Handles graceful shutdown and reporting
```

**Key Functions:**
- Application entry point (public static void main)
- Thread factory and initializer
- Producer/Consumer/Analytics coordination
- Thread pool management
- Exception handling at system level
- Lifecycle orchestration

**Code Structure:**
```java
public class Application {
    public static void main(String[] args) throws InterruptedException {
        // 1. Configuration
        SharedBuffer<Transaction> buffer = new SharedBuffer(10);
        AnalyticsEngine analyticsEngine = new AnalyticsEngine();
        
        // 2. Single Producer Thread
        Thread producerThread = new Thread(new CsvProducer(buffer, csvFile));
        producerThread.start();
        
        // 3. Thread Pool for Multiple Consumers
        ExecutorService consumers = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 4; i++) {
            consumers.submit(new TransactionConsumer(buffer, analyticsEngine));
        }
        
        // 4. Lifecycle Management
        producerThread.join();      // Wait for producer completion
        Thread.sleep(5000);         // Buffer drain time
        analyticsEngine.generateReport();  // Generate analytics
        consumers.shutdown();       // Stop consumer threads
    }
}
```

---

### 2. **Data Access Layer** (`CsvProducer.java`)

**Responsibility:** Reading and providing data from external sources

```
CsvProducer (Runnable)
├── Opens CSV file
├── Reads line-by-line
├── Parses data
├── Populates SharedBuffer
└── Handles I/O exceptions
```

**Architecture Pattern:** **Producer** (Active Data Source)

**Data Flow:**
```
CSV File → BufferedReader → CsvParser → SharedBuffer → Memory
```

**Threading Model:**
- Runs on dedicated producer thread
- Non-blocking operations (except file I/O)
- Communicates through SharedBuffer only

**Error Handling Strategy:**
- File not found exceptions
- I/O interruption handling
- Graceful failure modes

---

### 3. **Core Business Logic Layer** (`CsvParser.java`)

**Responsibility:** Data transformation and validation

```
CsvParser
├── Parse CSV string
├── Field extraction
├── Type conversion
├── Object instantiation
└── Validation
```

**Transformation Pipeline:**
```
"1,2024-01-01T10:15:30,ACC100,DEBIT,500.50"
           ↓
String[] split
           ↓
Transaction object creation
           ↓
Type conversions:
  - id: String
  - timestamp: LocalDateTime.parse()
  - accountId: String
  - type: TransactionType.valueOf()
  - amount: Double.parseDouble()
           ↓
Transaction record
```

**Design Principles:**
- Single responsibility (parsing only)
- Stateless operations
- Reusable across components
- Pure function behavior

---

### 4. **Data Model Layer** (`model/`)

**Responsibility:** Data representation and structure

```
model/
├── Transaction (Java Record)
│   ├── id: String
│   ├── timestamp: LocalDateTime
│   ├── accountId: String
│   ├── type: TransactionType
│   └── amount: double
│
└── TransactionType (Enum)
    ├── CREDIT
    └── DEBIT
```

**Benefits of Using Records:**
- Immutable by default
- Auto-generated equals/hashCode/toString
- Thread-safe data representation
- Reduced boilerplate code

**Enum Pattern:**
- Type-safe transaction classification
- Prevents invalid values
- Clear business semantics

---

### 5. **Synchronization Layer** (`SharedBuffer.java`)

**Responsibility:** Thread coordination and resource management

```
SharedBuffer<T> (Generic, Thread-Safe)
├── Internal Queue (LinkedList)
├── Capacity Management
├── Synchronization Primitives
│   ├── synchronized methods
│   ├── wait() for blocking
│   └── notifyAll() for signaling
└── Thread State Management
```

**Synchronization Architecture:**

```
Producer Thread                SharedBuffer              Consumer Thread Pool
    │                              │                           │
    ├──► add(item)                 │                           │
    │    synchronized              │                           │
    │    wait if full ─────────────┼──────────────────────┐    │
    │                              │                       │    │
    │    queue.add(item)           │                       │    │
    │    notifyAll() ──────────────┼─────────────► wake up │    │
    │                              │                    remove()
    │                              │                       │
    │                              │    item returned      │
    │                              │◄──────────────────────┤
    │                              │                       │
    └─ (repeat)             (synchronized)          ▼ process
                                   │               (repeat for each consumer)
```

---

### 6. **Analytics Layer** (`AnalyticsEngine.java`)

**Responsibility:** Real-time analytics and data aggregation

```
AnalyticsEngine
├── Maintains ConcurrentHashMap for accounts
├── Uses DoubleAdder for high-concurrency updates
├── Processes transactions concurrently
├── Generates analytical reports
└── Thread-safe data aggregation
```

**Architecture Features:**
- **Thread-safe data structure**: ConcurrentHashMap
- **High-concurrency numeric updates**: DoubleAdder (reduces contention)
- **Real-time aggregation**: Transactions processed as they arrive
- **Report generation**: Summarized analytics output

**Processing Model:**
```
Multiple Consumer Threads
    │
    ├──► Transaction 1
    │    ├─► analyticsEngine.processData(txn)
    │    └─► Update debitPerAccount[ACC100] += 500.50
    │
    ├──► Transaction 2
    │    ├─► analyticsEngine.processData(txn)
    │    └─► Update debitPerAccount[ACC101] += 1200.00
    │
    └──► Transaction N
         ├─► analyticsEngine.processData(txn)
         └─► Update debitPerAccount[ACCxxx] += amount

    After All Transactions Processed
    │
    └──► generateReport()
         ├─► Account ACC100: Total Debit = 700.50
         ├─► Account ACC101: Total Debit = 1200.00
         └─► Account ACCxxx: Total Debit = xxxxx.xx
```

**Key Implementation Details:**
```java
public class AnalyticsEngine {
    // ConcurrentHashMap: Thread-safe without full synchronization
    private final ConcurrentHashMap<String, DoubleAdder> debitPerAccount;
    
    // DoubleAdder: High-concurrency numeric updates
    // Better than AtomicDouble for scenarios with many threads
    
    public void processData(Transaction txn) {
        if (txn.type().equals(TransactionType.DEBIT)) {
            debitPerAccount
                .computeIfAbsent(txn.accountId(), k -> new DoubleAdder())
                .add(txn.amount());
        }
    }
}
```

---

## 🔄 Producer-Consumer Pattern Architecture

### Pattern Description

The **Producer-Consumer** pattern decouples data production from consumption through an intermediary buffer, enabling:

1. **Asynchronous Processing** - Producer and consumer work independently
2. **Load Balancing** - Buffer absorbs production/consumption speed differences
3. **Resource Efficiency** - Prevents memory overflow or thread starvation
4. **Scalability** - Easy to add multiple producers or consumers

### Pattern Components

| Component | Role | Thread Safety |
|-----------|------|----------------|
| **Producer** | Generates data from source | Non-blocking writes to buffer |
| **SharedBuffer** | Stores data and coordinates threads | Fully synchronized |
| **Consumer** | Processes data from buffer | Non-blocking reads from buffer |

### Communication Protocol

```
┌─────────────────────────────────────────────────┐
│           SYNCHRONIZATION PROTOCOL              │
├─────────────────────────────────────────────────┤
│                                                 │
│  PRODUCER → ADD(item)                           │
│              ├─ Lock acquired                   │
│              ├─ Check capacity                  │
│              ├─ If full: WAIT()                 │
│              ├─ Add to queue                    │
│              ├─ NOTIFYALL()                     │
│              └─ Lock released                   │
│                                                 │
│  CONSUMER → REMOVE()                            │
│              ├─ Lock acquired                   │
│              ├─ Check if empty                  │
│              ├─ If empty: WAIT()                │
│              ├─ Poll from queue                 │
│              ├─ NOTIFYALL()                     │
│              └─ Lock released                   │
│                                                 │
└─────────────────────────────────────────────────┘
```

---

## 🧵 Threading Model Architecture

### Multi-Threaded Execution Strategy

```
MAIN THREAD (Orchestrator)
│
├─► Create Shared Resources
│   │
│   ├─► SharedBuffer instance
│   └─► AnalyticsEngine instance
│
├─► Spawn Producer Thread (1 thread)
│   │
│   └─► CsvProducer.run()
│       ├─ Open file
│       ├─ Read CSV
│       └─ Add to buffer (blocking operations)
│
├─► Spawn Consumer Thread Pool (4 threads via ExecutorService)
│   │
│   ├─► TransactionConsumer.run() [Thread 1]
│   ├─► TransactionConsumer.run() [Thread 2]
│   ├─► TransactionConsumer.run() [Thread 3]
│   └─► TransactionConsumer.run() [Thread 4]
│       ├─ Read from buffer (blocking operations)
│       ├─ Process transactions
│       └─ Send to AnalyticsEngine
│
└─► Coordinate Shutdown
    ├─ Wait for Producer (join)
    ├─ Wait for buffer to drain
    ├─ Generate analytics report
    └─ Shutdown consumer thread pool
```

### Thread Pool Architecture

```
ExecutorService (Thread Pool)
┌─────────────────────────────────────┐
│ Fixed Thread Pool (4 threads)       │
├─────────────────────────────────────┤
│                                     │
│  Worker Thread 1  Worker Thread 2   │
│  Worker Thread 3  Worker Thread 4   │
│                                     │
│  Each thread:                       │
│  ├─ Independently consumes from     │
│  │  shared buffer                   │
│  ├─ Processes transactions          │
│  ├─ Updates analytics engine        │
│  └─ Repeats until interrupted       │
│                                     │
└─────────────────────────────────────┘
```

### Multi-Consumer Synchronization

```
Producer (Single Thread)
    │ add() synchronized
    ▼
SharedBuffer
    │ remove() synchronized (4 consumers competing)
    ├─► Consumer Thread 1: Gets Transaction 1
    ├─► Consumer Thread 2: Gets Transaction 2
    ├─► Consumer Thread 3: Gets Transaction 3
    └─► Consumer Thread 4: Gets Transaction 4
        │
        └─► All update AnalyticsEngine (thread-safe)
```

### Thread State Transitions

```
                    ┌─────────────┐
                    │   CREATED   │
                    └──────┬──────┘
                           │ start()
                           ▼
                    ┌─────────────┐
          ┌────────►│   RUNNABLE  │◄────────┐
          │         └──────┬──────┘         │
          │                │ wait()         │ notifyAll()
          │                ▼               │
     (wait for) ┌──────────────────┐       │
          │     │   WAITING        │       │
          │     │  (on buffer)     │       │
          └─────┤                  ├───────┘
                └──────────────────┘
                         │
                    interrupt()
                         ▼
                    ┌─────────────┐
                    │  TERMINATED │
                    └─────────────┘
```

### Critical Section Management

```
CRITICAL SECTION (SharedBuffer methods)

add() method:
    synchronized(this) {
        ┌────────────────────────────────────┐
        │ ONLY ONE THREAD AT A TIME          │
        │                                    │
        │ while (queue.size() == capacity) { │
        │     wait();  // Release lock       │
        │ }                                  │
        │                                    │
        │ queue.add(item);                   │
        │ notifyAll(); // Wake waiting       │
        └────────────────────────────────────┘
    }

remove() method:
    synchronized(this) {
        ┌────────────────────────────────────┐
        │ MUTUAL EXCLUSION GUARANTEED        │
        │                                    │
        │ while (queue.isEmpty()) {          │
        │     wait();  // Release lock       │
        │ }                                  │
        │                                    │
        │ item = queue.poll();               │
        │ notifyAll(); // Wake waiting       │
        └────────────────────────────────────┘
    }
```

---
---------
## 📊 Data Flow Architecture

### End-to-End Data Journey

```
1. FILE SYSTEM
   │
   └─► transactions.csv
       ├─ Headers: id,timestamp,accountId,type,amount
       └─ Rows: n transaction records

2. PRODUCER THREAD (CsvProducer)
   │
   ├─► FileReader
   ├─► BufferedReader (buffered I/O)
   └─► Line-by-line reading

3. PARSING LAYER (CsvParser)
   │
   ├─► String split(",")
   ├─► Field validation
   ├─► Type conversion
   │   ├─ id: direct String
   │   ├─ timestamp: LocalDateTime.parse()
   │   ├─ accountId: direct String
   │   ├─ type: TransactionType.valueOf()
   │   └─ amount: Double.parseDouble()
   └─► Transaction record creation

4. SHARED BUFFER (Synchronization)
   │
   ├─► Queue storage (LinkedList)
   ├─► Capacity enforcement
   ├─► Thread coordination
   └─► Blocking operations

5. CONSUMER THREAD (TransactionConsumer)
   │
   ├─► Buffer polling
   ├─► Item retrieval
   ├─► Processing logic
   └─► Display/logging

6. OUTPUT
   │
   └─► Console display
       └─ Transaction objects printed
```

### Example Data Transformation

```
INPUT (CSV):
  "1,2024-01-01T10:15:30,ACC100,DEBIT,500.50"

PROCESSING STAGES:

Stage 1 - String Split:
  ["1", "2024-01-01T10:15:30", "ACC100", "DEBIT", "500.50"]

Stage 2 - Type Conversion:
  id = "1"
  timestamp = LocalDateTime.parse("2024-01-01T10:15:30")
  accountId = "ACC100"
  type = TransactionType.valueOf("DEBIT")
  amount = Double.parseDouble("500.50")

Stage 3 - Object Creation:
  new Transaction(
    id="1",
    timestamp=2024-01-01T10:15:30,
    accountId="ACC100",
    type=DEBIT,
    amount=500.5
  )

OUTPUT (Transaction Record):
  Transaction[id=1, timestamp=2024-01-01T10:15:30, 
              accountId=ACC100, type=DEBIT, amount=500.5]
```

---

## 🔐 Concurrency Architecture

### Thread Safety Mechanisms

#### 1. **Synchronization**
```java
public synchronized void add(T item) {
    // Only one thread can execute at a time
    // Others wait outside the monitor
}
```

#### 2. **Monitoring & Signaling**
```java
while (queue.size() == capacity) {
    wait();  // Release lock, wait for signal
}
queue.add(item);
notifyAll();  // Wake all waiting threads
```

#### 3. **Immutable Data**
```java
public record Transaction(...) { }
// Records are immutable - safe to share across threads
```

#### 4. **Thread Naming**
```java
producerThread.setName("Producer-Thread");
consumerThread.setName("Consumer-Thread");
// Helps with debugging and monitoring
```

### Race Condition Prevention

**Scenario: Multiple threads accessing queue simultaneously**

```
WITHOUT Synchronization (UNSAFE):
┌─────────────────────┬─────────────────────┐
│ Producer Thread     │ Consumer Thread     │
├─────────────────────┼─────────────────────┤
│ Read size = 2       │ Read size = 2       │
│ Check: size < cap?  │ Check: empty?       │
│ Add item            │ Poll item           │
│ Write size = 3      │ Write size = 1      │
│                     │ ❌ DATA CORRUPTION  │
└─────────────────────┴─────────────────────┘

WITH Synchronization (SAFE):
┌─────────────────────┬─────────────────────┐
│ Producer Thread     │ Consumer Thread     │
├─────────────────────┼─────────────────────┤
│ ACQUIRE LOCK ✓      │ WAIT (blocked)      │
│ Read size = 2       │                     │
│ Check: size < cap?  │                     │
│ Add item            │                     │
│ Write size = 3      │                     │
│ RELEASE LOCK        │                     │
│                     │ ACQUIRE LOCK ✓      │
│                     │ Read size = 3       │
│                     │ Poll item           │
│                     │ Write size = 2      │
│                     │ RELEASE LOCK        │
│                     │ ✓ DATA CONSISTENT   │
└─────────────────────┴─────────────────────┘
```

## 🚀 Running the Application

### Prerequisites
- Java 17+ (supports new features)
- IDE with Java support (IntelliJ IDEA recommended)

### Execution Flow

**From IDE:**
1. Open the project in IntelliJ IDEA
2. Navigate to `src/app/Application.java`
3. Right-click and select **Run** or press `Shift+F10`

**From Command Line:**
```bash
# Compile
javac -d out src/**/*.java

# Run
java -cp out app.Application

# Output Example:
# Added: Transaction[id=1, timestamp=2024-01-01T10:15:30, ...]
# Consumed: Transaction[id=1, timestamp=2024-01-01T10:15:30, ...] by pool-1-thread-1
# Consumed: Transaction[id=2, timestamp=2024-01-01T10:16:00, ...] by pool-1-thread-2
# [repeated for each transaction]
# Producer finished...
# Debit Amount per Account Report:
# Account ID: ACC100, Total Debit: 700.50
# Account ID: ACC101, Total Debit: 1200.00
# Consumer thread finished.
```

---

## 📚 Key Concepts 

1. **Producer-Consumer Pattern** - Decoupled concurrent processing
2. **Monitor Pattern** - Synchronized access to shared resources
3. **Thread Coordination** - Wait/notify mechanisms
4. **Bounded Buffer** - Resource management and backpressure
5. **Immutable Objects** - Thread-safe data representation
6. **Layered Architecture** - Separation of concerns
7. **Generic Programming** - Reusable SharedBuffer<T>
8. **State Machines** - Thread lifecycle management

