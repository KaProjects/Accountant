import {
    budgetHeaderStyle,
    budgetPlannedRowStyle,
    budgetRowStyle,
    pinnedFirstColumn,
    statementHeaderStyle,
    statementRowStyle,
} from "../tableStyles";
import {neutral} from "../palette";

// The expected values are transcribed from the inline style functions these
// replaced, so any drift in the extraction shows up here.

describe("budgetRowStyle", () => {
    it("tints a plain income row, in plain weight, over a hairline", () => {
        expect(budgetRowStyle("INCOME", false, true)).toEqual({
            fontWeight: "normal",
            background: "#f1fbf1",
            color: "#222",
            border: "0px",
            borderBottom: "1px solid #e3e3e3",
            borderLeft: "0px",
            borderRight: "2px solid",
        });
    });

    it("draws the rows that add others up in bold, on a deeper tint than the rows they add up", () => {
        ["INCOME_SUM", "EXPENSE_SUM", "BALANCE", "OF_BUDGET_BALANCE"].forEach((type) => {
            expect(budgetRowStyle(type, false, false).fontWeight).toBe("bold");
        });
        expect(budgetRowStyle("INCOME_SUM", false, false).background).toBe("#dcf3dc");
        expect(budgetRowStyle("EXPENSE_SUM", false, false).background).toBe("#f8dede");
        expect(budgetRowStyle("EXPENSE", false, false).background).toBe("#fdf3f3");
    });

    it("writes every row in the same dark ink, leaving colour to the differences", () => {
        ["INCOME", "INCOME_SUM", "EXPENSE", "EXPENSE_SUM", "BALANCE", "OF_BUDGET", "OF_BUDGET_BALANCE"]
            .forEach((type) => expect(budgetRowStyle(type, false, false).color).toBe("#222"));
    });

    it("leaves colours undefined for an unknown row type", () => {
        const style = budgetRowStyle("SOMETHING_ELSE", false, false);
        expect(style.background).toBeUndefined();
        expect(style.color).toBeUndefined();
    });

    it("applies the left and right borders independently", () => {
        expect(budgetRowStyle("INCOME", true, false).borderLeft).toBe("2px solid");
        expect(budgetRowStyle("INCOME", true, false).borderRight).toBe("0px");
    });
});

describe("budgetPlannedRowStyle", () => {
    it("is plain when there is no delta", () => {
        expect(budgetPlannedRowStyle("EXPENSE", false, null)).toEqual({
            fontWeight: "normal",
            background: "#fff",
            color: "#000",
            border: "0px",
            boxShadow: "0 0 1px 0 #000",
            paddingBottom: null,
        });
    });

    it("treats a surplus as good news on income and balance rows", () => {
        ["INCOME", "INCOME_SUM", "BALANCE"].forEach((type) => {
            expect(budgetPlannedRowStyle(type, false, 5).color).toBe("#017901");
            expect(budgetPlannedRowStyle(type, false, 5).background).toBe("#f5fff5");
        });
    });

    it("treats a surplus as bad news on expense rows", () => {
        expect(budgetPlannedRowStyle("EXPENSE", false, 5).color).toBe("#c42424");
        expect(budgetPlannedRowStyle("EXPENSE", false, 5).background).toBe("#fff2f2");
    });

    it("flips the meaning for a shortfall", () => {
        expect(budgetPlannedRowStyle("INCOME", false, -5).color).toBe("#c42424");
        expect(budgetPlannedRowStyle("EXPENSE", false, -5).color).toBe("#017901");
    });

    it("uses the neutral colour for an exact match", () => {
        expect(budgetPlannedRowStyle("EXPENSE", false, 0).color).toBe("#002e88");
        expect(budgetPlannedRowStyle("EXPENSE", false, 0).background).toBe("#f8f8ff");
    });

    it("leaves off-budget rows uncoloured even when they have a delta", () => {
        ["OF_BUDGET", "OF_BUDGET_BALANCE"].forEach((type) => {
            expect(budgetPlannedRowStyle(type, false, 100).color).toBe("#000");
            expect(budgetPlannedRowStyle(type, false, 100).background).toBe("#fff");
            expect(budgetPlannedRowStyle(type, false, 100).fontWeight).toBe("bold");
        });
    });

    it("adds bottom padding only when asked", () => {
        expect(budgetPlannedRowStyle("EXPENSE", true, null).paddingBottom).toBe("10px");
        expect(budgetPlannedRowStyle("EXPENSE", false, null).paddingBottom).toBeNull();
    });
});

describe("budgetHeaderStyle", () => {
    it("borders the label and sum columns, and leaves the table's own side after the last", () => {
        expect(budgetHeaderStyle(0).borderRight).toBe("2px solid");
        expect(budgetHeaderStyle(13).borderLeft).toBe("2px solid");
        expect(budgetHeaderStyle(13).borderRight).toBe("2px solid");
        expect(budgetHeaderStyle(14).borderRight).toBe("0px");
    });

    it("leaves the month columns unbordered, over a single rule", () => {
        expect(budgetHeaderStyle(5).borderLeft).toBe("0px");
        expect(budgetHeaderStyle(5).borderRight).toBe("0px");
        expect(budgetHeaderStyle(5).borderBottom).toBe("1px solid #9aa1a9");
    });

    it("centres every heading", () => {
        [0, 5, 14].forEach((index) => expect(budgetHeaderStyle(index).textAlign).toBe("center"));
    });
});

describe("statementRowStyle", () => {
    it("renders in the monospace face used by the statements", () => {
        expect(statementRowStyle("BALANCE_CLASS", true, true).fontFamily).toBe("Monaco");
    });

    it("emphasises group and summary rows only", () => {
        expect(statementRowStyle("BALANCE_CLASS", false, false).fontWeight).toBe("bold");
        expect(statementRowStyle("BALANCE_ACCOUNT", false, false).fontWeight).toBe("normal");
        expect(statementRowStyle("BALANCE_ACCOUNT", false, false).borderTop).toBe("0px");
    });

    it("keeps the account level rows on a white background", () => {
        expect(statementRowStyle("INCOME_ACCOUNT", false, false).background).toBe("#ffffff");
        expect(statementRowStyle("INCOME_ACCOUNT", false, false).color).toBe("#227222");
    });

    it("uses the shared palette for group rows", () => {
        expect(statementRowStyle("INCOME_GROUP", false, false).background).toBe("#67da67");
        expect(statementRowStyle("EXPENSE_GROUP", false, false).color).toBe("#a62d2d");
    });
});

describe("statementHeaderStyle", () => {
    const layout = {columnCount: 15, hasInitial: true, hasTotal: true};

    it("edges the label column on both sides, in the header's own colour", () => {
        // pinned, so its side edges are shadows that travel with it rather than the table's borders
        const style = statementHeaderStyle(0, layout);

        expect(style.boxShadow).toContain("inset 2px 0 0 0 " + neutral.headerBorder);
        expect(style.boxShadow).toContain("inset -2px 0 0 0 " + neutral.headerBorder);
        expect([style.borderLeft, style.borderRight]).toEqual(["0px", "0px"]);
    });

    it("pins the label column, on a background of its own", () => {
        expect(statementHeaderStyle(0, layout)).toMatchObject({position: "sticky", left: 0, background: neutral.white});
        expect(statementHeaderStyle(1, layout).position).toBeUndefined();
    });

    it("closes off the initial column when there is one", () => {
        expect(statementHeaderStyle(1, layout).borderRight).toBe("2px solid");
        expect(statementHeaderStyle(1, {...layout, hasInitial: false}).borderRight).toBe("0px");
    });

    it("borders the total column when there is one", () => {
        expect(statementHeaderStyle(14, layout).borderLeft).toBe("2px solid");
        expect(statementHeaderStyle(14, {...layout, hasTotal: false}).borderLeft).toBe("0px");
        expect(statementHeaderStyle(14, layout).borderRight).toBe("2px solid");
    });
});

describe("pinnedFirstColumn", () => {
    const cell = {borderLeft: "2px solid", borderRight: "2px solid", boxShadow: "0 0 8px 0", background: "#fff"};

    it("keeps the cell in view at the left edge while the rest scrolls under it", () => {
        expect(pinnedFirstColumn(cell)).toMatchObject({position: "sticky", left: 0, background: "#fff"});
    });

    it("draws its side edges as inset shadows, which travel with the cell", () => {
        const pinned = pinnedFirstColumn(cell);

        expect(pinned.boxShadow).toMatch(/^inset 2px 0 0 0 currentColor, inset -2px 0 0 0 currentColor, /);
        expect([pinned.borderLeft, pinned.borderRight]).toEqual(["0px", "0px"]);
    });

    it("draws only the edges the cell had", () => {
        expect(pinnedFirstColumn({...cell, borderLeft: "0px"}).boxShadow).toMatch(/^inset -2px 0 0 0 currentColor, inset 0/);
    });

    it("draws inside itself the glow its neighbours can no longer spill into it", () => {
        // lifted above its neighbours, it would cover their glow, and a row without borders of its
        // own would lose every line around its name
        expect(pinnedFirstColumn(cell).boxShadow).toContain("inset 0 0 8px 0, 0 0 8px 0");
        expect(pinnedFirstColumn({...cell, boxShadow: "0 0 8px 0 #A3D07A"}).boxShadow)
            .toContain("inset 0 0 8px 0 #A3D07A, 0 0 8px 0 #A3D07A");
    });

    it("leaves the top and bottom borders to the table", () => {
        expect(pinnedFirstColumn({...cell, borderTop: "2px solid"}).borderTop).toBe("2px solid");
    });
});
