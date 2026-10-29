import {cleanup, fireEvent, render, screen, within} from "@testing-library/react";
import {Table, TableBody} from "@mui/material";
import StatementRow from "../StatementRow";

const grandchild = {
    schemaId: "210", name: "Current account", type: "ASSET",
    initial: 310, monthlyValues: [41, 42, 43], yearlyValues: [91, 92], total: 440, children: [],
};

const child = {
    schemaId: "21", name: "Bank", type: "ASSET",
    initial: 320, monthlyValues: [51, 52, 53], yearlyValues: [81, 82], total: 450, children: [grandchild],
};

const childWithoutChildren = {
    schemaId: "22", name: "Cash", type: "ASSET",
    initial: 330, monthlyValues: [61, 62, 63], yearlyValues: [66, 67], total: 460, children: [],
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

    it("sets the thousands of every figure apart", () => {
        mountRow({row: {...row, initial: 1234567, monthlyValues: [-25000, 999, 0], total: 13775706}});

        expect(within(rowNamed("ASSETS")).getAllByRole("cell").map((cell) => cell.textContent))
            .toEqual([" ASSETS", "1,234,567", "-25,000", "999", "0", "13,775,706"]);
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

    it("paints the row in a shade of its own when it is given one", () => {
        mountRow({shade: {fill: "#3B6D11", ink: "#EAF3DE", edge: "#27500A"}});

        within(rowNamed("ASSETS")).getAllByRole("cell").forEach((cell) => {
            expect(cell).toHaveStyle({background: "#3B6D11", color: "#EAF3DE"});
        });
    });

    it("paints the rows it expands into in the fainter shade that goes with its own", () => {
        mountRow({showChildren: true, shade: {
            fill: "#7FBB4F", ink: "#173404", edge: "#27500A",
            accounts: {fill: "#F3F9EC", ink: "#3B6D11", edge: "#3B6D11"},
        }});

        within(rowNamed("Bank")).getAllByRole("cell").forEach((cell) => {
            expect(cell).toHaveStyle({background: "#F3F9EC", color: "#3B6D11"});
        });
    });

    it("leaves the rows it expands into alone when its shade has none for them", () => {
        mountRow({showChildren: true, shade: {fill: "#7FBB4F", ink: "#173404", edge: "#27500A"}});

        expect(within(rowNamed("Bank")).getAllByRole("cell")[0]).not.toHaveStyle({background: "#7FBB4F"});
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

    const leafMount = (overrides = {}) => mountRow({
        row: {...row, children: [childWithoutChildren]},
        showChildren: true,
        ...overrides,
    });
    const cashCell = () => within(rowNamed("Cash")).getAllByRole("cell")[1];

    it("points the transactions dialog at an account's month, and clears it on leaving", () => {
        const props = leafMount();

        fireEvent.mouseEnter(cashCell());
        expect(props.transactionsDialog.target).toHaveBeenCalledWith("Cash", "22", 1);

        fireEvent.mouseLeave(cashCell());
        expect(props.transactionsDialog.clearTarget).toHaveBeenCalled();
    });

    it("points the transactions dialog at a grandchild's month", () => {
        const props = mountRow({showChildren: true, showGrandChild: "21"});

        fireEvent.mouseEnter(within(rowNamed("Current account")).getAllByRole("cell")[2]);

        expect(props.transactionsDialog.target).toHaveBeenCalledWith("Current account", "210", 2);
    });

    it("leaves the cells of a node with children alone, as they are no set of transactions", () => {
        // A node with children aggregates them, so its figures are not transactions.
        const props = mountRow({showChildren: true, transactionsDialog: dialog({isTargeting: jest.fn().mockReturnValue(true)})});
        const cell = within(rowNamed("Bank")).getAllByRole("cell")[1];

        fireEvent.mouseEnter(cell);
        fireEvent.click(cell);

        expect(props.transactionsDialog.target).not.toHaveBeenCalled();
        expect(props.transactionsDialog.setOpen).not.toHaveBeenCalled();
        expect(within(cell).queryByTestId("corner-mark")).not.toBeInTheDocument();
    });

    it("shades the account's cell being pointed at, and marks its corner", () => {
        leafMount({transactionsDialog: dialog({isTargeting: jest.fn().mockReturnValue(true)})});

        // top left, away from the figure, which is set to the right
        expect(within(cashCell()).getByTestId("corner-mark")).toHaveAttribute("data-corner", "top-left");
        expect(cashCell().style.boxShadow).toContain("inset 0 0 0 100vmax");
        expect(cashCell().style.cursor).toBe("pointer");
    });

    it("leaves a cell plain while the pointer is elsewhere", () => {
        leafMount();

        expect(within(cashCell()).queryByTestId("corner-mark")).not.toBeInTheDocument();
        expect(cashCell().style.boxShadow).not.toContain("100vmax");
    });

    it("opens the transactions dialog on a click anywhere on the cell", () => {
        const props = leafMount();

        fireEvent.click(cashCell());

        expect(props.transactionsDialog.target).toHaveBeenCalledWith("Cash", "22", 1);
        expect(props.transactionsDialog.setOpen).toHaveBeenCalledWith(true);
    });

    it("opens it from the keyboard too", () => {
        const props = leafMount();

        fireEvent.keyDown(cashCell(), {key: "Enter"});

        expect(props.transactionsDialog.setOpen).toHaveBeenCalledWith(true);
    });

    it("does not also expand the row it is in", () => {
        const props = leafMount();

        fireEvent.click(cashCell());

        expect(props.onToggleGrandChild).not.toHaveBeenCalled();
    });

    it("expands into yearly figures in the overall view", () => {
        mountRow({isOverall: true, showChildren: true, hasInitial: false, hasTotal: false});

        // the row's name is a header cell, so the figures are all the plain cells there are
        expect(within(rowNamed("Bank")).getAllByRole("cell").map((cell) => cell.textContent))
            .toEqual(["81", "82"]);
    });

    it("expands to the third level in the overall view as well", () => {
        mountRow({
            isOverall: true, showChildren: true, showGrandChild: "21",
            hasInitial: false, hasTotal: false,
        });

        expect(within(rowNamed("Current account")).getAllByRole("cell").map((cell) => cell.textContent))
            .toEqual(["91", "92"]);
    });

    it("offers no transactions dialog in the overall view", () => {
        // A single year already fills that dialog; every year at once would be no use.
        const transactionsDialog = dialog({isTargeting: jest.fn().mockReturnValue(true)});
        mountRow({
            row: {...row, children: [childWithoutChildren]},
            isOverall: true, showChildren: true, hasInitial: false, hasTotal: false,
            transactionsDialog,
        });

        const cell = within(rowNamed("Cash")).getAllByRole("cell")[0];
        fireEvent.mouseEnter(cell);
        fireEvent.click(cell);

        expect(within(cell).queryByTestId("corner-mark")).not.toBeInTheDocument();
        expect(transactionsDialog.target).not.toHaveBeenCalled();
        expect(transactionsDialog.setOpen).not.toHaveBeenCalled();
    });
});
