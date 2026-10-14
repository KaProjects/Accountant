/**
 * A run-together view name as words: broken before a capital that follows no other - capitals in a
 * row stay together - and before a number, which stays whole, and begun with a capital - so
 * "SummerTrip2020" reads as "Summer Trip 2020", "TripUSA" as "Trip USA", "byvanie" as "Byvanie".
 */
export function formatViewTitle(title) {
    const words = title.split(/(?<=[^A-Z])(?=[A-Z])|(?<=\D)(?=\d)/).join(" ");
    return words.charAt(0).toUpperCase() + words.slice(1);
}
