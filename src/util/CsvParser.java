package util;

import model.Transaction;
import model.TransactionType;

import java.time.LocalDateTime;

public class CsvParser {
    // Takes a CSV line and returns a Transaction object
    public Transaction parse(String line) {
        String[] token = line.split(",");

        String id = token[0];
        LocalDateTime timestamp = LocalDateTime.parse(token[1]);
        String accountId = token[2];
        TransactionType type = TransactionType.valueOf(token[3]);

        double amount = Double.parseDouble(token[4]);
        return new Transaction(id, timestamp, accountId, type, amount);
    }
}
