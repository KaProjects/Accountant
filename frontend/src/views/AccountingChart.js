import React, {useEffect} from "react";
import PropTypes from "prop-types";
import {FormControl, InputLabel, MenuItem, Select} from "@mui/material";
import {useData} from "../fetch";
import DataView from "../components/common/DataView";
import AccountingBarChart from "../components/chart/AccountingBarChart";
import {getChartConfigStyle} from "../theme/palette";

const centered = {
    display: "grid", placeContent: "center", position: "absolute",
    left: 0, top: 50, bottom: 0, right: 0,
};

const AccountingChart = props => {

    const {data, loaded, error} = useData("/chart/config")

    useEffect(() => {
        props.setYearly(false)
        // eslint-disable-next-line
    }, []);

    return (
        <DataView loaded={loaded} error={error}>
            {() => (
                <div style={centered}>
                    {!props.selectedValue &&
                        <FormControl sx={{minWidth: "200px"}}>
                            <InputLabel id="chart-select-label">Select a dataset</InputLabel>
                            <Select
                                labelId="chart-select-label"
                                value={props.selectedValue}
                                label="chart-select-label"
                                onChange={event => {props.setSelectedValue(event.target.value);props.setSelectValues(data);}}
                            >
                                {data.map((value, index) => (
                                    <MenuItem key={index} value={value} style={getChartConfigStyle(value.id)}>{value.name}</MenuItem>
                                ))}
                            </Select>
                        </FormControl>
                    }
                    {props.selectedValue && <AccountingBarChart config={props.selectedValue}/>}
                </div>
            )}
        </DataView>
    )
}

AccountingChart.propTypes = {
    setYearly: PropTypes.func.isRequired,
    selectedValue: PropTypes.object,
    setSelectedValue: PropTypes.func.isRequired,
    setSelectValues: PropTypes.func.isRequired,
}

export default AccountingChart;
