/**
 * The backend stores a transaction's date as the four digits "ddMM", because a
 * transaction is only ever read in the context of its own year. This renders it
 * as "d.m.", dropping the leading zeros.
 *
 * Anything that is not four digits is handed back untouched rather than sliced
 * into nonsense.
 */
export function formatTransactionDate(date) {
    if (typeof date !== "string" || !/^\d{4}$/.test(date)) return date;
    return parseInt(date.slice(0, 2), 10) + "." + parseInt(date.slice(2), 10) + ".";
}
