package org.kaleta.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * The money-weighted annual return of an asset: the yearly rate at which everything put in and
 * taken out of it, each at the time it moved, comes out at what it is worth now - its internal
 * rate of return, annualised.
 * <p>
 * The asset's total return says how much it gained against everything ever deposited, whatever
 * the time it took; this says how fast the money grew while it was in, which is what makes an
 * asset held for nine years comparable with one held for two. The opening value counts at the
 * start of the first month, a month's deposits and withdrawals in its middle, and the current
 * value at the end of the last month.
 */
public final class AnnualReturn
{
    /**
     * Shorter than a year, an annual rate only extrapolates a few months' luck - whether the stretch
     * itself is shorter, or the money was in the asset for less than that on average. An asset
     * bought and sold within days, a few times over several years, has a long stretch but almost no
     * time invested, and its rate per year came out in the thousands of percent.
     */
    private static final int MINIMUM_MONTHS = 12;

    /**
     * The monthly rates a return is looked for between: from losing half a month - nearly all of it
     * in a year - to doubling every month. A rate closer to -100% a month cannot be computed over
     * a long timeline: raised to a hundred months, what is left of the money underflows to nought,
     * and the value of the flows becomes the difference of two infinities.
     */
    private static final double LOWEST_MONTHLY_RATE = -0.5;
    private static final double HIGHEST_MONTHLY_RATE = 1.0;
    private static final double SCAN_STEP = 0.0005;
    private static final double PRECISION = 1e-12;

    private AnnualReturn() {}

    /**
     * @param balances what the asset was worth at the end of each month
     * @return the annual return in percent, to two decimal places; or null where there is none to
     * state - a timeline shorter than a year, money in it for less than a year on average, nothing
     * ever put in, or flows no single rate settles
     */
    public static BigDecimal of(Integer initialValue, Integer[] deposits, Integer[] withdrawals, Integer[] balances, Integer currentValue)
    {
        int months = deposits.length;
        if (months < MINIMUM_MONTHS || withdrawals.length != months || balances.length != months) return null;

        double[] amounts = new double[months + 2];
        double[] times = new double[months + 2];
        double putIn = 0;

        amounts[0] = -valueOf(initialValue);
        times[0] = 0;
        putIn += valueOf(initialValue);
        for (int month = 0; month < months; month++) {
            amounts[month + 1] = valueOf(withdrawals[month]) - valueOf(deposits[month]);
            times[month + 1] = month + 0.5;
            putIn += valueOf(deposits[month]);
        }
        amounts[months + 1] = valueOf(currentValue);
        times[months + 1] = months;
        if (putIn <= 0) return null;
        if (monthsInvested(balances, putIn) < MINIMUM_MONTHS) return null;

        Double monthly = rateSettling(amounts, times);
        if (monthly == null) return null;

        double annual = Math.pow(1 + monthly, 12) - 1;
        return BigDecimal.valueOf(annual * 100).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * The monthly rate at which the flows' present value is nought, or null where there is none.
     * <p>
     * Flows that go in and out more than once can be settled by more than one rate. The range is
     * scanned for every place the value changes sign, and the one nearest no growth at all is the
     * rate taken - the only one that reads as a return - then narrowed down by halving.
     */
    private static Double rateSettling(double[] amounts, double[] times)
    {
        Double best = null;
        double previousRate = LOWEST_MONTHLY_RATE;
        double previousValue = presentValue(amounts, times, previousRate);
        for (double rate = LOWEST_MONTHLY_RATE + SCAN_STEP; rate <= HIGHEST_MONTHLY_RATE + 1e-9; rate += SCAN_STEP) {
            double value = presentValue(amounts, times, rate);
            if (Double.isFinite(value) && Double.isFinite(previousValue) && Math.signum(value) != Math.signum(previousValue)) {
                double root = bisect(amounts, times, previousRate, rate, previousValue);
                if (best == null || Math.abs(root) < Math.abs(best)) best = root;
            }
            previousRate = rate;
            previousValue = value;
        }
        return best;
    }

    private static double bisect(double[] amounts, double[] times, double low, double high, double lowValue)
    {
        for (int step = 0; step < 200 && high - low > PRECISION; step++) {
            double middle = (low + high) / 2;
            double middleValue = presentValue(amounts, times, middle);
            if (Math.signum(middleValue) == Math.signum(lowValue)) {
                low = middle;
                lowValue = middleValue;
            } else {
                high = middle;
            }
        }
        return (low + high) / 2;
    }

    /**
     * How long the money put in was in the asset, on average: what the asset was worth month by
     * month, added up, for each unit put in. A month worth less than nothing - a sale booked before
     * the revaluation that covers it - counts as nothing held.
     */
    private static double monthsInvested(Integer[] balances, double putIn)
    {
        double held = 0;
        for (Integer balance : balances) held += Math.max(0, valueOf(balance));
        return held / putIn;
    }

    private static double presentValue(double[] amounts, double[] times, double monthlyRate)
    {
        double value = 0;
        for (int i = 0; i < amounts.length; i++) {
            value += amounts[i] / Math.pow(1 + monthlyRate, times[i]);
        }
        return value;
    }

    private static double valueOf(Integer amount)
    {
        return amount == null ? 0 : amount;
    }
}
