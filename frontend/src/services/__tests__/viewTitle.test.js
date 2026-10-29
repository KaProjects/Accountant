import {formatViewTitle} from "../viewTitle";

describe("formatViewTitle", () => {
    it("breaks before each capital", () => {
        expect(formatViewTitle("SummerTrip")).toBe("Summer Trip");
    });

    it("breaks before a number, keeping it whole", () => {
        expect(formatViewTitle("Trip2020")).toBe("Trip 2020");
        expect(formatViewTitle("SummerTrip2020")).toBe("Summer Trip 2020");
    });

    it("keeps capitals that follow each other together", () => {
        expect(formatViewTitle("TripUSA")).toBe("Trip USA");
        expect(formatViewTitle("CZTrip")).toBe("CZTrip");
    });

    it("begins with a capital", () => {
        expect(formatViewTitle("byvanie")).toBe("Byvanie");
    });

    it("handles an empty name", () => {
        expect(formatViewTitle("")).toBe("");
    });
});
