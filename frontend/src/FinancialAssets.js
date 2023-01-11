import {useParams} from "react-router-dom";
import React, {useCallback, useEffect, useState} from "react";
import Loader from "./components/Loader";
import axios from "axios";
import FinancialChart from "./components/FinancialChart";
import {Collapse, List, ListItem, ListItemText, ListSubheader} from "@mui/material";
import {ExpandLess, ExpandMore} from "@mui/icons-material";


const FinancialAssets = props => {
    let { year } = useParams();

    const [loaded, setLoaded] = useState(false)
    const [error, setError] = useState(null)
    const [data, setData] = useState(null)
    const [chartFlags, setChartFlags] = useState([])

    const loadData = useCallback((props, year) => {
        axios.get("http://" + props.host + ":" + props.port + "/financial/assets/" + year).then(
            (response) => {
                setData(response.data);
                setChartFlags(constructInitialFlags(response.data))
                setError(null)
                setLoaded(true);
            }).catch((error) => {
            console.error(error)
            setError(error)
        })
    }, [])

    useEffect(() => {
        if (year === undefined) year = "";
        loadData(props, year);
    }, [loadData, props, year]);

    function constructInitialFlags(data){
        let flags = [data.groups.length];
        for (let g=0; g < data.groups.length; g++){
            flags[g] = Array(data.groups[g].accounts.length).fill(false)
        }
        return flags
    }

    function toggleChart(gIndex, aIndex){
        const newFlag = !chartFlags[gIndex][aIndex]
        const newFlags = constructInitialFlags(data)
        newFlags[gIndex][aIndex] = newFlag
        setChartFlags(newFlags)
    }

    // function getHeaderStyle(index) {
    //     let width = null
    //     if (index <= 1) {
    //         width = "60px"
    //     } else if (index <= 3) {
    //         width = "300px"
    //     }
    //
    //     return {width: width, fontWeight: "bold"}
    // }

    // function getTitleStyle(index) {
    //     let boxShadow = "0 0 8px 0";
    //     let background = transactionFlags[index] ? "#87befc" : "#b6d8ff";
    //     let color = "#3361bb";
    //     return {boxShadow: boxShadow, background: background, color: color};
    // }

    const constructChartData = (account) => {
        const data = [];
        for (let i = 0; i < account.fundingSequence.length; i++) {
            data.push({ valuation: account.valuationSequence[i], funding: account.fundingSequence[i], month: account.labels[i] });
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
                        <ListSubheader component="div" id="nested-list-subheader">
                            {group.name}
                        </ListSubheader>
                    }
                    component="div" disablePadding
                >
                {group.accounts.map((account, aIndex) => (
                    <div key={aIndex}>
                        <ListItem  button onClick={() => toggleChart(gIndex, aIndex)}>
                            <ListItemText primary={account.name} primaryTypographyProps={{ style: {fontWeight: "bold"} }}/>
                            {chartFlags[gIndex][aIndex] ? <ExpandLess /> : <ExpandMore />}
                        </ListItem>
                        <Collapse in={chartFlags[gIndex][aIndex]} timeout="auto" unmountOnExit>


                            <FinancialChart data={constructChartData(account)}/>


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