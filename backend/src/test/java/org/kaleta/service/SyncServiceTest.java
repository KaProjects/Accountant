package org.kaleta.service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kaleta.persistence.api.AccountDao;
import org.kaleta.persistence.api.SchemaDao;
import org.kaleta.persistence.api.TransactionDao;
import org.kaleta.persistence.entity.Account;
import org.kaleta.persistence.entity.Schema;
import org.kaleta.persistence.entity.Transaction;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.kaleta.framework.Generator.account;
import static org.kaleta.framework.Generator.schema;
import static org.kaleta.framework.Generator.transaction;
import static org.mockito.Mockito.when;

/**
 * The checks a synced year has to pass, each one broken on its own in an otherwise valid year.
 */
@QuarkusTest
class SyncServiceTest
{
    @InjectMock
    TransactionDao transactionDao;
    @InjectMock
    SchemaDao schemaDao;
    @InjectMock
    AccountDao accountDao;

    @Inject
    SyncService syncService;

    private final List<Schema> schemas = new ArrayList<>();
    private final List<Account> accounts = new ArrayList<>();
    private final List<Transaction> transactions = new ArrayList<>();

    /** A small valid year: a bank account, an expense and an income, which balance. */
    @BeforeEach
    void validYear()
    {
        schemas.addAll(List.of(
                schema("2024", "2", "Finance", ""), schema("2024", "21", "bank", ""), schema("2024", "210", "current", "A"),
                schema("2024", "4", "Funding", ""), schema("2024", "40", "capital", ""), schema("2024", "400", "own", "L"),
                schema("2024", "5", "Expenses", ""), schema("2024", "51", "consumption", ""), schema("2024", "510", "food", "E"),
                schema("2024", "6", "Revenues", ""), schema("2024", "60", "work", ""), schema("2024", "600", "salary", "R")));
        accounts.addAll(List.of(account("2024", "210.0"), account("2024", "400.0"), account("2024", "510.0"), account("2024", "600.0")));
        transactions.addAll(List.of(
                transaction("2024", "0101", 100, "210.0", "400.0"),
                transaction("2024", "1502", 50, "210.0", "600.0"),
                transaction("2024", "2003", 30, "510.0", "210.0")));
        when(schemaDao.list("2024")).thenReturn(schemas);
        when(accountDao.list("2024")).thenReturn(accounts);
        when(transactionDao.list("2024")).thenReturn(transactions);
    }

    @Test
    void aValidYearPasses()
    {
        assertThat(syncService.validate("2024", true), is("year 2024 data valid"));
    }

    @Test
    void aTransactionDateHasToBeADayAndAMonth()
    {
        transactions.add(transaction("2024", "3213", 1, "210.0", "600.0"));

        assertThat(syncService.validate("2024", true), containsString("invalid day number"));

        transactions.set(transactions.size() - 1, transaction("2024", "0113", 1, "210.0", "600.0"));
        assertThat(syncService.validate("2024", true), containsString("invalid month number"));

        transactions.set(transactions.size() - 1, transaction("2024", "011", 1, "210.0", "600.0"));
        assertThat(syncService.validate("2024", true), containsString("invalid length"));

        transactions.set(transactions.size() - 1, transaction("2024", "0x01", 1, "210.0", "600.0"));
        assertThat(syncService.validate("2024", true), containsString("NumberFormatException"));
    }

    @Test
    void aTransactionHasToBeBetweenAccountsThatExist()
    {
        transactions.add(transaction("2024", "0104", 1, "211.0", "600.0"));
        assertThat(syncService.validate("2024", true), containsString("debit account not found"));

        transactions.set(transactions.size() - 1, transaction("2024", "0104", 1, "210.0", "601.0"));
        assertThat(syncService.validate("2024", true), containsString("credit account not found"));
    }

    @Test
    void anAccountHasToBelongToASchemaAccount()
    {
        accounts.add(account("2024", "520.0"));

        assertThat(syncService.validate("2024", true), is("year 2024 account 520.0: schema not found"));
    }

    @Test
    void onlySchemaAccountsHaveATypeAndEachHasOne()
    {
        schemas.set(0, schema("2024", "2", "Finance", "A"));
        assertThat(syncService.validate("2024", true), containsString("schema 2: shouldn't have type assigned"));
        schemas.set(0, schema("2024", "2", "Finance", ""));

        schemas.set(1, schema("2024", "21", "bank", "A"));
        assertThat(syncService.validate("2024", true), containsString("schema 21: shouldn't have type assigned"));
        schemas.set(1, schema("2024", "21", "bank", ""));

        schemas.set(2, schema("2024", "210", "current", ""));
        assertThat(syncService.validate("2024", true), containsString("schema 210: should have type assigned"));
    }

    @Test
    void aSchemaAccountHasToBeInAClassAndAGroup()
    {
        schemas.add(schema("2024", "311", "receivable", "A"));
        assertThat(syncService.validate("2024", true), containsString("schema 311: couldn't find schema class"));

        schemas.add(schema("2024", "3", "Relations", ""));
        assertThat(syncService.validate("2024", true), containsString("schema 311: couldn't find schema group"));
    }

    @Test
    void aRunningYearHasToBalance()
    {
        // assets of 120 against liabilities of 100 and a profit of 20; a move within one side
        // keeps it so
        transactions.add(transaction("2024", "2503", 5, "210.0", "210.0"));
        assertThat(syncService.validate("2024", true), is("year 2024 data valid"));

        // money that came from off the balance sheet leaves the assets 5 over
        schemas.addAll(List.of(schema("2024", "7", "Off-balance", ""), schema("2024", "79", "other", ""), schema("2024", "790", "other", "X")));
        accounts.add(account("2024", "790.0"));
        transactions.add(transaction("2024", "2703", 5, "210.0", "790.0"));
        assertThat(syncService.validate("2024", true),
                is("year 2024: assets='125' != liabilities='100' + revenues='50' - expenses='30'"));
    }

    @Test
    void everyAccountOfAClosedYearHasToEndAtNought()
    {
        assertThat(syncService.validate("2024", false), is("year 2024 account 210.0: debit='150' != credit='30'"));

        // closed: every account's balance moved to the closing account
        accounts.add(account("2024", "701.0"));
        schemas.addAll(List.of(schema("2024", "7", "Off-balance", ""), schema("2024", "70", "closing", ""), schema("2024", "701", "closing", "X")));
        transactions.add(transaction("2024", "3112", 120, "701.0", "210.0"));
        transactions.add(transaction("2024", "3112", 100, "400.0", "701.0"));
        transactions.add(transaction("2024", "3112", 50, "600.0", "701.0"));
        transactions.add(transaction("2024", "3112", 30, "701.0", "510.0"));
        assertThat(syncService.validate("2024", false), is("year 2024 data valid"));
    }
}
