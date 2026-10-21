package ua.kpi.comsys.dop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TransactionProcessor & Domain Models Test Suite")
class TransactionProcessorTest {

    private TransactionProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new TransactionProcessor();
    }

    @Nested
    @DisplayName("Process Method Pattern Matching Tests")
    class ProcessSuccessTests {

        @Test
        @DisplayName("Should correctly format Deposit transaction")
        void testProcessDeposit() {
            Transaction deposit = new Deposit("ACC-12345", new BigDecimal("150.50"));
            String expected = """
                    Transaction Type: DEPOSIT
                    Account: ACC-12345
                    Amount: USD 150.50""";

            String result = processor.process(deposit);
            assertEquals(expected, result);
        }

        @Test
        @DisplayName("Should correctly format Withdrawal transaction")
        void testProcessWithdrawal() {
            Transaction withdrawal = new Withdrawal("ACC-98765", new BigDecimal("49.99"), "Groceries");
            String expected = """
                    Transaction Type: WITHDRAWAL
                    Account: ACC-98765
                    Category: Groceries
                    Amount: USD 49.99""";

            String result = processor.process(withdrawal);
            assertEquals(expected, result);
        }

        @Test
        @DisplayName("Should correctly format Transfer transaction")
        void testProcessTransfer() {
            Transaction transfer = new Transfer("ACC-FROM", "ACC-TO", new BigDecimal("1000.00"));
            String expected = """
                    Transaction Type: TRANSFER
                    From: ACC-FROM
                    To: ACC-TO
                    Amount: USD 1000.00""";

            String result = processor.process(transfer);
            assertEquals(expected, result);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when transaction is null")
        void testProcessNullThrows() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> processor.process(null)
            );
            assertEquals("Transaction cannot be null", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Deposit Record Validation Tests")
    class DepositValidationTests {

        @ParameterizedTest(name = "Invalid accountId: \"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t", "\n"})
        void testInvalidAccountIdThrows(String accountId) {
            assertThrows(IllegalArgumentException.class, () ->
                    new Deposit(accountId, new BigDecimal("100.00"))
            );
        }

        @Test
        @DisplayName("Null amount throws IllegalArgumentException")
        void testNullAmountThrows() {
            assertThrows(IllegalArgumentException.class, () ->
                    new Deposit("ACC-001", null)
            );
        }

        @ParameterizedTest(name = "Non-positive amount: {0}")
        @ValueSource(strings = {"0", "0.00", "-0.01", "-100.00"})
        void testNonPositiveAmountThrows(String amountStr) {
            BigDecimal amount = new BigDecimal(amountStr);
            assertThrows(IllegalArgumentException.class, () ->
                    new Deposit("ACC-001", amount)
            );
        }

        @Test
        @DisplayName("Valid Deposit creation succeeds")
        void testValidDeposit() {
            Deposit deposit = new Deposit("ACC-VALID", new BigDecimal("250.75"));
            assertEquals("ACC-VALID", deposit.accountId());
            assertEquals(new BigDecimal("250.75"), deposit.amount());
        }
    }

    @Nested
    @DisplayName("Withdrawal Record Validation Tests")
    class WithdrawalValidationTests {

        @ParameterizedTest(name = "Invalid accountId: \"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   "})
        void testInvalidAccountIdThrows(String accountId) {
            assertThrows(IllegalArgumentException.class, () ->
                    new Withdrawal(accountId, new BigDecimal("50.00"), "Dining")
            );
        }

        @ParameterizedTest(name = "Invalid category: \"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t"})
        void testInvalidCategoryThrows(String category) {
            assertThrows(IllegalArgumentException.class, () ->
                    new Withdrawal("ACC-001", new BigDecimal("50.00"), category)
            );
        }

        @Test
        @DisplayName("Null amount throws IllegalArgumentException")
        void testNullAmountThrows() {
            assertThrows(IllegalArgumentException.class, () ->
                    new Withdrawal("ACC-001", null, "Utilities")
            );
        }

        @ParameterizedTest(name = "Non-positive amount: {0}")
        @ValueSource(strings = {"0", "-5.00"})
        void testNonPositiveAmountThrows(String amountStr) {
            assertThrows(IllegalArgumentException.class, () ->
                    new Withdrawal("ACC-001", new BigDecimal(amountStr), "Utilities")
            );
        }

        @Test
        @DisplayName("Valid Withdrawal creation succeeds")
        void testValidWithdrawal() {
            Withdrawal withdrawal = new Withdrawal("ACC-1", new BigDecimal("75.00"), "Travel");
            assertEquals("ACC-1", withdrawal.accountId());
            assertEquals(new BigDecimal("75.00"), withdrawal.amount());
            assertEquals("Travel", withdrawal.category());
        }
    }

    @Nested
    @DisplayName("Transfer Record Validation Tests")
    class TransferValidationTests {

        @ParameterizedTest(name = "Invalid fromAccount: \"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\n"})
        void testInvalidFromAccountThrows(String fromAccount) {
            assertThrows(IllegalArgumentException.class, () ->
                    new Transfer(fromAccount, "TO-ACC", new BigDecimal("100.00"))
            );
        }

        @ParameterizedTest(name = "Invalid toAccount: \"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t"})
        void testInvalidToAccountThrows(String toAccount) {
            assertThrows(IllegalArgumentException.class, () ->
                    new Transfer("FROM-ACC", toAccount, new BigDecimal("100.00"))
            );
        }

        @Test
        @DisplayName("Null amount throws IllegalArgumentException")
        void testNullAmountThrows() {
            assertThrows(IllegalArgumentException.class, () ->
                    new Transfer("FROM-ACC", "TO-ACC", null)
            );
        }

        @ParameterizedTest(name = "Non-positive amount: {0}")
        @ValueSource(strings = {"0", "-1.00"})
        void testNonPositiveAmountThrows(String amountStr) {
            assertThrows(IllegalArgumentException.class, () ->
                    new Transfer("FROM-ACC", "TO-ACC", new BigDecimal(amountStr))
            );
        }

        @Test
        @DisplayName("Valid Transfer creation succeeds")
        void testValidTransfer() {
            Transfer transfer = new Transfer("SRC", "DST", new BigDecimal("500.00"));
            assertEquals("SRC", transfer.fromAccount());
            assertEquals("DST", transfer.toAccount());
            assertEquals(new BigDecimal("500.00"), transfer.amount());
        }
    }
}
