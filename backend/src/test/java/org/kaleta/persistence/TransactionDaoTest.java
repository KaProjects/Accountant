package org.kaleta.persistence;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.kaleta.dao.TransactionDao;
import org.kaleta.entity.Transaction;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

/**
 * Pins down the query semantics the budgeting and statement services rely on:
 * exact versus wildcard account matching, description filters including the "!"
 * negation, and which queries exclude the off-balance 7xx accounts.
 * <p>
 * Everything is written into year 1999 so it cannot collide with the seeded test
 * data, and {@link TestTransaction} rolls each test back.
 */
@QuarkusTest
class TransactionDaoTest
{
    private static final String YEAR = "1999";

    @Inject
    EntityManager entityManager;

    @Inject
    TransactionDao transactionDao;

    private void insert(String id, String date, int amount, String debit, String credit, String description)
    {
        entityManager.createNativeQuery(
                        "INSERT INTO Transaction (id, year, date, description, amount, debit, credit) VALUES (?,?,?,?,?,?,?)")
                .setParameter(1, id)
                .setParameter(2, YEAR)
                .setParameter(3, date)
                .setParameter(4, description)
                .setParameter(5, amount)
                .setParameter(6, debit)
                .setParameter(7, credit)
                .executeUpdate();
    }

    private static List<String> amountsOf(List<Transaction> transactions)
    {
        return transactions.stream().map(t -> String.valueOf(t.getAmount())).toList();
    }

    @Test
    @TestTransaction
    void listByAccountsMatchesAnExactAccountId()
    {
        insert("t1", "0101", 100, "510.0", "210.0", "");
        insert("t2", "0101", 200, "510.1", "210.0", "");
        entityManager.flush();

        List<Transaction> result = transactionDao.listByAccounts(YEAR, "510.0", "", "");

        assertThat(amountsOf(result), containsInAnyOrder("100"));
    }

    @Test
    @TestTransaction
    void listByAccountsTreatsAPercentAsAPrefixMatch()
    {
        insert("t1", "0101", 100, "510.0", "210.0", "");
        insert("t2", "0101", 200, "510.1", "210.0", "");
        insert("t3", "0101", 300, "520.0", "210.0", "");
        entityManager.flush();

        List<Transaction> result = transactionDao.listByAccounts(YEAR, "510%", "", "");

        assertThat(amountsOf(result), containsInAnyOrder("100", "200"));
    }

    @Test
    @TestTransaction
    void listByAccountsFiltersByDescriptionSubstring()
    {
        insert("t1", "0101", 100, "510.0", "210.0", "summer vacation");
        insert("t2", "0101", 200, "510.0", "210.0", "groceries");
        entityManager.flush();

        List<Transaction> result = transactionDao.listByAccounts(YEAR, "510.0", "", "vac");

        assertThat(amountsOf(result), containsInAnyOrder("100"));
    }

    @Test
    @TestTransaction
    void listByAccountsNegatesADescriptionPrefixedWithBang()
    {
        insert("t1", "0101", 100, "510.0", "210.0", "summer vacation");
        insert("t2", "0101", 200, "510.0", "210.0", "groceries");
        entityManager.flush();

        List<Transaction> result = transactionDao.listByAccounts(YEAR, "510.0", "", "!vac");

        assertThat(amountsOf(result), containsInAnyOrder("200"));
    }

    @Test
    @TestTransaction
    void listByAccountsExcludesOffBalanceAccounts()
    {
        insert("t1", "0101", 100, "510.0", "210.0", "");
        insert("t2", "0101", 200, "510.0", "701.0", "");
        insert("t3", "0101", 300, "700.0", "210.0", "");
        entityManager.flush();

        List<Transaction> result = transactionDao.listByAccounts(YEAR, "", "", "");

        assertThat(amountsOf(result), containsInAnyOrder("100"));
    }

    @Test
    @TestTransaction
    void listByDescriptionMatchingAlsoExcludesOffBalanceAccounts()
    {
        insert("t1", "0101", 100, "510.0", "210.0", "rent");
        insert("t2", "0101", 200, "510.0", "701.0", "rent");
        entityManager.flush();

        assertThat(amountsOf(transactionDao.listByDescriptionMatching(YEAR, "rent")), containsInAnyOrder("100"));
    }

    @Test
    @TestTransaction
    void listByDescriptionMatchingWithAnEmptyStringReturnsEveryOnBalanceTransaction()
    {
        insert("t1", "0101", 100, "510.0", "210.0", "rent");
        insert("t2", "0101", 200, "520.0", "210.0", "");
        insert("t3", "0101", 300, "510.0", "700.0", "closing");
        entityManager.flush();

        assertThat(amountsOf(transactionDao.listByDescriptionMatching(YEAR, "")), containsInAnyOrder("100", "200"));
    }

    @Test
    @TestTransaction
    void listBySchemaZeroPadsASingleDigitMonth()
    {
        // dates are DDMM, so 1503 is 15 March
        insert("t1", "1503", 100, "510.0", "210.0", "");
        insert("t2", "0511", 200, "510.0", "210.0", "");
        entityManager.flush();

        assertThat(amountsOf(transactionDao.listBySchema(YEAR, "510", "3")), containsInAnyOrder("100"));
        assertThat(amountsOf(transactionDao.listBySchema(YEAR, "510", "03")), containsInAnyOrder("100"));
        assertThat(amountsOf(transactionDao.listBySchema(YEAR, "510", "11")), containsInAnyOrder("200"));
    }

    @Test
    @TestTransaction
    void listForClasses2456IgnoresOtherClasses()
    {
        insert("t1", "0101", 100, "510.0", "210.0", "");
        insert("t2", "0101", 200, "310.0", "320.0", "");
        insert("t3", "0101", 300, "410.0", "210.0", "");
        entityManager.flush();

        assertThat(amountsOf(transactionDao.listForClasses2456(YEAR)), containsInAnyOrder("100", "300"));
    }

    @Test
    @TestTransaction
    void listProfitTransactionsSelectsOnlyClasses5And6()
    {
        insert("t1", "0101", 100, "510.0", "210.0", "");
        insert("t2", "0101", 200, "210.0", "600.0", "");
        insert("t3", "0101", 300, "210.0", "220.0", "");
        entityManager.flush();

        assertThat(amountsOf(transactionDao.listProfitTransactions(YEAR)), containsInAnyOrder("100", "200"));
    }

    @Test
    @TestTransaction
    void listFinancialAssetTransactionsCoversBoth23xAnd546Accounts()
    {
        insert("t1", "0101", 100, "230.0", "210.0", "");
        insert("t2", "0101", 200, "546.0", "210.0", "");
        insert("t3", "0101", 300, "510.0", "210.0", "");
        entityManager.flush();

        assertThat(amountsOf(transactionDao.listFinancialAssetTransactions(YEAR)), containsInAnyOrder("100", "200"));
    }

    @Test
    @TestTransaction
    void listByPrefixesMatchesDebitAndCreditIndependently()
    {
        insert("t1", "0101", 100, "510.0", "210.0", "");
        insert("t2", "0101", 200, "510.0", "220.0", "");
        entityManager.flush();

        assertThat(amountsOf(transactionDao.list(YEAR, "510", "21")), containsInAnyOrder("100"));
        assertThat(amountsOf(transactionDao.list(YEAR, "510", "")), containsInAnyOrder("100", "200"));
    }

    @Test
    @TestTransaction
    void yearScopingKeepsOtherYearsOut()
    {
        insert("t1", "0101", 100, "510.0", "210.0", "");
        entityManager.flush();

        assertThat(transactionDao.list("1998"), is(empty()));
        assertThat(amountsOf(transactionDao.list(YEAR)), containsInAnyOrder("100"));
    }
}
