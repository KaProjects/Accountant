import React, {useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import {
    Checkbox,
    Collapse,
    FormControlLabel,
    List,
    ListItem,
    ListItemText,
    ListSubheader,
} from "@mui/material";
import {ExpandLess, ExpandMore} from "@mui/icons-material";
import {useData} from "../fetch";
import DataView from "../components/common/DataView";
import FinancialChart from "../components/FinancialChart";
import AssetSummaryCard from "../components/financial/AssetSummaryCard";
import {assetTitleStyle} from "../theme/tableStyles";
import {closedFlagsFor, isFullyWithdrawn, toAssetChartSeries} from "../services/financialAssets";
import {useAppState} from "../state/appState";

const subheaderStyle = {
    fontWeight: "bold", boxShadow: "0 0 8px 0", fontSize: "18px", fontFamily: "Copperplate",
};

const FinancialAssets = () => {
    const {year, setYearly} = useAppState();
    const {all} = useParams();
    const isOverall = all !== undefined;

    const [chartFlags, setChartFlags] = useState([])
    const [chartOptions, setChartOptions] = useState([false])

    const {data, loaded, error} = useData("/financial/assets/" + (isOverall ? "" : year))

    useEffect(() => {
        setYearly(!isOverall)
        // eslint-disable-next-line
    }, []);

    // Note: this initialises the flags during render when they are still empty,
    // which is how the original behaved. It would be better done from a state
    // initialiser, but that would change when the flags first appear.
    const isOpen = (gIndex, aIndex) => {
        if (chartFlags[gIndex] === undefined) {
            setChartFlags(closedFlagsFor(data.groups))
            return undefined
        }
        return chartFlags[gIndex][aIndex]
    }

    /** Opening an account closes every other one. */
    const toggleAccount = (gIndex, aIndex) => {
        const account = data.groups[gIndex].accounts[aIndex]
        setChartOptions([isFullyWithdrawn(account)])

        const opened = !isOpen(gIndex, aIndex)
        const flags = closedFlagsFor(data.groups)
        flags[gIndex][aIndex] = opened
        setChartFlags(flags)
    }

    const toggleChartOption = (index) => {
        const options = {...chartOptions}
        options[index] = !chartOptions[index]
        setChartOptions(options)
    }

    return (
        <DataView loaded={loaded} error={error}>
            {() => (
                <List component="nav" aria-labelledby="nested-list-subheader">
                    {data.groups.map((group, gIndex) => (
                        <List key={gIndex}
                              subheader={
                                  <ListSubheader component="div" id="nested-list-subheader" style={subheaderStyle}>
                                      {group.name}
                                  </ListSubheader>
                              }
                              component="div" disablePadding
                        >
                            {group.accounts.map((account, aIndex) => (
                                <div key={aIndex}>
                                    <ListItem button
                                              onClick={() => toggleAccount(gIndex, aIndex)}
                                              style={assetTitleStyle(isOpen(gIndex, aIndex))}>
                                        <ListItemText primary={account.name}
                                                      primaryTypographyProps={{style: {fontWeight: "bold", fontFamily: "Copperplate"}}}/>
                                        {isOpen(gIndex, aIndex) ? <ExpandLess/> : <ExpandMore/>}
                                    </ListItem>

                                    <Collapse in={isOpen(gIndex, aIndex)} timeout="auto" unmountOnExit>
                                        <AssetSummaryCard account={account}/>

                                        <div style={{display: "inline-block", verticalAlign: "middle", width: "85%", marginLeft: 10}}>
                                            <FormControlLabel
                                                control={<Checkbox checked={chartOptions[0]} onChange={() => toggleChartOption(0)}/>}
                                                label="Decompose Funding"
                                                style={{marginLeft: "50px"}}
                                            />
                                            <FinancialChart data={toAssetChartSeries(account)}
                                                            decomposedFunding={chartOptions[0]}
                                                            width={isOverall ? "100%" : 700}
                                            />
                                        </div>
                                    </Collapse>
                                </div>
                            ))}
                        </List>
                    ))}
                </List>
            )}
        </DataView>
    )
}


export default FinancialAssets;
