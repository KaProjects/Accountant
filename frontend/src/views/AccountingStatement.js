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

    const [showAccounts, setShowAccounts] = React.useState([]);

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
        const borderLeft = index === 13 ? "2px solid" : "0px";
        const borderRight = (index === 0 || index === 13 || index === 14) ? "2px solid" : "0px";
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

        let borderTop;
        let borderBottom;
        let fontWeight;
        if (type === 'INCOME_GROUP' || type === 'EXPENSE_GROUP' || type === 'PROFIT_SUMMARY' || type === 'CASH_FLOW_SUMMARY' || type === 'CASH_FLOW_GROUP'){
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

    function Row(props) {
        const {row, id } = props;

        const handleShowAccounts = (id) => {
            const newFlag = !showAccounts[id]
            const newFlags =showAccounts.slice()
            newFlags[id] = newFlag
            setShowAccounts(newFlags)
        }

        return (
            <React.Fragment>
                <TableRow key={id} onClick={() => handleShowAccounts(id)}>
                    <TableCell key={-1} style={getRowStyle(row.type, true, true)}>
                        {" " + row.name}
                    </TableCell>
                    {overall === undefined && row.type.startsWith("CASH_FLOW_") &&
                        <TableCell key={-2} align="right" style={getRowStyle(row.type, false, true)}>
                            {row.initial}
                        </TableCell>
                    }
                    {overall === undefined && row.monthlyValues.map((month, index) => (
                        <TableCell
                            align="right" key={index}
                            style={getRowStyle(row.type, false, false)}
                        >
                            {month}
                        </TableCell>
                    ))}
                    {overall !== undefined && row.yearlyValues.map((year, index) => (
                        <TableCell
                            align="right" key={index}
                            style={getRowStyle(row.type, false, false)}
                        >
                            {year}
                        </TableCell>
                    ))}
                    {(overall === undefined || row.type === "profit") &&
                        <TableCell
                            align="right" key={12}
                            style={getRowStyle(row.type, true, true)}
                        >
                            {row.total}
                        </TableCell>
                    }
                </TableRow>
                {showAccounts[id] && row.accounts.map((account, index) => (
                    <TableRow key={id + "a" + index}>
                        <TableCell component="th" scope="row" key={-1} style={getRowStyle(account.type, false, true)}>
                            {account.name}
                        </TableCell>
                        {account.type.startsWith("CASH_FLOW_") &&
                            <TableCell key={-2} align="right" style={getRowStyle(account.type, false, true)}>
                                {account.initial}
                            </TableCell>
                        }
                        {account.monthlyValues.map((month, index) => (
                            <TableCell
                                align="right" key={index}
                                style={getRowStyle(account.type, false, false)}
                                onClick={() => setTransactionsDialogProps(account.name, account.schemaId, index + 1)}
                                onMouseLeave={() => setTransactionsDialogProps(null, null, -1)}
                            >
                                {month}
                                {account.schemaId === transactionsDialogRowId && index + 1 === transactionsDialogMonth &&
                                    <IconButton
                                        style={{height: "2px", width: "25px", color: account.type === 'INCOME_ACCOUNT' ? "#227222" : "#a13c3c"}}
                                        onClick={() => setShowTransactionsDialog(true)}
                                    >
                                        <ReceiptLongIcon sx={{width: 18}}/>
                                    </IconButton>
                                }
                            </TableCell>
                        ))}
                        <TableCell align="right" key={12} style={getRowStyle(account.type, true, true)}>{account.total}</TableCell>
                    </TableRow>
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