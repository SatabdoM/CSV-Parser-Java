package app;

import SharedBuffer.SharedBuffer;
import analytics.AnalyticsEngine;
import consumer.TransactionConsumer;
import producer.CsvProducer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Application {
    public static void main(String[] args) throws InterruptedException {
        String csvFile = "data/transactions.csv";

        SharedBuffer buffer = new SharedBuffer(10); // Buffer size of 10
        AnalyticsEngine analyticsEngine = new AnalyticsEngine();


        //start the producers
        Thread producerThread = new Thread(new CsvProducer(buffer, csvFile));
        producerThread.start();

        //Start the consumers
        ExecutorService consumers = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 4; i++) {
            consumers.submit(new TransactionConsumer(buffer, analyticsEngine));
        }
        // Wait for the producer to finish
        producerThread.join();
        Thread.sleep(5000);
        analyticsEngine.generateReport();
        // Shutdown the consumers after producer is done
        consumers.shutdown();
    }
}
