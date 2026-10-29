import {render, screen} from "@testing-library/react";
import BudgetChartDialog, {BarLabel, CustomTooltip} from "../BudgetChartDialog";

const series = [
    {name: "1", planned: 15, base: 10, deficit: 0, surplus: 5},
    {name: "2", planned: 15, base: 15, deficit: 5, surplus: 0},
];

describe("BudgetChartDialog", () => {
    it("names the row it is charting", () => {
        render(<BudgetChartDialog open={true} onClose={jest.fn()} name="Groceries" data={series} isExpense={true}/>);

        expect(screen.getByText("Budget Difference for Groceries")).toBeInTheDocument();
    });

    it("shows nothing while closed", () => {
        render(<BudgetChartDialog open={false} onClose={jest.fn()} name="Groceries" data={series} isExpense={true}/>);

        expect(screen.queryByText(/Budget Difference/)).not.toBeInTheDocument();
    });
});

const payload = (planned, base, deficit, surplus) => [
    {value: planned, fill: "#8884d8"},
    {value: base, fill: "#8884d8"},
    {value: deficit, fill: "#eb5e5e"},
    {value: surplus, fill: "#2cc143"},
];

describe("CustomTooltip", () => {
    it("shows nothing unless it is active and has something to report", () => {
        expect(render(<CustomTooltip active={false} payload={payload(15, 10, 5, 0)} label="1"/>)
            .container).toBeEmptyDOMElement();
        expect(render(<CustomTooltip active={true} payload={[]} label="1"/>)
            .container).toBeEmptyDOMElement();
    });

    it("reports the month and the planned figure", () => {
        render(<CustomTooltip active={true} payload={payload(15, 10, 5, 0)} label="3"/>);

        expect(screen.getByText(/month:/)).toHaveTextContent("3");
        expect(screen.getByText(/planned:/)).toHaveTextContent("15");
    });

    it("reports only the difference that actually occurred", () => {
        const {unmount} = render(<CustomTooltip active={true} payload={payload(15, 10, 5, 0)} label="1"/>);
        expect(screen.getAllByText(/difference:/)).toHaveLength(1);
        unmount();

        render(<CustomTooltip active={true} payload={payload(15, 10, 0, 0)} label="1"/>);
        expect(screen.queryByText(/difference:/)).not.toBeInTheDocument();
    });

    it("signs the difference by whether the plan was met", () => {
        // The first two entries are equal when the whole plan was spent as planned.
        const {unmount} = render(<CustomTooltip active={true} payload={payload(15, 15, 5, 0)} label="1"/>);
        expect(screen.getByText(/difference:/)).toHaveTextContent("+5");
        unmount();

        render(<CustomTooltip active={true} payload={payload(15, 10, 5, 0)} label="1"/>);
        expect(screen.getByText(/difference:/)).toHaveTextContent("-5");
    });
});

describe("BarLabel", () => {
    it("labels a bar that has a value, with its sign", () => {
        render(<svg><BarLabel value={7} sign="+" fill="#000" x={0} y={20} width={10}/></svg>);

        expect(screen.getByText("+7")).toBeInTheDocument();
    });

    it("labels nothing when there is no difference to show", () => {
        // Scoped to the chart itself: recharts leaves a hidden measurement span in the document.
        render(<svg data-testid="bar"><BarLabel value={0} sign="+" fill="#000" x={0} y={20} width={10}/></svg>);

        expect(screen.getByTestId("bar")).toHaveTextContent("");
    });
});
