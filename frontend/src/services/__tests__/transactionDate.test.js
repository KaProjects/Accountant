import {formatTransactionDate} from "../transactionDate";

describe("formatTransactionDate", () => {
    it("renders the stored ddMM as d.m.", () => {
        expect(formatTransactionDate("0509")).toBe("5.9.");
    });

    it("drops the leading zeros", () => {
        expect(formatTransactionDate("0101")).toBe("1.1.");
    });

    it("keeps both figures where there is nothing to drop", () => {
        expect(formatTransactionDate("3112")).toBe("31.12.");
        expect(formatTransactionDate("1509")).toBe("15.9.");
    });

    it("leaves a value that is not four digits alone", () => {
        expect(formatTransactionDate("101")).toBe("101");
        expect(formatTransactionDate("01-01")).toBe("01-01");
    });

    it("leaves a missing date alone", () => {
        expect(formatTransactionDate(undefined)).toBeUndefined();
        expect(formatTransactionDate(null)).toBeNull();
    });
});
