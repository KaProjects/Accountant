import {colors, getChartConfigStyle, netIncomeColor, profitGroupColors, startingLevelColor} from "../palette";

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
        expect(getChartConfigStyle("549.0-0")).toEqual(styleOf("expense_group"))
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

// perceptual lightness (OKLab L) of an sRGB colour, which is what "as bright as" means to the eye
const lightness = (hex) => {
    const linear = [1, 3, 5].map((at) => {
        const channel = parseInt(hex.slice(at, at + 2), 16) / 255;
        return channel <= 0.04045 ? channel / 12.92 : ((channel + 0.055) / 1.055) ** 2.4;
    });
    const [r, g, b] = linear;
    const l = Math.cbrt(0.4122214708 * r + 0.5363325363 * g + 0.0514459929 * b);
    const m = Math.cbrt(0.2119034982 * r + 0.6806995451 * g + 0.1073969566 * b);
    const s = Math.cbrt(0.0883024619 * r + 0.2817188376 * g + 0.6299787005 * b);
    return 0.2104542553 * l + 0.7936177850 * m - 0.0040720468 * s;
};

describe("profitGroupColors", () => {
    it("is equally light throughout, so that no group stands out or fades against the others", () => {
        profitGroupColors.forEach((color) => {
            expect(lightness(color)).toBeCloseTo(lightness(profitGroupColors[0]), 2);
        });
    });

    it("is lighter than the blue of the operating profit beside it", () => {
        profitGroupColors.forEach((color) => {
            expect(lightness(color)).toBeGreaterThan(lightness(startingLevelColor));
        });
    });

    it("gives every group a colour of its own", () => {
        expect(new Set(profitGroupColors).size).toBe(profitGroupColors.length);
    });
});

describe("netIncomeColor", () => {
    it("is a lighter blue than the starting level, so it does not weigh on the costs laid over it", () => {
        expect(lightness(netIncomeColor)).toBeGreaterThan(lightness(startingLevelColor) + 0.05);
    });
});
