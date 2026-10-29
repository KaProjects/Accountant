import React, {useEffect} from "react";
import Paper from '@mui/material/Paper';
import {Table, TableBody, TableCell, TableContainer, TableHead, TableRow} from "@mui/material";
import DataView from "../components/common/DataView";
import {useData} from "../fetch";
import TransactionsDialog from "../components/dialog/TransactionsDialog";
import BudgetChartDialog from "../components/dialog/BudgetChartDialog";
import BudgetRow from "../components/budgeting/BudgetRow";
import BudgetEditorDialog from "../components/budgeting/BudgetEditorDialog";
import {initiallyFolded, isFoldable, summedParts} from "../services/budgetGroups";
import {budgetHeaderStyle} from "../theme/tableStyles";
import {useTransactionsDialog} from "../hooks/useTransactionsDialog";
import {useBudgetChartDialog} from "../hooks/useBudgetChartDialog";
import {useAppState} from "../state/appState";

// room under the last row, so it can be scrolled up off the bottom edge of the window; and the
// table's two sides, as heavy as the line after the names - on the container rather than the
// table, whose own edges the container cuts in half as it scrolls the table sideways
const tableStyle = {
    marginBottom: "48px",
    borderLeft: "2px solid #222",
    borderRight: "2px solid #222",
    // the sides within the window's width, not added to it, and straight to their ends
    boxSizing: "border-box",
    borderRadius: 0,
};

// laid out by the widths its headings set, so that every month takes the same width, whatever
// its figures; narrower than the months need, it scrolls sideways
const tableLayoutStyle = {tableLayout: "fixed"};

/**
 * The balances, which take one part of the budget from another, have no transactions to show; the
 * totals of the income and of the mandatory expenses show every one of their part's.
 */
const AGGREGATE_ROW_IDS = ["e", "ntme", "bcf", "dcf"];

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

    // the parts with a total that can be folded up into it, as they start for each budget read and
    // as they are folded and unfolded after
    const parts = loaded ? summedParts(data.rows) : [];
    const [toggled, setToggled] = React.useState({budget: null, folded: {}});
    const folded = toggled.budget === data ? toggled.folded : initiallyFolded(parts);
    const toggleFolded = (summaryId) =>
        setToggled({budget: data, folded: {...folded, [summaryId]: !folded[summaryId]}});
    const hiddenIds = new Set(parts.filter((part) => folded[part.summaryId]).flatMap((part) => part.memberIds));
    const foldOf = (row) => {
        const part = parts.find((one) => one.summaryId === row.id);
        return part && isFoldable(part)
            ? {folded: Boolean(folded[part.summaryId]), onToggle: () => toggleFolded(part.summaryId)}
            : undefined;
    };

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
        <TableContainer component={Paper} style={tableStyle}>
            <Table sx={{minWidth: 1340}} style={tableLayoutStyle} size="small" aria-label="a dense table">
                <TableHead>
                    <TableRow key={-1}>
                        {data.columns.map((column, index) => (
                            <TableCell key={index} style={budgetHeaderStyle(index)}>{column}</TableCell>
                        ))}
                    </TableRow>
                </TableHead>
                <TableBody>
                    {data.rows.map((row, index) => (hiddenIds.has(row.id) ? null :
                        <BudgetRow
                            key={index}
                            row={row}
                            id={index}
                            expansion={{
                                showSubRows: Boolean(showSubRows[index]),
                                onToggleSubRows: toggleAt(showSubRows, setShowSubRows),
                            }}
                            fold={foldOf(row)}
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
