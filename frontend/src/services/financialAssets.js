/** A flag per account, all closed, shaped to match the groups. */
export function closedFlagsFor(groups) {
    return groups.map((group) => Array(group.accounts.length).fill(false));
}

/**
 * The chart series for one asset account, opened with a point for the account's
 * state before the first month.
 */
export function toAssetChartSeries(account) {
    const opening = {
        valuation: account.initialValue,
        funding: account.initialValue,
        month: "0",
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

/** An account that has been fully withdrawn defaults to showing its funding split. */
export function isFullyWithdrawn(account) {
    return account.balances[account.balances.length - 1] === 0;
}
