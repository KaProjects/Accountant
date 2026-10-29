/**
 * The money-weighted annual return of a stretch of an asset's history: the yearly rate at which
 * what it was worth at the start, and everything put in and taken out after, comes out at what it
 * was worth at the end - its internal rate of return, annualised.
 *
 * The same calculation as the backend's AnnualReturn, which answers it for an asset's whole
 * history; this one answers it for whatever stretch of it the chart's slider picks. The starting
 * value counts at the start, a month's deposits and withdrawals in its middle, and the end value
 * at the end of the last month.
 */

// shorter than a year, an annual rate only extrapolates a few months' luck - whether the stretch is
// shorter, or the money was in the asset for less than that on average, as for an asset bought and
// sold within days a few times over several years
const MINIMUM_MONTHS = 12;

// from losing half a month to doubling every month: a rate nearer -100% a month cannot be computed
// over a long stretch, as what is left of the money underflows to nought
const LOWEST_MONTHLY_RATE = -0.5;
const HIGHEST_MONTHLY_RATE = 1.0;
const SCAN_STEP = 0.0005;
const PRECISION = 1e-12;

const presentValue = (flows, rate) =>
    flows.reduce((value, flow) => value + flow.amount / Math.pow(1 + rate, flow.time), 0);

const bisect = (flows, low, high, lowValue) => {
    for (let step = 0; step < 200 && high - low > PRECISION; step++) {
        const middle = (low + high) / 2;
        const middleValue = presentValue(flows, middle);
        if (Math.sign(middleValue) === Math.sign(lowValue)) {
            low = middle;
            lowValue = middleValue;
        } else {
            high = middle;
        }
    }
    return (low + high) / 2;
};

/**
 * The monthly rate at which the flows are worth nought, or null where there is none. Flows that
 * go in and out more than once can be settled by more than one rate, and the one nearest no
 * growth at all is taken.
 */
const rateSettling = (flows) => {
    let best = null;
    let previousRate = LOWEST_MONTHLY_RATE;
    let previousValue = presentValue(flows, previousRate);
    for (let rate = LOWEST_MONTHLY_RATE + SCAN_STEP; rate <= HIGHEST_MONTHLY_RATE + 1e-9; rate += SCAN_STEP) {
        const value = presentValue(flows, rate);
        if (Number.isFinite(value) && Number.isFinite(previousValue) && Math.sign(value) !== Math.sign(previousValue)) {
            const root = bisect(flows, previousRate, rate, previousValue);
            if (best === null || Math.abs(root) < Math.abs(best)) best = root;
        }
        previousRate = rate;
        previousValue = value;
    }
    return best;
};

/**
 * How long the money put in was in the asset, on average: what it was worth month by month, added
 * up, for each unit put in. A month worth less than nothing counts as nothing held.
 */
const monthsInvested = (balances, putIn) =>
    balances.reduce((held, balance) => held + Math.max(0, balance), 0) / putIn;

/**
 * The annual return in percent, to two decimal places; or null where there is none to state - a
 * stretch shorter than a year, money in the asset for less than a year on average, nothing in it
 * to begin with or put in, or flows no rate settles. The balances are what the asset was worth at
 * the end of each month.
 */
export function annualReturn(startValue, deposits, withdrawals, balances, endValue) {
    const months = deposits.length;
    if (months < MINIMUM_MONTHS || withdrawals.length !== months || balances.length !== months) return null;
    const putIn = startValue + deposits.reduce((sum, amount) => sum + amount, 0);
    if (putIn <= 0) return null;
    if (monthsInvested(balances, putIn) < MINIMUM_MONTHS) return null;

    const flows = [{amount: -startValue, time: 0}];
    for (let month = 0; month < months; month++) {
        flows.push({amount: withdrawals[month] - deposits[month], time: month + 0.5});
    }
    flows.push({amount: endValue, time: months});

    const monthly = rateSettling(flows);
    if (monthly === null) return null;
    return Math.round((Math.pow(1 + monthly, 12) - 1) * 10000) / 100;
}
