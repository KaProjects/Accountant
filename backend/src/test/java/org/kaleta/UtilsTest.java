package org.kaleta;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.kaleta.framework.Generator.monthly;

/**
 * The monthly figures in every statement, chart and budget are built by these
 * array helpers, so their edge cases are worth pinning down.
 */
public class UtilsTest
{
    @Test
    void mergeIntegerArraysSumsPositionWise()
    {
        assertThat(Utils.mergeIntegerArrays(monthly(1, 2, 3), monthly(10, 20, 30), monthly(100, 200, 300)),
                is(arrayContaining(111, 222, 333)));
    }

    @Test
    void mergeIntegerArraysHandlesNegativesAndSingleArgument()
    {
        assertThat(Utils.mergeIntegerArrays(monthly(5, -5), monthly(-3, 3)), is(arrayContaining(2, -2)));
        assertThat(Utils.mergeIntegerArrays(monthly(7, 8)), is(arrayContaining(7, 8)));
    }

    @Test
    void mergeIntegerArraysRejectsDifferentLengths()
    {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> Utils.mergeIntegerArrays(monthly(1, 2), monthly(1, 2, 3)));
        assertThat(e.getMessage(), containsString("arrays not same length"));
    }

    @Test
    void subtractIntegerArraysSubtractsPositionWise()
    {
        assertThat(Utils.subtractIntegerArrays(monthly(10, 10, 10), monthly(1, 5, 20)),
                is(arrayContaining(9, 5, -10)));
    }

    @Test
    void subtractIntegerArraysRejectsDifferentLengths()
    {
        assertThrows(IllegalArgumentException.class,
                () -> Utils.subtractIntegerArrays(monthly(1, 2, 3), monthly(1, 2)));
    }

    @Test
    void addIntegerArraysAddsPositionWise()
    {
        assertThat(Utils.addIntegerArrays(monthly(10, -10), monthly(1, 5)), is(arrayContaining(11, -5)));
    }

    @Test
    void addIntegerArraysRejectsDifferentLengths()
    {
        assertThrows(IllegalArgumentException.class,
                () -> Utils.addIntegerArrays(monthly(1), monthly(1, 2)));
    }

    @Test
    void toCumulativeArrayAccumulatesRunningTotal()
    {
        assertThat(Utils.toCumulativeArray(monthly(1, 2, 3, 4)), is(arrayContaining(1, 3, 6, 10)));
    }

    @Test
    void toCumulativeArrayCarriesNegativesAndZeroes()
    {
        assertThat(Utils.toCumulativeArray(monthly(5, -5, 0, -2)), is(arrayContaining(5, 0, 0, -2)));
        assertThat(Utils.toCumulativeArray(new Integer[0]).length, is(0));
    }

    @Test
    void sumArrayTotalsEveryElement()
    {
        assertThat(Utils.sumArray(monthly(1, 2, 3)), is(6));
        assertThat(Utils.sumArray(monthly(10, -10)), is(0));
        assertThat(Utils.sumArray(new Integer[0]), is(0));
    }

    @Test
    void invertValuesNegatesEveryElement()
    {
        assertThat(Utils.invertValues(monthly(1, -2, 0)), is(arrayContaining(-1, 2, 0)));
    }

    @Test
    void concatArraysJoinsPreservingOrder()
    {
        assertThat(Utils.concatArrays(monthly(1, 2), monthly(3)), is(arrayContaining(1, 2, 3)));
        assertThat(Utils.concatArrays(new String[]{"a"}, new String[]{"b", "c"}),
                is(arrayContaining("a", "b", "c")));
    }

    @Test
    void initialMonthlyBalanceIsTwelveZeroes()
    {
        Integer[] balance = Utils.initialMonthlyBalance();

        assertThat(balance.length, is(12));
        assertThat(Utils.sumArray(balance), is(0));
    }

    @Test
    void compareDatesOrdersByMonthFirstThenDay()
    {
        // dates are DDMM, so 0102 is 1 February and 1501 is 15 January
        assertThat(Utils.compareDates("0102", "1501") > 0, is(true));
        assertThat(Utils.compareDates("1501", "0102") < 0, is(true));
    }

    @Test
    void compareDatesOrdersWithinTheSameMonthByDay()
    {
        assertThat(Utils.compareDates("0503", "1503") < 0, is(true));
        assertThat(Utils.compareDates("1503", "0503") > 0, is(true));
        assertThat(Utils.compareDates("1503", "1503"), is(0));
    }
}
