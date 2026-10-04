import {emptyStretches, figuresBetween, groupTitle, isFullyWithdrawn, monthBefore, splitByActivity, toAssetChartSeries} from "../financialAssets";

const account = {
    initialValue: 1000,
    balances: [1000, 1100],
    funding: [1000, 1050],
    labels: ["1", "2"],
    cumulativeDeposits: [0, 50],
    cumulativeWithdrawals: [0, 10],
};

describe("toAssetChartSeries", () => {
    it("opens with the account's initial state", () => {
        const series = toAssetChartSeries(account);

        expect(series[0]).toEqual({
            valuation: 1000, funding: 1000, month: "0", deposits: 0, withdrawals: 0,
        });
    });

    it("labels the opening point with the month before the first, whose end it is", () => {
        const series = toAssetChartSeries({...account, labels: ["1/18", "2/18"]});

        expect(series.map((point) => point.month)).toEqual(["12/17", "1/18", "2/18"]);
    });

    it("adds a point per month after the opening one", () => {
        const series = toAssetChartSeries(account);

        expect(series).toHaveLength(3);
        expect(series[2]).toEqual({
            valuation: 1100, funding: 1050, month: "2", deposits: 50, withdrawals: 10,
        });
    });
});

describe("monthBefore", () => {
    it("steps back a month within the year", () => {
        expect(monthBefore("10/23")).toBe("9/23");
        expect(monthBefore("2/24")).toBe("1/24");
    });

    it("steps back over the turn of the year, and of the century", () => {
        expect(monthBefore("1/18")).toBe("12/17");
        expect(monthBefore("1/00")).toBe("12/99");
        expect(monthBefore("1/10")).toBe("12/09");
    });

    it("is 0 without a month to go back from", () => {
        expect(monthBefore(undefined)).toBe("0");
        expect(monthBefore("1")).toBe("0");
    });
});

describe("isFullyWithdrawn", () => {
    it("is true only when the final balance is zero", () => {
        expect(isFullyWithdrawn({balances: [100, 0]})).toBe(true);
        expect(isFullyWithdrawn({balances: [100, 50]})).toBe(false);
    });
});

describe("splitByActivity", () => {
    it("puts the assets still held apart from the historical ones, each in the order given", () => {
        const accounts = [
            {id: "230.0", active: false}, {id: "230.1", active: true},
            {id: "230.2", active: false}, {id: "230.3", active: true},
        ];

        const {active, historical} = splitByActivity(accounts);

        expect(active.map((account) => account.id)).toEqual(["230.1", "230.3"]);
        expect(historical.map((account) => account.id)).toEqual(["230.0", "230.2"]);
    });
});

describe("groupTitle", () => {
    it("writes the name the backend shouts in capitals as a title", () => {
        expect(groupTitle("PODIELY")).toBe("Podiely");
        expect(groupTitle("TERMINOVANE VKLADY")).toBe("Terminovane Vklady");
    });
});

describe("figuresBetween", () => {
    // two years: 1000 to begin with, 500 more in the fourth month, 300 out in the tenth of the
    // second year, worth 1600 at the end
    const twoYears = {
        initialValue: 1000,
        deposits: [0, 0, 0, 500, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
        withdrawals: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 300, 0, 0],
        balances: [1000, 1000, 1000, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1600,
            1600, 1600, 1700, 1700, 1800, 1800, 1800, 1850, 1900, 1600, 1600, 1600],
    };

    it("over the whole chart, are the asset's figures as they are now", () => {
        const figures = figuresBetween(twoYears, 0, 24);

        expect(figures.startValue).toBe(1000);
        expect(figures.endValue).toBe(1600);
        expect(figures.depositsSum).toBe(500);
        expect(figures.withdrawalsSum).toBe(300);
        // (1600 + 300) / (1000 + 500) - 1
        expect(figures.totalReturn).toBe(26.67);
        expect(figures.annualReturn).toBeGreaterThan(0);
    });

    it("over a stretch, start from the value at its first point and count only what moved after it", () => {
        // from the end of month 12 (worth 1600) to the end of month 24
        const figures = figuresBetween(twoYears, 12, 24);

        expect(figures.startValue).toBe(1600);
        expect(figures.endValue).toBe(1600);
        expect(figures.depositsSum).toBe(0);
        expect(figures.withdrawalsSum).toBe(300);
        // 1600 + the 300 taken out, against the 1600 it started at
        expect(figures.totalReturn).toBe(18.75);
        expect(figures.annualReturn).not.toBeNull();
    });

    it("start from the value under the first point, the month that point ends left behind", () => {
        // from the end of month 4 (worth 1500, its 500 already in) to the end of month 5
        const figures = figuresBetween(twoYears, 4, 5);

        expect(figures.startValue).toBe(1500);
        expect(figures.depositsSum).toBe(0);
        expect(figures.endValue).toBe(1500);
    });

    it("state no annual return for a stretch shorter than a year", () => {
        expect(figuresBetween(twoYears, 3, 14).annualReturn).toBeNull();
        expect(figuresBetween(twoYears, 3, 15).annualReturn).not.toBeNull();
    });

    it("are no return at all for a stretch with nothing in it", () => {
        const empty = {...twoYears, initialValue: 0, deposits: Array(24).fill(0), balances: Array(24).fill(0)};

        expect(figuresBetween(empty, 0, 24).totalReturn).toBe(0);
    });
});

describe("emptyStretches", () => {
    const series = (values) => [{month: "0", valuation: 0}]
        .concat(values.map((valuation, index) => ({month: String(index + 1), valuation})));

    it("finds none in an asset worth something every month", () => {
        expect(emptyStretches(series([100, 200, 300]))).toEqual([]);
    });

    it("reaches from the last month with a value to the first with one again", () => {
        expect(emptyStretches(series([100, 0, 0, 300, 400]))).toEqual([{from: "1", to: "4"}]);
    });

    it("shows even a single empty month, and counts a value below nought as empty", () => {
        expect(emptyStretches(series([100, -50, 200]))).toEqual([{from: "1", to: "3"}]);
    });

    it("finds each gap", () => {
        expect(emptyStretches(series([100, 0, 200, 300, 0, 0, 50])))
            .toEqual([{from: "1", to: "3"}, {from: "4", to: "7"}]);
    });

    it("does not mark the months before the first value, while the asset was being bought", () => {
        expect(emptyStretches(series([0, 0, 100, 200]))).toEqual([]);
    });

    it("does not mark the months after the last value, once the asset was sold", () => {
        expect(emptyStretches(series([100, 200, 0, 0]))).toEqual([]);
        expect(emptyStretches(series([100, 0, 200, -30]))).toEqual([{from: "1", to: "3"}]);
    });

    it("counts an opening value as one a gap can start from", () => {
        const opened = [{month: "0", valuation: 500}, {month: "1", valuation: 0}, {month: "2", valuation: 600}];
        expect(emptyStretches(opened)).toEqual([{from: "0", to: "2"}]);
    });
});
