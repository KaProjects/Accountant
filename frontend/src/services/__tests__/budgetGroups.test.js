import {initiallyFolded, isFoldable, summedParts} from "../budgetGroups";

const rows = (...ids) => ids.map((id) => ({id}));
const ids = (prefix, count) => Array.from({length: count}, (_, index) => prefix + (index + 1));

describe("summedParts", () => {
    it("finds the income and the mandatory expenses with the rows their totals add up", () => {
        const parts = summedParts(rows("i1", "i2", "i", "me1", "me", "ntme", "e1", "bcf"));

        expect(parts.map(({summaryId, memberIds}) => ({summaryId, memberIds}))).toEqual([
            {summaryId: "i", memberIds: ["i1", "i2"]},
            {summaryId: "me", memberIds: ["me1"]},
        ]);
    });

    it("finds no part whose total is not there", () => {
        expect(summedParts(rows("e1", "e2", "bcf"))).toEqual([]);
    });
});

describe("folding", () => {
    const part = (summaryId, count) =>
        summedParts([...rows(...ids(summaryId, count)), {id: summaryId}])[0];

    it("lets a part of more than three rows be folded", () => {
        expect(isFoldable(part("me", 3))).toBe(false);
        expect(isFoldable(part("me", 4))).toBe(true);
    });

    it("starts the mandatory expenses folded from more than six rows on", () => {
        expect(initiallyFolded([part("me", 3)])).toEqual({me: false});
        expect(initiallyFolded([part("me", 6)])).toEqual({me: false});
        expect(initiallyFolded([part("me", 7)])).toEqual({me: true});
    });

    it("starts the income folded from more than eight rows on", () => {
        expect(initiallyFolded([part("i", 8)])).toEqual({i: false});
        expect(initiallyFolded([part("i", 9)])).toEqual({i: true});
    });
});
