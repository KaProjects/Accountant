import React, {useEffect} from "react";
import Paper from '@mui/material/Paper';
import {Table, TableBody, TableCell, TableContainer, TableHead, TableRow} from "@mui/material";
import PropTypes from "prop-types";
import DataView from "../components/common/DataView";
import {useData} from "../fetch";
import TransactionsDialog from "../components/TransactionsDialog";
import BudgetChartDialog from "../components/BudgetChartDialog";
import BudgetRow from "../components/budgeting/BudgetRow";
import {budgetHeaderStyle} from "../theme/tableStyles";
import {useTransactionsDialog} from "../hooks/useTransactionsDialog";
import {useBudgetChartDialog} from "../hooks/useBudgetChartDialog";

/** Summary rows aggregate other rows, so they have no transactions of their own. */
const AGGREGATE_ROW_IDS = ["i", "me", "e", "ntme", "bcf", "dcf"];

const Budgeting = props => {

    const {data, loaded, error} = useData("/budget/" + props.year)

    useEffect(() => {
        props.setYearly(true)
        // eslint-disable-next-line
    }, []);

    const [showSubRows, setShowSubRows] = React.useState([]);
    const [showDeltas, setShowDeltas] = React.useState([]);

    const transactionsDialog = useTransactionsDialog({ignoredRowIds: AGGREGATE_ROW_IDS});
    const budgetChart = useBudgetChartDialog();

    const toggleAt = (flags, setFlags) => (id) => {
        const updated = flags.slice()
        updated[id] = !flags[id]
        setFlags(updated)
    }

    return (
    <DataView loaded={loaded} error={error}>
        {() => (
        <>
        <TableContainer component={Paper}>
            <Table sx={{ minWidth: 650 }} size="small" aria-label="a dense table">
                <TableHead>
                    <TableRow key={-1}>
                        {data.columns.map((column, index) => (
                            <TableCell key={index} style={budgetHeaderStyle(index)}>{column}</TableCell>
                        ))}
                    </TableRow>
                </TableHead>
                <TableBody>
                    {data.rows.map((row, index) => (
                        <BudgetRow
                            key={index}
                            row={row}
                            id={index}
                            showSubRows={Boolean(showSubRows[index])}
                            showDeltas={Boolean(showDeltas[index])}
                            onToggleSubRows={toggleAt(showSubRows, setShowSubRows)}
                            onToggleDeltas={toggleAt(showDeltas, setShowDeltas)}
                            transactionsDialog={transactionsDialog}
                            budgetChart={budgetChart}
                        />
                    ))}
                </TableBody>
            </Table>
        </TableContainer>
        <TransactionsDialog
            open={transactionsDialog.open}
            onClose={transactionsDialog.close}
            year={props.year}
            row={transactionsDialog.rowName}
            rowId={transactionsDialog.rowId}
            month={transactionsDialog.month}
            type="BUDGET"
        />
        <BudgetChartDialog
            open={budgetChart.open}
            onClose={budgetChart.close}
            data={budgetChart.data}
            name={budgetChart.name}
            isExpense={budgetChart.isExpense}
        />
        </>
        )}
    </DataView>
    )
}

Budgeting.propTypes = {
    year: PropTypes.number.isRequired,
    setYearly: PropTypes.func.isRequired,
}

export default Budgeting;
