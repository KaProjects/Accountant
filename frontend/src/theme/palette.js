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
