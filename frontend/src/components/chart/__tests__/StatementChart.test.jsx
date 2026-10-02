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
        {year: "2019", s0: 10, s1: -20, summary: -10},
        {year: "2020", s0: 15, s1: 25, summary: 40},
    ],
    ...overrides,
});

/* eslint-disable testing-library/no-container, testing-library/no-node-access -- a chart has no accessible roles to query by */

describe("StatementChart", () => {
    it("names the chart", () => {
        render(<StatementChart chart={chart()}/>);

        expect(screen.getByRole("heading", {name: "Assets"})).toBeInTheDocument();
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
                {year: "2019", s0: -10, s1: -20},
                {year: "2020", s0: -15, s1: -25},
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
