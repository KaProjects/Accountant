package org.kaleta.rest;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kaleta.Utils;
import org.kaleta.dto.AccountingDto;

import java.util.List;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Every overall statement shows a closed year as read off its year-end closing, and every year's
 * own statement builds the same year up month by month. The two have to agree, row for row: for
 * the income statement the months add up to the year, for the balance sheet and the cash flow the
 * opening balance and the months do.
 * <p>
 * The year seeded here is a small closed year in the shape of the desktop data, including what
 * made the real years disagree: transactions from an account to itself, an opening balance below
 * zero written the other way round, and corrections booked against 700.0 on an expense account.
 * It goes into 2016, which the seeded test data does not use, and {@link TestTransaction} rolls
 * it back after each test.
 */
@QuarkusTest
class StatementsAgreeWithClosingTest
{
    private static final String YEAR = "2016";

    @Inject
    EntityManager entityManager;

    @Inject
    AccountingResource accountingResource;

    private int transactionId = 0;

    @BeforeEach
    void resetIds()
    {
        transactionId = 0;
    }

    private void seedClosedYear()
    {
        // the same chart of accounts as the running year, so the rows line up position by position
        entityManager.createNativeQuery("INSERT INTO ASchema (year, id, name, type)"
                + " SELECT '" + YEAR + "', id, name, type FROM ASchema WHERE year = '2020'").executeUpdate();
        for (String schemaId : List.of("200", "210", "220", "230", "400", "510", "550", "600", "630", "700"))
        {
            entityManager.createNativeQuery("INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES (?,?,'0','general','')")
                    .setParameter(1, YEAR)
                    .setParameter(2, schemaId)
                    .executeUpdate();
        }

        // opening balances, one of them below zero and so written the other way round
        transaction("0101", 3000, "200.0", "700.0", "initiation");
        transaction("0112", 300, "700.0", "210.0", "zostatok");
        transaction("0101", 2000, "700.0", "220.0", "initiation");
        transaction("0101", 700, "700.0", "400.0", "initiation");

        // the year
        transaction("0502", 5000, "210.0", "600.0", "salary");
        transaction("0303", 400, "510.0", "200.0", "groceries");
        transaction("0909", 500, "550.0", "200.0", "fee");
        transaction("0110", 200, "210.0", "630.0", "interest");
        transaction("0111", 1000, "220.0", "210.0", "repayment");

        // transactions from an account to itself, on each kind of account
        transaction("0104", 250, "210.0", "210.0", "");
        transaction("0106", 120, "600.0", "600.0", "preuctovanie");
        transaction("0107", 80, "510.0", "510.0", "");
        transaction("0108", 90, "220.0", "220.0", "");

        // corrections against 700.0 on an expense account, and the asset found to square them
        transaction("0101", 600, "550.0", "700.0", "nezauctovane odpisy");
        transaction("3112", 900, "700.0", "550.0", "najdene");
        transaction("3112", 300, "230.0", "700.0", "zostatok");

        // the year-end closing, as the desktop app writes it
        transaction("3112", 2100, "701.0", "200.0", "closure");
        transaction("3112", 3900, "701.0", "210.0", "closure");
        transaction("3112", 300, "701.0", "230.0", "closure");
        transaction("3112", 1000, "220.0", "701.0", "closure");
        transaction("3112", 700, "400.0", "701.0", "closure");
        transaction("3112", 5000, "600.0", "710.0", "closure");
        transaction("3112", 200, "630.0", "710.0", "closure");
        transaction("3112", 400, "710.0", "510.0", "closure");
        transaction("3112", 200, "710.0", "550.0", "closure");
        transaction("3112", 4600, "710.0", "701.0", "closure");

        entityManager.flush();
    }

    private void transaction(String date, int amount, String debit, String credit, String description)
    {
        entityManager.createNativeQuery(
                        "INSERT INTO Transaction (id, year, date, description, amount, debit, credit) VALUES (?,?,?,?,?,?,?)")
                .setParameter(1, YEAR + "-" + (++transactionId))
                .setParameter(2, YEAR)
                .setParameter(3, date)
                .setParameter(4, description)
                .setParameter(5, amount)
                .setParameter(6, debit)
                .setParameter(7, credit)
                .executeUpdate();
    }

    private static AccountingDto entity(jakarta.ws.rs.core.Response response)
    {
        assertThat(response.getStatus(), is(200));
        return (AccountingDto) response.getEntity();
    }

    /**
     * Walks both statements side by side. Rows are matched by position, because schema ids
     * repeat: both sides of the balance sheet have classes 2 and 3, and the income statement
     * comes back to groups 55 and 63 after the operating profit.
     */
    private static void assertAgree(List<AccountingDto.Row> overall, int column, List<AccountingDto.Row> yearly,
                                     Function<AccountingDto.Row, Integer> yearOf, String path)
    {
        assertThat(path + " rows", yearly.size(), is(overall.size()));
        for (int i = 0; i < overall.size(); i++)
        {
            AccountingDto.Row overallRow = overall.get(i);
            AccountingDto.Row yearlyRow = yearly.get(i);
            String rowPath = path + "/" + overallRow.getSchemaId() + " " + overallRow.getName();

            assertThat(rowPath, yearOf.apply(yearlyRow), is(overallRow.getYearlyValues()[column]));
            assertAgree(overallRow.getChildren(), column, yearlyRow.getChildren(), yearOf, rowPath);
        }
    }

    private static int column(AccountingDto overall)
    {
        // the first column is the label, then one per year
        return overall.getColumns().indexOf(YEAR) - 1;
    }

    private static Integer monthsOf(AccountingDto.Row row)
    {
        return Utils.sumArray(row.getMonthlyValues());
    }

    private static Integer openingAndMonthsOf(AccountingDto.Row row)
    {
        return (row.getInitial() == null ? 0 : row.getInitial()) + Utils.sumArray(row.getMonthlyValues());
    }

    @Test
    @TestTransaction
    void incomeStatementOfAClosedYearAgreesWithItsClosing()
    {
        seedClosedYear();

        AccountingDto overall = entity(accountingResource.getOverallProfit());
        AccountingDto yearly = entity(accountingResource.getProfit(YEAR));

        assertAgree(overall.getRows(), column(overall), yearly.getRows(), StatementsAgreeWithClosingTest::monthsOf, "");
    }

    @Test
    @TestTransaction
    void balanceSheetOfAClosedYearAgreesWithItsClosing()
    {
        seedClosedYear();

        AccountingDto overall = entity(accountingResource.getOverallBalanceSheet());
        AccountingDto yearly = entity(accountingResource.getBalanceSheet(YEAR));

        assertAgree(overall.getRows(), column(overall), yearly.getRows(), StatementsAgreeWithClosingTest::openingAndMonthsOf, "");
    }

    @Test
    @TestTransaction
    void balanceSheetOfAClosedYearBalances()
    {
        seedClosedYear();

        List<AccountingDto.Row> rows = entity(accountingResource.getBalanceSheet(YEAR)).getRows();
        AccountingDto.Row assets = rows.stream().filter(row -> row.getSchemaId().equals("a")).findFirst().orElseThrow();
        AccountingDto.Row liabilities = rows.stream().filter(row -> row.getSchemaId().equals("l")).findFirst().orElseThrow();

        assertThat(openingAndMonthsOf(assets), is(6300));
        assertThat(openingAndMonthsOf(liabilities), is(6300));
    }

    @Test
    @TestTransaction
    void cashFlowOfAClosedYearAgreesWithItsClosing()
    {
        seedClosedYear();

        AccountingDto overall = entity(accountingResource.getOverallCashFlow());
        AccountingDto yearly = entity(accountingResource.getCashFlow(YEAR));

        assertAgree(overall.getRows(), column(overall), yearly.getRows(), StatementsAgreeWithClosingTest::openingAndMonthsOf, "");
    }
}
