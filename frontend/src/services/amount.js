/**
 * An amount as the statements print it: whole units, with the thousands set apart by a comma, so
 * that 13775706 reads as 13,775,706.
 */
export function formatAmount(value) {
    if (typeof value !== "number" || !Number.isFinite(value)) return value;
    const sign = value < 0 ? "-" : "";
    return sign + String(Math.abs(value)).replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}
