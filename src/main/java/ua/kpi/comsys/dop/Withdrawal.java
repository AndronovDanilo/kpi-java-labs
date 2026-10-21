package ua.kpi.comsys.dop;

import java.math.BigDecimal;

public record Withdrawal(String accountId, BigDecimal amount, String category) implements Transaction {
    public Withdrawal {
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("accountId cannot be null or blank");
        }
        if (amount == null) {
            throw new IllegalArgumentException("amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("category cannot be null or blank");
        }
    }
}
