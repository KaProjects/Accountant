import React from "react";
import {useParams} from "react-router-dom";
import {Table, TableBody, TableCell, TableContainer, TableHead, TableRow} from "@material-ui/core";
import Paper from '@mui/material/Paper';
import {IconButton} from "@mui/material";
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import KeyboardArrowUpIcon from '@mui/icons-material/KeyboardArrowUp';
import Loader from "./components/Loader";
import {useData} from "./fetch";


const Budgeting = props => {
    const { year } = useParams()

    const {data, loaded, error} = useData("http://" + props.host + ":" + props.port + "/budget/" + year)

    function getRowStyle(type, hasLeftBorder, hasRightBorder){
        const borderLeft = hasLeftBorder ? "2px solid" : "0px";
        const borderRight = hasRightBorder ? "2px solid" : "0px";

        let boxShadow = "0 0 8px 0";

        let background;
        let color;
        if (type === 'INCOME'){
            background = "#b4ffb4";
            color = "#017901";
        }
        if (type === 'INCOME_SUM'){
            background = "#67da67";
            color = "#017901";
        }
        if (type === 'EXPENSE'){
            background = "#fdc6c6";
            color = "#c42424";
        }
        if (type === 'EXPENSE_SUM'){
            background = "#fc9e9e";
            color = "#ab0000";
        }
        if (type === 'BALANCE' || type === 'OF_BUDGET_BALANCE'){
            background = "#7eb9ff";
            color = "#002e88";
        }
        if (type === 'OF_BUDGET'){
            background = "#b6d8ff";
            color = "#3361bb";
        }

        let borderTop;
        let borderBottom;
        if (type === 'EXPENSE_SUM' || type === 'INCOME_SUM' || type === 'BALANCE' || type === 'OF_BUDGET_BALANCE'){
            borderTop = "2px solid";
            borderBottom = "2px solid";
        } else {
            borderTop = "0px";
            borderBottom = "0px";
        }

        return {fontWeight: "bold", border: "0px", boxShadow: boxShadow, background: background, color: color, borderTop: borderTop, borderBottom: borderBottom, borderLeft: borderLeft, borderRight: borderRight};
    }

    function getPlannedRowStyle(type, hasBottomPadding, delta){
        let background;
        let color;
        let fontWeight;
        let boxShadow = "0 0 1px 0 #000"

        if (delta != null){
            fontWeight = "bold";
            if (type === 'OF_BUDGET' || type === 'OF_BUDGET_BALANCE') {
                color = "#000";
                background = "#fff";
            } else {
                if (delta > 0) {
                    if (type === 'INCOME' || type === 'INCOME_SUM' || type === 'BALANCE'){
                        color = "#017901";
                        background = "#f5fff5";
                    } else {
                        color = "#c42424";
                        background = "#fff2f2";
                    }
                } else if (delta < 0) {
                    if (type === 'INCOME' || type === 'INCOME_SUM' || type === 'BALANCE'){
                        color = "#c42424";
                        background = "#fff2f2";
                    } else {
                        color = "#017901";
                        background = "#f5fff5";
                    }
                } else {
                    color = "#002e88";
                    background = "#f8f8ff";
                }
            }
        } else {
            fontWeight = "normal"
            color = "#000";
            background = "#fff";
        }

        if (hasBottomPadding){
            return {fontWeight: fontWeight, background: background, color: color, border: "0px", boxShadow: boxShadow, paddingBottom: "10px"}
        } else {
            return {fontWeight: fontWeight, background: background, color: color, border: "0px", boxShadow: boxShadow}
        }
    }

    function getHeaderStyle(index) {
        const borderLeft = index === 13 ? "2px solid" : "0px";
        const borderRight = (index === 0 || index === 13 || index === 14) ? "2px solid" : "0px";
        const color = "#676767";
        return {boxShadow: "0 0 3px 0", border: "0px", textAlign: "center", borderBottom: "2px solid", borderLeft: borderLeft, borderRight: borderRight, borderColor: color};
    }

    function Row(props) {
        const { row } = props;
        const [subRowOpened, setSubRowOpened] = React.useState(false);
        const [deltasOpened, setDeltasOpened] = React.useState(false);

        return (
            <React.Fragment>
                <TableRow>
                    <TableCell style={getRowStyle(row.type, false, true)}>
                        <IconButton
                            aria-label="expand row"
                            style={{height: "2px", width: "10px"}}
                            onClick={() => setDeltasOpened(!deltasOpened)}
                        >
                            {deltasOpened ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
                        </IconButton>
                        {" " + row.name}
                        {(row.subRows.length !== 0) && <IconButton
                            aria-label="expand row"
                            style={{height: "2px", width: "25px"}}
                            onClick={() => setSubRowOpened(!subRowOpened)}
                        >
                            {subRowOpened ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
                        </IconButton>}
                    </TableCell>
                    {row.actual.map((month, index) => (
                        (index < row.lastFilledMonth)
                            ? <TableCell style={getRowStyle(row.type, false, false)} align="right">{month}</TableCell>
                            : <TableCell style={getPlannedRowStyle(row.type)} align="right">{row.planned[index]}</TableCell>

                    ))}
                    <TableCell style={getRowStyle(row.type, true, false)} align="right">{row.actualSum}</TableCell>
                    <TableCell style={getRowStyle(row.type, true, true)} align="right">{row.actualAvg}</TableCell>
                    <TableCell style={getPlannedRowStyle(row.type)} align="right">{row.plannedAvgToFilledMonth}</TableCell>
                    <TableCell style={getPlannedRowStyle(row.type, false, row.deltaAvg)} align="right">{row.deltaAvg}</TableCell>
                </TableRow>
                {subRowOpened && row.subRows.map((subrow) => (
                    <TableRow>
                        <TableCell component="th" scope="row">
                            {subrow.name}
                        </TableCell>
                        {subrow.actual.map((month, index) => (
                            (index < row.lastFilledMonth)
                                ? <TableCell align="right">{month}</TableCell>
                                : <TableCell align="right">{subrow.planned[index]}</TableCell>
                        ))}
                        <TableCell align="right">{subrow.actualSum}</TableCell>
                        <TableCell align="right">{subrow.actualAvg}</TableCell>
                        <TableCell align="right">{subrow.plannedAvgToFilledMonth}</TableCell>
                        <TableCell align="right">{subrow.deltaAvg}</TableCell>
                    </TableRow>
                ))}
                {deltasOpened &&
                    <>
                    <TableRow>
                        <TableCell style={getPlannedRowStyle(row.type)} component="th" scope="row">Planned</TableCell>
                        {row.planned.map((month, index) => (
                            (index < row.lastFilledMonth)
                            ? <TableCell style={getPlannedRowStyle(row.type)} align="right">{month}</TableCell>
                            : <TableCell style={getPlannedRowStyle(row.type)} align="right">-</TableCell>
                        ))}
                        <TableCell style={getPlannedRowStyle(row.type)} align="right">{row.plannedSumToFilledMonth}</TableCell>
                        <TableCell style={getPlannedRowStyle(row.type)} align="right">{row.plannedAvgToFilledMonth}</TableCell>
                        <TableCell style={getPlannedRowStyle(row.type)} align="right">-</TableCell>
                        <TableCell style={getPlannedRowStyle(row.type)} align="right">-</TableCell>
                    </TableRow>
                    <TableRow>
                    <TableCell style={getPlannedRowStyle(row.type, true)} component="th" scope="row">Difference</TableCell>
                {row.planned.map((month, index) => (
                    (index < row.lastFilledMonth)
                    ? <TableCell style={getPlannedRowStyle(row.type, true, row.actual[index] - month)} align="right">{row.actual[index] - month}</TableCell>
                    : <TableCell style={getPlannedRowStyle(row.type, true)} align="right">-</TableCell>
                    ))}
                    <TableCell style={getPlannedRowStyle(row.type, true, row.actualSum - row.plannedSumToFilledMonth)} align="right">{row.actualSum - row.plannedSumToFilledMonth}</TableCell>
                    <TableCell style={getPlannedRowStyle(row.type, true, row.actualAvg - row.plannedAvgToFilledMonth)} align="right">{row.actualAvg - row.plannedAvgToFilledMonth}</TableCell>
                    <TableCell style={getPlannedRowStyle(row.type, true)} align="right">-</TableCell>
                    <TableCell style={getPlannedRowStyle(row.type, true)} align="right">-</TableCell>
                    </TableRow>
                    </>
                }
            </React.Fragment>
        );
    }

    return (
    <>
    {!loaded &&
        <Loader error ={error}/>
    }
    {loaded &&
        <TableContainer component={Paper}>
            <Table sx={{ minWidth: 650 }} size="small" aria-label="a dense table">
                <TableHead>
                    <TableRow>
                        {data.columns.map((column, index) => (
                            <TableCell key={index} style={getHeaderStyle(index)}>{column}</TableCell>
                        ))}
                    </TableRow>
                </TableHead>
                <TableBody>
                    {data.rows.map((row, index) => (
                        <Row key={index} row={row}/>
                    ))}
                </TableBody>
            </Table>
        </TableContainer>
    }
    </>
    )
}

export default Budgeting;
