import {statementSeriesColors, summaryLineColor} from "../theme/palette";

/** How many groups a side of the income statement names before the rest become "Other". */
const GROUPS_PER_SIDE = 3;

/**
 * The charts an overall statement is drawn with, derived from the very payload the table above
 * them is drawn from. Nothing is fetched for them: the rows already carry a value per year, and
 * their type already says which of them are components and which are the totals of those
 * components, so a chart cannot drift from the table it sits under.
 *
 * Each chart is {key, title, form, series, line, points}, where form is "stacked" for columns
 * stacked on one another and "lines" for a line apiece, and a point is one year carrying a
 * figure under every series key.
 */
export function statementCharts(type, data) {
    const years = chartYears(data.columns);
    if (years.length === 0) return [];
    if (type === "balance") return balanceCharts(data, years);
    if (type === "cashflow") return cashFlowCharts(data, years);
    if (type === "profit") return profitCharts(data, years);
    return [];
}

/** The year columns: everything after the statement's name, less the Total the income statement has. */
function chartYears(columns) {
    const afterTitle = columns.slice(1);
    return afterTitle[afterTitle.length - 1] === "Total" ? afterTitle.slice(0, -1) : afterTitle;
}

/**
 * One chart per side of the balance sheet: what the wealth is made of, and what it is funded by.
 * Each summary row opens a section, and the class rows that follow it are its components.
 */
function balanceCharts(data, years) {
    const sections = [];
    data.rows.forEach((row) => {
        if (row.type === "BALANCE_SUMMARY") sections.push({summary: row, components: []});
        else if (sections.length > 0) sections[sections.length - 1].components.push(row);
    });

    return sections.map((section) => chart({
        key: section.summary.schemaId,
        title: readableName(section.summary.name),
        form: "stacked",
        entries: entriesOf(section.components, years),
        summary: section.summary,
        years,
    }));
}

function cashFlowCharts(data, years) {
    const summary = data.rows.find((row) => row.type === "CASH_FLOW_SUMMARY");
    if (summary === undefined) return [];

    return [chart({
        key: "cf",
        title: readableName(summary.name),
        form: "stacked",
        entries: entriesOf(data.rows.filter((row) => row.type === "CASH_FLOW_GROUP"), years),
        summary,
        years,
    })];
}

/**
 * The income statement gets two charts, because it answers two different questions.
 *
 * The first is where the money came from and went: income stacked above nought, costs below it,
 * and the year's net profit as the line that runs between them.
 *
 * The second is the three profit levels. They are the same quantity read at three depths - net
 * income is what came in, operating profit is that less the cost of running, net profit is that
 * less everything else - so the gap between the lines is the cost of getting from one level to
 * the next. They run close together and cross, so each takes a colour of its own rather than a
 * shade of one colour. They are not stacked: that would claim they add up, and they do not,
 * because each one contains the one before it.
 */
function profitCharts(data, years) {
    const charts = [];

    const income = sideOf(data.rows, "INCOME_GROUP", 1, years, "Other income");
    const costs = sideOf(data.rows, "EXPENSE_GROUP", -1, years, "Other costs");
    const levels = data.rows.filter((row) => row.type === "PROFIT_SUMMARY");

    if (income.length + costs.length > 0) {
        charts.push(chart({
            key: "groups",
            title: "Income and costs",
            form: "stacked",
            entries: disambiguated(income, "income").concat(disambiguated(costs, "cost")),
            summary: levels[levels.length - 1],
            years,
        }));
    }

    if (levels.length > 0) {
        charts.push({
            key: "profit",
            title: "Profit",
            form: "lines",
            series: levels.map((row, index) => ({
                key: "s" + index,
                name: row.name,
                color: statementSeriesColors[index % statementSeriesColors.length],
            })),
            line: null,
            points: pointsOf(years, entriesOf(levels, years), null),
        });
    }

    return charts;
}

/**
 * One side of the income statement.
 *
 * A group can be reported on two lines - the statement splits one of them around a subtotal -
 * and for a chart of composition that is one group, so the lines are added back together. Costs
 * are negated, so that what was spent hangs below nought rather than standing on top of what was
 * earned.
 */
function sideOf(rows, type, sign, years, otherName) {
    const entries = [];
    rows.filter((row) => row.type === type).forEach((row) => {
        const values = years.map((year, index) => sign * valueAt(row, index));
        const sameGroup = entries.find((entry) => entry.schemaId === row.schemaId);
        if (sameGroup === undefined) entries.push({schemaId: row.schemaId, name: row.name, values});
        else sameGroup.values = sameGroup.values.map((value, index) => value + values[index]);
    });
    return folded(entries, otherName);
}

/**
 * A dozen groups in one chart is a dozen colours nobody can tell apart, so only the largest few
 * are named and the rest are gathered up. Nothing is gathered unless there are at least two of
 * them, because folding a single group only renames it.
 */
function folded(entries, otherName) {
    if (entries.length <= GROUPS_PER_SIDE + 1) return entries;

    const largest = entries.slice()
        .sort((one, other) => magnitude(other) - magnitude(one))
        .slice(0, GROUPS_PER_SIDE);
    const rest = entries.filter((entry) => !largest.includes(entry));
    const other = {
        schemaId: "other",
        name: otherName,
        values: rest[0].values.map((value, index) =>
            rest.reduce((sum, entry) => sum + entry.values[index], 0)),
    };

    return entries.filter((entry) => largest.includes(entry)).concat([other]);
}

function magnitude(entry) {
    return entry.values.reduce((sum, value) => sum + Math.abs(value), 0);
}

/**
 * Income and costs are named after the same schema groups, so a chart that shows both sides
 * would otherwise have two entries called the same thing.
 */
function disambiguated(entries, side) {
    return entries.map((entry) => ({...entry, name: entry.name, side}));
}

function chart({key, title, form, entries, summary, years}) {
    const series = entries.map((entry, index) => ({
        key: "s" + index,
        name: nameOf(entry, entries),
        color: statementSeriesColors[index % statementSeriesColors.length],
    }));
    const line = summary === undefined || summary === null ? null : {
        key: "summary",
        name: readableName(summary.name),
        color: summaryLineColor,
    };

    return {key, title, form, series, line, points: pointsOf(years, entries, summary)};
}

/** Only a name that appears on both sides is qualified; the rest read better plain. */
function nameOf(entry, entries) {
    const clashes = entries.some((other) => other !== entry && other.name === entry.name);
    return clashes && entry.side !== undefined ? entry.name + " (" + entry.side + ")" : entry.name;
}

function entriesOf(rows, years) {
    return rows.map((row) => ({
        schemaId: row.schemaId,
        name: row.name,
        values: years.map((year, index) => valueAt(row, index)),
    }));
}

function pointsOf(years, entries, summary) {
    return years.map((year, yearIndex) => {
        const point = {year};
        entries.forEach((entry, index) => {
            point["s" + index] = entry.values[yearIndex];
        });
        if (summary !== undefined && summary !== null) point.summary = valueAt(summary, yearIndex);
        return point;
    });
}

function valueAt(row, yearIndex) {
    const values = row === undefined || row === null ? null : row.yearlyValues;
    return values === null || values === undefined ? 0 : values[yearIndex];
}

/**
 * The balance sheet shouts its summary rows - ASSETS, LIABILITIES - which reads as shouting in a
 * chart title too. A name that is already properly cased, such as Cash Flow, is left alone.
 */
function readableName(name) {
    if (name !== name.toUpperCase()) return name;
    return name.charAt(0) + name.slice(1).toLowerCase();
}

/** Twelve years of a growing balance sheet need the axis shortened to stay readable. */
export function abbreviateAmount(value) {
    const sign = value < 0 ? "-" : "";
    const size = Math.abs(value);
    if (size >= 1000000) return sign + round(size / 1000000) + "M";
    if (size >= 1000) return sign + round(size / 1000) + "k";
    return String(value);
}

function round(value) {
    return Math.round(value * 10) / 10;
}
