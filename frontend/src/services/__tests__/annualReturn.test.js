import {annualReturn} from "../annualReturn";

const months = (count, fill = {}) => Array.from({length: count}, (unused, month) => fill[month] ?? 0);
const held = (count, value) => Array(count).fill(value);

// the same cases as the backend's AnnualReturnTest: the two calculations have to agree
describe("annualReturn", () => {
    it("is a tenth a year for a value that grew a tenth in a year", () => {
        expect(annualReturn(1000, months(12), months(12), held(12, 1050), 1100)).toBeCloseTo(10.0, 1);
    });

    it("is a tenth a year for the same over two years compounded", () => {
        expect(annualReturn(1000, months(24), months(24), held(24, 1100), 1210)).toBeCloseTo(10.0, 1);
    });

    it("is higher for money put in later, which had less time to grow", () => {
        // 2000 growing 10% a year for two years comes to 2420; half of it put in six months late
        const annual = annualReturn(1000, months(24, {6: 1000}), months(24), held(24, 1700), 2420);

        expect(annual).toBeGreaterThan(10);
        expect(annual).toBeCloseTo(11.63, 1);
    });

    it("counts withdrawals as what the asset gave back", () => {
        const balances = held(24, 550).map((value, month) => (month < 5 ? 1000 : value));

        expect(annualReturn(1000, months(24), months(24, {5: 550}), balances, 550)).toBeCloseTo(8.23, 1);
    });

    it("is negative for a loss", () => {
        expect(annualReturn(1000, months(12), months(12), held(12, 1000), 900)).toBeCloseTo(-10.0, 1);
    });

    it("keeps the rate an asset earned once it is all withdrawn", () => {
        expect(annualReturn(1000, months(12), months(12, {11: 1100}), held(12, 1050), 0)).toBeCloseTo(10.4, 0);
    });

    it("is an ordinary rate over a long stretch of deposits and withdrawals", () => {
        // the case whose rate once came out as -100% a year, the money left at the lowest rates
        // looked at being too small to compute
        const deposits = months(120).map(() => 300);
        const withdrawals = months(120).map((unused, month) => (month % 4 === 3 ? 250 : 0));

        const rising = deposits.map((unused, month) => Math.round(32000 * (month + 1) / 120));
        const annual = annualReturn(0, deposits, withdrawals, rising, 32000);

        expect(annual).toBeGreaterThan(0);
        expect(annual).toBeLessThan(10);
    });

    it("is not stated for less than a year, nor where nothing was ever put in", () => {
        expect(annualReturn(1000, months(11), months(11), held(11, 1050), 1100)).toBeNull();
        expect(annualReturn(0, months(12), months(12), held(12, 0), 0)).toBeNull();
    });

    it("is given to two decimal places", () => {
        const annual = annualReturn(1000, months(13), months(13), held(13, 1050), 1100);

        expect(Number(annual.toFixed(2))).toBe(annual);
    });

    it("is not stated for money in the asset for less than a year on average", () => {
        // three years, but bought and sold within the month each year
        const deposits = months(36, {9: 240, 21: 240, 33: 240});
        const withdrawals = months(36, {9: 600, 21: 600, 33: 600});

        expect(annualReturn(0, deposits, withdrawals, months(36, {9: 400}), 0)).toBeNull();
    });
});
