import {useMemo, useState} from "react";
import PropTypes from "prop-types";
import {Box, Checkbox, Chip, FormControlLabel, Paper, Typography} from "@mui/material";
import FinancialChart from "../FinancialChart";
import AssetSummaryCard from "./AssetSummaryCard";
import {formatAmount} from "../../services/amount";
import {figuresBetween, isFullyWithdrawn, toAssetChartSeries} from "../../services/financialAssets";

const panelStyle = {padding: "12px 16px", marginBottom: "12px"};
// an asset sold off: greyed a little, so that opened among the ones still held it is not taken for one
const historicalStyle = {...panelStyle, backgroundColor: "#f7f7f7"};
const headerStyle = {display: "flex", alignItems: "center", gap: "12px", flexWrap: "wrap", marginBottom: "8px"};
const bodyStyle = {display: "flex", alignItems: "center", gap: "16px", flexWrap: "wrap"};
const chartStyle = {flex: "1 1 480px", minWidth: 0};

const returnColor = (currentReturn) => (currentReturn > 0 ? "success" : currentReturn < 0 ? "error" : "default");

/**
 * One asset, always open: its name and what it is worth now at the top, its headline figures
 * and its chart below.
 *
 * A slider under the chart narrows it to a stretch of the months, and the figures beside it are
 * worked out for that stretch: what the asset was worth at its start and end, what went in and
 * out in between, and the return over it - the header keeps the asset's figures as they are now.
 *
 * Its funding can be shown decomposed into deposits and withdrawals, which is how a fully
 * withdrawn asset opens - with nothing left in it, the split is the only thing worth seeing.
 */
const AssetPanel = ({account}) => {
    const [decomposed, setDecomposed] = useState(() => isFullyWithdrawn(account));
    // the points of the chart the slider picks: all of them, until it is moved
    const [range, setRange] = useState({from: 0, to: account.balances.length});
    // the same array on every render: the slider starts its drag over on new data
    const series = useMemo(() => toAssetChartSeries(account), [account]);

    return (
        <Paper variant="outlined" style={account.active ? panelStyle : historicalStyle} data-testid="asset-panel">
            <Box style={headerStyle}>
                <Typography variant="subtitle1" component="h3" sx={{fontWeight: 600}}>{account.name}</Typography>
                <Typography variant="caption" color="text.secondary">{account.id}</Typography>
                <Box sx={{flexGrow: 1}}/>
                <Typography variant="subtitle1">{formatAmount(account.currentValue)}</Typography>
                <Chip size="small" variant="outlined" color={returnColor(account.currentReturn)}
                      label={(account.currentReturn > 0 ? "+" : "") + account.currentReturn + "%"}/>
            </Box>
            <Box style={bodyStyle}>
                <AssetSummaryCard figures={figuresBetween(account, range.from, range.to)}
                                  period={series[range.from].month + " – " + series[range.to].month}/>
                <Box style={chartStyle}>
                    <FormControlLabel
                        control={<Checkbox size="small" checked={decomposed} onChange={() => setDecomposed(!decomposed)}/>}
                        label="Decompose Funding"
                    />
                    <FinancialChart data={series} decomposedFunding={decomposed} width="100%"
                                    onRangeChange={setRange}/>
                </Box>
            </Box>
        </Paper>
    );
};

AssetPanel.propTypes = {
    account: PropTypes.shape({
        id: PropTypes.string,
        name: PropTypes.string.isRequired,
        active: PropTypes.bool,
        balances: PropTypes.array.isRequired,
        labels: PropTypes.array.isRequired,
        currentValue: PropTypes.number,
        currentReturn: PropTypes.number,
    }).isRequired,
};

export default AssetPanel;
