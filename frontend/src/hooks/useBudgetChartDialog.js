import {useState} from "react";
import {isExpenseBudgetRow, toBudgetChartSeries} from "../services/budgetChart";

/**
 * Holds the state behind the budget chart dialog. Pointing at a row's difference
 * cell previews that row; the dialog itself is opened separately.
 */
export function useBudgetChartDialog() {
    const [open, setOpen] = useState(false);
    const [data, setData] = useState(null);
    const [name, setName] = useState("");
    const [isExpense, setIsExpense] = useState(false);

    const preview = (row) => {
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
        preview(null);
    };

    /** True when the named row is the one currently previewed. */
    const isPreviewing = (rowName) => data !== null && name === rowName;

    return {open, data, name, isExpense, setOpen, preview, close, isPreviewing};
}
