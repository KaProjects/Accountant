import {useData} from "../fetch";
import React, {useEffect} from "react";
import Loader from "../components/Loader";
import {FormControl, InputLabel, MenuItem, Select} from "@mui/material";
import {Bar, BarChart, Brush, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis} from "recharts";


const AccountingChart = props => {

    const {data, loaded, error} = useData("/chart/config")

    useEffect(() => {
        props.setYearly(false)
        // eslint-disable-next-line
    }, []);

    function Chart(){

        const {data, loaded, error} = useData("/chart/data/" + props.selectedValue.id)

        return (
            <React.Fragment>
                {!loaded &&
                    <Loader error ={error}/>
                }
                {loaded &&
                    <div style={{minHeight: "85vh", minWidth: "90vw"}}>
                        <ResponsiveContainer width="100%" height="100%">
                            <BarChart
                                data={data.values}
                                margin={{top: 5, right: 30, left: 30, bottom: 5}}
                            >
                                <CartesianGrid strokeDasharray="3 3" />
                                <XAxis dataKey="label" />
                                <YAxis />
                                <Tooltip />
                                <Bar dataKey={props.selectedValue.type.toLowerCase()} fill="#8884d8"/>
                                <Brush />
                            </BarChart>
                        </ResponsiveContainer>
                    </div>
                }
            </React.Fragment>
        )
    }

    return (
        <>
            {!loaded &&
                <Loader error ={error}/>
            }
            {loaded &&
                <div style={{display: "grid", placeContent: "center", position: "absolute", left:0,top:50,bottom:0,right:0}}>
                    {!props.selectedValue &&
                        <FormControl sx={{ minWidth: "200px"}}>
                            <InputLabel id="chart-select-label" >Select a dataset</InputLabel>
                            <Select
                                labelId="chart-select-label"
                                value={props.selectedValue}
                                label="chart-select-label"
                                onChange={event => {props.setSelectedValue(event.target.value);props.setSelectValues(data);}}
                            >
                                {data.map((value, index) => (
                                    <MenuItem key={index} value={value}>{value.name}</MenuItem>
                                ))}
                            </Select>
                        </FormControl>
                    }
                    {props.selectedValue && <Chart/>}
                </div>
            }
        </>
    )
}

export default AccountingChart;