# Financial CSV Parser

A multi-threaded Java application that parses financial transaction data from CSV files using the **Producer-Consumer pattern** with a shared buffer for efficient processing.

## Overview

This project demonstrates advanced Java concepts including:
- **Multi-threading**: Producer and Consumer threads working concurrently
- **Synchronization**: Thread-safe shared buffer using locks and wait/notify mechanisms
- **CSV Parsing**: Efficient reading and conversion of CSV data into structured objects
- **Design Patterns**: Implementation of the Producer-Consumer architectural pattern

## Project Structure

```
FinancialCSVParser/
├── src/
│   ├── Main.java                 # Entry point - orchestrates producer and consumer threads
│   ├── app/
│   │   └── Application.java      # Application logic container
│   ├── consumer/
│   │   └── TransactionConsumer.java    # Consumer thread - processes transactions from buffer
│   ├── model/
│   │   ├── Transaction.java      # Record class representing a financial transaction
│   │   └── TransactionType.java  # Enum for CREDIT and DEBIT transaction types
│   ├── producer/
│   │   └── CsvProducer.java      # Producer thread - reads CSV and populates buffer
│   ├── util/
│   │   └── CsvParser.java        # Parses CSV lines into Transaction objects
│   └── SharedBuffer/
│       └── SharedBuffer.java     # Thread-safe queue with capacity management
├── data/
│   └── transactions.csv          # Sample financial transaction data
└── README.md                      # This file
```

## Key Components

### 1. **SharedBuffer** (`src/SharedBuffer/SharedBuffer.java`)
A thread-safe generic buffer implementing the Producer-Consumer synchronization pattern.

**Features:**
- Bounded queue with configurable capacity
- Synchronized `add()` and `remove()` methods
- Wait/notify mechanism to block when full or empty
- Thread-safe operations

```java
SharedBuffer<T> buffer = new SharedBuffer<>(capacity);
buffer.add(item);      // Blocks if buffer is full
T item = buffer.remove(); // Blocks if buffer is empty
```

### 2. **CsvProducer** (`src/producer/CsvProducer.java`)
Runs as a separate thread and reads transactions from the CSV file.

**Responsibilities:**
- Opens and reads the CSV file line by line
- Parses each line into a Transaction object
- Adds parsed transactions to the shared buffer
- Gracefully handles file I/O exceptions

### 3. **TransactionConsumer** (`src/consumer/TransactionConsumer.java`)
Runs as a separate thread and processes transactions from the buffer.

**Responsibilities:**
- Continuously reads transactions from the shared buffer
- Processes each transaction (display, logging, etc.)
- Handles thread interruption gracefully
- Exits cleanly when signaled

### 4. **CsvParser** (`src/util/CsvParser.java`)
Converts CSV string data into Transaction objects.

**Parsing Logic:**
- Splits CSV line by comma delimiter
- Maps fields: id, timestamp, accountId, type, amount
- Converts LocalDateTime format: `2024-01-01T10:15:30`
- Converts TransactionType enum: CREDIT/DEBIT
- Converts amount to double value

### 5. **Transaction** (`src/model/Transaction.java`)
Java Record representing a financial transaction.

```java
public record Transaction(
    String id,
    LocalDateTime timestamp,
    String accountId,
    TransactionType type,
    double amount
) {}
```

**Fields:**
- `id`: Unique transaction identifier
- `timestamp`: Transaction date and time
- `accountId`: Associated bank account
- `type`: CREDIT or DEBIT
- `amount`: Transaction value in currency

## CSV Format

The input CSV file should follow this format:

```csv
id,timestamp,accountId,type,amount
1,2024-01-01T10:15:30,ACC100,DEBIT,500.50
2,2024-01-01T10:16:00,ACC101,CREDIT,1200.00
3,2024-01-01T10:17:00,ACC100,DEBIT,200.00
```

**Column Specifications:**
- **id**: Unique identifier (string)
- **timestamp**: ISO 8601 format with time (`YYYY-MM-DDTHH:MM:SS`)
- **accountId**: Account identifier (string)
- **type**: Transaction type - `CREDIT` or `DEBIT`
- **amount**: Numeric value (double)

## How It Works

### Execution Flow

```
Main Thread
    ↓
┌───────────────────────────────────┐
│   Create SharedBuffer (capacity=3) │
└───────────────────────────────────┘
    ↓
┌─────────────────────────────────────────────────────┐
│ Producer Thread                 │ Consumer Thread    │
├─────────────────────────────────────────────────────┤
│ 1. Open CSV file                │ 1. Read buffer    │
│ 2. Parse each line              │ 2. Process trans. │
│ 3. Add to buffer (blocks if full)│ 3. Display output │
│ 4. Close file                   │ 4. Repeat until   │
│ 5. Exit                         │    interrupted    │
└─────────────────────────────────────────────────────┘
    ↓
Main waits for Producer to finish (join)
    ↓
Main interrupts Consumer
    ↓
All threads exit gracefully
```

### Thread Synchronization

1. **Producer adds to buffer:**
   - Checks if buffer is at capacity
   - If full, waits for space
   - Adds item and notifies all waiting threads
   - Sleeps 2 seconds between additions (simulates processing)

2. **Consumer removes from buffer:**
   - Checks if buffer is empty
   - If empty, waits for items
   - Removes item and notifies all waiting threads
   - Processes the transaction

## Usage

### Prerequisites
- Java 17 or higher (uses `void main()` syntax)
- IDE: IntelliJ IDEA or similar
- No external dependencies required

### Running the Application

**From IDE:**
1. Open the project in IntelliJ IDEA
2. Right-click on `Main.java`
3. Select **Run** or press `Shift+F10`

**From Command Line:**
```bash
# Compile
javac -d out src/**/*.java

# Run
java -cp out Main
```

### Expected Output

```
Added: Transaction[id=1, timestamp=2024-01-01T10:15:30, accountId=ACC100, type=DEBIT, amount=500.5]
Consumed: Transaction[id=1, timestamp=2024-01-01T10:15:30, accountId=ACC100, type=DEBIT, amount=500.5]

Added: Transaction[id=2, timestamp=2024-01-01T10:16:00, accountId=ACC101, type=CREDIT, amount=1200.0]
Consumed: Transaction[id=2, timestamp=2024-01-01T10:16:00, accountId=ACC101, type=CREDIT, amount=1200.0]

Added: Transaction[id=3, timestamp=2024-01-01T10:17:00, accountId=ACC100, type=DEBIT, amount=200.0]
Consumed: Transaction[id=3, timestamp=2024-01-01T10:17:00, accountId=ACC100, type=DEBIT, amount=200.0]

Producer finished. Waiting for consumer to finish...
Consumer thread finished.
```

## Configuration

### Buffer Capacity

Modify the buffer capacity in `Main.java`:

```java
SharedBuffer sharedBuffer = new SharedBuffer(10); // Increase or decrease as needed
```

**Effects:**
- **Smaller capacity (1-5)**: More frequent thread blocking, demonstrates synchronization
- **Larger capacity (10+)**: Smoother execution, less thread contention

### CSV File Location

Change the CSV file path in `Main.java`:

```java
String csvFile = "path/to/your/transactions.csv";
```

### Producer Delay

Modify the sleep time in `SharedBuffer.add()` to simulate different processing speeds:

```java
Thread.sleep(2000); // Adjust milliseconds as needed
```

## Design Patterns

### Producer-Consumer Pattern
- Decouples data production from consumption
- Allows independent scaling of producer and consumer speeds
- Prevents resource exhaustion through bounded buffer

### Thread-Safe Collection
- Synchronization using `synchronized` keyword
- Wait/notify mechanism for efficient thread communication
- Prevents race conditions and data corruption

## Error Handling

The application handles:
- ✅ File not found exceptions
- ✅ CSV parsing errors
- ✅ Thread interruption signals
- ✅ Invalid data format recovery

## Future Enhancements

Potential improvements:
- [ ] Add transaction filtering and aggregation
- [ ] Implement persistence layer (database storage)
- [ ] Add transaction validation and error reporting
- [ ] Create REST API endpoints for transaction queries
- [ ] Add metrics and performance monitoring
- [ ] Support for multiple CSV files
- [ ] Implement thread pool for scalability
- [ ] Add logging framework (SLF4J, Log4j)

## Testing

### Manual Testing Steps

1. **Test with empty buffer:**
   - Set capacity to 0 and verify consumer waits

2. **Test with different data:**
   - Create custom CSV files with various transaction types
   - Verify parsing accuracy

3. **Test thread termination:**
   - Monitor console output to confirm graceful shutdown

### Sample Test Data

```csv
id,timestamp,accountId,type,amount
T001,2024-01-15T09:30:00,ACC001,DEBIT,100.00
T002,2024-01-15T09:31:00,ACC002,CREDIT,500.00
T003,2024-01-15T09:32:00,ACC001,CREDIT,250.50
T004,2024-01-15T09:33:00,ACC003,DEBIT,75.25
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| **Consumer thread hangs** | Check buffer capacity; increase if too small |
| **CSV file not found** | Verify file path and ensure it exists |
| **Parsing errors** | Check CSV format matches specification |
| **Slow performance** | Reduce `Thread.sleep()` duration in SharedBuffer |
| **Memory issues** | Decrease buffer capacity or process in batches |

## Performance Notes

- Buffer capacity of 3-10 balances thread contention and throughput
- CSV parsing is I/O bound; consider buffering for large files
- Synchronization overhead increases with contention
- Consider using `ConcurrentLinkedQueue` for high-throughput scenarios

## License

This project is for educational purposes.

## Author

Developed as a demonstration of multi-threaded Java programming and design patterns.

---

**Last Updated:** January 2026

For questions or improvements, review the code and experiment with different configurations!

