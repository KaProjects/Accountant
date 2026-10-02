import {fireEvent, render, screen} from "@testing-library/react";
import VariantChart from "../VariantChart";

// recharts measures its container, and jsdom lays nothing out; given a size, the chart draws.
jest.mock("recharts", () => {
    const recharts = jest.requireActual("recharts");
    const {cloneElement} = jest.requireActual("react");
    return {
        ...recharts,
        ResponsiveContainer: ({children}) => cloneElement(children, {width: 800, height: 400}),
    };
});

const chart = (key, periods) => ({
    key,
    title: "Net Income",
    form: "stacked",
    series: [{key: "s0", name: "work", color: "#2a78d6"}],
    line: {key: "summary", name: "Net Income", color: "#000"},
    points: periods.map((period) => ({period, s0: 10, summary: 10})),
});
const yearly = chart("level0", ["2019", "2020"]);
const monthly = chart("level0-monthly", ["January 2019", "February 2019"]);

// the periods named along the axis; recharts also measures text in a hidden span of its own, so
// the text alone would be found twice
/* eslint-disable-next-line testing-library/no-node-access -- an axis tick has no role to query by */
const periods = () => Array.from(document.querySelectorAll(".recharts-xAxis .recharts-cartesian-axis-tick-value"))
    .map((tick) => tick.textContent);

describe("VariantChart", () => {
    it("opens on the years", () => {
        render(<VariantChart chart={yearly} monthly={monthly}/>);

        expect(periods()).toEqual(["2019", "2020"]);
        expect(screen.getByRole("button", {name: "Yearly"})).toHaveAttribute("aria-pressed", "true");
    });

    it("switches to the months and back", () => {
        render(<VariantChart chart={yearly} monthly={monthly}/>);

        fireEvent.click(screen.getByRole("button", {name: "Monthly"}));
        expect(periods()).toEqual(["January 2019", "February 2019"]);

        fireEvent.click(screen.getByRole("button", {name: "Yearly"}));
        expect(periods()).toEqual(["2019", "2020"]);
    });

    it("stays on the scale shown when the pressed button is pressed again", () => {
        render(<VariantChart chart={yearly} monthly={monthly}/>);

        fireEvent.click(screen.getByRole("button", {name: "Yearly"}));

        expect(screen.getByRole("button", {name: "Yearly"})).toHaveAttribute("aria-pressed", "true");
    });

    it("keeps the title it is given on either scale", () => {
        render(<VariantChart chart={yearly} monthly={monthly}/>);

        fireEvent.click(screen.getByRole("button", {name: "Monthly"}));

        expect(screen.getByRole("heading", {name: "Net Income"})).toBeInTheDocument();
    });

    it("is the chart of the years alone, with no switch, without months to switch to", () => {
        render(<VariantChart chart={yearly}/>);

        expect(periods()).toEqual(["2019", "2020"]);
        expect(screen.queryByRole("button")).not.toBeInTheDocument();
    });

    it("sets the switch beside an untitled chart too", () => {
        render(<VariantChart chart={yearly} monthly={monthly} titled={false}/>);

        expect(screen.queryByRole("heading")).not.toBeInTheDocument();
        expect(screen.getByRole("button", {name: "Monthly"})).toBeInTheDocument();
    });
});
