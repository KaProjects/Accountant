import PropTypes from "prop-types";
import {Card, CardContent, Typography} from "@mui/material";
import {formatAmount} from "../../services/amount";

const cardStyle = {width: 150};
const cardBoxStyle = {backgroundColor: "white", display: "inline-block", verticalAlign: "middle", marginLeft: 10};
const labelStyle = {fontSize: 14};
// in pixels: a bare number in sx is a step of the theme's spacing, 8px each
const periodStyle = {fontSize: 12, fontWeight: 600, marginBottom: "4px"};

const returnColor = (currentReturn) =>
    currentReturn > 0 ? "#158615" : currentReturn < 0 ? "#b93333" : "black";

const signed = (percent) => (percent > 0 ? "+" : "") + percent + "%";

const Figure = ({label, children}) => (
    <>
        <Typography sx={labelStyle} color="text.secondary" align={"center"}>
            {label}
        </Typography>
        <Typography color="text.secondary" align={"center"}>
            {children}
        </Typography>
    </>
);

Figure.propTypes = {
    label: PropTypes.string.isRequired,
    children: PropTypes.node,
};

/**
 * The headline figures of an asset beside its chart, over the stretch of it the chart's slider
 * picks, which heads the card. They are named after the stretch's start and end however much of
 * the chart it covers, so that moving the slider changes the figures and never what they are
 * called; what the asset is worth now, and its return, stand in the panel's own heading.
 */
const AssetSummaryCard = ({figures, period = null}) => (
    <Card sx={cardStyle}
          style={cardBoxStyle}>
        <CardContent>
            {period !== null &&
                <Typography sx={periodStyle} align={"center"}>{period}</Typography>
            }
            <Typography sx={labelStyle} color="text.secondary" align={"center"}>
                Return
            </Typography>
            <Typography variant="h5" component="div" align={"center"}
                        style={{color: returnColor(figures.totalReturn)}}>
                {signed(figures.totalReturn)}
            </Typography>
            <Typography sx={labelStyle} color="text.secondary" align={"center"}>
                Annual Return
            </Typography>
            {/* the money-weighted rate per year; none for a stretch shorter than a year */}
            <Typography align={"center"} style={{color: returnColor(figures.annualReturn ?? 0)}}
                        title="Money-weighted return per year: how fast the money grew while it was in">
                {figures.annualReturn === null || figures.annualReturn === undefined
                    ? "—"
                    : signed(figures.annualReturn) + " p.a."}
            </Typography>
            <Figure label="End Value">{formatAmount(figures.endValue)}</Figure>
            <Figure label="Start Value">{formatAmount(figures.startValue)}</Figure>
            <Figure label="Withdrawals">{formatAmount(figures.withdrawalsSum)}</Figure>
            <Figure label="Deposits">{formatAmount(figures.depositsSum)}</Figure>
        </CardContent>
    </Card>
);

AssetSummaryCard.propTypes = {
    figures: PropTypes.shape({
        totalReturn: PropTypes.number.isRequired,
        annualReturn: PropTypes.number,
        startValue: PropTypes.number.isRequired,
        endValue: PropTypes.number.isRequired,
        depositsSum: PropTypes.number.isRequired,
        withdrawalsSum: PropTypes.number.isRequired,
    }).isRequired,
    /** The stretch the figures are for, as "start – end"; null for an asset with no months. */
    period: PropTypes.string,
};

export default AssetSummaryCard;
