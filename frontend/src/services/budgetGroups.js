/**
 * The parts of the budget that end in a row adding them up - the income, and the mandatory expenses
 * - each told by the id of that row and the prefix of the ids of the rows it adds up.
 */
const SUMMED_PARTS = [
    // from how many rows on each is folded up to begin with
    {summaryId: "i", member: /^i\d/, foldedFrom: 9},
    {summaryId: "me", member: /^me\d/, foldedFrom: 7},
];

/** A part can be folded up into its total from this many rows on: fewer are no trouble to read. */
const FOLDABLE_FROM = 4;

/** The parts of the given rows that have a total, each with the ids of the rows its total adds up. */
export const summedParts = (rows) => SUMMED_PARTS
    .filter(({summaryId}) => rows.some((row) => row.id === summaryId))
    .map(({summaryId, member, foldedFrom}) => ({
        summaryId,
        memberIds: rows.filter((row) => member.test(row.id)).map((row) => row.id),
        foldedFrom,
    }));

export const isFoldable = (part) => part.memberIds.length >= FOLDABLE_FROM;

/** Which parts start folded up, by the id of their total. */
export const initiallyFolded = (parts) =>
    Object.fromEntries(parts.map((part) => [part.summaryId, isFoldable(part) && part.memberIds.length >= part.foldedFrom]));
