import {render, screen} from "@testing-library/react";
import StackTooltip from "../StackTooltip";

const chart = {
    form: "changes",
    series: [
        {key: "s0", name: "Cash", color: "#7FBB4F"},
        {key: "s1", name: "Bank", color: "#A3D07A"},
        {key: "s2", name: "Credit", color: "#F09595"},
    ],
    line: {key: "summary", name: "Cash Flow", color: "#3a0032"},
};

const show = (point, label = "May") => render(
    <StackTooltip active label={label} chart={chart} payload={[{payload: point}]}/>);

/* eslint-disable testing-library/no-container, testing-library/no-node-access -- the order of the parts, separators included, is the thing under test */

const readOut = (container) => Array.from(container.firstChild.children).map((child) =>
    child.getAttribute("role") === "separator"
        ? ({baseline: "---- baseline ----", nought: "==== nought ===="}[child.getAttribute("aria-label")] ?? "--------")
        : child.textContent);

describe("StackTooltip", () => {
    it("sets out what stands above the baseline, then what hangs below it, then the total", () => {
        const {container} = show({s0: 2000, s1: -5000, s2: 300, summary: 1234567});

        expect(readOut(container)).toEqual([
            "May",
            "Cash : 2,000", "Credit : 300",
            "---- baseline ----",
            "Bank : -5,000",
            "--------",
            "Cash Flow : 1,234,567",
        ]);
    });

    it("keeps the dashed baseline when one side has nothing on it", () => {
        const {container} = show({s0: -1, s1: -2, s2: 0, summary: 10});

        expect(readOut(container)).toEqual([
            "May", "---- baseline ----", "Cash : -1", "Bank : -2", "--------", "Cash Flow : 10",
        ]);
    });

    it("leaves out a group that did not move", () => {
        show({s0: 0, s1: 5, s2: 0, summary: 10});

        expect(screen.queryByText(/Cash :/)).not.toBeInTheDocument();
        expect(screen.queryByText(/Credit :/)).not.toBeInTheDocument();
    });

    it("shows only the balance in a month where nothing moved", () => {
        const {container} = show({s0: 0, s1: 0, s2: 0, summary: 50}, "March");

        expect(readOut(container)).toEqual(["March", "Cash Flow : 50"]);
    });

    it("shows nothing for a month the year has not reached yet", () => {
        const {container} = show({summary: null}, "November");

        expect(container).toBeEmptyDOMElement();
    });

    it("shows nothing at the opening, where the line only sets off from", () => {
        const {container} = show({summary: 50, opening: true}, "Initial");

        expect(container).toBeEmptyDOMElement();
    });

    it("shows nothing while the pointer is off the chart", () => {
        const {container} = render(<StackTooltip active={false} chart={chart} payload={[]}/>);

        expect(container).toBeEmptyDOMElement();
    });

    describe("on a chart stacked on nought", () => {
        const onNought = {
            form: "stacked",
            series: [
                {key: "s0", name: "Operating Profit", color: "#5D9DE6"},
                {key: "s1", name: "depreciation", color: "#F9D4D4"},
                {key: "s2", name: "finance", color: "#F5BB6E"},
            ],
            line: {key: "summary", name: "Net Profit", color: "#000"},
        };
        const showOn = (point) => render(
            <StackTooltip active label="2022" chart={onNought} payload={[{payload: point}]}/>);

        it("splits its parts at nought, drawing nought as the solid line the chart draws", () => {
            const {container} = showOn({s0: 100, s1: -10, s2: -40, summary: 50});

            expect(readOut(container)).toEqual([
                "2022",
                "Operating Profit : 100",
                "==== nought ====",
                "depreciation : -10", "finance : -40",
                "--------",
                "Net Profit : 50",
            ]);
        });
    });

    describe("on a total with its parts laid over it", () => {
        const split = {
            form: "split",
            series: [
                {key: "s0", name: "Net Income", color: "#5D9DE6", layer: "behind"},
                {key: "s1", name: "consumption", color: "#F4B6B6", layer: "taken"},
                {key: "s2", name: "services", color: "#EF9999", layer: "taken"},
            ],
            line: {key: "summary", name: "Operating Profit", color: "#000"},
        };

        it("reads in the order of the key: the total, the parts laid over it, then the line", () => {
            const {container} = render(
                <StackTooltip active label="2022" chart={split} payload={[{payload: {s0: 100, lift: -20, s1: 40, s2: 80, summary: -20}}]}/>);

            expect(readOut(container)).toEqual([
                "2022",
                "Net Income : 100",
                "consumption : 40", "services : 80",
                "Operating Profit : -20",
            ]);
        });

        it("lists the layers in the order of the table, leaving out any part laid over that came to nothing", () => {
            const incomes = {
                form: "split",
                series: [
                    {key: "s0", name: "work", color: "#7CBC42", layer: "behind"},
                    {key: "s1", name: "office (cost)", color: "#EF9999", layer: "taken"},
                    {key: "s2", name: "office (income)", color: "#C4E3A0", layer: "given"},
                ],
                line: {key: "summary", name: "Net Income", color: "#000"},
            };
            const read = (point) => readOut(render(<StackTooltip active label="2020" chart={incomes} payload={[{payload: point}]}/>).container);

            expect(read({s0: 120, s1: 20, s2: 5, lift: 100, summary: 105})).toEqual([
                "2020", "work : 120", "office (cost) : 20", "office (income) : 5", "Net Income : 105",
            ]);
            expect(read({s0: 120, s1: 20, s2: 0, lift: 100, summary: 100})).toEqual([
                "2020", "work : 120", "office (cost) : 20", "Net Income : 100",
            ]);
        });
    });
});
