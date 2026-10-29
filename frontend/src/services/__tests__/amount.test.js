import {formatAmount} from "../amount";

describe("formatAmount", () => {
    it("sets the thousands apart", () => {
        expect(formatAmount(13775706)).toBe("13,775,706");
        expect(formatAmount(1000)).toBe("1,000");
    });

    it("leaves a figure under a thousand as it is", () => {
        expect(formatAmount(999)).toBe("999");
        expect(formatAmount(0)).toBe("0");
    });

    it("keeps the sign in front of the figure", () => {
        expect(formatAmount(-1497227)).toBe("-1,497,227");
        expect(formatAmount(-500)).toBe("-500");
    });

    it("passes a missing figure through untouched", () => {
        expect(formatAmount(null)).toBeNull();
        expect(formatAmount(undefined)).toBeUndefined();
    });
});
