package ua.kpi.comsys.dop;

public sealed interface Transaction permits Deposit, Withdrawal, Transfer {
}
