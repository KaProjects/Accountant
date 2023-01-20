import {useParams} from "react-router-dom";
import React, {useState} from "react";
import Loader from "./components/Loader";
import FinancialChart from "./components/FinancialChart";
import {Checkbox, Collapse, FormControlLabel, List, ListItem, ListItemText, ListSubheader} from "@mui/material";
import {ExpandLess, ExpandMore} from "@mui/icons-material";
import {useData} from "./fetch";


const FinancialAssets = props => {
    let { year } = useParams();

    const [chartFlags, setChartFlags] = useState([])
    const [chartOptions, setChartOptions] = useState([false])

    const {data, loaded, error} = useData("http://" + props.host + ":" + props.port + "/financial/assets/" + ((year === undefined) ? "" : year))

    function constructInitialFlags(data)
    {
        let flags = [data.groups.length];
        for (let g=0; g < data.groups.length; g++){
            flags[g] = Array(data.groups[g].accounts.length).fill(false)
        }
        return flags
    }

    function getChartFlag(gIndex, aIndex)
    {
        if (chartFlags[gIndex] === undefined){
            setChartFlags(constructInitialFlags(data))
        } else {
            return chartFlags[gIndex][aIndex]
        }
    }

    function toggleChart(gIndex, aIndex)
    {
        resetChartOptions(gIndex, aIndex)

        const newFlag = !getChartFlag(gIndex, aIndex)
        const newFlags = constructInitialFlags(data)
        newFlags[gIndex][aIndex] = newFlag
        setChartFlags(newFlags)
    }

    function resetChartOptions(gIndex, aIndex)
    {
        const account = data.groups[gIndex].accounts[aIndex]
        const showWithdrawals = account.balances[account.balances.length - 1] === 0
        setChartOptions([showWithdrawals])
    }

    function toggleChartOption(index)
    {
        const newOption = !chartOptions[index]
        const newOptions = {...chartOptions}
        newOptions[index] = newOption
        setChartOptions(newOptions)
    }

    function getTitleStyle(gIndex, aIndex) {
        let boxShadow = "0 0 8px 0";
        let background = getChartFlag(gIndex, aIndex) ? "#87befc" : "#b6d8ff";
        let color = "#3361bb";
        return {boxShadow: boxShadow, background: background, color: color, fontWeight: "bold"};
    }

    const constructChartData = (account) => {
        const data = [];
        data.push({
            valuation: account.initialValue,
            funding: account.initialValue,
            month: "0",
            deposits: 0,
            withdrawals: 0
        });
        for (let i = 0; i < account.balances.length; i++) {
            data.push({
                valuation: account.balances[i],
                funding: account.funding[i],
                month: account.labels[i],
                deposits: account.cumulativeDeposits[i],
                withdrawals: account.cumulativeWithdrawals[i]
            });
        }
        return data;
    };

    return (
        <>
        {!loaded &&
            <Loader error={error}/>
        }
        {loaded &&
            <List
                component="nav"
                aria-labelledby="nested-list-subheader"
            >
            {data.groups.map((group, gIndex) => (
                <List key={gIndex}
                    subheader={
                        <ListSubheader component="div" id="nested-list-subheader"
                                       style={{fontWeight: "bold", boxShadow: "0 0 8px 0", fontSize: "18px"}}>
                            {group.name}
                        </ListSubheader>
                    }
                    component="div" disablePadding
                >
                {group.accounts.map((account, aIndex) => (
                    <div key={aIndex}>
                        <ListItem  button onClick={() => toggleChart(gIndex, aIndex)} style={getTitleStyle(gIndex, aIndex)}>
                            <ListItemText primary={account.name} primaryTypographyProps={{ style: {fontWeight: "bold"} }}/>
                            {getChartFlag(gIndex, aIndex) ? <ExpandLess /> : <ExpandMore />}
                        </ListItem>

                        <Collapse in={getChartFlag(gIndex,aIndex)} timeout="auto" unmountOnExit>

                            <div style={{marginLeft: "50px"}}>
                                <FormControlLabel control={<Checkbox checked={chartOptions[0]} onChange={() => toggleChartOption(0)}/>} label="Decompose Funding" />
                            </div>

                            <FinancialChart data={constructChartData(account)}
                                            decomposedFunding={chartOptions[0]}
                                            width={year === "" || year === undefined ? "98%" : 700}/>


                         </Collapse>
                     </div>
                 ))}
                 </List>
             ))}
             </List>
        }
        </>
    )
}

export default FinancialAssets;