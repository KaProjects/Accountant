import React, {useEffect} from "react";
import Paper from '@mui/material/Paper';
import {Table, TableBody, TableCell, TableContainer, TableHead, TableRow} from "@mui/material";
import DataView from "../components/common/DataView";
import {useData} from "../fetch";
import TransactionsDialog from "../components/dialog/TransactionsDialog";
import BudgetChartDialog from "../components/dialog/BudgetChartDialog";
import BudgetRow from "../components/budgeting/BudgetRow";
import BudgetEditorDialog from "../components/budgeting/BudgetEditorDialog";
import {budgetHeaderStyle} from "../theme/tableStyles";
import {useTransactionsDialog} from "../hooks/useTransactionsDialog";
import {useBudgetChartDialog} from "../hooks/useBudgetChartDialog";
import {useAppState} from "../state/appState";

/** Summary rows aggregate other rows, so they have no transactions of their own. */
const AGGREGATE_ROW_IDS = ["i", "me", "e", "ntme", "bcf", "dcf"];

const Budgeting = () => {
    const {year, setYearly, setEditAction} = useAppState();

    const {data, loaded, error, reload} = useData("/budget/" + year)

    useEffect(() => {
        setYearly(true)
    }, [setYearly]);

    // the main bar's edit button opens the editor of the year's budget, while this page is shown
    const [editorOpen, setEditorOpen] = React.useState(false);
    useEffect(() => {
        setEditAction(() => setEditorOpen(true));
        return () => setEditAction(null);
    }, [setEditAction]);

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
    <>
    <BudgetEditorDialog
        open={editorOpen}
        year={year}
        onClose={() => setEditorOpen(false)}
        onSaved={() => {
            setEditorOpen(false);
            reload();
        }}
    />
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
                            expansion={{
                                showSubRows: Boolean(showSubRows[index]),
                                showDeltas: Boolean(showDeltas[index]),
                                onToggleSubRows: toggleAt(showSubRows, setShowSubRows),
                                onToggleDeltas: toggleAt(showDeltas, setShowDeltas),
                            }}
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
            year={year}
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
    </>
    )
}


export default Budgeting;
