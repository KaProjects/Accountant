import {closedFlagsFor, isFullyWithdrawn, toAssetChartSeries} from "../financialAssets";

const account = {
    initialValue: 1000,
    balances: [1000, 1100],
    funding: [1000, 1050],
    labels: ["1", "2"],
    cumulativeDeposits: [0, 50],
    cumulativeWithdrawals: [0, 10],
};

describe("closedFlagsFor", () => {
    it("shapes a closed flag for every account", () => {
        const flags = closedFlagsFor([{accounts: [{}, {}]}, {accounts: [{}]}]);

        expect(flags).toEqual([[false, false], [false]]);
    });

    it("handles a group with no accounts", () => {
        expect(closedFlagsFor([{accounts: []}])).toEqual([[]]);
    });
});

describe("toAssetChartSeries", () => {
    it("opens with the account's initial state", () => {
        const series = toAssetChartSeries(account);

        expect(series[0]).toEqual({
            valuation: 1000, funding: 1000, month: "0", deposits: 0, withdrawals: 0,
        });
    });

    it("adds a point per month after the opening one", () => {
        const series = toAssetChartSeries(account);

        expect(series).toHaveLength(3);
        expect(series[2]).toEqual({
            valuation: 1100, funding: 1050, month: "2", deposits: 50, withdrawals: 10,
        });
    });
});

describe("isFullyWithdrawn", () => {
    it("is true only when the final balance is zero", () => {
        expect(isFullyWithdrawn({balances: [100, 0]})).toBe(true);
        expect(isFullyWithdrawn({balances: [100, 50]})).toBe(false);
    });
});
