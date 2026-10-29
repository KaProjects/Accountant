import {isExpenseRow} from "../theme/tableStyles";

/**
 * Turns a budget row into the twelve-point series the budget chart draws.
 *
 * Each month is split into the portion that went to plan (`base`) and the
 * variance, which is reported as a `deficit` or a `surplus` depending on whether
 * the row is an expense. Months beyond the last filled one are left at zero, so
 * the chart stops where the actual figures stop.
 */
export function toBudgetChartSeries(row) {
    const isExpense = isExpenseRow(row.type);

    return Array.from({length: 12}, (ignored, month) => {
        let base = 0;
        let deficit = 0;
        let surplus = 0;

        if (month < row.lastFilledMonth) {
            const actual = row.actual[month];
            const planned = row.planned[month];

            if (actual > planned) {
                base = planned;
                if (isExpense) {
                    deficit = actual - planned;
                } else {
                    surplus = actual - planned;
                }
            } else {
                base = actual;
                if (isExpense) {
                    surplus = planned - actual;
                } else {
                    deficit = planned - actual;
                }
            }
        }

        return {name: month + 1, planned: row.planned[month], base, deficit, surplus};
    });
}

export const isExpenseBudgetRow = isExpenseRow;
