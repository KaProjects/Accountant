/**
 * The single source of colour truth for the application.
 *
 * `groups` holds the shared tokens used by charts and statements alike; the
 * per-row maps below name the colours each table assigns to a row type.
 */
export const colors = {
    income_group: {background: "#67da67", foreground: "#017901"},
    expense_group: {background: "#fc9e9e", foreground: "#a62d2d"},
    profit_summary: {background: "#8bbefa", foreground: "#22468d"},
    cash_flow_group: {background: "#ab75a5", foreground: "#620155"},
    cash_flow_summary: {background: "#983f8d", foreground: "#3a0032"},
    balance_class: {background: "#f3e2ac", foreground: "#544209"},
    balance_summary: {background: "#cdf8f3", foreground: "#3fab9e"},
}

export const neutral = {
    white: "#ffffff",
    // the budgeting table's plain cells use the short form; kept verbatim
    plainBackground: "#fff",
    headerBorder: "#676767",
    text: "#000",
    positive: "#017901",
    positiveBackground: "#f5fff5",
    negative: "#c42424",
    negativeBackground: "#fff2f2",
    neutralValue: "#002e88",
    neutralBackground: "#f8f8ff",
}

/** Row colours for the budgeting table, keyed by the row type the backend sends. */
export const budgetRowColors = {
    INCOME: {background: "#b4ffb4", foreground: "#017901"},
    INCOME_SUM: {background: "#67da67", foreground: "#017901"},
    EXPENSE: {background: "#fdc6c6", foreground: "#c42424"},
    EXPENSE_SUM: {background: "#fc9e9e", foreground: "#ab0000"},
    BALANCE: {background: "#7eb9ff", foreground: "#002e88"},
    OF_BUDGET_BALANCE: {background: "#7eb9ff", foreground: "#002e88"},
    OF_BUDGET: {background: "#b6d8ff", foreground: "#3361bb"},
}

/** Budget row types drawn with a heavier top and bottom border. */
export const emphasisedBudgetRows = ["EXPENSE_SUM", "INCOME_SUM", "BALANCE", "OF_BUDGET_BALANCE"];

/** Budget row types counted as expenses, where overspending is the bad direction. */
export const expenseBudgetRows = ["EXPENSE_SUM", "EXPENSE"];

/** Budget row types where a positive delta is the good direction. */
export const favourablyPositiveBudgetRows = ["INCOME", "INCOME_SUM", "BALANCE"];

/** Row colours for the accounting statements, keyed by the row type. */
export const statementRowColors = {
    INCOME_ACCOUNT: {background: neutral.white, foreground: "#227222"},
    INCOME_GROUP: {background: colors.income_group.background, foreground: colors.income_group.foreground},
    EXPENSE_ACCOUNT: {background: neutral.white, foreground: "#a13c3c"},
    EXPENSE_GROUP: {background: colors.expense_group.background, foreground: colors.expense_group.foreground},
    PROFIT_SUMMARY: {background: colors.profit_summary.background, foreground: colors.profit_summary.foreground},
    CASH_FLOW_ACCOUNT: {background: neutral.white, foreground: "#721c67"},
    CASH_FLOW_GROUP: {background: colors.cash_flow_group.background, foreground: colors.cash_flow_group.foreground},
    CASH_FLOW_SUMMARY: {background: colors.cash_flow_summary.background, foreground: colors.cash_flow_summary.foreground},
    BALANCE_SUMMARY: {background: colors.balance_summary.background, foreground: colors.balance_summary.foreground},
    BALANCE_CLASS: {background: colors.balance_class.background, foreground: colors.balance_class.foreground},
    BALANCE_GROUP: {background: "#fdfac4", foreground: "#65612a"},
    BALANCE_ACCOUNT: {background: "#fdfcf3", foreground: "#797746"},
}

/**
 * The total of a stacked statement chart is drawn over its components in plain ink rather than
 * in a colour of its own: it took the colour of its row in the table until that turned out to be
 * a near match for one of the components it was drawn over.
 */
export const summaryLineColor = neutral.text;

/**
 * Shades for a chart whose components fall on either side of nought, from palest to deepest.
 * Every component on one side takes a shade of that side's hue, so the parts of a column are told
 * apart from one another but the side each belongs to is plain at a glance: what is held in green,
 * what is owed in red.
 *
 * The rows of the table the chart sits under are painted in the same shades, so each shade also
 * carries the ink its text is written in, and the edge its cell borders are drawn in, which is the
 * deepest shade of its side for all of them. The greens stop short of the deep end of the hue so
 * that even the deepest of them still takes dark text like the rest of the table.
 */
export const aboveZeroShades = [
    {fill: "#E4F2D2", ink: "#173404", edge: "#27500A"},
    {fill: "#C6E3A3", ink: "#173404", edge: "#27500A"},
    {fill: "#A3D07A", ink: "#173404", edge: "#27500A"},
    {fill: "#7FBB4F", ink: "#173404", edge: "#27500A"},
    {fill: "#5E9E2E", ink: "#0B2101", edge: "#27500A"},
];
/**
 * The accounts a painted group expands into: the same side's hue, but fainter than the plain
 * account rows of the table, so the group still stands out from what it is made of. The edge is
 * kept soft as well: the cell borders are shadows that bleed several pixels into the cell, and a
 * strong edge darkened the whole of a pale row.
 */
export const aboveZeroAccountShade = {fill: "#F8FCF3", ink: "#3B6D11", edge: "#A3D07A"};
export const belowZeroAccountShade = {fill: "#FEF6F6", ink: "#A32D2D", edge: "#F09595"};

/**
 * The cash flow's own total, a step lighter than the statement's usual summary row. The chart
 * draws the same total as its line in the row's ink, so the two are matched up at a glance.
 */
export const cashFlowSummaryShade = {fill: "#B877AF", ink: "#3a0032", edge: "#3a0032"};

export const belowZeroShades = [
    {fill: "#F7C1C1", ink: "#791F1F", edge: "#791F1F"},
    {fill: "#F09595", ink: "#501313", edge: "#791F1F"},
    {fill: "#E24B4A", ink: "#501313", edge: "#791F1F"},
    {fill: "#A32D2D", ink: "#FCEBEB", edge: "#791F1F"},
    {fill: "#791F1F", ink: "#FCEBEB", edge: "#791F1F"},
];

/**
 * Series colours for the overall statement charts, in a fixed order so that a component keeps
 * its colour as the statement grows. One hue per component, and the hues are ordered so that the
 * first few are as far apart as the palette allows, for the charts that only use a few. The
 * summary drawn over them takes the foreground colour of its own row in the table instead.
 */
export const statementSeriesColors = [
    "#2a78d6", "#eb6834", "#1baf7a", "#eda100",
    "#e87ba4", "#6250d6", "#e34948", "#888780",
];

/** Statement row types drawn bold with heavier borders. */
export const emphasisedStatementRows = [
    "INCOME_GROUP", "EXPENSE_GROUP", "PROFIT_SUMMARY",
    "CASH_FLOW_SUMMARY", "CASH_FLOW_GROUP",
    "BALANCE_SUMMARY", "BALANCE_CLASS",
];

export function getChartConfigStyle(id) {
    let backgroundColor = "white"
    let color = "black"

    if (id.startsWith("5")) {backgroundColor = colors.expense_group.background; color= colors.expense_group.foreground}
    if (id.startsWith("6")) {backgroundColor = colors.income_group.background; color = colors.income_group.foreground}
    if (["ni", "op", "np"].includes(id)) {backgroundColor = colors.profit_summary.background; color = colors.profit_summary.foreground}
    if (["l", "a"].includes(id)) {backgroundColor = colors.balance_summary.background; color = colors.balance_summary.foreground}
    if (["l", "a", "p"].includes(id)) {backgroundColor = colors.balance_summary.background; color = colors.balance_summary.foreground}
    if (["0", "1", "2", "3", "4"].includes(id.substring(0,1))) {backgroundColor = colors.balance_class.background; color = colors.balance_class.foreground}
    if (["20", "21", "22", "23"].includes(id)) {backgroundColor = colors.cash_flow_group.background; color = colors.cash_flow_group.foreground}
    if (id === "cf") {backgroundColor = colors.cash_flow_summary.background; color = colors.cash_flow_summary.foreground}

    return {backgroundColor: backgroundColor, color: color}
}
