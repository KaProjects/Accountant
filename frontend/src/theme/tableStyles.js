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

export const statementHeaderStyle = (index, {columnCount, hasInitial, hasTotal}) =>
    headerStyle(
        index === 0 || (index === columnCount - 1 && hasTotal),
        index === 0 || (hasInitial && index === 1) || index === columnCount - 1,
    );

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
