package org.kaleta.model;

import org.junit.jupiter.api.Test;
import org.kaleta.Constants;
import org.kaleta.entity.Account;
import org.kaleta.entity.Transaction;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.kaleta.framework.Generator.account;
import static org.kaleta.framework.Generator.transaction;

/**
 * Covers the engine behind the balance sheet, income statement and cash flow:
 * how a transaction moves an account's monthly balance depending on whether the
 * account is a debit or a credit one, and how opening balances are read from the
 * 700.0 initial account.
 */
class AccountingDataTest
{
    private static final String YEAR = "2024";

    private static SchemaClass schemaClass(String classId, String groupId, SchemaClass.Group.Account... accounts)
    {
        SchemaClass schemaClass = new SchemaClass(classId, "class " + classId);
        SchemaClass.Group group = new SchemaClass.Group(groupId, "group " + groupId);
        for (SchemaClass.Group.Account account : accounts) {
            group.addAccount(account);
        }
        schemaClass.addGroup(group);
        return schemaClass;
    }

    private static SchemaClass.Group.Account schemaAccount(String id, Constants.AccountType type)
    {
        return new SchemaClass.Group.Account(id, "account " + id, type);
    }

    @Test
    void aDebitAccountGrowsWhenItIsDebitedAndShrinksWhenCredited()
    {
        // 210 is a bank account (asset), so money in is a debit
        SchemaClass schema = schemaClass("2", "21", schemaAccount("210", Constants.AccountType.A));
        List<Transaction> transactions = List.of(
                transaction(YEAR, "0101", 1000, "210.0", "700.0"),   // opening balance
                transaction(YEAR, "0502", 300, "210.0", "600.0"),    // income received
                transaction(YEAR, "0803", 120, "510.0", "210.0"));   // money spent

        GroupComponent group = new AccountingData(transactions, List.of(account(YEAR, "210.0")), schema)
                .getGroupComponent("21", "0");

        GroupComponent.AccountComponent bank = group.getAccounts().get(0);
        assertThat(bank.getInitialValue(), is(1000));
        assertThat(bank.getMonthlyBalance(), is(arrayContaining(0, 300, -120, 0, 0, 0, 0, 0, 0, 0, 0, 0)));
    }

    @Test
    void aCreditAccountMovesTheOppositeWay()
    {
        // 220 is a loan (liability), so drawing it down is a credit
        SchemaClass schema = schemaClass("2", "22", schemaAccount("220", Constants.AccountType.L));
        List<Transaction> transactions = List.of(
                transaction(YEAR, "0101", 5000, "700.0", "220.0"),   // opening balance
                transaction(YEAR, "0502", 400, "220.0", "210.0"));   // repayment

        GroupComponent group = new AccountingData(transactions, List.of(account(YEAR, "220.0")), schema)
                .getGroupComponent("22", "0");

        GroupComponent.AccountComponent loan = group.getAccounts().get(0);
        assertThat(loan.getInitialValue(), is(5000));
        assertThat(loan.getMonthlyBalance(), is(arrayContaining(0, -400, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)));
    }

    @Test
    void offBalanceTransactionsAreLeftOutOfMonthlyBalances()
    {
        SchemaClass schema = schemaClass("5", "51", schemaAccount("510", Constants.AccountType.E));
        List<Transaction> transactions = List.of(
                transaction(YEAR, "0101", 100, "510.0", "210.0"),
                transaction(YEAR, "0101", 900, "510.0", "701.0"));   // year-end closing

        GroupComponent group = new AccountingData(transactions, List.of(account(YEAR, "510.0")), schema)
                .getGroupComponent("51", "0");

        assertThat(group.getAccounts().get(0).getMonthlyBalance()[0], is(100));
    }

    @Test
    void severalAccountsUnderOneSchemaAccountAreSummed()
    {
        SchemaClass schema = schemaClass("2", "21", schemaAccount("210", Constants.AccountType.A));
        List<Transaction> transactions = List.of(
                transaction(YEAR, "0101", 100, "210.0", "700.0"),
                transaction(YEAR, "0101", 400, "210.1", "700.0"),
                transaction(YEAR, "0502", 30, "210.0", "600.0"),
                transaction(YEAR, "0502", 70, "210.1", "600.0"));

        GroupComponent group = new AccountingData(transactions,
                List.of(account(YEAR, "210.0"), account(YEAR, "210.1")), schema)
                .getGroupComponent("21", "0");

        GroupComponent.AccountComponent bank = group.getAccounts().get(0);
        assertThat(bank.getInitialValue(), is(500));
        assertThat(bank.getMonthlyBalance()[1], is(100));
    }

    @Test
    void expenseAndRevenueAccountsCarryNoOpeningBalance()
    {
        SchemaClass schema = schemaClass("5", "51", schemaAccount("510", Constants.AccountType.E));
        List<Transaction> transactions = List.of(transaction(YEAR, "0203", 250, "510.0", "210.0"));

        GroupComponent group = new AccountingData(transactions, List.of(account(YEAR, "510.0")), schema)
                .getGroupComponent("51", "0");

        // no 700.0 transaction is required, and none is read
        assertThat(group.getAccounts().get(0).getMonthlyBalance()[2], is(250));
    }

    @Test
    void aBalanceAccountWithoutExactlyOneOpeningTransactionIsRejected()
    {
        SchemaClass schema = schemaClass("2", "21", schemaAccount("210", Constants.AccountType.A));
        List<Transaction> transactions = List.of(
                transaction(YEAR, "0101", 100, "210.0", "700.0"),
                transaction(YEAR, "0101", 200, "210.0", "700.0"));

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> new AccountingData(transactions, List.of(account(YEAR, "210.0")), schema)
                        .getGroupComponent("21", "0"));

        assertThat(e.getMessage(), containsString("Illegal number of initial transactions"));
    }

    @Test
    void accumulatedEarningsMayCarryTwoOpeningTransactions()
    {
        // 401.0 is the one account allowed a split opening balance
        SchemaClass schema = schemaClass("4", "40", schemaAccount("401", Constants.AccountType.L));
        List<Transaction> transactions = List.of(
                transaction(YEAR, "0101", 700, "700.0", "401.0"),
                transaction(YEAR, "0101", 300, "700.0", "401.0"));

        GroupComponent group = new AccountingData(transactions, List.of(account(YEAR, "401.0")), schema)
                .getGroupComponent("40", "1");

        assertThat(group.getAccounts().get(0).getInitialValue(), is(1000));
    }

    @Test
    void schemaAccountsMissingFromAnOlderYearAreSkipped()
    {
        SchemaClass schema = schemaClass("2", "21", schemaAccount("210", Constants.AccountType.A));

        GroupComponent group = new AccountingData(List.of(), List.of(), schema)
                .getGroupComponent("21", "0", "9");

        // suffix 9 does not exist in this schema, so only one account component is built
        assertThat(group.getAccounts().size(), is(1));
    }

    @Test
    void aClassComponentCollectsItsGroups()
    {
        SchemaClass schema = schemaClass("2", "21", schemaAccount("210", Constants.AccountType.A));
        List<Transaction> transactions = List.of(transaction(YEAR, "0101", 100, "210.0", "700.0"));

        ClassComponent classComponent = new AccountingData(transactions, List.of(account(YEAR, "210.0")), schema)
                .getClassComponent();

        assertThat(classComponent.getSchemaId(), is("2"));
        assertThat(classComponent.getGroups().size(), is(1));
        assertThat(classComponent.getGroups().get(0).getSchemaId(), is("21"));
    }
}
