package org.kaleta.model;

import org.junit.jupiter.api.Test;
import org.kaleta.entity.Transaction;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.is;

/**
 * The years axis returned here drives the columns of every overall view, and the
 * balance sheet also loads its data for the last year on it, so a year that has no
 * data must never appear.
 */
public class AccountingYearlyDataTest
{
    private static Transaction closingIn(String year)
    {
        Transaction transaction = new Transaction();
        transaction.setYear(year);
        transaction.setDebit("701.0");
        transaction.setCredit("200.0");
        transaction.setAmount(0);
        return transaction;
    }

    @Test
    void runningYearFollowsTheClosedOnes()
    {
        AccountingYearlyData data = new AccountingYearlyData(
                List.of(closingIn("2023"), closingIn("2024")),
                List.of("2023", "2024", "2025"));

        assertThat(data.getYears(), is(arrayContaining("2023", "2024", "2025")));
    }

    @Test
    void runningYearIsNotInventedWhenItHasNoData()
    {
        // The desktop app writes a closing transaction into the year that is still
        // running, which used to make the axis end on a year that does not exist and
        // fail the overall balance sheet with a NullPointerException.
        AccountingYearlyData data = new AccountingYearlyData(
                List.of(closingIn("2023"), closingIn("2024"), closingIn("2025")),
                List.of("2023", "2024", "2025"));

        assertThat(data.getYears(), is(arrayContaining("2023", "2024", "2025")));
    }

    @Test
    void yearsWithoutAnyClosingAreStillReported()
    {
        AccountingYearlyData data = new AccountingYearlyData(List.of(), List.of("2023", "2024"));

        assertThat(data.getYears(), is(arrayContaining("2023", "2024")));
        assertThat(new AccountingYearlyData(List.of(), List.of()).getYears(), is(emptyArray()));
    }
}
