import PropTypes from "prop-types";
import React from "react";
import {Area, Brush, CartesianGrid, ComposedChart, ReferenceArea, ResponsiveContainer, Tooltip, XAxis, YAxis} from "recharts";
import {neutral} from "../theme/palette";
import {emptyStretches} from "../services/financialAssets";

const brushHeight = 24;
// the stretches the asset was worth nothing, tinted so that a gap in it stands out
const emptyFill = "#e53935";
const emptyOpacity = 0.12;

/**
 * An asset's valuation against what funded it, month by month, with a slider under it that picks
 * a stretch of the months - {from, to}, as indices of the chart's points - which the chart then
 * shows and is reported, for the panel to work its figures out for. Where the asset was worth
 * nothing for a while, the chart's background is tinted red.
 */
const FinancialChart = props => {

    return (
        <ResponsiveContainer width={props.width} height={280}>
            <ComposedChart data={props.data} margin={{top: 10, right: 30, left: 20, bottom: 10}}>
                <XAxis dataKey="month" />
                <YAxis type="number" domain={([dataMin, dataMax]) => {
                    return [0, Math.round(dataMax/1000) * 1100];
                }}/>
                <Tooltip />
                <CartesianGrid strokeDasharray="3 3" />
                {emptyStretches(props.data).map((stretch) => (
                    <ReferenceArea key={stretch.from + "-" + stretch.to} x1={stretch.from} x2={stretch.to}
                                   fill={emptyFill} fillOpacity={emptyOpacity} ifOverflow="hidden"/>
                ))}
                {props.decomposedFunding ?
                    <>
                        <Area type="linear" dataKey="deposits" stroke="red" fill="#eb5e5e" isAnimationActive={false} />
                        <Area type="linear" dataKey="valuation" stackId="1" stroke="#ffc658" fill="#ffc658" isAnimationActive={false} />
                        <Area type="linear" dataKey="withdrawals" stackId="1" stroke="green" fill="#2cc143" isAnimationActive={false} />
                    </>
                    :
                    <>
                        <Area type="linear" dataKey="funding" stroke="#8884d8" fill="#8884d8" isAnimationActive={false} />
                        <Area type="linear" dataKey="valuation" stroke="#ffc658" fill="#ffc658" isAnimationActive={false} />
                    </>
                }
                {/* The slider keeps its own position and only reports it: told where to be on
                    every render, while it reports moves that re-render the panel, it lost the drag
                    after the first step. For the same reason its data has to stay the same array
                    from one render to the next - see AssetPanel. */}
                {props.onRangeChange &&
                    <Brush
                        dataKey="month" height={brushHeight} stroke={neutral.headerBorder} travellerWidth={10}
                        onChange={({startIndex, endIndex}) => props.onRangeChange({from: startIndex, to: endIndex})}
                    />
                }
            </ComposedChart>
        </ResponsiveContainer>
    )
}

FinancialChart.propTypes = {
    data: PropTypes.array.isRequired,
    width: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
    /** Splits funding into deposits and withdrawals instead of showing it as one band. */
    decomposedFunding: PropTypes.bool,
    /** Told the stretch of points the slider picks, {from, to}; without it there is no slider. */
    onRangeChange: PropTypes.func,
};

export default FinancialChart;
