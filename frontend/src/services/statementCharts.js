import {
    aboveZeroAccountShade, aboveZeroShades, belowZeroAccountShade, belowZeroShades,
    cashFlowSummaryShade, netIncomeColor, profitGainShades, profitGroupColors, profitLossShades, startingLevelColor,
    statementSeriesColors, summaryLineColor,
} from "../theme/palette";

/**
 * The charts a statement is drawn with, derived from the very payload the table above them is
 * drawn from. Nothing is fetched for them: the rows already carry their figures, and their type
 * already says which of them are components and which are the totals of those components, so a
 * chart cannot drift from the table it sits under.
 *
 * Every overall statement is charted. Of the yearly ones only the cash flow is, so far.
 *
 * Each chart is {key, title, form, series, line, points}, where form is "stacked" for columns
 * stacked on one another, "changes" for each period's changes stacked on a fixed baseline other
 * than nought (which such a chart also carries, as `baseline`), and "split" for a total split into
 * the parts taken from it and what was left of it. A point is one period - a year, or a month - carrying a figure under every series
 * key. A chart that sets `alignToTable` is drawn with each point under the table column it is
 * named after, rather than spaced evenly.
 */
export function statementCharts(type, data, overall = true) {
    const axis = axisOf(data, overall);
    if (axis.labels.length === 0) return [];
    if (!overall) {
        if (type === "cashflow") return yearlyCashFlowCharts(data);
        if (type === "profit") return yearlyProfitCharts(data);
        return [];
    }
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
        title: readableName(summary.name),
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
 * The income statement is charted level by level, one chart for each of the three profits it
 * works down to. Their lines are the three profit levels, so between them the charts also show how
 * the levels move against one another, which a chart of the three alone used to show.
 *
 * Net income and operating profit are each drawn as what came in with the costs laid over it: net
 * income as the income of the first rows, in greens, under the costs of those rows, and operating
 * profit as net income, in blue, under the running costs. The line is the profit they come to.
 *
 * After operating profit the statement lists its groups in pairs, an income and a cost of the same
 * name, and those are charted as the one figure the pair comes to, which can be a gain one year and
 * a loss the next: operating profit as a blue column, each group added above nought or taken below
 * it, and net profit as the line. Green and red would change with the year there, so each group of
 * that chart keeps one colour of its own instead - neighbouring hues, as light as the blue beside
 * them - and its place above or below nought says which way the year went.
 */
function profitCharts(data, axis) {
    const sections = profitSections(data.rows);
    const isIncome = (row) => row.type === "INCOME_GROUP";

    return sections.map((section, index) => {
        if (index === 0) {
            // the work income is the column; the other groups are laid over it, the costs taken
            // from its top and the other incomes given back from where those end. A cost is laid
            // over as the accounts it is made of - the taxes and the insurances - which say more
            // than their sum.
            const rows = section.rows.flatMap((row) => isIncome(row) || !row.children?.length
                ? [row]
                : row.children.map((child) => ({...child, type: row.type}))).map(sided);
            const incomes = rows.filter(isIncome);
            const costs = rows.filter((row) => !isIncome(row));
            // palest first: the work income fills most of the column, and the incomes given back are
            // thin bands over the red, which a stronger green makes out
            const greens = spreadOver(profitGainShades, incomes.length);
            // lighter to darker, from a shade darker than the running costs' palest: over the pale
            // work income that one hardly showed
            const reds = costs.length <= 3
                ? profitLossShades.slice(2, 2 + costs.length)
                : spreadOver(profitLossShades, costs.length);
            const colourOf = (row) => isIncome(row) ? greens[incomes.indexOf(row)] : reds[costs.indexOf(row)];
            return laidOver({
                key: "level" + index,
                parts: rows.map((row, at) => ({
                    name: nameOf(row, rows), values: axis.valuesOf(row), color: colourOf(row),
                    layer: at === 0 ? "behind" : isIncome(row) ? "given" : "taken",
                })),
                left: section.summary,
                axis,
            });
        }
        if (index === 1) {
            const total = sections[0].summary;
            const reds = spreadOver(profitLossShades, section.rows.length);
            return laidOver({
                key: "level" + index,
                parts: [{name: readableName(total.name), values: axis.valuesOf(total), color: netIncomeColor, layer: "behind"}]
                    .concat(section.rows.map((row, at) => ({name: row.name, values: axis.valuesOf(row), color: reds[at], layer: "taken"}))),
                left: section.summary,
                axis,
            });
        }
        return netProfitChart({
            key: "level" + index,
            base: sections[index - 1].summary,
            rows: section.rows,
            summary: section.summary,
            axis,
        });
    });
}

/** A row of the first section, with the side of the statement it is on, to tell a pair apart by. */
function sided(row) {
    return {...row, side: row.type === "INCOME_GROUP" ? "income" : "cost"};
}

/**
 * A profit as what was left of what came in once the costs were paid, drawn in layers: what came in
 * as a column behind, the costs taken from it laid over its top, anything given back laid over the
 * foot of the costs, and the profit that was `left` as the line.
 *
 * The costs take most of what came in every year, so drawn as costs below nought against an income
 * above it, the chart grew both ways while the profit between them barely moved. Laid over the
 * income instead, they show what part of it went where, and the chart grows one way only.
 *
 * The costs reach down from the top of the column, so whatever of it they leave uncovered, at its
 * foot, is what was left - and what was given back, laid over the foot of the costs, wins back that
 * much of them, up to the line. In a year whose costs came to more than came in they cover all of
 * the column and reach on down past nought. Both are stood on an undrawn spacer, kept in each point
 * as `lift`: where the costs end, which is the profit less what was given back.
 *
 * Each part says which `layer` it is on - "behind", "taken" or "given" - and they are listed in the
 * order of the table.
 */
function laidOver({key, parts, left, axis}) {
    const leftOver = axis.valuesOf(left);
    const given = parts.filter((part) => part.layer === "given");

    return {
        key,
        title: readableName(left.name),
        form: "split",
        series: parts.map((part, index) => ({key: "s" + index, name: part.name, color: part.color, layer: part.layer})),
        line: {key: "summary", name: readableName(left.name), color: summaryLineColor, width: 2},
        points: axis.labels.map((period, periodIndex) => {
            const givenBack = given.reduce((sum, part) => sum + part.values[periodIndex], 0);
            const point = {period, lift: leftOver[periodIndex] - givenBack, summary: leftOver[periodIndex]};
            parts.forEach((part, index) => {
                point["s" + index] = part.values[periodIndex];
            });
            return point;
        }),
    };
}

/**
 * The income statement of a single year: the same three charts as over all the years, month by
 * month, each month under its column of the table.
 *
 * Unlike a balance, a profit is not carried from one month to the next - each month's figure is
 * what was earned and spent in it - so the months are charted as they are, not run together. The
 * months still to come are only named, as on the year's cash flow, rather than charted as noughts.
 *
 * Each chart decides for itself which months those are, by its own figures: a month counts once
 * anything in that chart moved in it, or in any month after it. Something booked for the end of
 * the year below operating profit used to carry every chart's line on to December, the first two
 * sitting on nought across months that had not happened yet.
 */
function yearlyProfitCharts(data) {
    return profitCharts(data, monthlyChangesAxis(data.columns)).map((chart) => ({
        ...untilOwnLastRecorded(chart),
        alignToTable: true,
    }));
}

/** A chart of months, whose months after the last one its own figures moved in are only named. */
function untilOwnLastRecorded(chart) {
    const moved = (point) => point.summary !== 0 || chart.series.some((series) => point[series.key] !== 0);
    return {...chart, points: untilLastRecorded(chart.points, chart.points.map(moved))};
}

/**
 * The overall income statement month by month: the same three charts as over the years, with
 * every year's months one after another, from the first year on. The axis names the years, at
 * their Januaries; a month itself is named in full in its tooltip.
 *
 * Each year's own statement lists the same rows as the overall one, in the same order, and that
 * order is what matches them up - a schema id alone does not, as the groups after operating profit
 * reuse the ids of the ones before it. A row a year does not have where the overall one has it is
 * charted as noughts for that year.
 *
 * Each chart's months after the last one its own figures moved in are only named, as on a single
 * year. Takes the overall statement and every year's own statement, as [{year, data}].
 */
export function monthlyProfitCharts(overall, years) {
    const statements = years.filter(({data}) => data.rows.length > 0);
    if (statements.length === 0) return [];

    const rowAt = (rows, [index, ...deeper]) => {
        const row = rows[index];
        return row === undefined || deeper.length === 0 ? row : rowAt(row.children ?? [], deeper);
    };
    const monthsOf = (row, path) => statements.flatMap(({data}) => {
        const own = rowAt(data.rows, path);
        return monthlyChangesAxis(data.columns).valuesOf(own !== undefined && own.schemaId === row.schemaId ? own : null);
    });
    const merged = (rows, path) => rows.map((row, index) => ({
        ...row,
        monthlyValues: monthsOf(row, path.concat(index)),
        children: merged(row.children ?? [], path.concat(index)),
    }));

    const axis = {
        labels: statements.flatMap(({year, data}) => monthlyChangesAxis(data.columns).labels.map((month) => month + " " + year)),
        valuesOf: (row) => row.monthlyValues,
    };
    const ticks = statements.map(({year, data}) => ({value: monthlyChangesAxis(data.columns).labels[0] + " " + year, label: String(year)}));

    return profitCharts({...overall, rows: merged(overall.rows, [])}, axis).map((chart) => ({
        ...untilOwnLastRecorded(chart),
        key: chart.key + "-monthly",
        ticks,
    }));
}

/** The statement's rows split at its profit levels: each level, and the groups that lead to it. */
function profitSections(rows) {
    const sections = [];
    let pending = [];
    rows.forEach((row) => {
        if (row.type === "PROFIT_SUMMARY") {
            sections.push({rows: pending, summary: row});
            pending = [];
        } else {
            pending.push(row);
        }
    });
    return sections;
}

/**
 * Net profit: operating profit in blue, the groups that lead from it, and net profit as the line.
 * Each group is the income and the cost of the same name netted into one figure, in a colour of
 * its own.
 *
 * Costs are negated, so that they hang below nought.
 */
function netProfitChart({key, base, rows, summary, axis}) {
    const signed = (row) => axis.valuesOf(row).map((value) => row.type === "INCOME_GROUP" ? value : -value);
    const sum = (series) => series.reduce((total, values) => total.map((value, index) => value + values[index]));

    const groups = [...new Set(rows.map((row) => row.name))].map((name) => ({
        name, values: sum(rows.filter((row) => row.name === name).map(signed)),
    }));
    const parts = [{name: readableName(base.name), values: axis.valuesOf(base), color: startingLevelColor}]
        .concat(groups.map((group, index) => ({...group, color: profitGroupColors[index % profitGroupColors.length]})));

    const totals = axis.valuesOf(summary);
    return {
        key,
        title: readableName(summary.name),
        form: "stacked",
        series: parts.map((part, index) => ({key: "s" + index, name: part.name, color: part.color})),
        line: {key: "summary", name: readableName(summary.name), color: summaryLineColor, width: 2},
        points: axis.labels.map((period, periodIndex) => {
            const point = {period};
            parts.forEach((part, index) => {
                point["s" + index] = part.values[periodIndex];
            });
            point.summary = totals[periodIndex];
            return point;
        }),
    };
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
