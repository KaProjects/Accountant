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

    const forgetTarget = () => {
        setRowName(null);
        setRowId(null);
        setMonth(-1);
    };

    /**
     * Forgets the cell being pointed at - unless the dialog is open, in which case that cell is
     * what the dialog is showing.
     *
     * The dialog covers the cell it was opened from, so the pointer leaves it the moment the
     * dialog appears. Forgetting the target then left the open dialog asking the backend for
     * transactions of no row in month -1, which it answered with a 400, and the dialog replaced
     * what the reader had just asked to see with an error.
     */
    const clearTarget = () => {
        if (!open) forgetTarget();
    };

    const target = (name, id, monthNumber) => {
        if (ignoredRowIds.includes(id)) {
            forgetTarget();
        } else {
            setRowName(name);
            setRowId(id);
            setMonth(monthNumber);
        }
    };

    const close = () => {
        setOpen(false);
        forgetTarget();
    };

    /** True when the given cell is the one currently pointed at. */
    const isTargeting = (candidateRowId, candidateMonth) =>
        candidateRowId === rowId && candidateMonth === month;

    return {open, rowName, rowId, month, setOpen, target, clearTarget, close, isTargeting};
}
