import {render, screen} from "@testing-library/react";
import VacationChart, {getColor, renderCustomizedLabel} from "../VacationChart";

const data = [
    {name: "hotel", value: 300},
    {name: "flights", value: 200},
    {name: "food", value: 100},
];

describe("VacationChart", () => {
    it("renders a chart container for its data", () => {
        // jsdom has no layout, so a responsive chart measures zero and draws no slices. What the
        // slices would say is covered by the unit tests below, which need no layout.
        const {container} = render(<VacationChart data={data} isBottom={true}/>);

        // eslint-disable-next-line testing-library/no-container, testing-library/no-node-access -- a chart container has no accessible role to query by
        expect(container.querySelector(".recharts-responsive-container")).toBeInTheDocument();
    });

    it("renders an empty chart without failing", () => {
        const {container} = render(<VacationChart data={[]} isBottom={false}/>);

        expect(container).not.toBeEmptyDOMElement();
    });
});

describe("getColor", () => {
    it("gives every slice a different hue, spread across the circle", () => {
        const colours = [0, 1, 2].map((index) => getColor(3, index));

        expect(new Set(colours).size).toBe(3);
        expect(colours[0]).toContain("hsla(0,");
        expect(colours[1]).toContain("hsla(120,");
        expect(colours[2]).toContain("hsla(240,");
    });

    it("keeps a single slice inside the circle", () => {
        expect(getColor(1, 0)).toContain("hsla(0,");
    });
});

describe("renderCustomizedLabel", () => {
    const label = (percent) => renderCustomizedLabel({
        cx: 100, cy: 100, midAngle: 0, innerRadius: 0, outerRadius: 100, percent, index: 0,
    });

    it("labels a slice with its whole-number share", () => {
        const {rerender} = render(<svg data-testid="slice">{label(0.255)}</svg>);
        expect(screen.getByTestId("slice")).toHaveTextContent("26%");

        rerender(<svg data-testid="slice">{label(1)}</svg>);
        expect(screen.getByTestId("slice")).toHaveTextContent("100%");
    });

    it("anchors the text on the side of the centre it falls", () => {
        const anchorAt = (midAngle) => renderCustomizedLabel({
            cx: 100, cy: 100, midAngle, innerRadius: 0, outerRadius: 100, percent: 0.5,
        }).props.textAnchor;

        expect(anchorAt(0)).toBe("start");
        expect(anchorAt(180)).toBe("end");
    });
});
