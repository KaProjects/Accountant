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

/**
 * The widths of the budgeting table's columns other than the months: its names, and its sums and
 * averages. The months share what is left equally, so that each is as wide as the next.
 */
const budgetColumnWidths = {0: "170px", 13: "110px", 14: "110px"};

/**
 * A heading of the budgeting table, in the look of its rows: the same ink on a plain grey, over a
 * single rule, centred, and bordered where the rows are.
 */
export const budgetHeaderStyle = (index) => ({
    width: budgetColumnWidths[index],
    // a cool grey, darker than any row's tint, so it reads as the heading and never as a row
    background: "#e4e7eb",
    color: "#222",
    fontWeight: "bold",
    fontSize: "13px",
    whiteSpace: "nowrap",
    textAlign: "center",
    border: "0px",
    borderBottom: "1px solid #9aa1a9",
    borderLeft: border(index === 13),
    // none after the last column: the table's own side is there
    borderRight: border(index === 0 || index === 13),
});

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

    return {
        // the rows that add others up stand out by their weight, the rest stay plain
        fontWeight: emphasisedBudgetRows.includes(type) ? "bold" : "normal",
        background: palette && palette.background,
        color: palette && palette.foreground,
        border: "0px",
        borderBottom: "1px solid #e3e3e3",
        borderLeft: border(hasLeftBorder),
        borderRight: border(hasRightBorder),
    };
}

/**
 * Style for a planned or difference cell. A non-null delta colours the cell by
 * whether the variance is favourable for that row type.
 */
/** How a difference from the planning reads on a row of the given type: good, bad or neither. */
function deltaColors(type, delta) {
    if (type === "OF_BUDGET" || type === "OF_BUDGET_BALANCE") {
        return {color: neutral.text, background: neutral.plainBackground};
    }
    if (delta === 0) {
        return {color: neutral.neutralValue, background: neutral.neutralBackground};
    }
    // a surplus is good news on income and balance rows, bad news on expense rows
    const higherIsBetter = favourablyPositiveBudgetRows.includes(type);
    const favourable = delta > 0 ? higherIsBetter : !higherIsBetter;
    return favourable
        ? {color: neutral.positive, background: neutral.positiveBackground}
        : {color: neutral.negative, background: neutral.negativeBackground};
}

/**
 * The difference from the planning under an actual figure: small, and coloured by whether it is good
 * or bad news, straight on the row's own background. A difference that is neither - none at all, or
 * one off the budget - takes the row's own ink, faded: an expense row's ink is the very red of an
 * overspend, and an income row's the green of a surplus, so at full strength it would read as one.
 */
export const budgetDeltaStyle = (type, delta) => {
    const neither = delta === 0 || type === "OF_BUDGET" || type === "OF_BUDGET_BALANCE";
    return {
        color: neither ? budgetRowColors[type]?.foreground : deltaColors(type, delta).color,
        opacity: neither ? 0.5 : 1,
        fontSize: "11px",
        fontWeight: "normal",
    };
};

export function budgetPlannedRowStyle(type, hasBottomPadding, delta) {
    let background = neutral.plainBackground;
    let color = neutral.text;
    let fontWeight = "normal";

    if (delta != null) {
        fontWeight = "bold";
        ({color, background} = deltaColors(type, delta));
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
        // the rows that add others up glow in their colour; the rest are set apart by a hairline
        boxShadow: emphasised ? "0 0 8px 0" : "0 0 1px 0 rgba(0, 0, 0, 0.3)",
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
