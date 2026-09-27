import PropTypes from "prop-types";
import {Card, CardContent, Typography} from "@mui/material";

const returnColor = (currentReturn) =>
    currentReturn > 0 ? "#158615" : currentReturn < 0 ? "#b93333" : "black";

const Figure = ({label, children}) => (
    <>
        <Typography sx={{fontSize: 14}} color="text.secondary" align={"center"}>
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

/** The headline figures for one asset account, beside its chart. */
const AssetSummaryCard = ({account}) => (
    <Card sx={{width: 150}}
          style={{backgroundColor: "white", display: "inline-block", verticalAlign: "middle", marginLeft: 10}}>
        <CardContent>
            <Typography sx={{fontSize: 14}} color="text.secondary" align={"center"}>
                Current Return
            </Typography>
            <Typography variant="h5" component="div" align={"center"}
                        style={{color: returnColor(account.currentReturn)}}>
                {account.currentReturn > 0 ? "+" : ""}{account.currentReturn}%
            </Typography>
            <Figure label="Current Value">{account.currentValue}</Figure>
            <Figure label="Initial Value">{account.initialValue}</Figure>
            <Figure label="Withdrawals">{account.withdrawalsSum}</Figure>
            <Figure label="Deposits">{account.depositsSum}</Figure>
        </CardContent>
    </Card>
);

AssetSummaryCard.propTypes = {
    account: PropTypes.object.isRequired,
};

export default AssetSummaryCard;
