package consumer;

import SharedBuffer.SharedBuffer;
import analytics.AnalyticsEngine;
import model.Transaction;

public class TransactionConsumer implements Runnable {
    SharedBuffer<Transaction> buffer;
    AnalyticsEngine analyticsEngine;

    public TransactionConsumer(SharedBuffer<Transaction> buffer, AnalyticsEngine analyticsEngine) {
        this.buffer = buffer;
        this.analyticsEngine = analyticsEngine;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Transaction transaction = buffer.remove();
                if (transaction != null) {
                    analyticsEngine.processData(transaction);
                    System.out.println("Consumed: " + transaction + "by " + Thread.currentThread().getName());
                }
            }
        } catch (Exception e) {
            System.err.println("Consumer interrupted: " + e.getMessage());
        }
        System.out.println("Consumer thread finished.");
    }
}

