package ua.kpi.comsys.dop;

import java.math.BigDecimal;

public record Deposit(String accountId, BigDecimal amount) implements Transaction {
    public Deposit {
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("accountId cannot be null or blank");
        }
        if (amount == null) {
            throw new IllegalArgumentException("amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
    }
}
