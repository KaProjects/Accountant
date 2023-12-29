import React, {useEffect} from "react";
import {useData} from "../fetch";
import Loader from "../components/Loader";
import Paper from "@mui/material/Paper";
import {IconButton, Table, TableBody, TableCell, TableContainer, TableHead, TableRow} from "@mui/material";
import ReceiptLongIcon from "@mui/icons-material/ReceiptLong";
import TransactionsDialog from "../components/TransactionsDialog";
import {useParams} from "react-router-dom";
import LaunchIcon from '@mui/icons-material/Launch';

const AccountingStatement = props => {
    let { type } = useParams();
    let { overall } = useParams();

    const {data, loaded, error} = useData("/accounting/" + type + "/" + (overall === undefined ? props.year : ""))

    useEffect(() => {
        props.setYearly(overall === undefined)
        // eslint-disable-next-line
    }, []);

    const [showChildren, setShowChildren] = React.useState([]);
    const [showGrandChild, setShowGrandChild] = React.useState(null);

    const [showTransactionsDialog, setShowTransactionsDialog] = React.useState(false);
    const [transactionsDialogRowName, setTransactionsDialogRowName] = React.useState(null);
    const [transactionsDialogRowId, setTransactionsDialogRowId] = React.useState(null);
    const [transactionsDialogMonth, setTransactionsDialogMonth] = React.useState(-1);

    const handleTransactionsDialogClose = () => {
        setShowTransactionsDialog(false)
        setTransactionsDialogProps(null, null, -1)
    }

    const setTransactionsDialogProps = (rowName, rowId, month) => {
        setTransactionsDialogRowName(rowName)
        setTransactionsDialogRowId(rowId)
        setTransactionsDialogMonth(month)
    }

    const [redirectYearIndex, setRedirectYearIndex] = React.useState(-1);

    const redirectToYear = () => {
        sessionStorage.setItem('year', data.columns[redirectYearIndex])
        window.location.href='/accounting/' + type
    }

    function getHeaderStyle(index) {
        const borderLeft = (index === 0 || (index === data.columns.length - 1 && hasTotal())) ? "2px solid" : "0px";
        const borderRight = (index === 0 || (hasInitial() && index === 1) || index === data.columns.length - 1) ? "2px solid" : "0px";
        const color = "#676767";
        return {boxShadow: "0 0 3px 0", border: "0px", textAlign: "center", borderBottom: "2px solid", borderLeft: borderLeft, borderRight: borderRight, borderColor: color};
    }

    function getRowStyle(type, hasLeftBorder, hasRightBorder){
        const borderLeft = hasLeftBorder ? "2px solid" : "0px";
        const borderRight = hasRightBorder ? "2px solid" : "0px";

        let boxShadow = "0 0 8px 0";

        let background;
        let color;
        if (type === 'INCOME_ACCOUNT'){
            background = "#ffffff";
            color = "#227222";
        }
        if (type === 'INCOME_GROUP'){
            background = "#67da67";
            color = "#017901";
        }
        if (type === 'EXPENSE_ACCOUNT'){
            background = "#ffffff";
            color = "#a13c3c";
        }
        if (type === 'EXPENSE_GROUP'){
            background = "#fc9e9e";
            color = "#a62d2d";
        }
        if (type === 'PROFIT_SUMMARY'){
            background = "#8bbefa";
            color = "#22468d";
        }
        if (type === 'CASH_FLOW_ACCOUNT'){
            background = "#ffffff";
            color = "#721c67";
        }
        if (type === 'CASH_FLOW_GROUP'){
            background = "#ab75a5";
            color = "#620155";
        }
        if (type === 'CASH_FLOW_SUMMARY'){
            background = "#983f8d";
            color = "#3a0032";
        }
        if (type === 'BALANCE_SUMMARY'){
            background = "#cdf8f3";
            color = "#3fab9e";
        }
        if (type === 'BALANCE_CLASS'){
            background = "#f3e2ac";
            color = "#544209";
        }
        if (type === 'BALANCE_GROUP'){
            background = "#fdfac4";
            color = "#65612a";
        }
        if (type === 'BALANCE_ACCOUNT'){
            background = "#fdfcf3";
            color = "#797746";
        }

        let borderTop;
        let borderBottom;
        let fontWeight;
        if (type === 'INCOME_GROUP' || type === 'EXPENSE_GROUP' || type === 'PROFIT_SUMMARY'
            || type === 'CASH_FLOW_SUMMARY' || type === 'CASH_FLOW_GROUP'
            || type === 'BALANCE_SUMMARY' || type === 'BALANCE_CLASS'){
            borderTop = "2px solid";
            borderBottom = "2px solid";
            fontWeight = "bold";
        } else {
            borderTop = "0px";
            borderBottom = "0px";
            fontWeight = "normal";
        }
        return {fontFamily: "Monaco", fontWeight: fontWeight, background: background, color: color,
            border: "0px", boxShadow: boxShadow, borderTop: borderTop, borderBottom: borderBottom, borderLeft: borderLeft, borderRight: borderRight};
    }

    function hasInitial() {
        return data.columns[1] === "Initial"
    }

    function hasTotal() {
        return data.columns[data.columns.length - 1] === "Total"
    }

    function Row(props) {
        const {row, id } = props;

        const handleShowChildren = (id) => {
            const newFlag = !showChildren[id]
            const newFlags = showChildren.slice()
            newFlags[id] = newFlag
            setShowChildren(newFlags)
        }

        const handleShowGrandChildren = (id) => {
            if (showGrandChild === id){
                setShowGrandChild(null)
            } else {
                setShowGrandChild(id)
            }
        }

        return (
            <React.Fragment>
                <TableRow key={id} onClick={() => handleShowChildren(id)}>
                    <TableCell key={-1} style={getRowStyle(row.type, true, true)}>
                        {" " + row.name}
                    </TableCell>
                    {hasInitial() &&
                        <TableCell key={-2} align="right" style={getRowStyle(row.type, false, true)}>
                            {row.initial}
                        </TableCell>
                    }
                    {overall === undefined && row.monthlyValues.map((month, index) => (
                        <TableCell
                            align="right" key={index}
                            style={getRowStyle(row.type, false, row.monthlyValues.length -1 === index)}
                        >
                            {month}
                        </TableCell>
                    ))}
                    {overall !== undefined && row.yearlyValues.map((year, index) => (
                        <TableCell
                            align="right" key={index}
                            style={getRowStyle(row.type, false, row.yearlyValues.length -1 === index)}
                        >
                            {year}
                        </TableCell>
                    ))}
                    {hasTotal() &&
                        <TableCell
                            align="right" key={-3}
                            style={getRowStyle(row.type, false, true)}
                        >
                            {row.total}
                        </TableCell>
                    }
                </TableRow>
                {showChildren[id] && row.children.map((child, index) => (
                    <React.Fragment key={child.schemaId + "f" + index}>
                    <TableRow key={child.schemaId + "x" + index} onClick={() => handleShowGrandChildren(child.schemaId)}>
                        <TableCell component="th" scope="row" key={-1} style={getRowStyle(child.type, true, true)}>
                            {child.name}
                        </TableCell>
                        {hasInitial() &&
                            <TableCell key={-2} align="right" style={getRowStyle(child.type, false, true)}>
                                {child.initial}
                            </TableCell>
                        }
                        {child.monthlyValues.map((month, index) => (
                            <TableCell
                                align="right" key={index}
                                style={getRowStyle(child.type, false, child.monthlyValues.length -1 === index)}
                                onClick={() => setTransactionsDialogProps(child.name, child.schemaId, index + 1)}
                                onMouseLeave={() => setTransactionsDialogProps(null, null, -1)}
                            >
                                {month}
                                {child.children.length === 0 && child.schemaId === transactionsDialogRowId && index + 1 === transactionsDialogMonth &&
                                    <IconButton
                                        style={{height: "2px", width: "25px", color: getRowStyle(child.type).color}}
                                        onClick={() => setShowTransactionsDialog(true)}
                                    >
                                        <ReceiptLongIcon sx={{width: 18}}/>
                                    </IconButton>
                                }
                            </TableCell>
                        ))}
                        {hasTotal() &&
                            <TableCell align="right" key={-3} style={getRowStyle(child.type, true, true)}>{child.total}</TableCell>
                        }
                    </TableRow>
                    {type === "balance" && (showGrandChild === child.schemaId) && child.children.map((grandchild, index) => (
                        <TableRow key={grandchild.schemaId + "x" + index}>
                            <TableCell component="th" scope="row" key={-1} style={getRowStyle(grandchild.type, true, true)}>
                                {grandchild.name}
                            </TableCell>
                            <TableCell key={-2} align="right" style={getRowStyle(grandchild.type, false, true)}>
                                {grandchild.initial}
                            </TableCell>
                            {grandchild.monthlyValues.map((month, index) => (
                                <TableCell
                                    align="right" key={index}
                                    style={getRowStyle(grandchild.type, false, grandchild.monthlyValues.length -1 === index)}
                                    onClick={() => setTransactionsDialogProps(grandchild.name, grandchild.schemaId, index + 1)}
                                    onMouseLeave={() => setTransactionsDialogProps(null, null, -1)}
                                >
                                    {month}
                                    {grandchild.children.length === 0 && grandchild.schemaId === transactionsDialogRowId && index + 1 === transactionsDialogMonth &&
                                        <IconButton
                                            style={{height: "2px", width: "25px", color: getRowStyle(grandchild.type).color}}
                                            onClick={() => setShowTransactionsDialog(true)}
                                        >
                                            <ReceiptLongIcon sx={{width: 18}}/>
                                        </IconButton>
                                    }
                                </TableCell>
                            ))}
                            <TableCell align="right" key={-3} style={getRowStyle(grandchild.type, true, true)}>{grandchild.total}</TableCell>
                        </TableRow>
                    ))}
                    </React.Fragment>
                ))}
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
                                    <TableCell key={index}
                                               style={getHeaderStyle(index)}
                                               onClick={() => {if (index !== 0) setRedirectYearIndex(index)}}
                                               onMouseLeave={() => setRedirectYearIndex(-1)}
                                    >
                                        {column}
                                        {overall !== undefined && index === redirectYearIndex &&
                                            <IconButton
                                                style={{height: "2px", width: "25px"}}
                                                onClick={() => redirectToYear()}
                                            >
                                                <LaunchIcon sx={{width: 18}}/>
                                            </IconButton>
                                        }
                                    </TableCell>
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
                    type="ACCOUNTING"
                />
                </>
            }
        </>
    )
}

export default AccountingStatement;