package org.kaleta.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.kaleta.dto.AccountingDto;

import java.util.List;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

/**
 * The statements as the service assembles them, without HTTP between: what each summary row is
 * made of. These hold for any books, so they are asserted on the fixtures without restating any
 * of their figures.
 */
@QuarkusTest
class StatementServiceTest
{
    @Inject
    StatementService statementService;

    private static AccountingDto.Row row(AccountingDto statement, String schemaId)
    {
        List<AccountingDto.Row> matching = statement.getRows().stream().filter(row -> row.getSchemaId().equals(schemaId)).toList();
        return matching.get(0);
    }

    /** The given rows added up column by column, each signed: + adds it, - takes it away. */
    private static Integer[] combined(Function<AccountingDto.Row, Integer[]> values, Object... signedRows)
    {
        Integer[] sum = null;
        for (int i = 0; i < signedRows.length; i += 2) {
            int sign = signedRows[i].equals("+") ? 1 : -1;
            Integer[] row = values.apply((AccountingDto.Row) signedRows[i + 1]);
            if (sum == null) sum = new Integer[row.length];
            for (int c = 0; c < row.length; c++) sum[c] = (sum[c] == null ? 0 : sum[c]) + sign * row[c];
        }
        return sum;
    }

    private static void assertProfitLevels(AccountingDto profit, Function<AccountingDto.Row, Integer[]> values)
    {
        List<AccountingDto.Row> rows = profit.getRows();
        assertThat(rows.stream().map(AccountingDto.Row::getSchemaId).toList(), contains(
                "60", "55", "63", "ni", "51", "52", "53", "op", "50", "61", "56", "62", "54", "63", "55", "np"));

        assertThat(values.apply(rows.get(3)), is(combined(values, "+", rows.get(0), "-", rows.get(1), "+", rows.get(2))));
        assertThat(values.apply(rows.get(7)), is(combined(values,
                "+", rows.get(3), "-", rows.get(4), "-", rows.get(5), "-", rows.get(6))));
        assertThat(values.apply(rows.get(15)), is(combined(values, "+", rows.get(7), "-", rows.get(8),
                "+", rows.get(9), "-", rows.get(10), "+", rows.get(11), "-", rows.get(12), "+", rows.get(13), "-", rows.get(14))));
    }

    @Test
    void anIncomeStatementWorksDownFromNetIncomeToNetProfit()
    {
        assertProfitLevels(statementService.getProfit("2019"), AccountingDto.Row::getMonthlyValues);
        assertProfitLevels(statementService.getOverallProfit(), AccountingDto.Row::getYearlyValues);
    }

    @Test
    void aCashFlowIsItsGroupsTogether()
    {
        AccountingDto yearly = statementService.getCashFlow("2020");
        List<AccountingDto.Row> rows = yearly.getRows();
        assertThat(rows.stream().map(AccountingDto.Row::getSchemaId).toList(), contains("20", "21", "23", "22", "cf"));
        assertThat(rows.get(4).getMonthlyValues(), is(combined(AccountingDto.Row::getMonthlyValues,
                "+", rows.get(0), "+", rows.get(1), "+", rows.get(2), "+", rows.get(3))));
        assertThat(rows.get(4).getInitial(), is(rows.get(0).getInitial() + rows.get(1).getInitial()
                + rows.get(2).getInitial() + rows.get(3).getInitial()));

        List<AccountingDto.Row> overall = statementService.getOverallCashFlow().getRows();
        assertThat(overall.get(4).getYearlyValues(), is(combined(AccountingDto.Row::getYearlyValues,
                "+", overall.get(0), "+", overall.get(1), "+", overall.get(2), "+", overall.get(3))));
    }

    @Test
    void eachSideOfABalanceSheetIsItsClassesTogether()
    {
        for (AccountingDto balance : List.of(statementService.getBalanceSheet("2020"), statementService.getOverallBalanceSheet())) {
            Function<AccountingDto.Row, Integer[]> values = balance.getRows().get(0).getMonthlyValues() != null
                    ? AccountingDto.Row::getMonthlyValues : AccountingDto.Row::getYearlyValues;
            List<AccountingDto.Row> rows = balance.getRows();
            assertThat(rows.stream().map(AccountingDto.Row::getSchemaId).toList(),
                    contains("a", "0", "1", "2", "3", "l", "2", "3", "4", "p"));
            assertThat(values.apply(row(balance, "a")), is(combined(values,
                    "+", rows.get(1), "+", rows.get(2), "+", rows.get(3), "+", rows.get(4))));
            assertThat(values.apply(row(balance, "l")), is(combined(values,
                    "+", rows.get(6), "+", rows.get(7), "+", rows.get(8), "+", rows.get(9))));
        }
    }
}
