import {useState} from "react";
import {isExpenseBudgetRow, toBudgetChartSeries} from "../services/budgetChart";

/**
 * Holds the state behind the budget chart dialog. Pointing at a row's name previews that row; the
 * dialog itself is opened separately, and shows the row previewed until it is closed.
 */
export function useBudgetChartDialog() {
    const [open, setOpen] = useState(false);
    const [data, setData] = useState(null);
    const [name, setName] = useState("");
    const [isExpense, setIsExpense] = useState(false);

    const preview = (row) => {
        // opening the chart lays it over the row, which the pointer then leaves: what it shows stays
        if (open) return;
        if (row === null) {
            setData(null);
            setName("");
        } else {
            setName(row.name);
            setIsExpense(isExpenseBudgetRow(row.type));
            setData(toBudgetChartSeries(row));
        }
    };

    const close = () => {
        setOpen(false);
        setData(null);
        setName("");
    };

    /** True when the named row is the one currently previewed. */
    const isPreviewing = (rowName) => data !== null && name === rowName;

    return {open, data, name, isExpense, setOpen, preview, close, isPreviewing};
}
