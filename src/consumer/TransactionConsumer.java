package consumer;

import SharedBuffer.SharedBuffer;
import model.Transaction;

public class TransactionConsumer implements Runnable {
    SharedBuffer<Transaction> buffer;

    public TransactionConsumer(SharedBuffer<Transaction> buffer) {
        this.buffer = buffer;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Transaction transaction = buffer.remove();
                if (transaction != null) {
                    System.out.println("Consumed: " + transaction);
                }
            }
        } catch (Exception e) {
            System.err.println("Consumer interrupted: " + e.getMessage());
        }
        System.out.println("Consumer thread finished.");
    }
}

