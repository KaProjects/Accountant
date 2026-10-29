import {fireEvent, render, screen, within} from "@testing-library/react";
import ViewPanel from "../ViewPanel";

const view = {
    name: "vacation_2020_italy",
    expenses: 1234,
    transactions: [
        {date: "0101", amount: 100, debit: "500.0", credit: "210.0", description: "hotel"},
        {date: "0202", amount: 200, debit: "500.1", credit: "210.0", description: "flights"},
    ],
    chartData: [{name: "hotel", value: 100}],
};

const columns = ["Date", "Amount", "Debit", "Credit", "Description"];

const mountPanel = (props = {}) => {
    const merged = {view, columns, isOpen: true, onToggle: jest.fn(), ...props};
    render(<ViewPanel {...merged}/>);
    return merged;
};

describe("ViewPanel", () => {
    beforeEach(() => jest.clearAllMocks());

    it("titles itself with the view's formatted name", () => {
        mountPanel();

        // The raw name is an identifier; the panel shows the readable form of it.
        expect(screen.queryByText("vacation_2020_italy")).not.toBeInTheDocument();
        expect(screen.getByRole("button")).toHaveTextContent(/italy/i);
    });

    it("lists every transaction under the given columns", () => {
        mountPanel();

        columns.forEach((column) => expect(screen.getByText(column)).toBeInTheDocument());
        expect(within(screen.getByRole("row", {name: new RegExp("hotel")})).getAllByRole("cell")
            .map((cell) => cell.textContent))
            .toEqual(["0101", "100", "500.0", "210.0", "hotel"]);
        expect(screen.getByText("flights")).toBeInTheDocument();
    });

    it("shows the total spent", () => {
        mountPanel();

        expect(screen.getByText(/Total Expenses:/)).toHaveTextContent("1234");
    });

    it("hides its contents while collapsed", () => {
        mountPanel({isOpen: false});

        expect(screen.queryByText("hotel")).not.toBeInTheDocument();
    });

    it("toggles when the title is clicked", () => {
        const props = mountPanel();

        fireEvent.click(screen.getByRole("button"));

        expect(props.onToggle).toHaveBeenCalled();
    });
});
