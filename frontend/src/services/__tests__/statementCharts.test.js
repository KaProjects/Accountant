import {abbreviateAmount, statementCharts, statementRowShades} from "../statementCharts";
import {aboveZeroShades, belowZeroShades} from "../../theme/palette";

const greens = aboveZeroShades.map((shade) => shade.fill);
const reds = belowZeroShades.map((shade) => shade.fill);

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
            {period: "2019", s0: 10, s1: 20, summary: 30},
            {period: "2020", s0: 15, s1: 25, summary: 40},
        ]);
    });

    it("keeps the cash flow's own summary as the line over its groups", () => {
        const [chart] = statementCharts("cashflow", cashFlow);

        expect(chart.form).toBe("stacked");
        expect(chart.series.map((series) => series.name)).toEqual(["Cash", "Credit"]);
        expect(chart.line.name).toBe("Cash Flow");
        expect(chart.points[0]).toEqual({period: "2019", s0: 5, s1: -2, summary: 3});
    });

    it("colours the cash flow by side: green for what is held, red for what is owed", () => {
        const [chart] = statementCharts("cashflow", cashFlow);

        expect(greens).toContain(chart.series[0].color);
        expect(reds).toContain(chart.series[1].color);
    });

    it("gives every component on one side a shade of its own, the first one the deepest", () => {
        const threeHeld = {...cashFlow, rows: [
            {type: "CASH_FLOW_GROUP", schemaId: "20", name: "Cash", yearlyValues: [5, 6]},
            {type: "CASH_FLOW_GROUP", schemaId: "21", name: "Bank", yearlyValues: [7, 8]},
            {type: "CASH_FLOW_GROUP", schemaId: "23", name: "Funds", yearlyValues: [9, 9]},
            {type: "CASH_FLOW_GROUP", schemaId: "22", name: "Credit", yearlyValues: [-2, -3]},
            {type: "CASH_FLOW_SUMMARY", schemaId: "cf", name: "Cash Flow", yearlyValues: [19, 20]},
        ]};

        const [chart] = statementCharts("cashflow", threeHeld);
        const held = chart.series.slice(0, 3).map((series) => greens.indexOf(series.color));

        // the shades run from palest to deepest, and the table's first group takes the deepest
        expect(new Set(held).size).toBe(3);
        expect(held).toEqual(held.slice().sort((one, other) => other - one));
    });

    it("draws the cash flow's line heavier than the totals of the other charts", () => {
        const [cashFlowChart] = statementCharts("cashflow", cashFlow);
        const [assets] = statementCharts("balance", balance);

        expect(cashFlowChart.line.width).toBeGreaterThan(assets.line.width);
    });

    it("draws a lone component in the pale shade that sits against the axis", () => {
        const [chart] = statementCharts("cashflow", cashFlow);

        expect(chart.series[1].color).toBe(reds[1]);
    });

    it("leaves the other statements in hues of their own", () => {
        const [assets] = statementCharts("balance", balance);

        expect(greens).not.toContain(assets.series[0].color);
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
            {period: "2019", s0: 100, s1: -30, summary: -10},
            {period: "2020", s0: 120, s1: -40, summary: 70},
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
            {period: "2019", s0: 90, s1: 60, s2: -10},
            {period: "2020", s0: 110, s1: 70, s2: 70},
        ]);
    });

    it("leaves the income statement's Total column off the years axis", () => {
        const [groups] = statementCharts("profit", profit);

        expect(groups.points.map((point) => point.period)).toEqual(["2019", "2020"]);
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

describe("statementCharts on a yearly statement", () => {
    const months = ["January", "February", "March", "April", "May", "June", "July",
        "August", "September", "October", "November", "December"];
    const yearlyCashFlow = {
        columns: ["Cash Flow Statement", "Initial", ...months, "Total"],
        rows: [
            {type: "CASH_FLOW_GROUP", schemaId: "20", name: "Cash",
                initial: 100, monthlyValues: [10, -20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5], total: 95},
            {type: "CASH_FLOW_GROUP", schemaId: "22", name: "Credit",
                initial: -50, monthlyValues: [-5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0], total: -55},
            {type: "CASH_FLOW_SUMMARY", schemaId: "cf", name: "Cash Flow",
                initial: 50, monthlyValues: [5, -20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5], total: 40},
        ],
    };

    const only = () => {
        const charts = statementCharts("cashflow", yearlyCashFlow, false);
        expect(charts).toHaveLength(1);
        return charts[0];
    };

    it("charts the year once, as its months' changes measured from the opening balance", () => {
        expect(only().form).toBe("changes");
        expect(only().baseline).toBe(50);
    });

    it("is drawn under the table, each point under its own column", () => {
        expect(only().alignToTable).toBe(true);
    });

    it("names its points as the table's header names its columns", () => {
        expect(only().points.map((point) => point.period)).toEqual(["Initial", ...months]);
    });

    it("sets the line off from the baseline under Initial, with nothing else drawn there", () => {
        expect(only().points[0]).toEqual({period: "Initial", summary: 50, opening: true});
    });

    it("reports each group's change for the month, as the table does", () => {
        const monthPoints = only().points.slice(1);

        expect(monthPoints.map((point) => point.s0)).toEqual([10, -20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5]);
        expect(monthPoints.map((point) => point.s1)).toEqual([-5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]);
    });

    it("draws where the cash flow actually stood, from the opening through each month end", () => {
        expect(only().points.map((point) => point.summary))
            .toEqual([50, 55, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 40]);
    });

    describe("in a year that is still running", () => {
        // recorded until April; the table still carries May to December, as noughts
        const running = {...yearlyCashFlow, rows: [
            {type: "CASH_FLOW_GROUP", schemaId: "20", name: "Cash",
                initial: 100, monthlyValues: [10, 0, 0, -5, 0, 0, 0, 0, 0, 0, 0, 0]},
            {type: "CASH_FLOW_GROUP", schemaId: "22", name: "Credit",
                initial: -50, monthlyValues: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]},
            {type: "CASH_FLOW_SUMMARY", schemaId: "cf", name: "Cash Flow",
                initial: 50, monthlyValues: [10, 0, 0, -5, 0, 0, 0, 0, 0, 0, 0, 0]},
        ]};
        const points = () => statementCharts("cashflow", running, false)[0].points;

        it("ends the line at the last month anything moved in", () => {
            expect(points().map((point) => point.summary))
                .toEqual([50, 60, 60, 60, 55, null, null, null, null, null, null, null, null]);
        });

        it("still counts a quiet month that has a recorded one after it", () => {
            expect(points()[2]).toMatchObject({period: "February", summary: 60, spacer: 50});
        });

        it("draws nothing at all in the months the year has not reached", () => {
            expect(points()[5]).toEqual({period: "May", summary: null});
        });

        it("keeps every month on the axis, under its column of the table", () => {
            expect(points().map((point) => point.period)).toEqual(["Initial", ...months]);
        });
    });

    it("stacks a month's losses down from the baseline and its gains up from it", () => {
        // January: Cash gains 10, Credit loses 5, measured from the opening 50
        const january = only().points[1];

        expect(january.spacer).toBe(45);
        expect(january.s1_below).toBe(5);
        expect(january.s0_above).toBe(10);
        expect([january.s0_below, january.s1_above]).toEqual([null, null]);
        // so the losses reach exactly back up to the baseline, where the gains begin
        expect(january.spacer + january.s1_below).toBe(only().baseline);
    });

    it("colours a group by where its balance stands, not by which way the year moved it", () => {
        // Cash ends the year lower than it began, but it is still cash held
        const dwindling = {...yearlyCashFlow, rows: yearlyCashFlow.rows.map((row) => row.schemaId === "20"
            ? {...row, monthlyValues: [-30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0], total: 70}
            : row)};

        expect(greens).toContain(statementCharts("cashflow", dwindling, false)[0].series[0].color);
    });

    it("colours and paints a yearly cash flow the way it does the overall one", () => {
        const [chart] = statementCharts("cashflow", yearlyCashFlow, false);
        const shades = statementRowShades("cashflow", yearlyCashFlow, false);

        expect(greens).toContain(chart.series[0].color);
        expect(reds).toContain(chart.series[1].color);
        expect(shades[0].fill).toBe(chart.series[0].color);
        expect(shades[1].fill).toBe(chart.series[1].color);
        expect(shades[2].ink).toBe(chart.line.color);
    });

    it("leaves the other yearly statements without a chart, for now", () => {
        const yearly = {...yearlyCashFlow, columns: ["Balance Sheet", "Initial", ...months, "Total"]};

        expect(statementCharts("balance", yearly, false)).toEqual([]);
        expect(statementCharts("profit", yearly, false)).toEqual([]);
    });
});

describe("statementRowShades", () => {
    it("paints each cash flow group in the shade of its part of the chart", () => {
        const [chart] = statementCharts("cashflow", cashFlow);
        const shades = statementRowShades("cashflow", cashFlow);

        expect(shades[0].fill).toBe(chart.series[0].color);
        expect(shades[1].fill).toBe(chart.series[1].color);
    });

    it("writes the total in the colour the chart draws its line in", () => {
        const [chart] = statementCharts("cashflow", cashFlow);

        expect(statementRowShades("cashflow", cashFlow)[2].ink).toBe(chart.line.color);
    });

    it("gives each group a faint shade of its own side for the accounts it expands into", () => {
        const [held, owed] = statementRowShades("cashflow", cashFlow);

        expect(held.accounts.fill).not.toBe(held.fill);
        expect(owed.accounts.fill).not.toBe(owed.fill);
        expect(held.accounts.fill).not.toBe(owed.accounts.fill);
    });

    it("leaves every row of the other statements alone", () => {
        expect(statementRowShades("balance", balance)).toEqual(balance.rows.map(() => null));
        expect(statementRowShades("profit", profit)).toEqual(profit.rows.map(() => null));
    });

    it("paints nothing when no year has been synced yet", () => {
        const empty = {columns: ["Yearly Cash Flow Statement"], rows: []};

        expect(statementRowShades("cashflow", empty)).toEqual([]);
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
