import {cleanup, fireEvent, render, screen, within} from "@testing-library/react";
import {Table, TableBody} from "@mui/material";
import StatementRow from "../StatementRow";

const grandchild = {
    schemaId: "210", name: "Current account", type: "ASSET",
    initial: 310, monthlyValues: [41, 42, 43], total: 440, children: [],
};

const child = {
    schemaId: "21", name: "Bank", type: "ASSET",
    initial: 320, monthlyValues: [51, 52, 53], total: 450, children: [grandchild],
};

const childWithoutChildren = {
    schemaId: "22", name: "Cash", type: "ASSET",
    initial: 330, monthlyValues: [61, 62, 63], total: 460, children: [],
};

const row = {
    name: "ASSETS", type: "CLASS",
    initial: 300, monthlyValues: [11, 12, 13], yearlyValues: [71, 72], total: 400,
    children: [child],
};

const dialog = (overrides = {}) => ({
    target: jest.fn(),
    clearTarget: jest.fn(),
    isTargeting: jest.fn().mockReturnValue(false),
    setOpen: jest.fn(),
    ...overrides,
});

const mountRow = ({type = "balance", isOverall, hasInitial = true, hasTotal = true,
                   showChildren, showGrandChild, ...props} = {}) => {
    const expansion = {
        showChildren, showGrandChild,
        onToggleChildren: jest.fn(), onToggleGrandChild: jest.fn(),
    };
    const merged = {
        row, id: 0,
        columns: {type, isOverall, hasInitial, hasTotal},
        expansion,
        transactionsDialog: dialog(),
        ...props,
    };
    render(<Table><TableBody><StatementRow {...merged}/></TableBody></Table>);
    return {...merged, ...expansion};
};

const rowNamed = (name) =>
    screen.getByRole("row", {name: name instanceof RegExp ? name : new RegExp(name)});

describe("StatementRow", () => {
    beforeEach(() => jest.clearAllMocks());

    it("shows the monthly figures, with the initial and total columns when asked for", () => {
        mountRow();
        const cells = within(rowNamed("ASSETS")).getAllByRole("cell");

        expect(cells.map((cell) => cell.textContent))
            .toEqual([" ASSETS", "300", "11", "12", "13", "400"]);
    });

    it("omits the initial and total columns when the statement has none", () => {
        mountRow({hasInitial: false, hasTotal: false});

        expect(within(rowNamed("ASSETS")).getAllByRole("cell").map((cell) => cell.textContent))
            .toEqual([" ASSETS", "11", "12", "13"]);
    });

    it("shows yearly figures instead of monthly ones in the overall view", () => {
        mountRow({isOverall: true});

        expect(within(rowNamed("ASSETS")).getAllByRole("cell").map((cell) => cell.textContent))
            .toEqual([" ASSETS", "300", "71", "72", "400"]);
    });

    it("expands children when the row is clicked", () => {
        const props = mountRow();

        fireEvent.click(rowNamed("ASSETS"));

        expect(props.onToggleChildren).toHaveBeenCalledWith(0);
    });

    it("shows children only while they are expanded", () => {
        mountRow();
        expect(screen.queryByText("Bank")).not.toBeInTheDocument();
        cleanup();

        mountRow({showChildren: true});
        expect(within(rowNamed("Bank")).getByText("51")).toBeInTheDocument();
        expect(within(rowNamed("Bank")).getByText("450")).toBeInTheDocument();
    });

    it("expands a grandchild when its child row is clicked", () => {
        const props = mountRow({showChildren: true});

        fireEvent.click(rowNamed("Bank"));

        expect(props.onToggleGrandChild).toHaveBeenCalledWith("21");
    });

    it("shows the third level only for the balance sheet, and only for the chosen child", () => {
        // The other statements stop at children, so the third level must not appear there even
        // when a grandchild is selected.
        mountRow({showChildren: true, showGrandChild: "21", type: "profit"});
        expect(screen.queryByText("Current account")).not.toBeInTheDocument();
        cleanup();

        mountRow({showChildren: true, showGrandChild: "99", type: "balance"});
        expect(screen.queryByText("Current account")).not.toBeInTheDocument();
        cleanup();

        mountRow({showChildren: true, showGrandChild: "21", type: "balance"});
        expect(within(rowNamed("Current account")).getByText("41")).toBeInTheDocument();
        expect(within(rowNamed("Current account")).getByText("440")).toBeInTheDocument();
    });

    it("points the transactions dialog at a child's month, and clears it on leaving", () => {
        const props = mountRow({showChildren: true});
        const cell = within(rowNamed("Bank")).getAllByRole("cell")[1];

        fireEvent.click(cell);
        expect(props.transactionsDialog.target).toHaveBeenCalledWith("Bank", "21", 1);

        fireEvent.mouseLeave(cell);
        expect(props.transactionsDialog.clearTarget).toHaveBeenCalled();
    });

    it("points the transactions dialog at a grandchild's month", () => {
        const props = mountRow({showChildren: true, showGrandChild: "21"});

        fireEvent.click(within(rowNamed("Current account")).getAllByRole("cell")[2]);

        expect(props.transactionsDialog.target).toHaveBeenCalledWith("Current account", "210", 2);
    });

    it("offers the dialog only on a node that has no children of its own", () => {
        // A node with children aggregates them, so its figures are not transactions.
        const transactionsDialog = dialog({isTargeting: jest.fn().mockReturnValue(true)});
        mountRow({showChildren: true, transactionsDialog});

        expect(within(rowNamed("Bank")).queryByRole("button")).not.toBeInTheDocument();
        cleanup();

        mountRow({
            row: {...row, children: [childWithoutChildren]},
            showChildren: true,
            transactionsDialog,
        });
        expect(within(rowNamed("Cash")).getAllByRole("button")).not.toHaveLength(0);
    });

    it("opens the transactions dialog from the cell being pointed at", () => {
        const transactionsDialog = dialog({isTargeting: jest.fn().mockReturnValue(true)});
        mountRow({
            row: {...row, children: [childWithoutChildren]},
            showChildren: true,
            transactionsDialog,
        });

        fireEvent.click(within(rowNamed("Cash")).getAllByRole("button")[0]);

        expect(transactionsDialog.setOpen).toHaveBeenCalledWith(true);
    });
});
