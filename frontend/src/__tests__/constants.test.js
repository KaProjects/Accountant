import {colors, getChartConfigStyle} from "../constants";

// Characterization tests: these pin down the behaviour getChartConfigStyle has
// today, including the fact that later rules deliberately override earlier ones
// (for example "20" matches both the balance class and the cash flow group, and
// the cash flow group wins).

describe("getChartConfigStyle", () => {
    const styleOf = (group) => ({
        backgroundColor: colors[group].background,
        color: colors[group].foreground,
    })

    it("styles expense accounts by their leading 5", () => {
        expect(getChartConfigStyle("510")).toEqual(styleOf("expense_group"))
        expect(getChartConfigStyle("546.0-0")).toEqual(styleOf("expense_group"))
    })

    it("styles income accounts by their leading 6", () => {
        expect(getChartConfigStyle("600")).toEqual(styleOf("income_group"))
    })

    it("styles the profit summaries", () => {
        ["ni", "op", "np"].forEach((id) => {
            expect(getChartConfigStyle(id)).toEqual(styleOf("profit_summary"))
        })
    })

    it("styles the balance summaries", () => {
        ["l", "a", "p"].forEach((id) => {
            expect(getChartConfigStyle(id)).toEqual(styleOf("balance_summary"))
        })
    })

    it("styles balance classes 0 through 4 by their first character", () => {
        ["0", "1", "310", "4"].forEach((id) => {
            expect(getChartConfigStyle(id)).toEqual(styleOf("balance_class"))
        })
    })

    it("lets the cash flow group override the balance class", () => {
        ["20", "21", "22", "23"].forEach((id) => {
            expect(getChartConfigStyle(id)).toEqual(styleOf("cash_flow_group"))
        })
        // only the exact ids are cash flow groups - 201 stays a balance class
        expect(getChartConfigStyle("201")).toEqual(styleOf("balance_class"))
    })

    it("styles the cash flow summary", () => {
        expect(getChartConfigStyle("cf")).toEqual(styleOf("cash_flow_summary"))
    })

    it("falls back to a plain style for unknown ids", () => {
        expect(getChartConfigStyle("zzz")).toEqual({backgroundColor: "white", color: "black"})
    })
})
