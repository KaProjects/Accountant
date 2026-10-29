import {toBudgetChartSeries} from "../budgetChart";

const months = (value) => Array.from({length: 12}, () => value);

const row = (overrides = {}) => ({
    type: "EXPENSE",
    lastFilledMonth: 3,
    actual: months(0),
    planned: months(0),
    ...overrides,
});

describe("toBudgetChartSeries", () => {
    it("always produces a point for every month", () => {
        const series = toBudgetChartSeries(row());

        expect(series).toHaveLength(12);
        expect(series[0].name).toBe(1);
        expect(series[11].name).toBe(12);
    });

    it("carries the planned figure for every month, filled or not", () => {
        const planned = months(0).map((ignored, i) => (i + 1) * 100);
        const series = toBudgetChartSeries(row({planned}));

        expect(series[0].planned).toBe(100);
        expect(series[11].planned).toBe(1200);
    });

    it("leaves months beyond the last filled one at zero", () => {
        const series = toBudgetChartSeries(row({
            actual: months(500), planned: months(100), lastFilledMonth: 2,
        }));

        expect(series[2]).toMatchObject({base: 0, deficit: 0, surplus: 0});
        expect(series[11]).toMatchObject({base: 0, deficit: 0, surplus: 0});
    });

    it("reports overspending on an expense row as a deficit", () => {
        const series = toBudgetChartSeries(row({
            type: "EXPENSE", actual: months(150), planned: months(100),
        }));

        expect(series[0]).toMatchObject({base: 100, deficit: 50, surplus: 0});
    });

    it("reports underspending on an expense row as a surplus", () => {
        const series = toBudgetChartSeries(row({
            type: "EXPENSE", actual: months(80), planned: months(100),
        }));

        expect(series[0]).toMatchObject({base: 80, deficit: 0, surplus: 20});
    });

    it("reverses the meaning for an income row", () => {
        const over = toBudgetChartSeries(row({
            type: "INCOME", actual: months(150), planned: months(100),
        }));
        expect(over[0]).toMatchObject({base: 100, deficit: 0, surplus: 50});

        const under = toBudgetChartSeries(row({
            type: "INCOME", actual: months(80), planned: months(100),
        }));
        expect(under[0]).toMatchObject({base: 80, deficit: 20, surplus: 0});
    });

    it("treats the expense summary row as an expense", () => {
        const series = toBudgetChartSeries(row({
            type: "EXPENSE_SUM", actual: months(150), planned: months(100),
        }));

        expect(series[0]).toMatchObject({deficit: 50, surplus: 0});
    });

    it("splits nothing when actual exactly matches planned", () => {
        const series = toBudgetChartSeries(row({actual: months(100), planned: months(100)}));

        expect(series[0]).toMatchObject({base: 100, deficit: 0, surplus: 0});
    });
});
