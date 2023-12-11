import React, {useEffect} from "react";
import {Table, TableBody, TableCell, TableContainer, TableHead, TableRow} from "@material-ui/core";
import Paper from '@mui/material/Paper';
import {IconButton} from "@mui/material";
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import KeyboardArrowUpIcon from '@mui/icons-material/KeyboardArrowUp';
import ReceiptLongIcon from '@mui/icons-material/ReceiptLong';
import Loader from "../components/Loader";
import {useData} from "../fetch";
import BudgetingTransactionsDialog from "../components/BudgetingTransactionsDialog";


const Budgeting = props => {

    const {data, loaded, error} = useData("/budget/" + props.year)

    useEffect(() => {
        props.setYearly(true)
    }, []);

    const [showTransactions, setShowTransactions] = React.useState(false);
    const [showTransactionsRowName, setShowTransactionsRowName] = React.useState(null);
    const [showTransactionsRowId, setShowTransactionsRowId] = React.useState(null);
    const [showTransactionsMonth, setShowTransactionsMonth] = React.useState(-1);
    const [showSubRow, setShowSubRow] = React.useState([]);
    const [showDeltas, setShowDeltas] = React.useState([]);

    const handleTransactionDialogClose = () => {
        setShowTransactions(false)
        setShowTransactionsValues(null, null, -1)
    }

    const setShowTransactionsValues = (rowName, rowId, month) => {
        if (rowId === "i" || rowId === "me" || rowId === "e" || rowId === "ntme" || rowId === "bcf" || rowId === "dcf") {
            setShowTransactionsValues(null, null, -1)
        } else {
            setShowTransactionsRowName(rowName)
            setShowTransactionsRowId(rowId)
            setShowTransactionsMonth(month)
        }
    }

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
        const {row, id } = props;

        const handleShowDeltas = (id) => {
            const newFlag = !showDeltas[id]
            const newFlags =showDeltas.slice()
            newFlags[id] = newFlag
            setShowDeltas(newFlags)
        }

        const handleShowSubrow = (id) => {
            const newFlag = !showSubRow[id]
            const newFlags =showSubRow.slice()
            newFlags[id] = newFlag
            setShowSubRow(newFlags)
        }

        return (
            <React.Fragment>
                <TableRow key={id}>
                    <TableCell style={getRowStyle(row.type, false, true)} key={-1}>
                        <IconButton
                            aria-label="expand row"
                            style={{height: "2px", width: "10px"}}
                            onClick={() => handleShowDeltas(id)}
                        >
                            {showDeltas[id] ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
                        </IconButton>
                        {" " + row.name}
                        {(row.subRows.length !== 0) && <IconButton
                            aria-label="expand row"
                            style={{height: "2px", width: "25px"}}
                            onClick={() => handleShowSubrow(id)}
                        >
                            {showSubRow[id] ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
                        </IconButton>}
                    </TableCell>
                    {row.actual.map((month, index) => (
                        (index < row.lastFilledMonth)
                            ? <TableCell
                                style={getRowStyle(row.type, false, false)} align="right" key={index}
                                onClick={() => {if (row.subRows.length === 0) setShowTransactionsValues(row.name, row.id, index + 1)}}
                                onMouseLeave={() => setShowTransactionsValues(null, null, -1)}
                              >
                                {month}
                                {row.id === showTransactionsRowId && index + 1 === showTransactionsMonth &&
                                    <IconButton
                                        style={{height: "2px", width: "25px"}}
                                        onClick={(event) => {setShowTransactions(true);event.stopPropagation();}}
                                    >
                                        <ReceiptLongIcon sx={{width: 18}}/>
                                    </IconButton>
                                }
                              </TableCell>
                            : <TableCell style={getPlannedRowStyle(row.type)} align="right" key={index}>{row.planned[index]}</TableCell>

                    ))}
                    <TableCell style={getRowStyle(row.type, true, false)} align="right" key={12}>{row.actualSum}</TableCell>
                    <TableCell style={getRowStyle(row.type, true, true)} align="right" key={13}>{row.actualAvg}</TableCell>
                    <TableCell style={getPlannedRowStyle(row.type)} align="right" key={14}>{row.plannedAvgToFilledMonth}</TableCell>
                    <TableCell style={getPlannedRowStyle(row.type, false, row.deltaAvg)} align="right" key={15}>{row.deltaAvg}</TableCell>
                </TableRow>
                {showSubRow[id] && row.subRows.map((subrow, index) => (
                    <TableRow key={id + "s" + index}>
                        <TableCell component="th" scope="row" key={-1}>
                            {subrow.name}
                        </TableCell>
                        {subrow.actual.map((month, index) => (
                            (index < row.lastFilledMonth)
                                ? <TableCell
                                    align="right" key={index}
                                    onClick={() => setShowTransactionsValues(subrow.name, subrow.id, index + 1)}
                                    onMouseLeave={() => setShowTransactionsValues(null, null, -1)}
                                >
                                    {month}
                                    {subrow.id === showTransactionsRowId && index + 1 === showTransactionsMonth &&
                                        <IconButton
                                            style={{height: "2px", width: "25px"}}
                                            onClick={(event) => {setShowTransactions(true);event.stopPropagation();}}
                                        >
                                            <ReceiptLongIcon sx={{width: 18}}/>
                                        </IconButton>
                                    }
                                </TableCell>
                                : <TableCell align="right" key={index}>{subrow.planned[index]}</TableCell>
                        ))}
                        <TableCell align="right" key={12}>{subrow.actualSum}</TableCell>
                        <TableCell align="right" key={13}>{subrow.actualAvg}</TableCell>
                        <TableCell align="right" key={14}>{subrow.plannedAvgToFilledMonth}</TableCell>
                        <TableCell align="right" key={15}>{subrow.deltaAvg}</TableCell>
                    </TableRow>
                ))}
                {showDeltas[id] &&
                    <>
                        <TableRow key={id + "dp"}>
                            <TableCell style={getPlannedRowStyle(row.type)} component="th" scope="row" key={-1}>Planned</TableCell>
                            {row.planned.map((month, index) => (
                                (index < row.lastFilledMonth)
                                    ? <TableCell style={getPlannedRowStyle(row.type)} align="right" key={index}>{month}</TableCell>
                                    : <TableCell style={getPlannedRowStyle(row.type)} align="right" key={index}>-</TableCell>
                            ))}
                            <TableCell style={getPlannedRowStyle(row.type)} align="right" key={12}>{row.plannedSumToFilledMonth}</TableCell>
                            <TableCell style={getPlannedRowStyle(row.type)} align="right" key={13}>{row.plannedAvgToFilledMonth}</TableCell>
                            <TableCell style={getPlannedRowStyle(row.type)} align="right" key={14}>-</TableCell>
                            <TableCell style={getPlannedRowStyle(row.type)} align="right" key={15}>-</TableCell>
                        </TableRow>
                        <TableRow key={id + "dd"}>
                            <TableCell style={getPlannedRowStyle(row.type, true)} component="th" scope="row" key={-1}>Difference</TableCell>
                            {row.planned.map((month, index) => (
                                (index < row.lastFilledMonth)
                                    ? <TableCell style={getPlannedRowStyle(row.type, true, row.actual[index] - month)} align="right" key={index}>{row.actual[index] - month}</TableCell>
                                    : <TableCell style={getPlannedRowStyle(row.type, true)} align="right" key={index}>-</TableCell>
                            ))}
                            <TableCell style={getPlannedRowStyle(row.type, true, row.actualSum - row.plannedSumToFilledMonth)} align="right" key={12}>{row.actualSum - row.plannedSumToFilledMonth}</TableCell>
                            <TableCell style={getPlannedRowStyle(row.type, true, row.actualAvg - row.plannedAvgToFilledMonth)} align="right" key={13}>{row.actualAvg - row.plannedAvgToFilledMonth}</TableCell>
                            <TableCell style={getPlannedRowStyle(row.type, true)} align="right" key={14}>-</TableCell>
                            <TableCell style={getPlannedRowStyle(row.type, true)} align="right" key={15}>-</TableCell>
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
        <>
        <TableContainer component={Paper}>
            <Table sx={{ minWidth: 650 }} size="small" aria-label="a dense table">
                <TableHead>
                    <TableRow key={-1}>
                        {data.columns.map((column, index) => (
                            <TableCell key={index} style={getHeaderStyle(index)}>{column}</TableCell>
                        ))}
                    </TableRow>
                </TableHead>
                <TableBody>
                    {data.rows.map((row, index) => (
                        <Row key={index} row={row} id={index}/>
                    ))}
                </TableBody>
            </Table>
        </TableContainer>
        <BudgetingTransactionsDialog
            open={showTransactions}
            onClose={handleTransactionDialogClose}
            year={props.year}
            row={showTransactionsRowName}
            rowId={showTransactionsRowId}
            month={showTransactionsMonth}
        />
        </>
    }
    </>
    )
}

export default Budgeting;
