import {abbreviateAmount, monthlyBalanceCharts, monthlyCashFlowChart, monthlyProfitCharts, statementCharts, statementRowShades} from "../statementCharts";
import {aboveZeroShades, balanceClassColors, belowZeroShades, netIncomeColor, profitGainShades, profitGroupColors, profitLossShades, startingLevelColor} from "../../theme/palette";

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

    describe("the colours of the balance sheet's classes", () => {
        const both = {
            columns: ["Yearly Balance Sheet", "2019"],
            rows: [
                {type: "BALANCE_SUMMARY", schemaId: "a", name: "ASSETS", yearlyValues: [40]},
                {type: "BALANCE_CLASS", schemaId: "0", name: "Fixed Assets", yearlyValues: [5]},
                {type: "BALANCE_CLASS", schemaId: "1", name: "Resources", yearlyValues: [5]},
                {type: "BALANCE_CLASS", schemaId: "2", name: "Finance", yearlyValues: [20]},
                {type: "BALANCE_CLASS", schemaId: "3", name: "Relations", yearlyValues: [10]},
                {type: "BALANCE_SUMMARY", schemaId: "l", name: "LIABILITIES", yearlyValues: [40]},
                {type: "BALANCE_CLASS", schemaId: "2", name: "Finance", yearlyValues: [10]},
                {type: "BALANCE_CLASS", schemaId: "3", name: "Relations", yearlyValues: [10]},
                {type: "BALANCE_CLASS", schemaId: "4", name: "Funding", yearlyValues: [15]},
                {type: "BALANCE_CLASS", schemaId: "p", name: "Profit", yearlyValues: [5]},
            ],
        };
        const colours = () => statementCharts("balance", both)
            .map((chart) => Object.fromEntries(chart.series.map((series) => [series.name, series.color])));

        it("keeps a class on both sides in one colour - green for finance, yellow for relations", () => {
            const [assets, liabilities] = colours();

            expect(assets.Finance).toBe(liabilities.Finance);
            expect(assets.Relations).toBe(liabilities.Relations);
            expect([assets.Finance, assets.Relations]).toEqual([balanceClassColors["2"], balanceClassColors["3"]]);
        });

        it("gives every other class a colour no other class has, across both charts", () => {
            const [assets, liabilities] = colours();
            const all = [assets["Fixed Assets"], assets.Resources, assets.Finance, assets.Relations, liabilities.Funding, liabilities.Profit];

            expect(new Set(all).size).toBe(all.length);
        });

        it("hatches the profit, which stands there only to balance the sheet, and nothing else", () => {
            const [assets, liabilities] = statementCharts("balance", both);

            expect(liabilities.series.filter((series) => series.hatched).map((series) => series.name)).toEqual(["Profit"]);
            expect(assets.series.some((series) => series.hatched)).toBe(false);
        });
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

    describe("the income statement", () => {
        // shaped like the real statement: three rows to net income, three costs to operating
        // profit, then depreciation and pairs of income and cost of one name to net profit
        const row = (type, schemaId, name, values) => ({type, schemaId, name, yearlyValues: values});
        const income = {
            columns: ["Yearly Income Statement", "2019", "2020", "Total"],
            rows: [
                row("INCOME_GROUP", "60", "work", [100, 120]),
                row("EXPENSE_GROUP", "55", "office", [10, 20]),
                row("INCOME_GROUP", "63", "office", [5, 0]),
                row("PROFIT_SUMMARY", "ni", "Net Income", [95, 100]),
                row("EXPENSE_GROUP", "51", "consumption", [30, 40]),
                row("EXPENSE_GROUP", "52", "services", [5, 10]),
                row("PROFIT_SUMMARY", "op", "Operating Profit", [60, 50]),
                row("EXPENSE_GROUP", "50", "depreciation", [8, 6]),
                row("INCOME_GROUP", "62", "finance", [10, 70]),
                row("EXPENSE_GROUP", "54", "finance", [30, 20]),
                row("PROFIT_SUMMARY", "np", "Net Profit", [32, 94]),
            ],
        };
        const charts = () => statementCharts("profit", income);
        const names = (chart) => chart.series.map((series) => series.name);

        it("charts each profit level, and nothing else", () => {
            expect(charts().map((chart) => chart.title)).toEqual(["Net Income", "Operating Profit", "Net Profit"]);
            expect(charts().map((chart) => chart.form)).toEqual(["split", "split", "stacked"]);
        });

        describe("net income, as the work income with the other groups of the first rows laid over it", () => {
            const netIncome = () => charts()[0];
            const layers = (chart) => chart.series.map((series) => series.name + ": " + series.layer);

            it("draws the work income as the column, and net income as the line", () => {
                expect(netIncome().series[0]).toMatchObject({name: "work", layer: "behind"});
                expect(netIncome().points[0].s0).toBe(100);
                expect(netIncome().line.name).toBe("Net Income");
                expect(netIncome().points.map((point) => point.summary)).toEqual([95, 100]);
            });

            it("takes the costs from it and gives the other incomes back, in the order of the table", () => {
                expect(layers(netIncome())).toEqual(["work: behind", "office (cost): taken", "office (income): given"]);
                expect(netIncome().points[0]).toMatchObject({s1: 10, s2: 5});
            });

            it("ends the costs where what was given back starts, so that it reaches up to net income", () => {
                // 100 of work, 10 to office, 5 back from it: the costs reach from 90 up to 100,
                // and the 5 given back from 90 up to the 95 left
                expect(netIncome().points[0].lift).toBe(90);
                expect(netIncome().points[0].lift + 10).toBe(100);
                expect(netIncome().points[0].lift + 5).toBe(95);
            });

            it("lays a cost over as the accounts it is made of, lighter to darker in the order of the table", () => {
                const account = (schemaId, name, values) => ({type: "EXPENSE_ACCOUNT", schemaId, name, yearlyValues: values, children: []});
                const detailed = {...income, rows: income.rows.map((row) => row.schemaId === "55"
                    ? {...row, children: [account("550", "tax", [6, 12]), account("551", "health", [3, 5]), account("552", "social", [1, 3])]}
                    : row)};

                const [netIncome] = statementCharts("profit", detailed);

                expect(layers(netIncome)).toEqual([
                    "work: behind", "tax: taken", "health: taken", "social: taken", "office: given",
                ]);
                expect(netIncome.points[0]).toMatchObject({s1: 6, s2: 3, s3: 1, lift: 90});
                const reds = netIncome.series.slice(1, 4).map((series) => profitLossShades.indexOf(series.color));
                expect(reds).toEqual([...reds].sort());
                expect(new Set(reds).size).toBe(3);
                expect(reds.every((index) => index > 0)).toBe(true);
            });

            it("colours the work income the lighter green, the rest of the income the darker, and the costs red", () => {
                const [work, cost, office] = netIncome().series;

                expect(profitGainShades.indexOf(work.color)).toBeLessThan(profitGainShades.indexOf(office.color));
                expect(profitGainShades).toContain(office.color);
                expect(profitLossShades).toContain(cost.color);
                // darker than the palest red, which hardly showed over the pale green
                expect(profitLossShades.indexOf(cost.color)).toBeGreaterThan(profitLossShades.indexOf(charts()[1].series[1].color));
            });
        });

        it("draws in blue the level a chart starts from: net income under its costs, operating profit before net profit", () => {
            const [, operating, net] = charts();

            expect(operating.series[0]).toMatchObject({name: "Net Income", color: netIncomeColor, layer: "behind"});
            expect(net.series[0]).toMatchObject({name: "Operating Profit", color: startingLevelColor});
        });

        describe("operating profit, as the running costs laid over net income", () => {
            const operating = () => charts()[1];

            it("draws net income as the column, and operating profit as the line", () => {
                expect(operating().line.name).toBe("Operating Profit");
                expect(operating().points.map((point) => point.s0)).toEqual([95, 100]);
                expect(operating().points.map((point) => point.summary)).toEqual([60, 50]);
            });

            it("lays the running costs over it, as the amounts they came to", () => {
                // 95 of net income: 30 to consumption, 5 to services, 60 left
                expect(names(operating())).toEqual(["Net Income", "consumption", "services"]);
                expect(operating().points[0]).toMatchObject({s0: 95, s1: 30, s2: 5});
            });

            it("stacks the costs from the operating profit up, so they end at net income", () => {
                expect(operating().points[0].lift).toBe(60);
                expect(operating().points[0].lift + 30 + 5).toBe(95);
            });

            it("starts the costs below nought in a year they overran net income", () => {
                const overrun = {...income, rows: income.rows.map((row) => {
                    if (row.schemaId === "52") return {...row, yearlyValues: [5, 80]};
                    if (row.schemaId === "op") return {...row, yearlyValues: [60, -20]};
                    return row;
                })};

                const year = statementCharts("profit", overrun)[1].points[1];

                // 100 of net income, 120 of costs: the costs reach from -20 up to 100
                expect(year).toMatchObject({s0: 100, lift: -20, s1: 40, s2: 80, summary: -20});
            });

            it("colours the costs red", () => {
                operating().series.slice(1).forEach((cost) => expect(profitLossShades).toContain(cost.color));
            });
        });


        it("nets each pair after operating profit into the one figure it comes to", () => {
            const [, , net] = charts();

            expect(names(net)).toEqual(["Operating Profit", "depreciation", "finance"]);
            // finance: 10 - 30 in 2019, a loss; 70 - 20 in 2020, a gain
            expect(net.points.map((point) => point.s2)).toEqual([-20, 50]);
        });

        it("gives every group after operating profit one colour of its own, in the order of the table", () => {
            // a netted pair adds one year and takes away the next, so green and red would change
            // with the year; its place above or below nought says that instead
            const [, , net] = charts();
            const groups = net.series.slice(1);

            expect(groups.map((group) => group.color)).toEqual(profitGroupColors.slice(0, groups.length));
        });

        it("names each group once, plainly, with no word for the netting", () => {
            const [, , net] = charts();

            expect(names(net)).toEqual(["Operating Profit", "depreciation", "finance"]);
        });

        it("keeps a lone group after operating profit as it is, a cost below nought", () => {
            const [, , net] = charts();

            expect(net.points.map((point) => point.s1)).toEqual([-8, -6]);
        });

        it("draws in a lighter range than the cash flow, with no near-black red", () => {
            charts().forEach((chart) => chart.series.forEach((series) => {
                expect(belowZeroShades.map((shade) => shade.fill)).not.toContain(series.color);
            }));
        });

        it("leaves the Total column off the years axis", () => {
            expect(charts()[0].points.map((point) => point.period)).toEqual(["2019", "2020"]);
        });
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

    describe("an income statement", () => {
        const row = (type, schemaId, name, values) => ({type, schemaId, name, monthlyValues: values.concat(values.reduce((a, b) => a + b, 0))});
        const quiet = Array(9).fill(0);
        const yearlyIncome = {
            columns: ["Income Statement", ...months, "Total"],
            rows: [
                row("INCOME_GROUP", "60", "work", [100, 120, 110].concat(quiet)),
                row("EXPENSE_GROUP", "55", "office", [10, 20, 0].concat(quiet)),
                row("PROFIT_SUMMARY", "ni", "Net Income", [90, 100, 110].concat(quiet)),
                row("EXPENSE_GROUP", "51", "consumption", [30, 40, 50].concat(quiet)),
                row("PROFIT_SUMMARY", "op", "Operating Profit", [60, 60, 60].concat(quiet)),
                row("EXPENSE_GROUP", "50", "depreciation", [5, 5, 5].concat(quiet)),
                row("PROFIT_SUMMARY", "np", "Net Profit", [55, 55, 55].concat(quiet)),
            ],
        };
        const charts = () => statementCharts("profit", yearlyIncome, false);

        it("charts the same three profit levels as over all the years", () => {
            expect(charts().map((chart) => chart.title)).toEqual(["Net Income", "Operating Profit", "Net Profit"]);
            expect(charts().map((chart) => chart.form)).toEqual(["split", "split", "stacked"]);
        });

        it("charts each month as it was, under its column of the table", () => {
            const [netIncome] = charts();

            expect(netIncome.alignToTable).toBe(true);
            expect(netIncome.points.map((point) => point.period)).toEqual(months);
            // what each month earned, not run on from the month before
            expect(netIncome.points.slice(0, 3).map((point) => point.summary)).toEqual([90, 100, 110]);
            expect(netIncome.points[1]).toMatchObject({s0: 120, s1: 20});
        });

        it("only names the months still to come", () => {
            charts().forEach((chart) => {
                expect(chart.points[3]).toEqual({period: "April", summary: null});
            });
        });

        it("ends each chart at the last month its own figures moved in", () => {
            // something booked for December below operating profit
            const booked = {...yearlyIncome, rows: yearlyIncome.rows.map((each) => {
                if (each.schemaId === "50") return row("EXPENSE_GROUP", "50", "depreciation", [5, 5, 5, 0, 0, 0, 0, 0, 0, 0, 0, 200]);
                if (each.schemaId === "np") return row("PROFIT_SUMMARY", "np", "Net Profit", [55, 55, 55, 0, 0, 0, 0, 0, 0, 0, 0, -200]);
                return each;
            })};

            const [netIncome, operating, net] = statementCharts("profit", booked, false);

            expect(netIncome.points[11]).toEqual({period: "December", summary: null});
            expect(operating.points[11]).toEqual({period: "December", summary: null});
            expect(net.points[11].summary).toBe(-200);
            // a quiet month before one that moved still happened
            expect(net.points[5].summary).toBe(0);
        });
    });

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

    describe("a balance sheet", () => {
        const quiet = (...values) => [...values, ...Array(12 - values.length).fill(0)];
        const yearlyBalance = {
            columns: ["Balance Sheet", "Initial", ...months, "Total"],
            rows: [
                {type: "BALANCE_SUMMARY", schemaId: "a", name: "ASSETS", initial: 100, monthlyValues: quiet(10, 0, -30)},
                {type: "BALANCE_CLASS", schemaId: "0", name: "Fixed Assets", initial: 40, monthlyValues: quiet(0, 0, -30)},
                {type: "BALANCE_CLASS", schemaId: "2", name: "Finance", initial: 60, monthlyValues: quiet(10)},
                {type: "BALANCE_SUMMARY", schemaId: "l", name: "LIABILITIES", initial: 100, monthlyValues: quiet(10, 0, -30)},
                {type: "BALANCE_CLASS", schemaId: "4", name: "Funding", initial: 100, monthlyValues: quiet()},
                {type: "BALANCE_CLASS", schemaId: "p", name: "Profit", initial: 0, monthlyValues: quiet(10, 0, -30)},
            ],
        };
        const charts = () => statementCharts("balance", yearlyBalance, false);

        it("charts each side, class by class, as over all the years", () => {
            expect(charts().map((chart) => chart.title)).toEqual(["Assets", "Liabilities"]);
            expect(charts()[0].series.map((series) => series.name)).toEqual(["Fixed Assets", "Finance"]);
            expect(charts()[0].form).toBe("stacked");
        });

        it("charts where the classes stood, from the opening balance under the Initial column", () => {
            const [assets] = charts();

            expect(assets.alignToTable).toBe(true);
            expect(assets.points[0]).toEqual({period: "Initial", s0: 40, s1: 60, summary: 100});
            // a quiet month carries the balance on
            expect(assets.points.slice(1, 4).map((point) => point.summary)).toEqual([110, 110, 80]);
            expect(assets.points[3]).toMatchObject({s0: 10, s1: 70});
        });

        it("only names the months still to come", () => {
            charts().forEach((chart) => {
                expect(chart.points[4]).toEqual({period: "April", summary: null});
            });
        });
    });
});

describe("monthlyProfitCharts", () => {
    const months = ["January", "February", "March", "April", "May", "June", "July",
        "August", "September", "October", "November", "December"];
    const monthly = (...values) => [...values, ...Array(12 - values.length).fill(0)];
    const leaf = (type, schemaId, name, values) => ({type, schemaId, name, monthlyValues: values, children: []});
    // net income is work less office, which is made of tax; operating profit nets consumption off;
    // after it, office appears again under the same ids, as the real statement's groups do
    const year = (work, tax, consumption, later) => ({
        columns: ["Income Statement", ...months, "Total"],
        rows: [
            leaf("INCOME_GROUP", "60", "work", work),
            {...leaf("EXPENSE_GROUP", "55", "office", tax), children: [leaf("EXPENSE_ACCOUNT", "550", "tax", tax)]},
            leaf("PROFIT_SUMMARY", "ni", "Net Income", work.map((value, month) => value - tax[month])),
            leaf("EXPENSE_GROUP", "51", "consumption", consumption),
            leaf("PROFIT_SUMMARY", "op", "Operating Profit", work.map((value, month) => value - tax[month] - consumption[month])),
            leaf("EXPENSE_GROUP", "55", "office", later),
            leaf("PROFIT_SUMMARY", "np", "Net Profit", work.map((value, month) => value - tax[month] - consumption[month] - later[month])),
        ],
    });
    const overall = {
        columns: ["Yearly Income Statement", "2019", "2020", "Total"],
        rows: [
            {type: "INCOME_GROUP", schemaId: "60", name: "work", yearlyValues: [300, 100, 400], children: []},
            {type: "EXPENSE_GROUP", schemaId: "55", name: "office", yearlyValues: [30, 10, 40],
                children: [{type: "EXPENSE_ACCOUNT", schemaId: "550", name: "tax", yearlyValues: [30, 10, 40], children: []}]},
            {type: "PROFIT_SUMMARY", schemaId: "ni", name: "Net Income", yearlyValues: [270, 90, 360], children: []},
            {type: "EXPENSE_GROUP", schemaId: "51", name: "consumption", yearlyValues: [60, 20, 80], children: []},
            {type: "PROFIT_SUMMARY", schemaId: "op", name: "Operating Profit", yearlyValues: [210, 70, 280], children: []},
            {type: "EXPENSE_GROUP", schemaId: "55", name: "office", yearlyValues: [7, 0, 7], children: []},
            {type: "PROFIT_SUMMARY", schemaId: "np", name: "Net Profit", yearlyValues: [203, 70, 273], children: []},
        ],
    };
    const years = [
        {year: "2019", data: year(monthly(100, 100, 100, ...Array(9).fill(0)), monthly(10, 10, 10), monthly(20, 20, 20), monthly(0, 0, 7))},
        {year: "2020", data: year(monthly(100), monthly(10), monthly(20), monthly())},
    ];
    const charts = () => monthlyProfitCharts(overall, years);

    it("charts the same three profit levels as the years, by month", () => {
        expect(charts().map((chart) => chart.title)).toEqual(["Net Income", "Operating Profit", "Net Profit"]);
        expect(charts().map((chart) => chart.form)).toEqual(["split", "split", "stacked"]);
        // keyed after the chart of the years each is a variant of
        expect(charts().map((chart) => chart.key)).toEqual(["level0-monthly", "level1-monthly", "level2-monthly"]);
    });

    it("runs every year's months one after another, naming the years at their Januaries", () => {
        const [netIncome] = charts();

        expect(netIncome.points.map((point) => point.period).slice(10, 14))
            .toEqual(["November 2019", "December 2019", "January 2020", "February 2020"]);
        expect(netIncome.ticks).toEqual([{value: "January 2019", label: "2019"}, {value: "January 2020", label: "2020"}]);
    });

    it("reads each month from its own year's statement", () => {
        const [netIncome] = charts();

        expect(netIncome.points[0]).toMatchObject({period: "January 2019", s0: 100, s1: 10, summary: 90});
        expect(netIncome.points[12]).toMatchObject({period: "January 2020", s0: 100, s1: 10, summary: 90});
        // the quiet months of a year that went on still happened
        expect(netIncome.points[5]).toMatchObject({s0: 0, summary: 0});
    });

    it("lays a cost over as its accounts, read from the account of the same place in each year", () => {
        const [netIncome] = charts();

        expect(netIncome.series.map((series) => series.name)).toEqual(["work", "tax"]);
        expect(netIncome.points[1].s1).toBe(10);
    });

    it("matches a year's rows to the overall ones by their place, as a schema id comes up twice", () => {
        const [, , net] = charts();

        // the second office, after operating profit, took 7 in March 2019 - not the first one's 10
        expect(net.series.map((series) => series.name)).toEqual(["Operating Profit", "office"]);
        expect(net.points[2]).toMatchObject({s1: -7, summary: 63});
    });

    it("ends each chart at the last month its own figures moved in", () => {
        const [netIncome] = charts();

        expect(netIncome.points[13]).toEqual({period: "February 2020", summary: null});
    });

    it("charts nothing without a year to chart", () => {
        expect(monthlyProfitCharts(overall, [])).toEqual([]);
    });
});

describe("monthlyBalanceCharts", () => {
    const months = ["January", "February", "March", "April", "May", "June", "July",
        "August", "September", "October", "November", "December"];
    const monthly = (...values) => [...values, ...Array(12 - values.length).fill(0)];
    // both sides have a Finance of the same id, as the real balance sheet does
    const year = (cash, credit, profit, initial) => ({
        columns: ["Balance Sheet", "Initial", ...months, "Total"],
        rows: [
            {type: "BALANCE_SUMMARY", schemaId: "a", name: "ASSETS", initial: initial.cash, monthlyValues: cash},
            {type: "BALANCE_CLASS", schemaId: "2", name: "Finance", initial: initial.cash, monthlyValues: cash},
            {type: "BALANCE_SUMMARY", schemaId: "l", name: "LIABILITIES", initial: initial.cash, monthlyValues: cash},
            {type: "BALANCE_CLASS", schemaId: "2", name: "Finance", initial: initial.credit, monthlyValues: credit},
            {type: "BALANCE_CLASS", schemaId: "4", name: "Funding", initial: initial.funding, monthlyValues: monthly()},
            {type: "BALANCE_CLASS", schemaId: "p", name: "Profit", initial: 0, monthlyValues: profit},
        ],
    });
    const overall = {
        columns: ["Yearly Balance Sheet", "2019", "2020"],
        rows: [
            {type: "BALANCE_SUMMARY", schemaId: "a", name: "ASSETS", yearlyValues: [130, 150]},
            {type: "BALANCE_CLASS", schemaId: "2", name: "Finance", yearlyValues: [130, 150]},
            {type: "BALANCE_SUMMARY", schemaId: "l", name: "LIABILITIES", yearlyValues: [130, 150]},
            {type: "BALANCE_CLASS", schemaId: "2", name: "Finance", yearlyValues: [20, 20]},
            {type: "BALANCE_CLASS", schemaId: "4", name: "Funding", yearlyValues: [80, 110]},
            {type: "BALANCE_CLASS", schemaId: "p", name: "Profit", yearlyValues: [30, 20]},
        ],
    };
    // 2019 opens with 100 of cash against 20 of credit and 80 of funding, and makes 30; 2020 opens
    // with that 30 moved into funding, and makes 20 by February
    const years = [
        {year: "2019", data: year(monthly(10, 0, 20), monthly(), monthly(10, 0, 20), {cash: 100, credit: 20, funding: 80})},
        {year: "2020", data: year(monthly(5, 15), monthly(), monthly(5, 15), {cash: 130, credit: 20, funding: 110})},
    ];
    const charts = () => monthlyBalanceCharts(overall, years);

    it("charts each side as the years do, keyed after the chart of the years each is a variant of", () => {
        expect(charts().map((chart) => chart.title)).toEqual(["Assets", "Liabilities"]);
        expect(charts().map((chart) => chart.key)).toEqual(["a-monthly", "l-monthly"]);
    });

    it("charts where the classes stood at the end of every month, one year after another", () => {
        const [assets] = charts();

        expect(assets.points.slice(0, 3).map((point) => point.summary)).toEqual([110, 110, 130]);
        expect(assets.points[11]).toMatchObject({period: "December 2019", summary: 130});
        expect(assets.points[12]).toMatchObject({period: "January 2020", summary: 135});
        expect(assets.ticks).toEqual([{value: "January 2019", label: "2019"}, {value: "January 2020", label: "2020"}]);
    });

    it("moves the year's profit into funding at the turn of the year, as the books do", () => {
        const [, liabilities] = charts();
        const [, funding, profit] = liabilities.series.map((series) => series.key);

        expect(liabilities.points[11]).toMatchObject({[funding]: 80, [profit]: 30});
        expect(liabilities.points[12]).toMatchObject({[funding]: 110, [profit]: 5});
    });

    it("matches a year's rows to the overall ones by their place, as both sides have a Finance", () => {
        const [, liabilities] = charts();

        expect(liabilities.series[0].name).toBe("Finance");
        expect(liabilities.points[0].s0).toBe(20);
    });

    it("only names the months after the last one anything moved in", () => {
        const [assets] = charts();

        expect(assets.points[13]).toMatchObject({period: "February 2020", summary: 150});
        expect(assets.points[14]).toEqual({period: "March 2020", summary: null});
    });

    it("charts nothing without a year to chart", () => {
        expect(monthlyBalanceCharts(overall, [])).toEqual([]);
    });
});

describe("monthlyCashFlowChart", () => {
    const months = ["January", "February", "March", "April", "May", "June", "July",
        "August", "September", "October", "November", "December"];
    const monthly = (...values) => [...values, ...Array(12 - values.length).fill(0)];
    const year = (initial, cash, credit) => {
        const total = cash.map((change, month) => change + credit[month]);
        return {
            columns: ["Cash Flow Statement", "Initial", ...months, "Total"],
            rows: [
                {type: "CASH_FLOW_GROUP", schemaId: "20", name: "Cash", initial: initial.cash, monthlyValues: cash},
                {type: "CASH_FLOW_GROUP", schemaId: "22", name: "Credit", initial: initial.credit, monthlyValues: credit},
                {type: "CASH_FLOW_SUMMARY", schemaId: "cf", name: "Cash Flow",
                    initial: initial.cash + initial.credit, monthlyValues: total},
            ],
        };
    };
    const overall = {
        columns: ["Yearly Cash Flow Statement", "2018", "2019", "2020"],
        rows: [
            {type: "CASH_FLOW_GROUP", schemaId: "20", name: "Cash", yearlyValues: [0, 110, 115]},
            {type: "CASH_FLOW_GROUP", schemaId: "22", name: "Credit", yearlyValues: [0, -50, -55]},
            {type: "CASH_FLOW_SUMMARY", schemaId: "cf", name: "Cash Flow", yearlyValues: [0, 60, 60]},
        ],
    };
    const years = [
        {year: "2018", data: year({cash: 0, credit: 0}, monthly(), monthly())},
        {year: "2019", data: year({cash: 100, credit: -50}, monthly(10), monthly(0, -5))},
        {year: "2020", data: year({cash: 110, credit: -55}, monthly(5), monthly())},
    ];
    const chart = () => monthlyCashFlowChart(overall, years);

    it("draws every month of every year, the way the chart of a single year draws them", () => {
        expect(chart().form).toBe("changes");
        expect(chart().points.slice(1, 3).map((point) => point.period)).toEqual(["January 2018", "February 2018"]);
    });

    it("starts from the first year, even one that recorded nothing", () => {
        expect(chart().ticks).toEqual([
            {value: "January 2018", label: "2018"},
            {value: "January 2019", label: "2019"},
            {value: "January 2020", label: "2020"},
        ]);
        expect(chart().points[1]).toMatchObject({period: "January 2018", summary: 0});
    });

    it("measures every month from one baseline: the opening of the first year", () => {
        const january2020 = chart().points[25];

        expect(chart().baseline).toBe(0);
        expect(january2020.period).toBe("January 2020");
        // 2020 opened at 55, but its month still stands on the first year's nought
        expect(january2020.spacer).toBe(0);
        expect(january2020.s0_above).toBe(5);
    });

    it("runs the line on through the turn of the year, from one month end to the next", () => {
        const line = chart().points.map((point) => point.summary);

        expect(line[12]).toBe(0); // December 2018
        expect(line[13]).toBe(60); // January 2019
        expect(line[14]).toBe(55); // February 2019
        expect(line[25]).toBe(60); // January 2020
    });

    it("sets the line off from the first year's opening", () => {
        expect(chart().points[0]).toEqual({period: "Initial", summary: 0, opening: true});
    });

    it("leaves empty only the months still to come", () => {
        expect(chart().points[26]).toEqual({period: "February 2020", summary: null});
        expect(chart().points).toHaveLength(1 + 36);
    });

    it("keeps a quiet month between recorded ones", () => {
        // nothing moved from March to December 2019, but January 2020 did
        expect(chart().points[15].summary).toBe(55);
    });

    it("keeps the colours and the order of the overall table", () => {
        const [overallChart] = statementCharts("cashflow", overall);

        expect(chart().series).toEqual(overallChart.series);
    });

    it("charts nothing when nothing was ever recorded", () => {
        expect(monthlyCashFlowChart(overall, years.slice(0, 1))).toBeNull();
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
