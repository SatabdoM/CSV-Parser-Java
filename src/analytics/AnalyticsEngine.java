package analytics;

import model.Transaction;
import model.TransactionType;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.DoubleAdder;

public class AnalyticsEngine {
    //DoubleAdder is designed for high-concurrency scenarios where multiple threads frequently update a shared sum
    //It reduces contention by maintaining multiple variables internally and combining them when needed
    private final ConcurrentHashMap<String, DoubleAdder> debitPerAccount = new ConcurrentHashMap<>();


    public void processData(Transaction txn) {
        if (txn.type().equals(TransactionType.DEBIT)) {
            debitPerAccount.computeIfAbsent(txn.accountId(), k -> new DoubleAdder()).add(txn.amount());
        }
    }

    public void generateReport() {
        System.out.println("Debit Amount per Account Report:");
        debitPerAccount.forEach((accountId, totalDebit) -> {
            System.out.println("Account ID: " + accountId + ", Total Debit: " + totalDebit.sum());
        });

    }
}
