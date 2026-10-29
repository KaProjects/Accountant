import PropTypes from "prop-types";
import {formatAmount} from "../../services/amount";
import {neutral} from "../../theme/palette";

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
 * The tooltip of a stacked chart, which sets out a period the way its column is built: what stands
 * above the line the column is stacked on, a rule where that line is, then what hangs below it, and
 * finally the figure the chart's own line stands at.
 *
 * The line a column is stacked on is nought, drawn solid, or - for a chart of changes - its
 * baseline, drawn dashed in the colour of the chart's line; the rule in the tooltip is drawn the
 * same way, so the two read as one.
 *
 * Each side lists its parts in the order of the table, as the column reads from top to bottom. A
 * part that did not move is left out, and the rule is drawn even when one side is empty, so it is
 * always plain which side a figure is on. The opening point, where the line sets off, and the
 * periods not reached yet have no tooltip.
 */
const StackTooltip = ({active, payload, label, chart}) => {
    if (!active || !payload || payload.length === 0) return null;

    // the opening is only where the line sets off from, its figure marked on the value axis; and
    // a period not reached yet has nothing to report
    const point = payload[0].payload;
    if (point.opening || point[chart.line.key] === null) return null;
    const moved = chart.series
        .map((series) => ({...series, value: point[series.key]}))
        .filter((series) => typeof series.value === "number" && series.value !== 0);
    const entry = (series) => (
        <p key={series.key} style={{...itemStyle, color: series.color}}>
            {series.name} : {formatAmount(series.value)}
        </p>
    );
    const stackedOnBaseline = chart.form === "changes";

    if (chart.form === "split") {
        // a whole with parts laid over it, in the order of the key, which is the table's, then the
        // line - what the whole came to once they were taken.
        // Nothing stands above or below a line here, so there is nothing to separate.
        const valued = chart.series.map((series) => ({...series, value: point[series.key]}));
        return (
            <div style={boxStyle}>
                <p style={titleStyle}>{label}</p>
                {valued.filter((series) => series.value !== 0 || series.layer === "behind").map(entry)}
                <p style={{...itemStyle, color: chart.line.color}}>
                    {chart.line.name} : {formatAmount(point[chart.line.key])}
                </p>
            </div>
        );
    }

    return (
        <div style={boxStyle}>
            <p style={titleStyle}>{label}</p>
            {moved.length > 0 && <>
                {moved.filter((series) => series.value > 0).map(entry)}
                <div
                    role="separator" aria-label={stackedOnBaseline ? "baseline" : "nought"}
                    style={stackedOnBaseline ? separatorStyle(chart.line.color, true) : separatorStyle(neutral.text, false)}
                />
                {moved.filter((series) => series.value < 0).map(entry)}
                <div role="separator" style={separatorStyle("#ccc", false)}/>
            </>}
            <p style={{...itemStyle, color: chart.line.color}}>
                {chart.line.name} : {formatAmount(point[chart.line.key])}
            </p>
        </div>
    );
};

StackTooltip.propTypes = {
    active: PropTypes.bool,
    payload: PropTypes.array,
    label: PropTypes.string,
    chart: PropTypes.shape({
        form: PropTypes.string,
        series: PropTypes.array.isRequired,
        line: PropTypes.object.isRequired,
    }).isRequired,
};

export default StackTooltip;
