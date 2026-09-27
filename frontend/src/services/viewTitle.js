/**
 * Splits a run-together view name into words, breaking before each capital or
 * digit, so "SummerTrip2020" reads as "Summer Trip 2 0 2 0".
 */
export function formatViewTitle(title) {
    return title.split(/(?=[A-Z]|[0-9])/).join(" ");
}
