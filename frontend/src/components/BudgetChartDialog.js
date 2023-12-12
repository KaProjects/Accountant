import React from "react";
import {Area, Bar, CartesianGrid, ComposedChart, LabelList, Tooltip, XAxis, YAxis} from "recharts";
import {Card, CardContent, Dialog, DialogTitle, Typography} from "@mui/material";


const BudgetChartDialog = props => {

    const CustomTooltip = ({ active, payload, label }) => {
        if (active && payload && payload.length) {
            const planned = payload[0].value
            const sign = (payload[0].value === payload[1].value) ? "+" : "-"
            return (
                <Card>
                    <CardContent>
                        <Typography variant="body2" component="div">
                            month: {label}
                        </Typography>
                        <Typography variant="body2" component="div" style={{ color:  payload[0].fill}}>
                            planned: {planned}
                        </Typography>
                        {payload[2].value > 0 &&
                            <Typography variant="body2" component="div" style={{ color:  payload[2].fill}}>
                                difference: {sign}{payload[2].value}
                            </Typography>
                        }
                        {payload[3].value > 0 &&
                            <Typography variant="body2" component="div" style={{ color:  payload[3].fill}}>
                                difference: {sign}{payload[3].value}
                            </Typography>
                        }
                    </CardContent>
                </Card>
            );
        }
        return null;
    };

    const BarLabel = (props) => {
        const {value, fill, x, y, width, height, offset, sign} = props;
        if (value > 0) {
            return (
                <text x={x + width / 2} y={y - 10} textAnchor="middle" dominantBaseline="middle" fill={fill} offset={offset} fontSize={13}>
                    {sign}{value}
                </text>
            );
        } else {
            return <text></text>;
        }
    };

    return (
        <Dialog
            open={props.open}
            onClose={props.onClose}
            fullWidth={false}
            maxWidth={'lg'}
        >
            <DialogTitle>Budget Difference for {props.name}</DialogTitle>

            <ComposedChart
                width={800}
                height={300}
                data={props.data}
                margin={{top: 20, right: 20, bottom: 20, left: 20}}
            >
                <CartesianGrid stroke="#f5f5f5" />
                <XAxis dataKey="name"/>
                <YAxis />
                <Area dataKey="planned" fill="#8884d8" stroke="#8884d8" type="step"/>
                <Bar dataKey="base" stackId="a" fill="#8884d8" />
                <Bar dataKey="deficit" stackId="a" fill="#eb5e5e">
                    <LabelList dataKey="deficit" content={<BarLabel fill="#eb5e5e" sign={props.isExpense ? "+" : "-"}/>}/>
                </Bar>
                <Bar dataKey="surplus" stackId="a" fill="#2cc143">
                    <LabelList dataKey="surplus" content={<BarLabel fill="#2cc143" sign={props.isExpense ? "-" : "+"}/>} />
                </Bar>
                <Tooltip content={<CustomTooltip />} cursor={{ fill: "transparent" }}/>
            </ComposedChart>
        </Dialog>
    )
}

export default BudgetChartDialog;