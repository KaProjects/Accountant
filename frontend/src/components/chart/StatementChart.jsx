import PropTypes from "prop-types";
import {
    Bar, Brush, CartesianGrid, ComposedChart, Legend, Line,
    Rectangle, ReferenceLine, ResponsiveContainer, Tooltip, XAxis, YAxis,
} from "recharts";
import Paper from "@mui/material/Paper";
import {neutral} from "../../theme/palette";
import {abbreviateAmount} from "../../services/statementCharts";
import {columnScale} from "./columnScale";
import StackTooltip from "./StackTooltip";

// The chart sits on the same surface as the table above it, rather than on the bare page, and
// keeps clear of the bottom edge of it.
const paperStyle = {marginTop: "16px", paddingBottom: "20px"};
const chartHeight = 640;
const chartStyle = {height: chartHeight + "px", width: "100%"};
// Under the table, on its surface and inside its scroll, set off from the rows by a rule.
const alignedStyle = {borderTop: "1px solid " + neutral.headerBorder, paddingBottom: "20px"};
const titleStyle = {margin: 0, padding: "16px 0 0 16px", fontSize: "1rem"};
// a title and a control share the line above the chart: the title at the left, the control at the
// right, over the far end of the plot
const headerStyle = {display: "flex", alignItems: "center", justifyContent: "space-between", padding: "12px 30px 0 0"};
const axisStyle = {fontSize: "12px"};
const margin = {top: 10, right: 30, left: 10, bottom: 5};
// Lined up with the table, the chart spans it from edge to edge, keeping just clear of either side.
// Its points stay under their columns whatever the margins, since their places are measured.
const alignedMargin = {top: 20, right: 20, left: 20, bottom: 5};
const brushHeight = 24;

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
    const total = (key) => chart.points.reduce((sum, point) => sum + (point[key] ?? 0), 0);
    const above = [];
    const below = [];
    chart.series.forEach((series) => (total(series.key) >= 0 ? above : below).push(series));
    return above.reverse().concat(below);
};

/**
 * The stack of a chart of changes, from the bottom up: the spacer that lifts it off nought, the
 * losses, then the gains.
 *
 * The column reads top to bottom in the order of the table, as the tooltip does: above the
 * baseline the table's first group is at the top and its last stands on the baseline, and below it
 * the first hangs from the baseline and the last is at the bottom. recharts stacks upwards in the
 * order it is given, so both halves are given backwards - the losses so that the first ends up just
 * under the baseline, and the gains so that the last ends up just above it.
 */
const changesInDrawingOrder = (chart) => {
    const backwards = chart.series.slice().reverse();
    return [{key: "spacer", name: "spacer", color: "transparent", spacer: true}]
        .concat(backwards.map((series) => ({...series, key: series.key + "_below"})))
        .concat(backwards.map((series) => ({...series, key: series.key + "_above"})));
};

/**
 * The ticks of an axis too long to name every point on, such as a decade of months: only the
 * points the chart lists in `ticks` are named, each by its own label.
 */
const namedTicks = (chart) => {
    if (!chart.ticks) return {};
    const labels = new Map(chart.ticks.map((tick) => [tick.value, tick.label]));
    return {ticks: chart.ticks.map((tick) => tick.value), tickFormatter: (value) => labels.get(value) ?? value, interval: 0};
};

/**
 * A whole with parts laid over it is drawn in layers. Behind, the whole as a column of its own. In
 * front, what was taken from it, as a stack on an axis of its own, which the reader never sees but
 * which puts it in exactly the place of the whole's column, with its width; and in front of that,
 * on a third such axis, whatever was given back. Each part in front is drawn short of the column's
 * left edge, so that the whole shows down the whole of that edge, and wherever the parts leave it
 * uncovered: the column reads as all of the whole, with the parts laid over most of it.
 *
 * The parts used to be a narrower stack beside an undrawn strip, sized in percentages of the
 * column; recharts gave the two equal halves of it whatever they asked for, and the strip left the
 * whole showing down half the column.
 */
const totalShowing = 0.15;
const takenAxis = "taken";
const givenAxis = "given";

const partShape = ({x, width, ...rest}) => (
    <Rectangle {...rest} x={x + width * totalShowing} width={width * (1 - totalShowing)}/>
);
// what was given back is drawn short of the right edge as well, so that the costs it covers show
// down that edge to their full length, and plainly run on behind it
const givenShape = ({x, width, ...rest}) => (
    <Rectangle {...rest} x={x + width * totalShowing} width={width * (1 - 2 * totalShowing)}/>
);

/**
 * One layer of a whole with parts laid over it, from the bottom up, given backwards so that the
 * table's first is on top; the layers in front stand on an undrawn spacer lifting them to where
 * the costs end.
 */
const layerInDrawingOrder = (chart, layer) => {
    const marks = chart.series.filter((series) => series.layer === layer).reverse();
    return layer === "behind" ? marks : [{key: "lift", name: "lift", color: "transparent", spacer: true}].concat(marks);
};
const hasLayer = (chart, layer) => chart.series.some((series) => series.layer === layer);

/**
 * A series drawn `hatched` is filled with diagonal stripes of its colour over a pale wash of it,
 * rather than with the plain colour: a part that is not like the others - the profit that is on
 * the liabilities side only to balance it. The stripes are a pattern each chart defines for
 * itself, under an id of its own, as several charts share the page.
 */
const hatchOf = (chart, series) => "hatch-" + chart.key + "-" + series.key;
const fillOf = (chart, series) => series.hatched ? "url(#" + hatchOf(chart, series) + ")" : series.color;

const hatches = (chart) => (
    <defs>
        {chart.series.filter((series) => series.hatched).map((series) => (
            <pattern
                key={series.key} id={hatchOf(chart, series)} width="8" height="8"
                patternUnits="userSpaceOnUse" patternTransform="rotate(45)"
            >
                <rect width="8" height="8" fill={series.color} fillOpacity="0.3"/>
                <rect width="4" height="8" fill={series.color}/>
            </pattern>
        ))}
    </defs>
);

const legendName = (value, entry) => <span style={{color: entry.payload?.ink ?? entry.color}}>{value}</span>;

/** The key, in the order of the table rather than in the order the marks happen to be drawn. */
const legend = (chart) => {
    // the icon is painted with the series' fill, which for a hatched one is its pattern; the name
    // is written in its plain colour, which recharts would otherwise also take from the fill
    const marks = chart.series.map((series) => ({
        value: series.name, color: fillOf(chart, series), id: series.key, type: "rect", payload: {ink: series.color},
    }));
    if (chart.line !== null) {
        marks.push({value: chart.line.name, color: chart.line.color, type: "line"});
    }
    return marks;
};

/**
 * One chart of a statement: its components as columns stacked on one another, with the row that
 * totals them drawn as a line over the top - stacked on nought, or each period's columns stacked on
 * a fixed baseline other than nought - or a total split into the parts taken from it and what was
 * left of it.
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
 * A chart that spaces its own points can also be narrowed to a stretch of them, with a slider under
 * it, and its value axis then fits that stretch: the early years of a balance sheet that has since
 * grown many times over sit flat at the foot of the whole chart, and fill it once the slider is
 * drawn in around them. A chart lined up with the table has no slider, as narrowing it would pull
 * its points out from under their columns.
 *
 * A chart is titled only when it is `titled`, which a view asks for when it shows more than one
 * and they need telling apart. A `control`, such as a switch between variants of the chart, is set
 * on the same line, at its right.
 */
const StatementChart = ({chart, layout = null, titled = true, control = null}) => {
    if (layout !== null && layout.width === 0) return null;

    // every category axis of a chart lined up with the table - the one shown, and those the layers
    // of a split column are drawn on - puts each point under its column
    const alignedTo = (table) => ({scale: columnScale(chart.points.map((point) => point.period), table)});

    const plot = (size) => (
        <ComposedChart
            {...size} data={chart.points} margin={layout === null ? margin : alignedMargin}
            stackOffset={chart.form === "stacked" ? "sign" : "none"}
        >
            {hatches(chart)}
            <CartesianGrid strokeDasharray="3 3"/>
            <XAxis dataKey="period" style={axisStyle} {...(layout === null ? namedTicks(chart) : alignedTo(layout))}/>
            <YAxis tickFormatter={abbreviateAmount} style={axisStyle}/>
            <Tooltip content={<StackTooltip chart={chart}/>}/>
            <Legend payload={legend(chart)} formatter={legendName}/>
            <ReferenceLine y={0} stroke={neutral.text} strokeWidth={1.5}/>
            {chart.form === "stacked" && drawingOrder(chart).map((series) => (
                <Bar
                    key={series.key} dataKey={series.key} name={series.name}
                    fill={fillOf(chart, series)} stackId="stack"
                    isAnimationActive={false}
                />
            ))}
            {chart.form === "split" && layerInDrawingOrder(chart, "behind").map((series) => (
                <Bar
                    key={series.key} dataKey={series.key} name={series.name} fill={series.color}
                    stackId="behind" isAnimationActive={false} legendType="none"
                />
            ))}
            {chart.form === "split" && [takenAxis, givenAxis].filter((layer) => hasLayer(chart, layer)).map((layer) => [
                <XAxis key={layer} xAxisId={layer} dataKey="period" hide {...(layout === null ? {} : alignedTo(layout))}/>,
                ...layerInDrawingOrder(chart, layer).map((mark) => (
                    <Bar
                        key={layer + mark.key} xAxisId={layer} dataKey={mark.key} name={mark.name} fill={mark.color}
                        stackId={layer} shape={layer === givenAxis ? givenShape : partShape} isAnimationActive={false}
                        legendType="none" tooltipType={mark.spacer ? "none" : undefined}
                    />
                )),
            ])}
            {chart.form === "changes" && changesInDrawingOrder(chart).map((half) => (
                <Bar
                    key={half.key} dataKey={half.key} name={half.name} fill={half.color}
                    stackId="changes" isAnimationActive={false}
                    legendType="none" tooltipType={half.spacer ? "none" : undefined}
                />
            ))}
            {chart.form === "changes" &&
                // marked on the value axis too, which is where the figure it stands for is read -
                // unless it is nought, which the axis already names
                <ReferenceLine
                    y={chart.baseline} stroke={chart.line.color} strokeWidth={1.5} strokeDasharray="5 4"
                    label={chart.baseline === 0 ? undefined : {
                        value: abbreviateAmount(chart.baseline), position: "left",
                        fill: chart.line.color, fontSize: 12, fontWeight: "bold",
                    }}
                />
            }
            {chart.line !== null &&
                <Line
                    type="linear" dataKey={chart.line.key} name={chart.line.name}
                    stroke={chart.line.color} strokeWidth={chart.line.width} dot={false}
                    isAnimationActive={false}
                />
            }
            {layout === null &&
                <Brush
                    dataKey="period" height={brushHeight} stroke={neutral.headerBorder}
                    travellerWidth={10}
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
    const title = titled ? <h3 style={titleStyle}>{chart.title}</h3> : null;
    return (
        <Paper style={paperStyle}>
            {control === null ? title : <div style={headerStyle}>{title ?? <span/>}{control}</div>}
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
        form: PropTypes.oneOf(["stacked", "changes", "split"]).isRequired,
        /** For a chart of changes: the figure every period's changes are stacked on. */
        baseline: PropTypes.number,
        /** The only points to name on the axis, each with its own label: [{value, label}]. */
        ticks: PropTypes.arrayOf(PropTypes.shape({value: PropTypes.string, label: PropTypes.string})),
        series: PropTypes.array.isRequired,
        line: PropTypes.object,
        points: PropTypes.array.isRequired,
    }).isRequired,
    /**
     * Where the table above the chart laid its columns out, to line the points up under them:
     * {width, columns: [{name, left, width}]}.
     */
    layout: PropTypes.shape({
        width: PropTypes.number.isRequired,
        columns: PropTypes.arrayOf(PropTypes.shape({
            name: PropTypes.string,
            left: PropTypes.number.isRequired,
            width: PropTypes.number.isRequired,
        })).isRequired,
    }),
    /** Whether to name the chart: only needed where a view shows more than one. */
    titled: PropTypes.bool,
    /** Set beside the title, at the right: a switch between variants of the chart, say. */
    control: PropTypes.node,
};

export default StatementChart;
