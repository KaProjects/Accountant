import {abbreviateAmount, statementCharts} from "../statementCharts";

const balance = {
    columns: ["Yearly Balance Sheet", "2019", "2020"],
    rows: [
        {type: "BALANCE_SUMMARY", schemaId: "a", name: "ASSETS", yearlyValues: [30, 40]},
        {type: "BALANCE_CLASS", schemaId: "0", name: "Fixed Assets", yearlyValues: [10, 15]},
        {type: "BALANCE_CLASS", schemaId: "2", name: "Finance", yearlyValues: [20, 25]},
        {type: "BALANCE_SUMMARY", schemaId: "l", name: "LIABILITIES", yearlyValues: [30, 40]},
        {type: "BALANCE_CLASS", schemaId: "4", name: "Funding", yearlyValues: [30, 40]},
    ],
};

const cashFlow = {
    columns: ["Yearly Cash Flow Statement", "2019", "2020"],
    rows: [
        {type: "CASH_FLOW_GROUP", schemaId: "20", name: "Cash", yearlyValues: [5, 6]},
        {type: "CASH_FLOW_GROUP", schemaId: "22", name: "Credit", yearlyValues: [-2, -3]},
        {type: "CASH_FLOW_SUMMARY", schemaId: "cf", name: "Cash Flow", yearlyValues: [3, 3]},
    ],
};

const profit = {
    columns: ["Yearly Income Statement", "2019", "2020", "Total"],
    rows: [
        {type: "INCOME_GROUP", schemaId: "60", name: "Work", yearlyValues: [100, 120]},
        {type: "PROFIT_SUMMARY", schemaId: "ni", name: "Net Income", yearlyValues: [90, 110]},
        {type: "EXPENSE_GROUP", schemaId: "51", name: "Consumption", yearlyValues: [30, 40]},
        {type: "PROFIT_SUMMARY", schemaId: "op", name: "Operating Profit", yearlyValues: [60, 70]},
        {type: "PROFIT_SUMMARY", schemaId: "np", name: "Net Profit", yearlyValues: [-10, 70]},
    ],
};

describe("statementCharts", () => {
    it("charts each side of the balance sheet on its own", () => {
        const charts = statementCharts("balance", balance);

        expect(charts.map((chart) => chart.title)).toEqual(["Assets", "Liabilities"]);
        expect(charts[0].series.map((series) => series.name)).toEqual(["Fixed Assets", "Finance"]);
        expect(charts[1].series.map((series) => series.name)).toEqual(["Funding"]);
    });

    it("stacks the classes under the row that totals them", () => {
        const [assets] = statementCharts("balance", balance);

        expect(assets.form).toBe("stacked");
        expect(assets.line.name).toBe("Assets");
        expect(assets.points).toEqual([
            {year: "2019", s0: 10, s1: 20, summary: 30},
            {year: "2020", s0: 15, s1: 25, summary: 40},
        ]);
    });

    it("keeps the cash flow's own summary as the line over its groups", () => {
        const [chart] = statementCharts("cashflow", cashFlow);

        expect(chart.form).toBe("stacked");
        expect(chart.series.map((series) => series.name)).toEqual(["Cash", "Credit"]);
        expect(chart.line.name).toBe("Cash Flow");
        expect(chart.points[0]).toEqual({year: "2019", s0: 5, s1: -2, summary: 3});
    });

    it("charts the income statement twice: its groups, then its profit levels", () => {
        expect(statementCharts("profit", profit).map((chart) => chart.title))
            .toEqual(["Income and costs", "Profit"]);
    });

    it("stacks income above nought and costs below it, under the net profit line", () => {
        const [groups] = statementCharts("profit", profit);

        expect(groups.form).toBe("stacked");
        expect(groups.series.map((series) => series.name)).toEqual(["Work", "Consumption"]);
        expect(groups.line.name).toBe("Net Profit");
        expect(groups.points).toEqual([
            {year: "2019", s0: 100, s1: -30, summary: -10},
            {year: "2020", s0: 120, s1: -40, summary: 70},
        ]);
    });

    it("adds up a group the statement reports on two lines", () => {
        // 63 is split around a subtotal in the table; in a chart of composition it is one group.
        const split = {...profit, rows: [
            ...profit.rows,
            {type: "INCOME_GROUP", schemaId: "60", name: "Work", yearlyValues: [5, 5]},
        ]};

        const [groups] = statementCharts("profit", split);

        expect(groups.series.map((series) => series.name)).toEqual(["Work", "Consumption"]);
        expect(groups.points[0].s0).toBe(105);
    });

    it("qualifies a name that appears on both sides of nought", () => {
        const bothSides = {...profit, rows: [
            ...profit.rows,
            {type: "EXPENSE_GROUP", schemaId: "54", name: "Work", yearlyValues: [7, 8]},
        ]};

        const [groups] = statementCharts("profit", bothSides);

        expect(groups.series.map((series) => series.name))
            .toEqual(["Work (income)", "Consumption", "Work (cost)"]);
    });

    it("draws the three profit levels as lines, not as a stack", () => {
        // They are the same figure read at three depths, so stacking would double count.
        const levels = statementCharts("profit", profit)[1];

        expect(levels.form).toBe("lines");
        expect(levels.line).toBeNull();
        expect(levels.series.map((series) => series.name))
            .toEqual(["Net Income", "Operating Profit", "Net Profit"]);
        expect(levels.points).toEqual([
            {year: "2019", s0: 90, s1: 60, s2: -10},
            {year: "2020", s0: 110, s1: 70, s2: 70},
        ]);
    });

    it("leaves the income statement's Total column off the years axis", () => {
        const [groups] = statementCharts("profit", profit);

        expect(groups.points.map((point) => point.year)).toEqual(["2019", "2020"]);
    });

    it("gives each component its own colour, and the line the colour of its row", () => {
        const [assets] = statementCharts("balance", balance);

        expect(assets.series[0].color).not.toBe(assets.series[1].color);
        expect(assets.line.color).toBeTruthy();
    });

    it("charts nothing at all when no year has been synced yet", () => {
        expect(statementCharts("balance", {columns: ["Yearly Balance Sheet"], rows: []})).toEqual([]);
    });

    it("charts nothing for a statement it does not know", () => {
        expect(statementCharts("nonsense", balance)).toEqual([]);
    });
});

describe("abbreviateAmount", () => {
    it("shortens thousands and millions", () => {
        expect(abbreviateAmount(13775706)).toBe("13.8M");
        expect(abbreviateAmount(489280)).toBe("489.3k");
    });

    it("keeps small figures as they are", () => {
        expect(abbreviateAmount(0)).toBe("0");
        expect(abbreviateAmount(-999)).toBe("-999");
    });

    it("puts the sign before the figure, not inside it", () => {
        expect(abbreviateAmount(-1500000)).toBe("-1.5M");
    });
});
