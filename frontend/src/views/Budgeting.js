import React, {useEffect} from "react";
import {Table, TableBody, TableCell, TableContainer, TableHead, TableRow} from "@material-ui/core";
import Paper from '@mui/material/Paper';
import {IconButton} from "@mui/material";
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import KeyboardArrowUpIcon from '@mui/icons-material/KeyboardArrowUp';
import ReceiptLongIcon from '@mui/icons-material/ReceiptLong';
import Loader from "../components/Loader";
import {useData} from "../fetch";
import TransactionsDialog from "../components/TransactionsDialog";
import BudgetChartDialog from "../components/BudgetChartDialog";
import BarChartIcon from '@mui/icons-material/BarChart';

const Budgeting = props => {

    const {data, loaded, error} = useData("/budget/" + props.year)

    useEffect(() => {
        props.setYearly(true)
    }, []);

    const [showSubRow, setShowSubRow] = React.useState([]);
    const [showDeltas, setShowDeltas] = React.useState([]);

    const [showTransactionsDialog, setShowTransactionsDialog] = React.useState(false);
    const [transactionsDialogRowName, setTransactionsDialogRowName] = React.useState(null);
    const [transactionsDialogRowId, setTransactionsDialogRowId] = React.useState(null);
    const [transactionsDialogMonth, setTransactionsDialogMonth] = React.useState(-1);

    const handleTransactionsDialogClose = () => {
        setShowTransactionsDialog(false)
        setTransactionsDialogProps(null, null, -1)
    }

    const setTransactionsDialogProps = (rowName, rowId, month) => {
        if (rowId === "i" || rowId === "me" || rowId === "e" || rowId === "ntme" || rowId === "bcf" || rowId === "dcf") {
            setTransactionsDialogProps(null, null, -1)
        } else {
            setTransactionsDialogRowName(rowName)
            setTransactionsDialogRowId(rowId)
            setTransactionsDialogMonth(month)
        }
    }

    const [showBudgetChartDialog, setShowBudgetChartDialog] = React.useState(false);
    const [budgetChartDialogData, setBudgetChartDialogData] = React.useState(null);
    const [budgetChartDialogName, setBudgetChartDialogName] = React.useState("");
    const [budgetChartDialogIsExpense, setBudgetChartDialogIsExpense] = React.useState(false);

    const handleBudgetChartDialogClose = () => {
        setShowBudgetChartDialog(false)
        computeBudgetChartDialogProps(null)
    }

    const computeBudgetChartDialogProps = (row) => {
        if (row === null) {
            setBudgetChartDialogData(null)
            setBudgetChartDialogName("")
        } else {
            setBudgetChartDialogName(row.name)
            setBudgetChartDialogIsExpense(row.type === 'EXPENSE_SUM' || row.type === 'EXPENSE')

            let data = [12]
            for (let i=0; i < 12; i++){
                let base = 0
                let deficit = 0
                let surplus = 0

                if (i < row.lastFilledMonth){
                    if (row.actual[i] > row.planned[i]) {
                        base = row.planned[i]
                        if (row.type === 'EXPENSE_SUM' || row.type === 'EXPENSE'){
                            deficit = row.actual[i] - row.planned[i]
                        } else {
                            surplus = row.actual[i] - row.planned[i]
                        }
                    } else {
                        base = row.actual[i]
                        if (row.type === 'EXPENSE_SUM' || row.type === 'EXPENSE'){
                            surplus = row.planned[i] - row.actual[i]
                        } else {
                            deficit = row.planned[i] - row.actual[i]
                        }
                    }
                }

                data[i] =  {name: i + 1, planned: row.planned[i], base: base, deficit: deficit, surplus: surplus}
            }
            setBudgetChartDialogData(data)
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

        return {fontWeight: "bold", background: background, color: color,
            border: "0px", boxShadow: boxShadow, borderTop: borderTop, borderBottom: borderBottom, borderLeft: borderLeft, borderRight: borderRight};
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
        let paddingBottom = hasBottomPadding ? "10px" : null
        return {fontWeight: fontWeight, background: background, color: color, border: "0px", boxShadow: boxShadow, paddingBottom: paddingBottom}
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
                                onClick={() => {if (row.subRows.length === 0) setTransactionsDialogProps(row.name, row.id, index + 1)}}
                                onMouseLeave={() => setTransactionsDialogProps(null, null, -1)}
                              >
                                {month}
                                {row.id === transactionsDialogRowId && index + 1 === transactionsDialogMonth &&
                                    <IconButton
                                        style={{height: "2px", width: "25px"}}
                                        onClick={() => setShowTransactionsDialog(true)}
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
                                    onClick={() => setTransactionsDialogProps(subrow.name, subrow.id, index + 1)}
                                    onMouseLeave={() => setTransactionsDialogProps(null, null, -1)}
                                >
                                    {month}
                                    {subrow.id === transactionsDialogRowId && index + 1 === transactionsDialogMonth &&
                                        <IconButton
                                            style={{height: "2px", width: "25px"}}
                                            onClick={() => setShowTransactionsDialog(true)}
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
                            <TableCell style={getPlannedRowStyle(row.type, true)} component="th" scope="row" key={-1}
                                       onMouseEnter={() => computeBudgetChartDialogProps(row)}
                                       onMouseLeave={() => computeBudgetChartDialogProps(null)}
                            >
                                Difference
                                {budgetChartDialogData !== null && budgetChartDialogName === row.name &&
                                    <IconButton
                                        style={{height: "2px", width: "25px"}}
                                        onClick={() => setShowBudgetChartDialog(true)}
                                    >
                                        <BarChartIcon sx={{width: 18}}/>
                                    </IconButton>
                                }
                            </TableCell>
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
        <TransactionsDialog
            open={showTransactionsDialog}
            onClose={handleTransactionsDialogClose}
            year={props.year}
            row={transactionsDialogRowName}
            rowId={transactionsDialogRowId}
            month={transactionsDialogMonth}
            type="BUDGET"
        />
        <BudgetChartDialog
            open={showBudgetChartDialog}
            onClose={handleBudgetChartDialogClose}
            data={budgetChartDialogData}
            name={budgetChartDialogName}
            isExpense={budgetChartDialogIsExpense}
        />
        </>
    }
    </>
    )
}

export default Budgeting;
