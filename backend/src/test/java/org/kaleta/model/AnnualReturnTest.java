package org.kaleta.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

class AnnualReturnTest
{
    private static Integer[] months(int count)
    {
        Integer[] values = new Integer[count];
        Arrays.fill(values, 0);
        return values;
    }

    /** The same value held at the end of every month. */
    private static Integer[] held(int count, int value)
    {
        Integer[] values = new Integer[count];
        Arrays.fill(values, value);
        return values;
    }

    /** A value growing evenly month by month up to the given one. */
    private static Integer[] rising(int count, int last)
    {
        Integer[] values = new Integer[count];
        for (int month = 0; month < count; month++) values[month] = last * (month + 1) / count;
        return values;
    }

    private static Integer[] with(Integer[] values, int month, int amount)
    {
        values[month] = amount;
        return values;
    }

    private static double percent(BigDecimal value)
    {
        return value.doubleValue();
    }

    @Test
    void anOpeningValueThatGrewATenthInAYearReturnsATenthAYear()
    {
        assertThat(percent(AnnualReturn.of(1000, months(12), months(12), held(12, 1050), 1100)), closeTo(10.0, 0.01));
    }

    @Test
    void theSameTotalGainOverTwoYearsIsLessAYear()
    {
        // 1000 to 1210 is +21% in all, but only +10% a year
        assertThat(percent(AnnualReturn.of(1000, months(24), months(24), held(24, 1100), 1210)), closeTo(10.0, 0.01));
    }

    @Test
    void moneyPutInLaterHadLessTimeToGrow()
    {
        // 2000 growing 10% a year for two years comes to 2420; with half of it put in only after
        // six months, the same 2420 means it grew faster
        Integer[] deposits = with(months(24), 6, 1000);

        double annual = percent(AnnualReturn.of(1000, deposits, months(24), held(24, 1700), 2420));

        assertThat(annual > 10.0, is(true));
        assertThat(annual, closeTo(11.63, 0.05));
    }

    @Test
    void withdrawalsCountAsWhatTheAssetGaveBack()
    {
        // 1000 in, 550 out after six months, 550 left at the end of two years
        Integer[] withdrawals = with(months(24), 5, 550);
        Integer[] balances = held(24, 550);
        for (int month = 0; month < 5; month++) balances[month] = 1000;

        assertThat(percent(AnnualReturn.of(1000, months(24), withdrawals, balances, 550)), closeTo(8.23, 0.05));
    }

    @Test
    void aLossIsANegativeRate()
    {
        assertThat(percent(AnnualReturn.of(1000, months(12), months(12), held(12, 1000), 900)), closeTo(-10.0, 0.01));
    }

    @Test
    void anAssetFullyWithdrawnStillHasTheRateItEarned()
    {
        Integer[] withdrawals = with(months(12), 11, 1100);

        assertThat(percent(AnnualReturn.of(1000, months(12), withdrawals, held(12, 1050), 0)), closeTo(10.4, 0.2));
    }

    @Test
    void thereIsNoAnnualRateForLessThanAYear()
    {
        assertThat(AnnualReturn.of(1000, months(11), months(11), held(11, 1050), 1100), is(nullValue()));
    }

    @Test
    void thereIsNoRateForAnAssetNothingWasEverPutInto()
    {
        assertThat(AnnualReturn.of(0, months(12), months(12), held(12, 0), 0), is(nullValue()));
    }

    @Test
    void theRateIsGivenToTwoDecimalPlaces()
    {
        assertThat(AnnualReturn.of(1000, months(12), months(12), held(12, 1050), 1100).scale(), is(2));
    }

    @Test
    void aLongTimelineOfDepositsAndWithdrawalsHasAnOrdinaryRate()
    {
        // ten years of a deposit every month and a withdrawal every fourth: at the lowest rates
        // looked at, the money left after a hundred months is too small to compute, which once
        // made the rate come out as -100% a year
        Integer[] deposits = months(120);
        Integer[] withdrawals = months(120);
        for (int month = 0; month < 120; month++) {
            deposits[month] = 300;
            if (month % 4 == 3) withdrawals[month] = 250;
        }

        double annual = percent(AnnualReturn.of(0, deposits, withdrawals, rising(120, 32000), 32000));

        assertThat(annual > 0, is(true));
        assertThat(annual < 10, is(true));
    }

    @Test
    void thereIsNoAnnualRateForMoneyInTheAssetForLessThanAYearOnAverage()
    {
        // three years, but bought and sold within the month each year: worth something for
        // three months of the thirty-six, for 240 put in each time
        Integer[] deposits = months(36);
        Integer[] withdrawals = months(36);
        Integer[] balances = months(36);
        for (int year = 0; year < 3; year++) {
            deposits[year * 12 + 9] = 240;
            withdrawals[year * 12 + 9] = 600;
            balances[year * 12 + 9] = 0;
        }
        balances[9] = 400;

        assertThat(AnnualReturn.of(0, deposits, withdrawals, balances, 0), is(nullValue()));
    }
}
