import {render, screen} from "@testing-library/react";
import ChangesTooltip from "../ChangesTooltip";

const chart = {
    series: [
        {key: "s0", name: "Cash", color: "#7FBB4F"},
        {key: "s1", name: "Bank", color: "#A3D07A"},
        {key: "s2", name: "Credit", color: "#F09595"},
    ],
    line: {key: "summary", name: "Cash Flow", color: "#3a0032"},
};

const show = (point, label = "May") => render(
    <ChangesTooltip active label={label} chart={chart} payload={[{payload: point}]}/>);

/* eslint-disable testing-library/no-container, testing-library/no-node-access -- the order of the parts, separators included, is the thing under test */

const readOut = (container) => Array.from(container.firstChild.children).map((child) =>
    child.getAttribute("role") === "separator"
        ? (child.getAttribute("aria-label") === "baseline" ? "---- baseline ----" : "--------")
        : child.textContent);

describe("ChangesTooltip", () => {
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
        const {container} = render(<ChangesTooltip active={false} chart={chart} payload={[]}/>);

        expect(container).toBeEmptyDOMElement();
    });
});
