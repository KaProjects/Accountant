import {formatViewTitle} from "../viewTitle";

describe("formatViewTitle", () => {
    it("breaks before each capital", () => {
        expect(formatViewTitle("SummerTrip")).toBe("Summer Trip");
    });

    it("breaks before each digit, one per group", () => {
        expect(formatViewTitle("Trip2020")).toBe("Trip 2 0 2 0");
    });

    it("leaves an already spaced name alone apart from its capitals", () => {
        expect(formatViewTitle("Summer")).toBe("Summer");
    });

    it("handles an empty name", () => {
        expect(formatViewTitle("")).toBe("");
    });
});
