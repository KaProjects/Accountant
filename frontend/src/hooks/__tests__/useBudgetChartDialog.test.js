import {act, renderHook} from "@testing-library/react";
import {useBudgetChartDialog} from "../useBudgetChartDialog";

const row = (overrides = {}) => ({
    name: "Groceries",
    type: "EXPENSE",
    actual: [10, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
    planned: [15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15],
    lastFilledMonth: 2,
    ...overrides,
});

describe("useBudgetChartDialog", () => {
    it("starts closed, previewing nothing", () => {
        const {result} = renderHook(() => useBudgetChartDialog());

        expect(result.current.open).toBe(false);
        expect(result.current.data).toBeNull();
        expect(result.current.isPreviewing("Groceries")).toBe(false);
    });

    it("previews the row it is given, with a series to draw", () => {
        const {result} = renderHook(() => useBudgetChartDialog());

        act(() => result.current.preview(row()));

        expect(result.current.name).toBe("Groceries");
        expect(result.current.isPreviewing("Groceries")).toBe(true);
        expect(result.current.data).toHaveLength(12);
    });

    it("previews only the named row, not any other", () => {
        const {result} = renderHook(() => useBudgetChartDialog());

        act(() => result.current.preview(row()));

        expect(result.current.isPreviewing("Rent")).toBe(false);
    });

    it("stops previewing when given nothing", () => {
        const {result} = renderHook(() => useBudgetChartDialog());
        act(() => result.current.preview(row()));

        act(() => result.current.preview(null));

        expect(result.current.data).toBeNull();
        expect(result.current.isPreviewing("Groceries")).toBe(false);
    });

    it("remembers whether the previewed row is an expense, which the chart draws differently", () => {
        const {result} = renderHook(() => useBudgetChartDialog());

        act(() => result.current.preview(row({type: "EXPENSE"})));
        const asExpense = result.current.isExpense;

        act(() => result.current.preview(row({type: "INCOME"})));

        expect(asExpense).not.toBe(result.current.isExpense);
    });

    it("closing both shuts the dialog and clears the preview", () => {
        const {result} = renderHook(() => useBudgetChartDialog());
        act(() => {result.current.preview(row()); result.current.setOpen(true)});

        act(() => result.current.close());

        expect(result.current.open).toBe(false);
        expect(result.current.data).toBeNull();
    });
});
