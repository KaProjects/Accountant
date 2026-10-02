/**
 * A scale for a chart's category axis that puts each category under the table column of the same
 * name, wherever that column happens to be, instead of spacing the categories evenly.
 *
 * The table sizes each column to its widest figure, so its columns are of unequal width, and an
 * evenly spaced axis drifts away from them. This takes the columns as the table actually laid them
 * out - {width, columns: [{name, left, width}]} - and answers recharts in the shape of a band
 * scale: where a category's band starts, and how wide every band is. A band is as wide as the
 * narrowest of the columns charted, so that none spills over into its neighbour, and it is centred
 * on its column.
 *
 * recharts sets the domain and the range itself, so both are accepted and handed back, but the
 * positions come from the columns alone.
 */
export function columnScale(categories, layout) {
    const columnOf = (category) => layout.columns.find((column) => column.name === category);
    const charted = categories.map(columnOf).filter((column) => column !== undefined);
    const bandwidth = charted.length === 0 ? 0 : Math.min(...charted.map((column) => column.width));

    let domain = [];
    let range = [0, layout.width];

    const scale = (category) => {
        const column = columnOf(category);
        return column === undefined ? undefined : column.left + (column.width - bandwidth) / 2;
    };
    scale.domain = (next) => {
        if (next === undefined) return domain;
        domain = next;
        return scale;
    };
    scale.range = (next) => {
        if (next === undefined) return range;
        range = next;
        return scale;
    };
    scale.bandwidth = () => bandwidth;
    scale.copy = () => columnScale(categories, layout).domain(domain).range(range);
    return scale;
}
