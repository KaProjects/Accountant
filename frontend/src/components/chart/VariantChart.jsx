import {useState} from "react";
import PropTypes from "prop-types";
import ToggleButton from "@mui/material/ToggleButton";
import ToggleButtonGroup from "@mui/material/ToggleButtonGroup";
import StatementChart from "./StatementChart";

/**
 * A statement chart that can also be shown month by month: the same chart over every year's months
 * instead of over the years, switched to and back from a toggle at its top right.
 *
 * The two used to be drawn one under the other, which doubled every chart of a view - six on the
 * overall income statement - for what is one chart read at two scales. It opens on the years, the
 * scale of the table above it.
 *
 * Without a `monthly` variant - not fetched yet, or nothing to chart - it is the yearly chart
 * alone, with no toggle.
 */
const VariantChart = ({chart, monthly = null, titled = true}) => {
    const [variant, setVariant] = useState("yearly");

    if (monthly === null) return <StatementChart chart={chart} titled={titled}/>;

    const toggle = (
        <ToggleButtonGroup
            size="small" exclusive value={variant} aria-label="scale of the chart"
            onChange={(event, chosen) => chosen !== null && setVariant(chosen)}
        >
            <ToggleButton value="yearly">Yearly</ToggleButton>
            <ToggleButton value="monthly">Monthly</ToggleButton>
        </ToggleButtonGroup>
    );
    return <StatementChart key={variant} chart={variant === "monthly" ? monthly : chart} titled={titled} control={toggle}/>;
};

VariantChart.propTypes = {
    chart: PropTypes.object.isRequired,
    monthly: PropTypes.object,
    titled: PropTypes.bool,
};

export default VariantChart;
