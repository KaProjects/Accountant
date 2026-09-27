import {useState} from "react";

/**
 * Holds the state behind the transactions dialog: which cell the reader is
 * pointing at, and whether the dialog itself is open.
 *
 * @param ignoredRowIds row ids that never open a dialog, such as the budgeting
 *                      summary rows which aggregate other rows rather than
 *                      mapping to transactions of their own
 */
export function useTransactionsDialog({ignoredRowIds = []} = {}) {
    const [open, setOpen] = useState(false);
    const [rowName, setRowName] = useState(null);
    const [rowId, setRowId] = useState(null);
    const [month, setMonth] = useState(-1);

    const clearTarget = () => {
        setRowName(null);
        setRowId(null);
        setMonth(-1);
    };

    const target = (name, id, monthNumber) => {
        if (ignoredRowIds.includes(id)) {
            clearTarget();
        } else {
            setRowName(name);
            setRowId(id);
            setMonth(monthNumber);
        }
    };

    const close = () => {
        setOpen(false);
        clearTarget();
    };

    /** True when the given cell is the one currently pointed at. */
    const isTargeting = (candidateRowId, candidateMonth) =>
        candidateRowId === rowId && candidateMonth === month;

    return {open, rowName, rowId, month, setOpen, target, clearTarget, close, isTargeting};
}
