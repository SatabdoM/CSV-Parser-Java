package model;
import java.time.LocalDateTime;

public record Transaction(String id,
                          LocalDateTime timestamp,
                          String accountId,
                          TransactionType type,
                          double amount) {
}
