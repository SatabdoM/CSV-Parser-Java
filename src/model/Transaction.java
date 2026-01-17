package model;

public record Transactions(String id, LocalDateTime timestamp, double amount, String type) {
}
