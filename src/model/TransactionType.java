package model;

public enum TransactionType {
    CREDIT("credit"),
    DEBIT("debit");

    private final String value;

    TransactionType(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}