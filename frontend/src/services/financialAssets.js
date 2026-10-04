import {annualReturn} from "./annualReturn";

/**
 * The chart series for one asset account: a point for the end of each of its months, opened with
 * one for its state before the first - the end of the month before, which is what it is labelled.
 * Without it the chart, and the slider over it, could only start from the end of the first month,
 * with that month's deposits and gain already behind them.
 */
export function toAssetChartSeries(account) {
    const opening = {
        valuation: account.initialValue,
        funding: account.initialValue,
        month: monthBefore(account.labels[0]),
        deposits: 0,
        withdrawals: 0,
    };

    return [opening, ...account.balances.map((balance, index) => ({
        valuation: balance,
        funding: account.funding[index],
        month: account.labels[index],
        deposits: account.cumulativeDeposits[index],
        withdrawals: account.cumulativeWithdrawals[index],
    }))];
}

/**
 * The label of the month before a "month/yy" one. Without a month to go back from, it is "0".
 */
export function monthBefore(label) {
    const parts = /^(\d{1,2})\/(\d{2})$/.exec(label ?? "");
    if (parts === null) return "0";
    const month = Number(parts[1]);
    const year = Number(parts[2]);
    return month === 1
        ? "12/" + String((year + 99) % 100).padStart(2, "0")
        : (month - 1) + "/" + parts[2];
}

/** An account that has been fully withdrawn defaults to showing its funding split. */
export function isFullyWithdrawn(account) {
    return account.balances[account.balances.length - 1] === 0;
}

/**
 * A group's assets split into the ones still held - in the latest year of the books - and the
 * historical ones, sold off in some earlier year. Each keeps the order the backend sends, which
 * is the order the assets were opened in.
 */
export function splitByActivity(accounts) {
    return {
        active: accounts.filter((account) => account.active),
        historical: accounts.filter((account) => !account.active),
    };
}

/**
 * A group's name as a title: the backend sends it in capitals, which read as shouting in a
 * tab, so each word keeps only its first letter capital.
 */
export function groupTitle(name) {
    return name.toLowerCase().replace(/(^|\s)\S/g, (start) => start.toUpperCase());
}

/**
 * An asset's figures between two points of its chart, as the chart's slider picks them. Point 0
 * is the asset's state before its first month, and point k the end of its k-th month, so the
 * stretch starts from what the asset was worth at the first point - the one under the slider's
 * left end - and counts what moved in the months after it, up to the second.
 *
 * Over the whole chart these are the figures the backend sends for the asset.
 */
export function figuresBetween(account, from, to) {
    const valueAt = (point) => (point === 0 ? account.initialValue : account.balances[point - 1]);
    const deposits = account.deposits.slice(from, to);
    const withdrawals = account.withdrawals.slice(from, to);
    const sum = (amounts) => amounts.reduce((total, amount) => total + amount, 0);

    const startValue = valueAt(from);
    const endValue = valueAt(to);
    const depositsSum = sum(deposits);
    const withdrawalsSum = sum(withdrawals);
    const putIn = startValue + depositsSum;
    const totalReturn = putIn === 0 ? 0 : Math.round(((endValue + withdrawalsSum) / putIn - 1) * 10000) / 100;

    return {
        startValue, endValue, depositsSum, withdrawalsSum, totalReturn,
        annualReturn: annualReturn(startValue, deposits, withdrawals, account.balances.slice(from, to), endValue),
    };
}

/**
 * The gaps in an asset's chart: the stretches where, between two months it was worth something, it
 * was worth nothing - or less, where a sale was booked before the revaluation that covers it - as
 * {from, to} labels of the chart's points.
 *
 * A gap reaches from the last point that still had a value to the first that has one again, so that
 * even a single empty month shows. The months before the first value, while the asset was still
 * being bought, and those after the last, once it was sold, are not gaps.
 */
export function emptyStretches(series) {
    const stretches = [];
    let lastHeld = null;
    for (let point = 0; point < series.length; point++) {
        if (series[point].valuation <= 0) continue;
        if (lastHeld !== null && point > lastHeld + 1) {
            stretches.push({from: series[lastHeld].month, to: series[point].month});
        }
        lastHeld = point;
    }
    return stretches;
}
