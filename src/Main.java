import producer.CsvProducer;
import consumer.TransactionConsumer;
import SharedBuffer.SharedBuffer;


void main() {
    String csvFile = "data/transactions.csv";

    SharedBuffer sharedBuffer = new SharedBuffer(3); // Buffer capacity of 3

    // Start producer thread
    Thread producerThread = new Thread(new CsvProducer(sharedBuffer, csvFile));
    producerThread.setName("Producer-Thread");
    producerThread.start();

    // Start consumer thread
    Thread consumerThread = new Thread(new TransactionConsumer(sharedBuffer));
    consumerThread.setName("Consumer-Thread");
    consumerThread.start();

    // Wait for producer to finish
    try {
        producerThread.join();
        System.out.println("\nProducer finished. Waiting for consumer to finish...");
        Thread.sleep(2000); // Give consumer time to process remaining items
        consumerThread.interrupt(); // Stop the consumer's infinite loop
    } catch (InterruptedException e) {
        System.err.println("Main thread interrupted: " + e.getMessage());
    }
}


