import React from "react";
import {Area, CartesianGrid, ComposedChart, ResponsiveContainer, Tooltip, XAxis, YAxis} from "recharts";


const FinancialChart = props => {

    return (
        <ResponsiveContainer width={props.width} height={250}>
            <ComposedChart data={props.data} margin={{top: 10, right: 30, left: 20, bottom: 20}}>
                <XAxis dataKey="month" />
                <YAxis type="number" domain={([dataMin, dataMax]) => {
                    return [0, Math.round(dataMax/1000) * 1100];
                }}/>
                <Tooltip />
                <CartesianGrid strokeDasharray="3 3" />
                {props.decomposedFunding ?
                    <>
                        <Area type="linear" dataKey="deposits" stroke="red" fill="#eb5e5e" />
                        <Area type="linear" dataKey="valuation" stackId="1" stroke="#ffc658" fill="#ffc658" />
                        <Area type="linear" dataKey="withdrawals" stackId="1" stroke="green" fill="#2cc143" />
                    </>
                    :
                    <>
                        <Area type="linear" dataKey="funding" stroke="#8884d8" fill="#8884d8" />
                        <Area type="linear" dataKey="valuation" stroke="#ffc658" fill="#ffc658" />
                    </>
                }
            </ComposedChart>
        </ResponsiveContainer>
    )
}

export default FinancialChart;