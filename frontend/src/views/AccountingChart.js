import React, {useEffect} from "react";
import {FormControl, InputLabel, MenuItem, Select} from "@mui/material";
import {useData} from "../fetch";
import DataView from "../components/common/DataView";
import AccountingBarChart from "../components/chart/AccountingBarChart";
import {getChartConfigStyle} from "../theme/palette";
import {useAppState} from "../state/appState";

const centered = {
    display: "grid", placeContent: "center", position: "absolute",
    left: 0, top: 50, bottom: 0, right: 0,
};

const AccountingChart = () => {
    const {selectedValue, setSelectedValue, setSelectValues, setYearly} = useAppState();

    const {data, loaded, error} = useData("/chart/config")

    useEffect(() => {
        setYearly(false)
        // eslint-disable-next-line
    }, []);

    return (
        <DataView loaded={loaded} error={error}>
            {() => (
                <div style={centered}>
                    {!selectedValue &&
                        <FormControl sx={{minWidth: "200px"}}>
                            <InputLabel id="chart-select-label">Select a dataset</InputLabel>
                            <Select
                                labelId="chart-select-label"
                                value={selectedValue}
                                label="chart-select-label"
                                onChange={event => {setSelectedValue(event.target.value);setSelectValues(data);}}
                            >
                                {data.map((value, index) => (
                                    <MenuItem key={index} value={value} style={getChartConfigStyle(value.id)}>{value.name}</MenuItem>
                                ))}
                            </Select>
                        </FormControl>
                    }
                    {selectedValue && <AccountingBarChart config={selectedValue}/>}
                </div>
            )}
        </DataView>
    )
}


export default AccountingChart;
