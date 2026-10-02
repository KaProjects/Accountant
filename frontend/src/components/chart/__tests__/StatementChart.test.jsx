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

    it("stacks either side outwards from the baseline in the order of the table", () => {
        // both series gain in January: the table's first is the one standing on the baseline
        const changes = chart({
            form: "changes",
            baseline: 100,
            points: [{period: "Jan", summary: 150, spacer: 100, s0: 30, s0_above: 30, s1: 20, s1_above: 20}],
        });
        const {container} = render(<StatementChart chart={changes}/>);

        const baseline = Number(container.querySelector(".recharts-reference-line line[stroke-dasharray]")
            .getAttribute("y1"));
        const footOf = (color) => {
            const column = Array.from(container.querySelectorAll(".recharts-rectangle"))
                .find((rectangle) => rectangle.getAttribute("fill") === color);
            const [, y, height] = /M\s*[-\d.]+,([-\d.]+) h [-\d.]+ v ([-\d.]+)/.exec(column.getAttribute("d"));
            return Math.max(Number(y), Number(y) + Number(height));
        };

        expect(Math.round(footOf("#2a78d6"))).toBe(Math.round(baseline));
        expect(footOf("#eb6834")).toBeLessThan(footOf("#2a78d6"));
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

        it("draws nothing until the table has been laid out", () => {
            const {container} = render(<StatementChart chart={aligned()} layout={{width: 0, columns: []}}/>);

            expect(container).toBeEmptyDOMElement();
        });
    });

    it("draws a line per series instead of columns when the chart is of lines", () => {
        const {container} = render(<StatementChart chart={chart({form: "lines", line: null})}/>);

        expect(container.querySelectorAll(".recharts-line")).toHaveLength(2);
        expect(container.querySelectorAll(".recharts-bar")).toHaveLength(0);
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
