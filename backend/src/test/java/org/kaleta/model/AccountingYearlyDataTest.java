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
        return closing(year, "701.0", "200.0", 0);
    }

    private static Transaction closing(String year, String debit, String credit, int amount)
    {
        Transaction transaction = new Transaction();
        transaction.setYear(year);
        transaction.setDebit(debit);
        transaction.setCredit(credit);
        transaction.setAmount(amount);
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
    void oneBranchOfTheSchemaIsFollowedAcrossTheYears()
    {
        AccountingYearlyData data = new AccountingYearlyData(
                List.of(closing("2023", "701.0", "200.0", 10),
                        closing("2023", "701.0", "201.0", 5),
                        closing("2024", "210.0", "701.0", 7),
                        closing("2024", "701.0", "200.1", 3)),
                List.of("2023", "2024"));

        // the whole group, then the schema accounts it is made of
        assertThat(data.getYearlyValues("20"), is(arrayContaining(15, 3)));
        assertThat(data.getYearlyValues("200"), is(arrayContaining(10, 3)));
        assertThat(data.getYearlyValues("201"), is(arrayContaining(5, 0)));
        assertThat(data.getYearlyValues("21"), is(arrayContaining(0, 7)));
    }

    @Test
    void accumulatedDepreciationReducesWhatItBelongsTo()
    {
        // It is a contra-asset, so its closing entries count against its class. The class method
        // already reads it that way, and a group or account of it has to agree, or the rows an
        // overall statement expands into would not add up to the row above them.
        AccountingYearlyData data = new AccountingYearlyData(
                List.of(closing("2023", "090.0", "701.0", 400)),
                List.of("2023"));

        assertThat(data.getYearlyValues("09"), is(arrayContaining(-400)));
        assertThat(data.getYearlyValues("090"), is(arrayContaining(-400)));
        assertThat(data.getYearlyValues("09"), is(data.getYearlyClassValues("0", "9")));
    }

    @Test
    void yearsWithoutAnyClosingAreStillReported()
    {
        AccountingYearlyData data = new AccountingYearlyData(List.of(), List.of("2023", "2024"));

        assertThat(data.getYears(), is(arrayContaining("2023", "2024")));
        assertThat(new AccountingYearlyData(List.of(), List.of()).getYears(), is(emptyArray()));
    }
}
