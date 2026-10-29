import {render, screen} from "@testing-library/react";
import StatementChart from "../StatementChart";

// recharts measures its container, and jsdom lays nothing out, so a responsive chart would draw
// nothing at all. Giving it a size is what lets the marks be asserted on.
jest.mock("recharts", () => {
    const recharts = jest.requireActual("recharts");
    const {cloneElement} = jest.requireActual("react");
    return {
        ...recharts,
        ResponsiveContainer: ({children}) => cloneElement(children, {width: 800, height: 400}),
    };
});

const chart = (overrides = {}) => ({
    key: "a",
    title: "Assets",
    form: "stacked",
    series: [
        {key: "s0", name: "Fixed Assets", color: "#2a78d6"},
        {key: "s1", name: "Finance", color: "#eb6834"},
    ],
    line: {key: "summary", name: "Assets", color: "#3fab9e"},
    points: [
        {period: "2019", s0: 10, s1: -20, summary: -10},
        {period: "2020", s0: 15, s1: 25, summary: 40},
    ],
    ...overrides,
});

/* eslint-disable testing-library/no-container, testing-library/no-node-access -- a chart has no accessible roles to query by */

describe("StatementChart", () => {
    it("names the chart", () => {
        render(<StatementChart chart={chart()}/>);

        expect(screen.getByRole("heading", {name: "Assets"})).toBeInTheDocument();
    });

    it("leaves the name off when the view does not need the charts told apart", () => {
        render(<StatementChart chart={chart()} titled={false}/>);

        expect(screen.queryByRole("heading")).not.toBeInTheDocument();
    });

    it("draws a column per component and a line for the summary", () => {
        const {container} = render(<StatementChart chart={chart()}/>);

        expect(container.querySelectorAll(".recharts-bar")).toHaveLength(2);
        expect(container.querySelectorAll(".recharts-line")).toHaveLength(1);
    });

    it("stacks the first series on top, where the table lists it first", () => {
        const {container} = render(<StatementChart chart={chart()}/>);

        // both series are positive in the second year, so the one nearer the top of the chart is
        // the one stacked above the other; each is found by the colour it was given
        const headOf = (color) => {
            const columns = Array.from(container.querySelectorAll(".recharts-rectangle"))
                .filter((column) => column.getAttribute("fill") === color);
            const last = columns[columns.length - 1];
            return Number(/M\s*[-\d.]+,([-\d.]+)/.exec(last.getAttribute("d"))[1]);
        };

        expect(headOf("#2a78d6")).toBeLessThan(headOf("#eb6834"));
    });

    it("puts the first cost directly under nought, where the table lists it first", () => {
        const costs = chart({
            series: [
                {key: "s0", name: "Rent", color: "#2a78d6"},
                {key: "s1", name: "Food", color: "#eb6834"},
            ],
            line: null,
            points: [
                {period: "2019", s0: -10, s1: -20},
                {period: "2020", s0: -15, s1: -25},
            ],
        });
        const {container} = render(<StatementChart chart={costs}/>);

        // a column below the axis is drawn upwards from its foot, so its head - the end nearer
        // nought - is the foot plus the (negative) height
        const headOf = (color) => {
            const columns = Array.from(container.querySelectorAll(".recharts-rectangle"))
                .filter((column) => column.getAttribute("fill") === color);
            const [, foot, height] = /M\s*[-\d.]+,([-\d.]+) h [-\d.]+ v ([-\d.]+)/
                .exec(columns[columns.length - 1].getAttribute("d"));
            return Number(foot) + Number(height);
        };
        const zero = Number(container.querySelector(".recharts-reference-line line").getAttribute("y1"));

        expect(Math.round(headOf("#2a78d6"))).toBe(Math.round(zero));
        expect(headOf("#eb6834")).toBeGreaterThan(headOf("#2a78d6"));
    });

    it("draws a series that gains one year and loses the next on either side of nought, in its one colour", () => {
        const crossing = chart({
            series: [{key: "s0", name: "finance", color: "#F5BB6E"}],
            line: null,
            points: [{period: "2019", s0: -20}, {period: "2020", s0: 50}],
        });
        const {container} = render(<StatementChart chart={crossing}/>);

        const zero = Math.round(Number(container.querySelector(".recharts-reference-line line").getAttribute("y1")));
        const [loss, gain] = Array.from(container.querySelectorAll(".recharts-rectangle"))
            .filter((rectangle) => rectangle.getAttribute("fill") === "#F5BB6E")
            .map((rectangle) => {
                const [, y, height] = /M\s*[-\d.]+,([-\d.]+) h [-\d.]+ v ([-\d.]+)/.exec(rectangle.getAttribute("d"));
                const ends = [Number(y), Number(y) + Number(height)];
                return {top: Math.round(Math.min(...ends)), bottom: Math.round(Math.max(...ends))};
            });

        expect(loss.top).toBe(zero);
        expect(gain.bottom).toBe(zero);
        expect(screen.getAllByText("finance")).toHaveLength(1);
    });

    describe("a total with its parts laid over it", () => {
        const split = () => chart({
            form: "split",
            series: [
                {key: "s0", name: "Net Income", color: "#000010", layer: "behind"},
                {key: "s1", name: "consumption", color: "#000011", layer: "taken"},
                {key: "s2", name: "services", color: "#000012", layer: "taken"},
            ],
            line: {key: "summary", name: "Operating Profit", color: "#000"},
            points: [
                // net income 100: 30 and 10 taken, 60 left
                {period: "2019", s0: 100, lift: 60, s1: 30, s2: 10, summary: 60},
                // net income 100: 40 and 80 taken, 20 more than there was
                {period: "2020", s0: 100, lift: -20, s1: 40, s2: 80, summary: -20},
            ],
        });
        const boxes = (container, color) => Array.from(container.querySelectorAll(".recharts-rectangle"))
            .filter((rectangle) => rectangle.getAttribute("fill") === color)
            .map((rectangle) => {
                const [, x, y, width, height] = /M\s*([-\d.]+),([-\d.]+) h ([-\d.]+) v ([-\d.]+)/.exec(rectangle.getAttribute("d"));
                const ends = [Number(y), Number(y) + Number(height)];
                return {
                    left: Math.round(Number(x)), right: Math.round(Number(x) + Number(width)),
                    top: Math.round(Math.min(...ends)), bottom: Math.round(Math.max(...ends)),
                };
            });
        const zeroOf = (container) => Math.round(Number(container.querySelector(".recharts-reference-line line").getAttribute("y1")));

        it("draws the total as a whole column, from nought up", () => {
            const {container} = render(<StatementChart chart={split()}/>);
            const [total] = boxes(container, "#000010");
            const [topCost] = boxes(container, "#000011");

            expect(total.bottom).toBe(zeroOf(container));
            expect(total.top).toBe(topCost.top);
        });

        it("lays the parts over it from the right, leaving the total showing down its left edge", () => {
            const {container} = render(<StatementChart chart={split()}/>);
            const [total] = boxes(container, "#000010");
            const [cost] = boxes(container, "#000011");

            expect(cost.left).toBeGreaterThan(total.left);
            expect(cost.right).toBe(total.right);
            // a strip down the edge, with the parts over most of the column - not half of it each
            expect(cost.right - cost.left).toBeGreaterThan(0.8 * (total.right - total.left));
        });

        it("leaves the foot of the total uncovered in a year with something left", () => {
            const {container} = render(<StatementChart chart={split()}/>);
            const [total] = boxes(container, "#000010");
            const [lowestCost] = boxes(container, "#000012");

            expect(lowestCost.bottom).toBeLessThan(total.bottom);
            // the table's first part is on top
            expect(boxes(container, "#000011")[0].bottom).toBe(lowestCost.top);
        });

        it("lets the parts reach on down past nought in a year they overran the total", () => {
            const {container} = render(<StatementChart chart={split()}/>);
            const lowestCost2020 = boxes(container, "#000012")[1];

            expect(lowestCost2020.bottom).toBeGreaterThan(zeroOf(container));
            expect(lowestCost2020.top).toBeLessThan(zeroOf(container));
        });

        it("never draws the spacer that lifts the parts", () => {
            const {container} = render(<StatementChart chart={split()}/>);

            const drawn = Array.from(container.querySelectorAll(".recharts-rectangle"))
                .map((rectangle) => rectangle.getAttribute("fill"))
                .filter((fill) => fill !== "transparent");
            // per year: the total and two parts
            expect(drawn).toHaveLength(6);
        });

        it("gives back from the foot of the costs what came in besides, up to the line", () => {
            const incomes = chart({
                form: "split",
                series: [
                    {key: "s0", name: "work", color: "#000020", layer: "behind"},
                    {key: "s1", name: "office (cost)", color: "#000022", layer: "taken"},
                    {key: "s2", name: "office (income)", color: "#000021", layer: "given"},
                ],
                line: {key: "summary", name: "Net Income", color: "#000"},
                // 100 of work, 40 to office, 10 back from it: 70 left, the costs ending at 60
                points: [{period: "2019", s0: 100, s1: 40, s2: 10, lift: 60, summary: 70}],
            });
            const {container} = render(<StatementChart chart={incomes}/>);
            const [work] = boxes(container, "#000020");
            const [cost] = boxes(container, "#000022");
            const [back] = boxes(container, "#000021");
            const lineAt = (value) => zeroOf(container) - (zeroOf(container) - work.top) * value / 100;

            expect(work.bottom).toBe(zeroOf(container));
            // the cost from the top of the work income down
            expect(cost.top).toBe(work.top);
            expect(cost.bottom).toBe(Math.round(lineAt(60)));
            // and what came back over its foot, up to what was left
            expect(back.bottom).toBe(cost.bottom);
            expect(back.top).toBe(Math.round(lineAt(70)));
            // short of the right edge too, so the cost shows down it to its full length
            expect(back.left).toBe(cost.left);
            expect(back.right).toBeLessThan(cost.right);
            // drawn in front of the cost it covers
            const fills = Array.from(container.querySelectorAll(".recharts-rectangle")).map((r) => r.getAttribute("fill"));
            expect(fills.indexOf("#000021")).toBeGreaterThan(fills.indexOf("#000022"));
        });
    });

    describe("a hatched series", () => {
        const hatched = () => chart({
            key: "l",
            series: [
                {key: "s0", name: "Funding", color: "#6250d6"},
                {key: "s1", name: "Profit", color: "#e87ba4", hatched: true},
            ],
            points: [{period: "2019", s0: 10, s1: 20, summary: 30}],
        });

        it("fills its columns with stripes of its colour, the others plainly", () => {
            const {container} = render(<StatementChart chart={hatched()}/>);

            const fills = Array.from(container.querySelectorAll(".recharts-bar-rectangle .recharts-rectangle"))
                .map((rectangle) => rectangle.getAttribute("fill"));
            expect(fills).toContain("#6250d6");
            expect(fills).toContain("url(#hatch-l-s1)");
            const stripes = container.querySelector("pattern#hatch-l-s1");
            expect(Array.from(stripes.querySelectorAll("rect")).map((rect) => rect.getAttribute("fill")))
                .toEqual(["#e87ba4", "#e87ba4"]);
        });

        it("keys it with the stripes, but names it in its plain colour", () => {
            const {container} = render(<StatementChart chart={hatched()}/>);

            const entry = Array.from(container.querySelectorAll(".recharts-legend-item"))
                .find((item) => item.textContent === "Profit");
            expect(entry.querySelector("path").getAttribute("fill")).toBe("url(#hatch-l-s1)");
            expect(screen.getByText("Profit", {selector: ".recharts-legend-item span span"})).toHaveStyle({color: "#e87ba4"});
        });
    });

    it("keys the chart in the order of the table, not the order the columns are drawn", () => {
        render(<StatementChart chart={chart()}/>);

        const key = screen.getAllByText(/Fixed Assets|Finance|Assets/)
            .map((entry) => entry.textContent);
        expect(key).toEqual(["Assets", "Fixed Assets", "Finance", "Assets"]);
    });

    it("stacks the columns, so the series of a year share one column", () => {
        const {container} = render(<StatementChart chart={chart()}/>);

        const xs = Array.from(container.querySelectorAll(".recharts-bar-rectangle path"))
            .map((bar) => /M\s*([-\d.]+)/.exec(bar.getAttribute("d"))[1]);

        expect(xs).toHaveLength(4);
        expect(new Set(xs).size).toBe(2);
    });

    it("stands a chart of changes on its baseline: losses below it, gains above it", () => {
        const changes = chart({
            form: "changes",
            baseline: 100,
            points: [
                {period: "Jan", summary: 110, spacer: 80, s0: 30, s0_above: 30, s0_below: null,
                    s1: -20, s1_above: null, s1_below: 20},
            ],
        });
        const {container} = render(<StatementChart chart={changes}/>);

        const baseline = Number(container.querySelector(".recharts-reference-line line[stroke-dasharray]")
            .getAttribute("y1"));
        const extentOf = (color) => {
            const column = Array.from(container.querySelectorAll(".recharts-rectangle"))
                .find((rectangle) => rectangle.getAttribute("fill") === color);
            const [, y, height] = /M\s*[-\d.]+,([-\d.]+) h [-\d.]+ v ([-\d.]+)/.exec(column.getAttribute("d"));
            const ends = [Number(y), Number(y) + Number(height)];
            return {top: Math.min(...ends), bottom: Math.max(...ends)};
        };

        // the gain sits on the baseline, the loss hangs from it
        expect(Math.round(extentOf("#2a78d6").bottom)).toBe(Math.round(baseline));
        expect(Math.round(extentOf("#eb6834").top)).toBe(Math.round(baseline));
    });

    describe("a column of changes, read top to bottom in the order of the table", () => {
        const twoGainsTwoLosses = () => chart({
            form: "changes",
            baseline: 100,
            series: [
                {key: "s0", name: "First", color: "#000001"},
                {key: "s1", name: "Second", color: "#000002"},
            ],
            points: [
                {period: "Jan", summary: 150, spacer: 100, s0: 30, s0_above: 30, s1: 20, s1_above: 20},
                {period: "Feb", summary: 50, spacer: 50, s0: -30, s0_below: 30, s1: -20, s1_below: 20},
            ],
        });
        const baselineOf = (container) => Math.round(Number(
            container.querySelector(".recharts-reference-line line[stroke-dasharray]").getAttribute("y1")));
        // a group's gain and its loss are told apart by the side of the baseline they are drawn on
        const partOf = (container, color, side) => Array.from(container.querySelectorAll(".recharts-rectangle"))
            .filter((rectangle) => rectangle.getAttribute("fill") === color)
            .map((rectangle) => {
                const [, y, height] = /M\s*[-\d.]+,([-\d.]+) h [-\d.]+ v ([-\d.]+)/.exec(rectangle.getAttribute("d"));
                const ends = [Number(y), Number(y) + Number(height)];
                return {top: Math.round(Math.min(...ends)), bottom: Math.round(Math.max(...ends))};
            })
            .find((extent) => side === "above"
                ? extent.bottom <= baselineOf(container)
                : extent.top >= baselineOf(container));

        it("puts the table's first gain at the top, and its last standing on the baseline", () => {
            const {container} = render(<StatementChart chart={twoGainsTwoLosses()}/>);
            const firstGain = partOf(container, "#000001", "above");
            const lastGain = partOf(container, "#000002", "above");

            expect(lastGain.bottom).toBe(baselineOf(container));
            expect(firstGain.bottom).toBe(lastGain.top);
        });

        it("hangs the table's first loss from the baseline, and its last at the bottom", () => {
            const {container} = render(<StatementChart chart={twoGainsTwoLosses()}/>);
            const firstLoss = partOf(container, "#000001", "below");
            const lastLoss = partOf(container, "#000002", "below");

            expect(firstLoss.top).toBe(baselineOf(container));
            expect(lastLoss.top).toBe(firstLoss.bottom);
        });
    });

    it("never draws the spacer that lifts a chart of changes off nought", () => {
        const changes = chart({
            form: "changes",
            baseline: 100,
            points: [{period: "Jan", summary: 110, spacer: 80, s0: 30, s0_above: 30, s1: -20, s1_below: 20}],
        });
        const {container} = render(<StatementChart chart={changes}/>);

        const fills = Array.from(container.querySelectorAll(".recharts-rectangle"))
            .map((rectangle) => rectangle.getAttribute("fill"));
        expect(fills.filter((fill) => fill !== "transparent")).toHaveLength(2);
    });

    describe("narrowed to a stretch of its points", () => {
        it("offers a slider under a chart that spaces its own points", () => {
            const {container} = render(<StatementChart chart={chart()}/>);

            expect(container.querySelector(".recharts-brush")).toBeInTheDocument();
        });

    });

    describe("over many years", () => {
        const decade = (baseline) => chart({
            form: "changes",
            baseline,
            points: [
                {period: "January 2019", summary: 10, spacer: baseline, s0: 10, s0_above: 10, s1: 0},
                {period: "February 2019", summary: 10, spacer: baseline, s0: 0, s1: 0},
                {period: "January 2020", summary: 15, spacer: baseline, s0: 5, s0_above: 5, s1: 0},
            ],
            ticks: [{value: "January 2019", label: "2019"}, {value: "January 2020", label: "2020"}],
        });

        it("names only the points it lists, by their own labels", () => {
            const {container} = render(<StatementChart chart={decade(0)}/>);

            const ticks = Array.from(container.querySelectorAll(".recharts-xAxis .recharts-cartesian-axis-tick"))
                .map((tick) => tick.textContent);
            expect(ticks).toEqual(["2019", "2020"]);
        });

        it("leaves a baseline at nought to the axis to name", () => {
            const {container} = render(<StatementChart chart={decade(0)}/>);

            expect(container.querySelector(".recharts-reference-line .recharts-label")).toBeNull();
        });

        it("names any other baseline on the value axis", () => {
            const {container} = render(<StatementChart chart={decade(100)}/>);

            expect(container.querySelector(".recharts-reference-line .recharts-label")).toHaveTextContent("100");
        });
    });

    describe("lined up with the table", () => {
        const layout = {
            width: 700,
            columns: [
                {name: "Statement", left: 0, width: 200},
                {name: "Initial", left: 200, width: 150},
                {name: "Jan", left: 350, width: 90},
                {name: "Feb", left: 440, width: 160},
                {name: "Total", left: 600, width: 100},
            ],
        };
        const aligned = () => chart({
            form: "changes",
            baseline: 100,
            points: [
                {period: "Jan", summary: 110, spacer: 100, s0: 10, s0_above: 10, s1: 0},
                {period: "Feb", summary: 130, spacer: 110, s0: 20, s0_above: 20, s1: 0},
            ],
        });

        it("draws each month's column under the table column of the same name", () => {
            const {container} = render(<StatementChart chart={aligned()} layout={layout}/>);

            const centres = Array.from(container.querySelectorAll(".recharts-rectangle"))
                .filter((rectangle) => rectangle.getAttribute("fill") === "#2a78d6")
                .map((rectangle) => {
                    const [, x, width] = /M\s*([-\d.]+),[-\d.]+ h ([-\d.]+)/.exec(rectangle.getAttribute("d"));
                    return Math.round(Number(x) + Number(width) / 2);
                });

            // Jan is centred at 350 + 90/2, Feb at 440 + 160/2
            expect(centres).toEqual([395, 520]);
        });

        it("is drawn as wide as the table, its value axis just in from the table's left edge", () => {
            const {container} = render(<StatementChart chart={aligned()} layout={layout}/>);

            expect(container.querySelector(".recharts-surface").getAttribute("width")).toBe("700");
            // a 20px margin, then recharts' default axis width: near the edge, not across the row names
            const axis = container.querySelector(".recharts-yAxis .recharts-cartesian-axis-line");
            expect(Math.round(Number(axis.getAttribute("x1")))).toBe(80);
        });

        it("keeps its columns under the table's columns, margins notwithstanding", () => {
            const {container} = render(<StatementChart chart={aligned()} layout={layout}/>);

            const centres = Array.from(container.querySelectorAll(".recharts-rectangle"))
                .filter((rectangle) => rectangle.getAttribute("fill") === "#2a78d6")
                .map((rectangle) => {
                    const [, x, width] = /M\s*([-\d.]+),[-\d.]+ h ([-\d.]+)/.exec(rectangle.getAttribute("d"));
                    return Math.round(Number(x) + Number(width) / 2);
                });
            expect(centres).toEqual([395, 520]);
        });

        it("marks the baseline's figure on the value axis", () => {
            render(<StatementChart chart={aligned()} layout={layout}/>);

            expect(screen.getByText("100")).toBeInTheDocument();
        });

        it("offers no slider, which would pull its points out from under their columns", () => {
            const {container} = render(<StatementChart chart={aligned()} layout={layout}/>);

            expect(container.querySelector(".recharts-brush")).not.toBeInTheDocument();
        });

        it("puts every layer of a split column under the table column, not only the one behind", () => {
            const split = chart({
                form: "split",
                series: [
                    {key: "s0", name: "Net Income", color: "#000030", layer: "behind"},
                    {key: "s1", name: "consumption", color: "#000031", layer: "taken"},
                ],
                line: {key: "summary", name: "Operating Profit", color: "#000"},
                points: [
                    {period: "Jan", s0: 100, s1: 30, lift: 70, summary: 70},
                    {period: "Feb", s0: 100, s1: 40, lift: 60, summary: 60},
                ],
            });
            const {container} = render(<StatementChart chart={split} layout={layout}/>);
            const rightEdges = (color) => Array.from(container.querySelectorAll(".recharts-rectangle"))
                .filter((rectangle) => rectangle.getAttribute("fill") === color)
                .map((rectangle) => {
                    const [, x, width] = /M\s*([-\d.]+),[-\d.]+ h ([-\d.]+)/.exec(rectangle.getAttribute("d"));
                    return Math.round(Number(x) + Number(width));
                });

            expect(rightEdges("#000031")).toEqual(rightEdges("#000030"));
        });

        it("draws nothing until the table has been laid out", () => {
            const {container} = render(<StatementChart chart={aligned()} layout={{width: 0, columns: []}}/>);

            expect(container).toBeEmptyDOMElement();
        });
    });

    it("draws the line at nought itself, so a column crossing it can be read", () => {
        const {container} = render(<StatementChart chart={chart()}/>);

        expect(container.querySelector(".recharts-reference-line")).toBeInTheDocument();
    });

    it("hangs a negative component below nought instead of notching it out of the column", () => {
        const {container} = render(<StatementChart chart={chart()}/>);

        const zero = Number(container.querySelector(".recharts-reference-line line").getAttribute("y1"));
        const negative = container.querySelectorAll(".recharts-bar")[1]
            .querySelector(".recharts-rectangle").getAttribute("d");
        const [, , top, , height] = /M\s*([-\d.]+),([-\d.]+) h ([-\d.]+) v ([-\d.]+)/.exec(negative);

        // a bar below the axis is drawn upwards from its foot, so its head is the foot plus the
        // (negative) height, and that head must sit exactly on nought
        expect(Math.round(Number(top) + Number(height))).toBe(Math.round(zero));
    });

    it("draws no summary line when the chart has no row that totals its series", () => {
        const {container} = render(<StatementChart chart={chart({line: null})}/>);

        expect(container.querySelectorAll(".recharts-line")).toHaveLength(0);
        expect(container.querySelectorAll(".recharts-bar")).toHaveLength(2);
    });
});
