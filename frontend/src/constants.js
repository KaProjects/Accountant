
export const colors = {
    income_group: {background: "#67da67", foreground: "#017901"},
    expense_group: {background: "#fc9e9e", foreground: "#a62d2d"},
    profit_summary: {background: "#8bbefa", foreground: "#22468d"},
    cash_flow_group: {background: "#ab75a5", foreground: "#620155"},
    cash_flow_summary: {background: "#983f8d", foreground: "#3a0032"},
    balance_class: {background: "#f3e2ac", foreground: "#544209"},
    balance_summary: {background: "#cdf8f3", foreground: "#3fab9e"},
}

export function getChartConfigStyle(id){
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