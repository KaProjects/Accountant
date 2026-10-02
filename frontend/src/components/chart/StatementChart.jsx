import PropTypes from "prop-types";
import {
    Bar, CartesianGrid, ComposedChart, Legend, Line,
    ReferenceLine, ResponsiveContainer, Tooltip, XAxis, YAxis,
} from "recharts";
import Paper from "@mui/material/Paper";
import {neutral} from "../../theme/palette";
import {abbreviateAmount} from "../../services/statementCharts";
import {formatAmount} from "../../services/amount";
import {columnScale} from "./columnScale";
import ChangesTooltip from "./ChangesTooltip";

// The chart sits on the same surface as the table above it, rather than on the bare page, and
// keeps clear of the bottom edge of it.
const paperStyle = {marginTop: "16px", paddingBottom: "20px"};
const chartHeight = 640;
const chartStyle = {height: chartHeight + "px", width: "100%"};
// Under the table, on its surface and inside its scroll, set off from the rows by a rule.
const alignedStyle = {borderTop: "1px solid " + neutral.headerBorder, paddingBottom: "20px"};
const titleStyle = {margin: 0, padding: "16px 0 0 16px", fontSize: "1rem"};
const axisStyle = {fontSize: "12px"};
const margin = {top: 10, right: 30, left: 10, bottom: 5};
// Lined up with the table, the chart spans it from edge to edge, keeping just clear of either side.
// Its points stay under their columns whatever the margins, since their places are measured.
const alignedMargin = {top: 20, right: 20, left: 20, bottom: 5};
/**
 * A chart of lines carries only three of them, and they are the whole chart rather than a reading
 * over the top of columns, so they are drawn heavier than the summary line of a stacked chart.
 */
const lineWidth = 3;

/**
 * The order a stack of columns is drawn in, chosen so that it reads from top to bottom in the
 * order of the table, the key and the tooltip.
 *
 * recharts stacks each sign away from nought in the order it is given the series: positives
 * upwards, negatives downwards. Above nought that puts the first series nearest the axis, which
 * read upside down against the table, so the series that stand above it are given backwards and
 * the first of them ends up on top. Below nought the first series given is already the first one
 * under the axis, which is where the table's first cost belongs, so those keep their order.
 *
 * Which side a series stands on is the sign of its figures taken together, because a series can
 * cross nought from one year to the next and is still drawn at one place in the order.
 */
const drawingOrder = (chart) => {
    const total = (series) => chart.points.reduce((sum, point) => sum + point[series.key], 0);
    const above = chart.series.filter((series) => total(series) >= 0);
    const below = chart.series.filter((series) => total(series) < 0);
    return above.reverse().concat(below);
};

/**
 * The tooltip lists whatever is under the pointer in the order the marks were drawn, which is
 * not the order of the table for a stack. It is put back into that order, so that the tooltip,
 * the key and the stack itself all read the same way. The total comes last, as it does in the key.
 */
const tooltipOrder = (chart) => (item) => {
    const index = chart.series.findIndex((series) => series.key === seriesKeyOf(item.dataKey));
    return index === -1 ? chart.series.length : index;
};

/** The series a mark belongs to: a chart of changes draws each series in two halves. */
const seriesKeyOf = (dataKey) => String(dataKey).split("_")[0];

/**
 * The stack of a chart of changes, from the bottom up: the spacer that lifts it off nought, the
 * losses, then the gains.
 *
 * Either side reads outwards from the baseline in the order of the table: the table's first group
 * is the change nearest the baseline, above it or below it, and its last group the one furthest
 * out. recharts stacks upwards in the order it is given, so the gains are given in the table's
 * order, and the losses - which are stacked up towards the baseline from beneath - backwards.
 */
const changesInDrawingOrder = (chart) => {
    const backwards = chart.series.slice().reverse();
    return [{key: "spacer", name: "spacer", color: "transparent", spacer: true}]
        .concat(backwards.map((series) => ({...series, key: series.key + "_below"})))
        .concat(chart.series.map((series) => ({...series, key: series.key + "_above"})));
};

/** The key, in the order of the table rather than in the order the marks happen to be drawn. */
const legend = (chart) => {
    const marks = chart.series.map((series) => ({
        value: series.name,
        color: series.color,
        id: series.key,
        type: chart.form === "lines" ? "line" : "rect",
    }));
    if (chart.line !== null) {
        marks.push({value: chart.line.name, color: chart.line.color, type: "line"});
    }
    return marks;
};

/**
 * One chart of a statement: its components as columns stacked on one another, with the row that
 * totals them drawn as a line over the top; the same, but each period's columns stacked on a
 * fixed baseline other than nought; or a line apiece where the figures do not add up to anything.
 *
 * The line at nought is drawn in full, not left to the grid: these statements go below it - a
 * year of losses, the credit accounts of the cash flow - and where a column turns around is the
 * first thing to read off them. For the same reason the stack is offset by sign: a negative
 * component hangs below nought instead of being notched out of the top of the column, where it
 * would read as something owned rather than something owed.
 *
 * The columns do not grow in: the figures are a dozen years of history, not an update, and
 * nothing is learnt from watching them arrive.
 *
 * Given the table's `layout`, the chart is drawn to the table's width, from its left edge to its
 * right, with each point under the column it is named after. Without one it fills the width it is
 * given and spaces its points evenly.
 *
 * A chart is titled only when it is `titled`, which a view asks for when it shows more than one
 * and they need telling apart.
 */
const StatementChart = ({chart, layout = null, titled = true}) => {
    if (layout !== null && layout.width === 0) return null;

    const plot = (size) => (
        <ComposedChart
            {...size} data={chart.points} margin={layout === null ? margin : alignedMargin}
            stackOffset={chart.form === "changes" ? "none" : "sign"}
        >
            <CartesianGrid strokeDasharray="3 3"/>
            {layout === null
                ? <XAxis dataKey="period" style={axisStyle}/>
                : <XAxis dataKey="period" style={axisStyle} scale={columnScale(chart.points.map((point) => point.period), layout)}/>
            }
            <YAxis tickFormatter={abbreviateAmount} style={axisStyle}/>
            {chart.form === "changes"
                ? <Tooltip content={<ChangesTooltip chart={chart}/>}/>
                : <Tooltip formatter={formatAmount} itemSorter={tooltipOrder(chart)}/>
            }
            <Legend payload={legend(chart)}/>
            <ReferenceLine y={0} stroke={neutral.text} strokeWidth={1.5}/>
            {chart.form === "stacked" && drawingOrder(chart).map((series) => (
                <Bar
                    key={series.key} dataKey={series.key} name={series.name}
                    fill={series.color} stackId="stack"
                    isAnimationActive={false}
                />
            ))}
            {chart.form === "changes" && changesInDrawingOrder(chart).map((half) => (
                <Bar
                    key={half.key} dataKey={half.key} name={half.name} fill={half.color}
                    stackId="changes" isAnimationActive={false}
                    legendType="none" tooltipType={half.spacer ? "none" : undefined}
                />
            ))}
            {chart.form === "changes" &&
                // marked on the value axis too, which is where the figure it stands for is read
                <ReferenceLine
                    y={chart.baseline} stroke={chart.line.color} strokeWidth={1.5} strokeDasharray="5 4"
                    label={{
                        value: abbreviateAmount(chart.baseline), position: "left",
                        fill: chart.line.color, fontSize: 12, fontWeight: "bold",
                    }}
                />
            }
            {chart.form === "lines" && chart.series.map((series) => (
                <Line
                    key={series.key} type="linear" dataKey={series.key} name={series.name}
                    stroke={series.color} strokeWidth={lineWidth} dot={false}
                    isAnimationActive={false}
                />
            ))}
            {chart.line !== null &&
                <Line
                    type="linear" dataKey={chart.line.key} name={chart.line.name}
                    stroke={chart.line.color} strokeWidth={chart.line.width} dot={false}
                    isAnimationActive={false}
                />
            }
        </ComposedChart>
    );

    if (layout !== null) {
        return (
            <div style={alignedStyle}>
                {titled && <h3 style={titleStyle}>{chart.title}</h3>}
                {plot({width: layout.width, height: chartHeight})}
            </div>
        );
    }
    return (
        <Paper style={paperStyle}>
            {titled && <h3 style={titleStyle}>{chart.title}</h3>}
            <div style={chartStyle}>
                <ResponsiveContainer width="100%" height="100%">
                    {plot({})}
                </ResponsiveContainer>
            </div>
        </Paper>
    );
};

StatementChart.propTypes = {
    chart: PropTypes.shape({
        title: PropTypes.string.isRequired,
        form: PropTypes.oneOf(["stacked", "changes", "lines"]).isRequired,
        /** For a chart of changes: the figure every period's changes are stacked on. */
        baseline: PropTypes.number,
        series: PropTypes.array.isRequired,
        line: PropTypes.object,
        points: PropTypes.array.isRequired,
    }).isRequired,
    /**
     * Where the table above the chart laid its columns out, to line the points up under them:
     * {width, columns: [{name, left, width}]}.
     */
    /** Whether to name the chart: only needed where a view shows more than one. */
    titled: PropTypes.bool,
    layout: PropTypes.shape({
        width: PropTypes.number.isRequired,
        columns: PropTypes.arrayOf(PropTypes.shape({
            name: PropTypes.string,
            left: PropTypes.number.isRequired,
            width: PropTypes.number.isRequired,
        })).isRequired,
    }),
};

export default StatementChart;
