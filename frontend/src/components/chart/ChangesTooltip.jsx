import PropTypes from "prop-types";
import {formatAmount} from "../../services/amount";

// Laid out as recharts lays out its own tooltip, so the two kinds read as one.
const boxStyle = {
    margin: 0, padding: "10px", backgroundColor: "#fff",
    border: "1px solid #ccc", whiteSpace: "nowrap",
};
const titleStyle = {margin: 0};
const itemStyle = {margin: 0, paddingTop: "4px"};
const separatorStyle = (color, dashed) => ({
    margin: "6px 0 2px",
    borderTop: "1.5px " + (dashed ? "dashed " : "solid ") + color,
});

/**
 * The tooltip of a chart of changes, which sets out a month the way its column is built: the
 * changes that stand above the baseline, a dashed line where the baseline is, then the changes that
 * hang below it, and finally where the total stood at the end of the month.
 *
 * Each side lists its groups in the order of the table. A group that did not move that month is
 * left out, and the dashed line is drawn even when one side is empty, so it is always plain which
 * side of the baseline a figure is on. The opening point, where the line sets off, and the months
 * the year has not reached yet have no tooltip.
 */
const ChangesTooltip = ({active, payload, label, chart}) => {
    if (!active || !payload || payload.length === 0) return null;

    // the opening is only where the line sets off from, its figure marked on the value axis; and
    // a month the year has not reached yet has nothing to report
    const point = payload[0].payload;
    if (point.opening || point[chart.line.key] === null) return null;
    const moved = chart.series
        .map((series) => ({...series, change: point[series.key]}))
        .filter((series) => typeof series.change === "number" && series.change !== 0);
    const entry = (series) => (
        <p key={series.key} style={{...itemStyle, color: series.color}}>
            {series.name} : {formatAmount(series.change)}
        </p>
    );

    return (
        <div style={boxStyle}>
            <p style={titleStyle}>{label}</p>
            {moved.length > 0 && <>
                {moved.filter((series) => series.change > 0).map(entry)}
                <div role="separator" aria-label="baseline" style={separatorStyle(chart.line.color, true)}/>
                {moved.filter((series) => series.change < 0).map(entry)}
                <div role="separator" style={separatorStyle("#ccc", false)}/>
            </>}
            <p style={{...itemStyle, color: chart.line.color}}>
                {chart.line.name} : {formatAmount(point[chart.line.key])}
            </p>
        </div>
    );
};

ChangesTooltip.propTypes = {
    active: PropTypes.bool,
    payload: PropTypes.array,
    label: PropTypes.string,
    chart: PropTypes.shape({
        series: PropTypes.array.isRequired,
        line: PropTypes.object.isRequired,
    }).isRequired,
};

export default ChangesTooltip;
