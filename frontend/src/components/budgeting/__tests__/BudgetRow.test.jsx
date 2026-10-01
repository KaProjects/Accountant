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
    ...overrides,
});

const chart = (overrides = {}) => ({
    preview: jest.fn(),
    isPreviewing: jest.fn().mockReturnValue(false),
    setOpen: jest.fn(),
    ...overrides,
});

const mountRow = ({showSubRows, showDeltas, ...props} = {}) => {
    const expansion = {
        showSubRows, showDeltas,
        onToggleSubRows: jest.fn(),
        onToggleDeltas: jest.fn(),
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

        // One name cell, twelve months, then the four summary cells.
        expect(cells).toHaveLength(17);
        expect(cells[1]).toHaveTextContent("11");
        expect(cells[3]).toHaveTextContent("33");
        expect(cells[4]).toHaveTextContent("400");
        expect(cells[12]).toHaveTextContent("1200");
    });

    it("summarises the row with its sum, average, planned average and difference", () => {
        mountRow();
        const cells = within(rowNamed(/Salary/)).getAllByRole("cell");

        expect(cells[13]).toHaveTextContent("66");
        expect(cells[14]).toHaveTextContent("24");
        expect(cells[15]).toHaveTextContent("199");
        expect(cells[16]).toHaveTextContent("-176");
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

    it("toggles the breakdown without also toggling the sub rows", () => {
        // The two controls sit in the same cell, so the inner click must not reach the row.
        const props = mountRow({row: withSubRow});

        fireEvent.click(within(rowNamed(/Salary/)).getAllByLabelText("expand row")[0]);

        expect(props.onToggleDeltas).toHaveBeenCalledWith(0);
        expect(props.onToggleSubRows).not.toHaveBeenCalled();
    });

    it("renders its sub rows only while they are expanded", () => {
        mountRow({row: withSubRow});
        expect(screen.queryByText("Bonus")).not.toBeInTheDocument();
        cleanup();

        mountRow({row: withSubRow, showSubRows: true});
        expect(screen.getByText("Bonus")).toBeInTheDocument();
        expect(within(rowNamed("Bonus")).getByText("7")).toBeInTheDocument();
        expect(within(rowNamed("Bonus")).getByText("1001")).toBeInTheDocument();
    });

    it("shows the planned and difference breakdown only while it is expanded", () => {
        mountRow();
        expect(screen.queryByText("Planned")).not.toBeInTheDocument();
        cleanup();

        mountRow({showDeltas: true});
        expect(within(rowNamed("Planned")).getByText("100")).toBeInTheDocument();
        expect(within(rowNamed("Planned")).getByText("600")).toBeInTheDocument();
    });

    it("computes each difference as actual minus planned, and dashes the unfilled months", () => {
        mountRow({showDeltas: true});
        const cells = within(rowNamed("Difference")).getAllByRole("cell");

        expect(cells[0]).toHaveTextContent("-89");
        expect(cells[1]).toHaveTextContent("-178");
        expect(cells[2]).toHaveTextContent("-267");
        expect(cells[3]).toHaveTextContent("-");
        expect(cells[12]).toHaveTextContent("-534");
        expect(cells[13]).toHaveTextContent("-175");
    });

    it("points the transactions dialog at the cell under the pointer, and clears it on leaving", () => {
        const props = mountRow();
        const cell = within(rowNamed(/Salary/)).getAllByRole("cell")[2];

        fireEvent.click(cell);
        expect(props.transactionsDialog.target).toHaveBeenCalledWith("Salary", "600", 2);

        fireEvent.mouseLeave(cell);
        expect(props.transactionsDialog.clearTarget).toHaveBeenCalled();
    });

    it("offers no transactions dialog on a row that only aggregates its sub rows", () => {
        const props = mountRow({row: withSubRow});

        fireEvent.click(within(rowNamed(/Salary/)).getAllByRole("cell")[2]);

        expect(props.transactionsDialog.target).not.toHaveBeenCalled();
    });

    it("opens the transactions dialog from the cell being pointed at", () => {
        const transactionsDialog = dialog({
            isTargeting: jest.fn((id, month) => id === "600" && month === 1),
        });
        mountRow({transactionsDialog});

        fireEvent.click(within(rowNamed(/Salary/)).getAllByRole("button")[1]);

        expect(transactionsDialog.setOpen).toHaveBeenCalledWith(true);
    });

    it("previews the budget chart from the difference row and opens it", () => {
        const budgetChart = chart({isPreviewing: jest.fn().mockReturnValue(true)});
        mountRow({showDeltas: true, budgetChart});
        const cell = within(rowNamed("Difference")).getByRole("rowheader");

        fireEvent.mouseEnter(cell);
        expect(budgetChart.preview).toHaveBeenCalledWith(row);

        fireEvent.click(within(cell).getByRole("button"));
        expect(budgetChart.setOpen).toHaveBeenCalledWith(true);

        fireEvent.mouseLeave(cell);
        expect(budgetChart.preview).toHaveBeenCalledWith(null);
    });
});
