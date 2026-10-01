import React from "react";
import PropTypes from "prop-types";
import Loader from "../Loader";
import {Dialog, DialogTitle, Table, TableBody, TableCell, TableContainer, TableFooter, TableHead, TableRow} from "@mui/material";
import Paper from "@mui/material/Paper";
import {useData} from "../../fetch";
import {formatTransactionDate} from "../../services/transactionDate";

// The container is what scrolls, so the sticky head and foot stay put while the
// transactions move between them.
const containerStyle = {maxHeight: "70vh"};
const tableStyle = {minWidth: 700};
const accountCellStyle = {minWidth: "150px"};
const dateCellStyle = {whiteSpace: "nowrap"};
// The footer keeps the body typography it had before it became a footer; only
// the pinning and the dividing line above it are new.
const totalCellStyle = {
    fontWeight: "bold",
    position: "sticky",
    bottom: 0,
    zIndex: 2,
    backgroundColor: "background.default",
    borderTop: "1px solid",
    borderTopColor: "divider",
};

const COLUMNS = ["Date", "Amount", "Debit", "Credit", "Description"];

const PATHS = {
    BUDGET: (year, rowId, month) => "/budget/" + year + "/transaction/" + rowId + "/month/" + month,
    ACCOUNTING: (year, rowId, month) => "/accounting/" + year + "/transaction/" + rowId + "/month/" + month,
};

/** The transactions behind one cell of a budget or accounting table. */
const TransactionsDialog = ({open, onClose, type, year, row, rowId, month}) => {
    const buildPath = PATHS[type];
    const path = buildPath === undefined ? "" : buildPath(year, rowId, month);

    // Nothing is fetched until the dialog is actually opened: it is rendered, pointed at a cell,
    // long before anybody asks to see it.
    const {data, loaded, error} = useData(path, open && buildPath !== undefined);

    const unknownType = buildPath === undefined ? {message: "INVALID TRANSACTION DIALOG TYPE"} : null;
    const total = () => (data ?? []).reduce((sum, transaction) => sum + parseInt(transaction.amount), 0);

    return (
        <Dialog open={open} onClose={onClose} fullWidth={false} maxWidth={'lg'}>
            <DialogTitle>Transactions for {row} {month}/{year}</DialogTitle>

            {!loaded && <Loader error={unknownType ?? error}/>}
            {loaded &&
                <TableContainer component={Paper} sx={containerStyle}>
                    <Table stickyHeader sx={tableStyle} size="small" aria-label="a dense table">
                        <TableHead>
                            <TableRow>
                                {COLUMNS.map((column, index) => (
                                    <TableCell key={index}>{column}</TableCell>
                                ))}
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {data.map((transaction, index) => (
                                <TableRow key={index}>
                                    <TableCell align="center" style={dateCellStyle}>{formatTransactionDate(transaction.date)}</TableCell>
                                    <TableCell align="right">{transaction.amount}</TableCell>
                                    <TableCell align="left" style={accountCellStyle}>{transaction.debit}</TableCell>
                                    <TableCell align="left" style={accountCellStyle}>{transaction.credit}</TableCell>
                                    <TableCell align="left" style={accountCellStyle}>{transaction.description}</TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                        <TableFooter>
                            <TableRow>
                                <TableCell variant="body" align="center" sx={totalCellStyle}>Total: </TableCell>
                                <TableCell variant="body" align="right" sx={totalCellStyle}>{total()}</TableCell>
                                <TableCell variant="body" sx={totalCellStyle}/>
                                <TableCell variant="body" sx={totalCellStyle}/>
                                <TableCell variant="body" sx={totalCellStyle}/>
                            </TableRow>
                        </TableFooter>
                    </Table>
                </TableContainer>
            }
        </Dialog>
    );
};

TransactionsDialog.propTypes = {
    open: PropTypes.bool,
    onClose: PropTypes.func.isRequired,
    type: PropTypes.oneOf(["BUDGET", "ACCOUNTING"]).isRequired,
    year: PropTypes.number.isRequired,
    row: PropTypes.string,
    rowId: PropTypes.string,
    month: PropTypes.number,
};

export default TransactionsDialog;
