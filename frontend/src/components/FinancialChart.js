import React from "react";
import {Area, CartesianGrid, ComposedChart, Tooltip, XAxis, YAxis} from "recharts";


const FinancialChart = props => {

    return (
        <ComposedChart width={1000} height={250} data={props.data} margin={{top: 10, right: 30, left: 20, bottom: 0}}>
            <XAxis dataKey="month" />
                 <YAxis type="number" domain={([dataMin, dataMax]) => {
                     return [Math.round(dataMin/1000) * 900, Math.round(dataMax/1000) * 1100];
                 }}/>
            <Tooltip />
            <CartesianGrid strokeDasharray="3 3" />
            <Area type="linear" dataKey="funding" stroke="#8884d8" fill="#8884d8" />
            <Area type="linear" dataKey="valuation" stroke="#ffc658" fill="#ffc658" />
        </ComposedChart>
    )
}

export default FinancialChart;