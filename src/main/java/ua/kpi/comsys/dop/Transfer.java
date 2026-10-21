package ua.kpi.comsys.dop;

import java.math.BigDecimal;

public record Transfer(String fromAccount, String toAccount, BigDecimal amount) implements Transaction {
    public Transfer {
        if (fromAccount == null || fromAccount.isBlank()) {
            throw new IllegalArgumentException("fromAccount cannot be null or blank");
        }
        if (toAccount == null || toAccount.isBlank()) {
            throw new IllegalArgumentException("toAccount cannot be null or blank");
        }
        if (amount == null) {
            throw new IllegalArgumentException("amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
    }
}
