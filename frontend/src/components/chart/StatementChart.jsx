import PropTypes from "prop-types";
import {
    Bar, CartesianGrid, ComposedChart, Legend, Line,
    ReferenceLine, ResponsiveContainer, Tooltip, XAxis, YAxis,
} from "recharts";
import Paper from "@mui/material/Paper";
import {neutral} from "../../theme/palette";
import {abbreviateAmount} from "../../services/statementCharts";

// The chart sits on the same surface as the table above it, rather than on the bare page.
const paperStyle = {marginTop: "16px"};
const chartStyle = {height: "640px", width: "100%"};
const titleStyle = {margin: 0, padding: "16px 0 0 16px", fontSize: "1rem"};
const axisStyle = {fontSize: "12px"};
const margin = {top: 10, right: 30, left: 10, bottom: 5};
/**
 * A chart of lines carries only three of them, and they are the whole chart rather than a reading
 * over the top of columns, so they are drawn heavier than the summary line of a stacked chart.
 */
const lineWidth = 3;

/**
 * The order the columns are drawn in, chosen so that a column read from top to bottom lists its
 * parts in the order of the table, the key and the tooltip.
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
    const index = chart.series.findIndex((series) => series.key === item.dataKey);
    return index === -1 ? chart.series.length : index;
};

/** The key, in the order of the table rather than in the order the marks happen to be drawn. */
const legend = (chart) => {
    const marks = chart.series.map((series) => ({
        value: series.name,
        color: series.color,
        id: series.key,
        type: chart.form === "stacked" ? "rect" : "line",
    }));
    if (chart.line !== null) {
        marks.push({value: chart.line.name, color: chart.line.color, type: "line"});
    }
    return marks;
};

/**
 * One chart of an overall statement: either its components as columns stacked on one another,
 * with the row that totals them drawn as a line over the top, or a line apiece where the figures
 * do not add up to anything.
 *
 * The line at nought is drawn in full, not left to the grid: these statements go below it - a
 * year of losses, the credit accounts of the cash flow - and where a column turns around is the
 * first thing to read off them. For the same reason the stack is offset by sign: a negative
 * component hangs below nought instead of being notched out of the top of the column, where it
 * would read as something owned rather than something owed.
 *
 * The columns do not grow in: the figures are a dozen years of history, not an update, and
 * nothing is learnt from watching them arrive.
 */
const StatementChart = ({chart}) => (
    <Paper style={paperStyle}>
        <h3 style={titleStyle}>{chart.title}</h3>
        <div style={chartStyle}>
            <ResponsiveContainer width="100%" height="100%">
                <ComposedChart data={chart.points} margin={margin} stackOffset="sign">
                    <CartesianGrid strokeDasharray="3 3"/>
                    <XAxis dataKey="year" style={axisStyle}/>
                    <YAxis tickFormatter={abbreviateAmount} style={axisStyle}/>
                    <Tooltip formatter={(value) => value.toLocaleString()} itemSorter={tooltipOrder(chart)}/>
                    <Legend payload={legend(chart)}/>
                    <ReferenceLine y={0} stroke={neutral.text} strokeWidth={1.5}/>
                    {chart.form === "stacked" && drawingOrder(chart).map((series) => (
                        <Bar
                            key={series.key} dataKey={series.key} name={series.name}
                            fill={series.color} stackId="stack"
                            isAnimationActive={false}
                        />
                    ))}
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
                            stroke={chart.line.color} strokeWidth={2} dot={false}
                            isAnimationActive={false}
                        />
                    }
                </ComposedChart>
            </ResponsiveContainer>
        </div>
    </Paper>
);

StatementChart.propTypes = {
    chart: PropTypes.shape({
        title: PropTypes.string.isRequired,
        form: PropTypes.oneOf(["stacked", "lines"]).isRequired,
        series: PropTypes.array.isRequired,
        line: PropTypes.object,
        points: PropTypes.array.isRequired,
    }).isRequired,
};

export default StatementChart;
