import {
    aboveZeroAccountShade, aboveZeroShades, belowZeroAccountShade, belowZeroShades,
    cashFlowSummaryShade, statementSeriesColors, summaryLineColor,
} from "../theme/palette";

/** How many groups a side of the income statement names before the rest become "Other". */
const GROUPS_PER_SIDE = 3;

/**
 * The charts a statement is drawn with, derived from the very payload the table above them is
 * drawn from. Nothing is fetched for them: the rows already carry their figures, and their type
 * already says which of them are components and which are the totals of those components, so a
 * chart cannot drift from the table it sits under.
 *
 * Every overall statement is charted. Of the yearly ones only the cash flow is, so far.
 *
 * Each chart is {key, title, form, series, line, points}, where form is "stacked" for columns
 * stacked on one another, "lines" for a line apiece, and "changes" for each period's changes
 * stacked on a fixed baseline other than nought (which such a chart also carries, as
 * `baseline`). A point is one period - a year, or a month - carrying a figure under every series
 * key. A chart that sets `alignToTable` is drawn with each point under the table column it is
 * named after, rather than spaced evenly.
 */
export function statementCharts(type, data, overall = true) {
    const axis = axisOf(data, overall);
    if (axis.labels.length === 0) return [];
    if (!overall) return type === "cashflow" ? yearlyCashFlowCharts(data) : [];
    if (type === "balance") return balanceCharts(data, axis);
    if (type === "cashflow") return cashFlowCharts(data, axis);
    if (type === "profit") return profitCharts(data, axis);
    return [];
}

/**
 * What a chart runs along, and how a row is read along it: {labels, valuesOf}.
 *
 * An overall statement already reports a figure per year. A yearly one does not report balances
 * at all: it reports what the year opened with, and then how much each month changed it. A chart
 * of those changes would show the traffic, not where the money stood, so a yearly row is read as
 * its running balance instead - what it opened with, then where it stood at the end of each month.
 */
function axisOf(data, overall) {
    return overall ? yearsAxis(data.columns) : runningBalanceAxis(data.columns);
}

/** The year columns: everything after the statement's name, less the Total the income statement has. */
function yearsAxis(columns) {
    const afterTitle = columns.slice(1);
    const labels = afterTitle[afterTitle.length - 1] === "Total" ? afterTitle.slice(0, -1) : afterTitle;
    return {
        labels,
        valuesOf: (row) => labels.map((label, index) => {
            const values = row === undefined || row === null ? null : row.yearlyValues;
            return values === null || values === undefined ? 0 : values[index];
        }),
    };
}

/**
 * The twelve months, each with the change the table reports for it, named as the table's header
 * names them.
 */
function monthlyChangesAxis(columns) {
    const months = columns.slice(1).filter((column) => !["Initial", "Total"].includes(column));
    return {
        labels: months,
        valuesOf: (row) => months.map((month, index) =>
            row === undefined || row === null ? 0 : (row.monthlyValues ?? [])[index] ?? 0),
    };
}

/**
 * The opening balance, then the twelve month ends: where each row stood over the year, which is
 * what decides the side of nought it is coloured for.
 */
function runningBalanceAxis(columns) {
    const months = columns.slice(1).filter((column) => !["Initial", "Total"].includes(column));
    return {
        labels: months.length === 0 ? [] : ["Initial"].concat(months),
        valuesOf: (row) => {
            if (row === undefined || row === null) return [];
            let balance = row.initial ?? 0;
            const balances = [balance];
            (row.monthlyValues ?? []).forEach((change) => {
                balance += change;
                balances.push(balance);
            });
            return balances;
        },
    };
}

/**
 * One chart per side of the balance sheet: what the wealth is made of, and what it is funded by.
 * Each summary row opens a section, and the class rows that follow it are its components.
 */
function balanceCharts(data, axis) {
    const sections = [];
    data.rows.forEach((row) => {
        if (row.type === "BALANCE_SUMMARY") sections.push({summary: row, components: []});
        else if (sections.length > 0) sections[sections.length - 1].components.push(row);
    });

    return sections.map((section) => chart({
        key: section.summary.schemaId,
        title: readableName(section.summary.name),
        form: "stacked",
        entries: entriesOf(section.components, axis),
        summary: section.summary,
        axis,
    }));
}

function cashFlowCharts(data, axis) {
    const summary = data.rows.find((row) => row.type === "CASH_FLOW_SUMMARY");
    if (summary === undefined) return [];

    return [chart({
        key: "cf",
        title: readableName(summary.name),
        form: "stacked",
        entries: entriesOf(data.rows.filter((row) => row.type === "CASH_FLOW_GROUP"), axis),
        summary,
        axis,
        colouring: (entries) => shadesBySide(entries).map((shade) => shade.fill),
        lineColor: cashFlowSummaryShade.ink,
        lineWidth: 3,
    })];
}

/**
 * The year's cash flow as its months' changes, group by group, each month stacked on the cash
 * flow the year opened with, under the line of where the total actually stood at each month end.
 *
 * The table reports changes, so the chart does too, and it measures them from the opening balance
 * - drawn as a dashed line, and marked on the value axis - so the month ends can be read off
 * against what the year started from. Every month is drawn under the column of the table it
 * belongs to, and the line sets off from the baseline under the table's Initial column, which has
 * nothing else drawn in it: that point is the `opening`, and has no changes to report.
 *
 * The year may still be running, so the line stops at the last month anything moved in.
 *
 * A group is coloured by the side its balance stands on, as its row in the table is, and not by
 * which way the year moved it: cash that dwindled over the year is still cash.
 */
function yearlyCashFlowCharts(data) {
    const summary = data.rows.find((row) => row.type === "CASH_FLOW_SUMMARY");
    const groups = data.rows.filter((row) => row.type === "CASH_FLOW_GROUP");
    const year = cashFlowMonths(data, groups.map((row) => row.schemaId));
    if (year === null) return [];

    const colours = shadesBySide(entriesOf(groups, runningBalanceAxis(data.columns))).map((shade) => shade.fill);

    return [{
        key: "cf",
        title: readableName(summary.name) + " from Initial",
        form: "changes",
        series: groups.map((row, index) => ({key: "s" + index, name: nameOf(row, groups), color: colours[index]})),
        line: cashFlowLine(summary),
        baseline: year.baseline,
        points: [{period: "Initial", summary: year.baseline, opening: true}]
            .concat(untilLastRecorded(year.points, year.recorded)),
        alignToTable: true,
    }];
}

/**
 * The overall cash flow month by month: every year's months one after another, from the first
 * year on, drawn as the chart of a single year draws its months.
 *
 * All the months are measured from one baseline, the cash flow the first year opened with, so
 * every column stands on the same line however far the years have carried the total from it. The
 * line runs on through the turn of each year, from one month end to the next, and stops at the last
 * month anything moved in, as it does for a single year; only the months still to come are left
 * empty, and a year that recorded nothing early on is charted as the nought it was. The groups keep
 * the colours and the order of the overall table, which is the one above this chart. The axis names
 * the years, at their Januaries; a month itself is named in full in its tooltip.
 *
 * Takes the overall statement and every year's own statement, as [{year, data}]; answers null
 * when there is nothing to chart.
 */
export function monthlyCashFlowChart(overall, years) {
    const summary = overall.rows.find((row) => row.type === "CASH_FLOW_SUMMARY");
    const groups = overall.rows.filter((row) => row.type === "CASH_FLOW_GROUP");
    const statements = years.filter(({data}) => data.rows.some((row) => row.type === "CASH_FLOW_SUMMARY"));
    if (summary === undefined || statements.length === 0) return null;

    const opening = statements[0].data.rows.find((row) => row.type === "CASH_FLOW_SUMMARY").initial ?? 0;
    const schemaIds = groups.map((row) => row.schemaId);
    const charted = statements
        .map(({year, data}) => ({year, months: cashFlowMonths(data, schemaIds, opening)}))
        .filter(({months}) => months !== null);
    if (!charted.some(({months}) => months.recorded.some((recorded) => recorded))) return null;

    const points = [];
    const recorded = [];
    charted.forEach(({year, months}) => months.points.forEach((point, index) => {
        points.push({...point, period: point.period + " " + year});
        recorded.push(months.recorded[index]);
    }));
    const colours = shadesBySide(entriesOf(groups, yearsAxis(overall.columns))).map((shade) => shade.fill);

    return {
        key: "cf-monthly",
        title: readableName(summary.name) + " by month",
        form: "changes",
        series: groups.map((row, index) => ({key: "s" + index, name: nameOf(row, groups), color: colours[index]})),
        line: cashFlowLine(summary),
        baseline: opening,
        points: [{period: "Initial", summary: opening, opening: true}].concat(untilLastRecorded(points, recorded)),
        ticks: charted.map(({year, months}) => ({value: months.points[0].period + " " + year, label: String(year)})),
    };
}

/** The line a cash flow chart draws its total with, in the ink of the total's row. */
function cashFlowLine(summary) {
    return {key: "summary", name: readableName(summary.name), color: cashFlowSummaryShade.ink, width: 3};
}

/**
 * A year of a cash flow statement as months to chart: {baseline, points, recorded}, or null when
 * the statement has no total to chart.
 *
 * The baseline is the cash flow the year opened with, unless another is given, and each point is a
 * month: where the total stood at its end, and each group's change in it, stacked on the baseline. recharts stacks only
 * upwards, from nought, so a month starts with a spacer that is never drawn, reaching up to the
 * bottom of the month's losses; the losses come next, reaching back up to the baseline, and the
 * gains stand on top of that. A group can lose in one month and gain in the next, so each group has
 * a half for either case, and only one of the two is filled in any month. The signed change itself
 * is kept under the group's own key, for the tooltip to report.
 *
 * The groups are taken in the order of `schemaIds`, so the months of several years line up with
 * one another; a group a year does not have counts as never having moved. `recorded` says, month
 * by month, whether any group moved at all.
 */
function cashFlowMonths(data, schemaIds, given) {
    const summary = data.rows.find((row) => row.type === "CASH_FLOW_SUMMARY");
    const changes = monthlyChangesAxis(data.columns);
    if (summary === undefined || changes.labels.length === 0) return null;

    const rows = schemaIds.map((schemaId) =>
        data.rows.find((row) => row.type === "CASH_FLOW_GROUP" && row.schemaId === schemaId));
    const baseline = given ?? summary.initial ?? 0;
    const monthEnds = runningBalanceAxis(data.columns).valuesOf(summary).slice(1);
    const changesIn = (month) => rows.map((row) => changes.valuesOf(row)[month]);

    const points = changes.labels.map((period, month) => {
        const monthChanges = changesIn(month);
        const lost = monthChanges.reduce((sum, change) => change < 0 ? sum + change : sum, 0);
        const point = {period, summary: monthEnds[month], spacer: baseline + lost};
        monthChanges.forEach((change, index) => {
            point["s" + index] = change;
            point["s" + index + "_below"] = change < 0 ? -change : null;
            point["s" + index + "_above"] = change > 0 ? change : null;
        });
        return point;
    });

    return {
        baseline,
        points,
        recorded: changes.labels.map((period, month) => changesIn(month).some((change) => change !== 0)),
    };
}

/**
 * The months up to the last one anything moved in, and after it only their names.
 *
 * The statements carry all twelve months whether they have happened or not, the ones still to
 * come as noughts, and a line carried on flat across them would claim the total stood still. A
 * quiet month with a recorded one after it still counts, as something did happen after it.
 */
function untilLastRecorded(points, recorded) {
    const last = recorded.lastIndexOf(true);
    return points.map((point, index) => index <= last ? point : {period: point.period, summary: null});
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
function profitCharts(data, axis) {
    const charts = [];

    const income = sideOf(data.rows, "INCOME_GROUP", 1, axis, "Other income");
    const costs = sideOf(data.rows, "EXPENSE_GROUP", -1, axis, "Other costs");
    const levels = data.rows.filter((row) => row.type === "PROFIT_SUMMARY");

    if (income.length + costs.length > 0) {
        charts.push(chart({
            key: "groups",
            title: "Income and costs",
            form: "stacked",
            entries: disambiguated(income, "income").concat(disambiguated(costs, "cost")),
            summary: levels[levels.length - 1],
            axis,
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
            points: pointsOf(axis, entriesOf(levels, axis), null),
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
function sideOf(rows, type, sign, axis, otherName) {
    const entries = [];
    rows.filter((row) => row.type === type).forEach((row) => {
        const values = axis.valuesOf(row).map((value) => sign * value);
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

function chart({
    key, title, form, entries, summary, axis,
    colouring = distinctHues, lineColor = summaryLineColor, lineWidth = 2,
}) {
    const colours = colouring(entries);
    const series = entries.map((entry, index) => ({
        key: "s" + index,
        name: nameOf(entry, entries),
        color: colours[index],
    }));
    const line = summary === undefined || summary === null ? null : {
        key: "summary",
        name: readableName(summary.name),
        color: lineColor,
        width: lineWidth,
    };

    return {key, title, form, series, line, points: pointsOf(axis, entries, summary)};
}

/** A hue of its own for every component, for charts whose components are all of one kind. */
function distinctHues(entries) {
    return entries.map((entry, index) => statementSeriesColors[index % statementSeriesColors.length]);
}

/**
 * The shades the rows of a statement's table are painted in, one per row and in the order of the
 * rows, so that a row and its part of the chart below it are the same colour. A row left as the
 * table would paint it has none.
 *
 * Only the cash flow is painted this way, because only its chart is coloured by side; the other
 * charts give each component a hue of its own, which would turn the table into a patchwork. A
 * group's shade carries the fainter one its accounts are painted in, under `accounts`, and the
 * total's text is written in the colour its line in the chart is drawn in.
 */
export function statementRowShades(type, data, overall = true) {
    const shades = data.rows.map(() => null);
    const axis = axisOf(data, overall);
    if (type !== "cashflow" || axis.labels.length === 0) return shades;

    const groups = data.rows.filter((row) => row.type === "CASH_FLOW_GROUP");
    const groupShades = shadesBySide(entriesOf(groups, axis));
    groups.forEach((row, index) => {
        shades[data.rows.indexOf(row)] = groupShades[index];
    });
    const summary = data.rows.findIndex((row) => row.type === "CASH_FLOW_SUMMARY");
    if (summary !== -1) shades[summary] = cashFlowSummaryShade;
    return shades;
}

/**
 * Green for the components that stand above nought and red for those that hang below it, each in
 * a shade of its own. The first component the table lists is the deepest, and the shades pale
 * towards the axis on both sides, so a column is darkest at its ends and lightest where it meets
 * nought.
 *
 * Which side a component falls on is the sign of its figures taken together, the same reckoning
 * the chart uses to decide where in the stack to draw it.
 */
function shadesBySide(entries) {
    const above = entries.filter((entry) => sumOf(entry) >= 0);
    const below = entries.filter((entry) => sumOf(entry) < 0);
    const aboveShades = spreadOver(aboveZeroShades, above.length).reverse();
    const belowShades = spreadOver(belowZeroShades, below.length);

    return entries.map((entry) => sumOf(entry) >= 0
        ? {...aboveShades[above.indexOf(entry)], accounts: aboveZeroAccountShade}
        : {...belowShades[below.indexOf(entry)], accounts: belowZeroAccountShade});
}

/**
 * As many shades as there are components, palest first, as far apart as the ramp allows. A few
 * components keep clear of its palest and deepest ends, which wash out against the page or read
 * as black. A lone component sits against the axis, so it takes the shade the axis always gets.
 */
function spreadOver(ramp, count) {
    if (count === 0) return [];
    if (count === 1) return [ramp[1]];
    const [first, last] = count <= 3 ? [1, ramp.length - 2] : [0, ramp.length - 1];
    return Array.from({length: count}, (unused, index) =>
        ramp[Math.round(first + index * (last - first) / (count - 1))]);
}

function sumOf(entry) {
    return entry.values.reduce((sum, value) => sum + value, 0);
}

/** Only a name that appears on both sides is qualified; the rest read better plain. */
function nameOf(entry, entries) {
    const clashes = entries.some((other) => other !== entry && other.name === entry.name);
    return clashes && entry.side !== undefined ? entry.name + " (" + entry.side + ")" : entry.name;
}

function entriesOf(rows, axis) {
    return rows.map((row) => ({
        schemaId: row.schemaId,
        name: row.name,
        values: axis.valuesOf(row),
    }));
}

function pointsOf(axis, entries, summary) {
    const summaryValues = summary === undefined || summary === null ? null : axis.valuesOf(summary);
    return axis.labels.map((period, periodIndex) => {
        const point = {period};
        entries.forEach((entry, index) => {
            point["s" + index] = entry.values[periodIndex];
        });
        if (summaryValues !== null) point.summary = summaryValues[periodIndex];
        return point;
    });
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
