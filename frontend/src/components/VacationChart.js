import React from "react";
import {PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer} from "recharts";


function getColor(total, index) {
    let saturation = 60
    let lightness = 60
    let alpha = 1.0
    let hue = index * Math.trunc(360 / total)

    return `hsla(${hue},${saturation}%,${lightness}%,${alpha})`
}

const renderCustomizedLabel = ({ cx, cy, midAngle, innerRadius, outerRadius, percent, index }) => {
    const radius = innerRadius + (outerRadius - innerRadius) * 0.5;
    const x = cx + radius * Math.cos(-midAngle * Math.PI / 180);
    const y = cy + radius * Math.sin(-midAngle * Math.PI / 180);

    return (
        <text x={x} y={y} fill="white" textAnchor={x > cx ? 'start' : 'end'} dominantBaseline="central">
            {`${(percent * 100).toFixed(0)}%`}
        </text>
    );
};

const VacationChart = props => {

    return (
        <ResponsiveContainer width={props.isBottom ? "100%" : 400} height={props.isBottom ? 300 : "90%"}>
        <PieChart width={400} height={400}>
            <Pie
                data={props.data}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={renderCustomizedLabel}
                outerRadius={120}
                fill="#8884d8"
                dataKey="value"
            >
                {props.data.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={getColor(props.data.length, index)} />
                ))}
            </Pie>
            <Tooltip />
            <Legend layout={props.isBottom ? "vertical" : "horizontal"}
                    verticalAlign={props.isBottom ? "middle" : "bottom"}
                    align={props.isBottom ? "left" : "center"}
                    payload={props.data.map((item, index) => ({
                        id: item.name,
                        type: "square",
                        value: `${item.name} (${item.value})`,
                        color: getColor(props.data.length, index)
                    })
                )
            }/>
        </PieChart>
        </ResponsiveContainer>
    )
}

export default VacationChart;