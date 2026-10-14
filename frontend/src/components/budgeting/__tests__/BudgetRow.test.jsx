import {cleanup, fireEvent, render, screen, within} from "@testing-library/react";
import {Table, TableBody} from "@mui/material";
import BudgetRow from "../BudgetRow";

/**
 * Values are deliberately all distinct, so an assertion cannot pass by matching a cell other than
 * the one it means.
 */
const row = {
    id: "600",
    name: "Salary",
    type: "INCOME",
    actual: [11, 22, 33, 0, 0, 0, 0, 0, 0, 0, 0, 0],
    planned: [100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 1100, 1200],
    lastFilledMonth: 3,
    actualSum: 66,
    actualAvg: 24,
    plannedSumToFilledMonth: 600,
    plannedAvgToFilledMonth: 199,
    deltaAvg: -176,
    subRows: [],
};

const withSubRow = {
    ...row,
    subRows: [{
        id: "600.1",
        name: "Bonus",
        actual: [7, 8, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0],
        planned: [70, 80, 90, 1001, 1002, 1003, 1004, 1005, 1006, 1007, 1008, 1009],
        actualSum: 24,
        actualAvg: 2,
        plannedAvgToFilledMonth: 77,
        deltaAvg: -75,
    }],
};

const dialog = (overrides = {}) => ({
    target: jest.fn(),
    clearTarget: jest.fn(),
    isTargeting: jest.fn().mockReturnValue(false),
    setOpen: jest.fn(),
    opensFor: jest.fn().mockReturnValue(true),
    ...overrides,
});

const chart = (overrides = {}) => ({
    preview: jest.fn(),
    isPreviewing: jest.fn().mockReturnValue(false),
    setOpen: jest.fn(),
    ...overrides,
});

const mountRow = ({showSubRows, ...props} = {}) => {
    const expansion = {
        showSubRows,
        onToggleSubRows: jest.fn(),
    };
    const merged = {
        row, id: 0, expansion,
        transactionsDialog: dialog(),
        budgetChart: chart(),
        ...props,
    };

    render(<Table><TableBody><BudgetRow {...merged}/></TableBody></Table>);
    return {...merged, ...expansion};
};

const rowNamed = (name) =>
    screen.getByRole("row", {name: name instanceof RegExp ? name : new RegExp(name)});

describe("BudgetRow", () => {
    beforeEach(() => jest.clearAllMocks());

    it("shows actual figures up to the last filled month and planned ones after it", () => {
        mountRow();
        const cells = within(rowNamed(/Salary/)).getAllByRole("cell");

        // One name cell, twelve months, then the sum and the average.
        expect(cells).toHaveLength(15);
        expect(cells[1]).toHaveTextContent("11");
        expect(cells[3]).toHaveTextContent("33");
        expect(cells[4]).toHaveTextContent("400");
        expect(cells[12]).toHaveTextContent("1,200");
    });

    it("summarises the row with its sum and average", () => {
        mountRow();
        const cells = within(rowNamed(/Salary/)).getAllByRole("cell");

        expect(cells[13]).toHaveTextContent("66");
        expect(cells[14]).toHaveTextContent("24");
    });

    it("does not expand a row that has no sub rows", () => {
        const props = mountRow();

        fireEvent.click(rowNamed(/Salary/));

        expect(props.onToggleSubRows).not.toHaveBeenCalled();
    });

    it("expands sub rows when a row that has them is clicked", () => {
        const props = mountRow({row: withSubRow});

        fireEvent.click(rowNamed(/Salary/));

        expect(props.onToggleSubRows).toHaveBeenCalledWith(0);
    });

    it("renders its sub rows only while they are expanded", () => {
        mountRow({row: withSubRow});
        expect(screen.queryByText("Bonus")).not.toBeInTheDocument();
        cleanup();

        mountRow({row: withSubRow, showSubRows: true});
        expect(screen.getByText("Bonus")).toBeInTheDocument();
        expect(within(rowNamed("Bonus")).getByText("7")).toBeInTheDocument();
        expect(within(rowNamed("Bonus")).getByText("1,001")).toBeInTheDocument();
    });

    it("shows under each actual figure how far it is from the planning, and none under a planned one", () => {
        mountRow();
        const cells = within(rowNamed(/Salary/)).getAllByRole("cell");

        expect(cells[1]).toHaveTextContent("11(-89)");
        expect(cells[2]).toHaveTextContent("22(-178)");
        expect(cells[3]).toHaveTextContent("33(-267)");
        expect(cells[4]).toHaveTextContent(/^400$/);
        // the sum against what was planned up to the last filled month, the average likewise
        expect(cells[13]).toHaveTextContent("66(-534)");
        expect(cells[14]).toHaveTextContent("24(-175)");
    });

    it("signs a difference above the planning", () => {
        mountRow({row: {...row, actual: [111, 22, 33, 0, 0, 0, 0, 0, 0, 0, 0, 0]}});

        expect(within(rowNamed(/Salary/)).getAllByRole("cell")[1]).toHaveTextContent("111(+11)");
    });

    it("shows no difference under a sub row's figures, which are not planned", () => {
        mountRow({row: withSubRow, showSubRows: true});

        expect(within(rowNamed("Bonus")).queryByText(/\(/)).not.toBeInTheDocument();
    });

    it("previews the budget chart from anywhere on the row, and opens it from the row's name", () => {
        const budgetChart = chart({isPreviewing: jest.fn().mockReturnValue(true)});
        const props = mountRow({row: withSubRow, budgetChart});
        const tableRow = rowNamed(/Salary/);
        const cell = within(tableRow).getAllByRole("cell")[0];

        fireEvent.mouseEnter(within(tableRow).getAllByRole("cell")[7]);
        expect(budgetChart.preview).toHaveBeenCalledWith(withSubRow);

        fireEvent.click(within(cell).getByRole("button", {name: "budget chart"}));
        expect(budgetChart.setOpen).toHaveBeenCalledWith(true);
        // opening the chart does not also open the sub rows
        expect(props.onToggleSubRows).not.toHaveBeenCalled();

        fireEvent.mouseLeave(tableRow);
        expect(budgetChart.preview).toHaveBeenCalledWith(null);
    });

    it("points the transactions dialog at the cell under the pointer, and clears it on leaving", () => {
        const props = mountRow();
        const cell = within(rowNamed(/Salary/)).getAllByRole("cell")[2];

        fireEvent.mouseEnter(cell);
        expect(props.transactionsDialog.target).toHaveBeenCalledWith("Salary", "600", 2);

        fireEvent.mouseLeave(cell);
        expect(props.transactionsDialog.clearTarget).toHaveBeenCalled();
    });

    it("opens the transactions of all its sub rows from a row made of them", () => {
        const props = mountRow({row: withSubRow});

        fireEvent.click(within(rowNamed(/Salary/)).getAllByRole("cell")[2]);

        expect(props.transactionsDialog.target).toHaveBeenCalledWith("Salary", "600", 2);
        expect(props.transactionsDialog.setOpen).toHaveBeenCalledWith(true);
        // the click on the cell is the cell's, not the row's, which would open the sub rows
        expect(props.onToggleSubRows).not.toHaveBeenCalled();
    });

    it("opens the transactions dialog from a click anywhere on a month cell, not also opening the row", () => {
        const props = mountRow({row: withSubRow, showSubRows: true});
        // the sub row's name is its row's header, so its cells are its months: the third is March
        const cell = within(rowNamed("Bonus")).getAllByRole("cell")[2];

        fireEvent.click(cell);

        expect(props.transactionsDialog.target).toHaveBeenCalledWith("Bonus", "600.1", 3);
        expect(props.transactionsDialog.setOpen).toHaveBeenCalledWith(true);
        expect(props.onToggleSubRows).not.toHaveBeenCalled();
    });

    it("opens the transactions dialog from the keyboard, with Enter on a month cell", () => {
        const props = mountRow();
        const cell = within(rowNamed(/Salary/)).getAllByRole("cell")[1];

        fireEvent.keyDown(cell, {key: "Enter"});

        expect(props.transactionsDialog.setOpen).toHaveBeenCalledWith(true);
    });

    it("shades and marks the month cell being pointed at, as the statements do", () => {
        const transactionsDialog = dialog({
            isTargeting: jest.fn((id, month) => id === "600" && month === 1),
        });
        mountRow({transactionsDialog});
        const cells = within(rowNamed(/Salary/)).getAllByRole("cell");

        expect(cells[1]).toHaveStyle({cursor: "pointer"});
        expect(within(cells[1]).getByTestId("corner-mark")).toBeInTheDocument();
        expect(cells[2]).not.toHaveStyle({cursor: "pointer"});
        expect(within(cells[2]).queryByTestId("corner-mark")).not.toBeInTheDocument();
    });

    it("opens nothing from a row that adds up other rows", () => {
        const transactionsDialog = dialog({opensFor: jest.fn().mockReturnValue(false)});
        mountRow({transactionsDialog});

        fireEvent.click(within(rowNamed(/Salary/)).getAllByRole("cell")[1]);

        expect(transactionsDialog.setOpen).not.toHaveBeenCalled();
    });

});
