import {
    budgetRowColors,
    emphasisedBudgetRows,
    emphasisedStatementRows,
    expenseBudgetRows,
    favourablyPositiveBudgetRows,
    neutral,
    statementRowColors,
} from "./palette";

const border = (present) => (present ? "2px solid" : "0px");

/** Shared shape of a table heading cell. */
const headerStyle = (hasLeftBorder, hasRightBorder) => ({
    boxShadow: "0 0 3px 0",
    border: "0px",
    textAlign: "center",
    borderBottom: "2px solid",
    borderLeft: border(hasLeftBorder),
    borderRight: border(hasRightBorder),
    borderColor: neutral.headerBorder,
});

export const budgetHeaderStyle = (index) =>
    headerStyle(index === 13, index === 0 || index === 13 || index === 14);

/**
 * A cell of the column of row names, which stays in view while a statement too wide for the screen
 * scrolls under it. The cell needs a background of its own, or the cells scrolling underneath would
 * show through it.
 *
 * The table collapses its borders, and collapsed borders are the table's rather than the cell's: the
 * pinned cell's left and right borders stayed behind with the table as it scrolled, and the cells
 * passing underneath showed through the gap they left. So those two are drawn as inset shadows
 * instead, which belong to the cell and travel with it. Its top and bottom borders are left to the
 * table: the cells passing under them have the same borders, so the lines read unbroken.
 *
 * Being pinned also lifts the cell above its neighbours, and the lines between the cells of these
 * tables are glows that spill from each cell into the next: a lifted cell covered the glow its
 * neighbours spilled into it, and a row with no borders of its own - an account a group expands
 * into - lost every line around its name. So the pinned cell draws that glow on its inside itself.
 */
export function pinnedFirstColumn(style) {
    const edge = style.borderColor ?? "currentColor";
    const widthOf = (side) => parseInt(side ?? "0", 10) || 0;
    const insets = [];
    if (widthOf(style.borderLeft) > 0) insets.push("inset " + widthOf(style.borderLeft) + "px 0 0 0 " + edge);
    if (widthOf(style.borderRight) > 0) insets.push("inset -" + widthOf(style.borderRight) + "px 0 0 0 " + edge);

    const glows = style.boxShadow ? ["inset " + style.boxShadow, style.boxShadow] : [];

    return {
        ...style,
        position: "sticky",
        left: 0,
        zIndex: 1,
        borderLeft: "0px",
        borderRight: "0px",
        boxShadow: insets.concat(glows).join(", "),
    };
}

export const statementHeaderStyle = (index, {columnCount, hasInitial, hasTotal}) => {
    const style = headerStyle(
        index === 0 || (index === columnCount - 1 && hasTotal),
        index === 0 || (hasInitial && index === 1) || index === columnCount - 1,
    );
    return index === 0 ? pinnedFirstColumn({...style, background: neutral.white}) : style;
};

export function budgetRowStyle(type, hasLeftBorder, hasRightBorder) {
    const palette = budgetRowColors[type];
    const emphasised = emphasisedBudgetRows.includes(type);

    return {
        fontWeight: "bold",
        background: palette && palette.background,
        color: palette && palette.foreground,
        border: "0px",
        boxShadow: "0 0 8px 0",
        borderTop: border(emphasised),
        borderBottom: border(emphasised),
        borderLeft: border(hasLeftBorder),
        borderRight: border(hasRightBorder),
    };
}

/**
 * Style for a planned or difference cell. A non-null delta colours the cell by
 * whether the variance is favourable for that row type.
 */
export function budgetPlannedRowStyle(type, hasBottomPadding, delta) {
    let background = neutral.plainBackground;
    let color = neutral.text;
    let fontWeight = "normal";

    if (delta != null) {
        fontWeight = "bold";
        if (type === "OF_BUDGET" || type === "OF_BUDGET_BALANCE") {
            color = neutral.text;
            background = neutral.plainBackground;
        } else if (delta === 0) {
            color = neutral.neutralValue;
            background = neutral.neutralBackground;
        } else {
            // a surplus is good news on income and balance rows, bad news on expense rows
            const higherIsBetter = favourablyPositiveBudgetRows.includes(type);
            const favourable = delta > 0 ? higherIsBetter : !higherIsBetter;
            color = favourable ? neutral.positive : neutral.negative;
            background = favourable ? neutral.positiveBackground : neutral.negativeBackground;
        }
    }

    return {
        fontWeight,
        background,
        color,
        border: "0px",
        boxShadow: "0 0 1px 0 #000",
        paddingBottom: hasBottomPadding ? "10px" : null,
    };
}

export function statementRowStyle(type, hasLeftBorder, hasRightBorder) {
    const palette = statementRowColors[type];
    const emphasised = emphasisedStatementRows.includes(type);

    return {
        fontFamily: "Monaco",
        fontWeight: emphasised ? "bold" : "normal",
        background: palette && palette.background,
        color: palette && palette.foreground,
        border: "0px",
        boxShadow: "0 0 8px 0",
        borderTop: border(emphasised),
        borderBottom: border(emphasised),
        borderLeft: border(hasLeftBorder),
        borderRight: border(hasRightBorder),
    };
}

/**
 * A statement row painted in a shade of its own rather than the colour of its type, so that it
 * matches its part of the chart beneath the table. The cell borders are shadows drawn in the text
 * colour, so they are given the shade's edge explicitly; a pale ink would otherwise draw pale
 * borders around a dark row.
 */
export function shadedStatementRowStyle(style, shade) {
    if (shade === null || shade === undefined) return style;
    return {
        ...style,
        background: shade.fill,
        color: shade.ink,
        boxShadow: "0 0 8px 0 " + shade.edge,
    };
}

/**
 * A cell that does something when clicked, while the pointer is over it: a step darker, and with a
 * pointer to say it can be clicked.
 *
 * The shade is an inset shadow laid over the cell's own colour, so it suits every row colour, and
 * it leaves the cell where it is in the painting order. A filter or an overlay would lift the cell
 * above its neighbours, and a lifted cell covers the glow they spill into it, which is what draws
 * the lines of these tables.
 */
export const highlightedCell = (style) => ({
    ...style,
    cursor: "pointer",
    boxShadow: [style.boxShadow, "inset 0 0 0 100vmax " + neutral.highlight].filter(Boolean).join(", "),
});

export const isExpenseRow = (type) => expenseBudgetRows.includes(type);

/** An asset list entry, highlighted while its chart is open. */
export const assetTitleStyle = (isOpen) => ({
    boxShadow: "0 0 8px 0",
    background: isOpen ? "#87befc" : "#b6d8ff",
    color: "#3361bb",
    fontWeight: "bold",
});

/** A view list entry, highlighted while its transactions are open. */
export const viewTitleStyle = (isOpen) => ({
    boxShadow: "0 0 8px 0",
    background: isOpen ? "#87befc" : "#b6d8ff",
    color: "#3361bb",
});

/** Column widths for the view transactions table. */
export const viewHeaderStyle = (index) => ({
    width: index <= 1 ? "60px" : index <= 3 ? "300px" : null,
    fontWeight: "bold",
});
