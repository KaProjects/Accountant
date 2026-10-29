import {act, renderHook} from "@testing-library/react";
import {useTransactionsDialog} from "../useTransactionsDialog";

describe("useTransactionsDialog", () => {
    it("starts closed and pointing at nothing", () => {
        const {result} = renderHook(() => useTransactionsDialog());

        expect(result.current.open).toBe(false);
        expect(result.current.rowName).toBeNull();
        expect(result.current.rowId).toBeNull();
        expect(result.current.month).toBe(-1);
    });

    it("remembers the cell being pointed at", () => {
        const {result} = renderHook(() => useTransactionsDialog());

        act(() => result.current.target("Groceries", "e1", 3));

        expect(result.current.rowName).toBe("Groceries");
        expect(result.current.rowId).toBe("e1");
        expect(result.current.month).toBe(3);
    });

    it("ignores rows that aggregate other rows", () => {
        const {result} = renderHook(() => useTransactionsDialog({ignoredRowIds: ["i", "me", "e"]}));

        act(() => result.current.target("Expenses", "e", 3));

        expect(result.current.rowId).toBeNull();
        expect(result.current.month).toBe(-1);
    });

    it("clears the target without closing anything", () => {
        const {result} = renderHook(() => useTransactionsDialog());
        act(() => result.current.target("Groceries", "e1", 3));

        act(() => result.current.clearTarget());

        expect(result.current.rowId).toBeNull();
        expect(result.current.month).toBe(-1);
    });

    it("closing both hides the dialog and forgets the target", () => {
        const {result} = renderHook(() => useTransactionsDialog());
        act(() => result.current.target("Groceries", "e1", 3));
        act(() => result.current.setOpen(true));

        act(() => result.current.close());

        expect(result.current.open).toBe(false);
        expect(result.current.rowId).toBeNull();
    });

    it("identifies the cell currently pointed at", () => {
        const {result} = renderHook(() => useTransactionsDialog());
        act(() => result.current.target("Groceries", "e1", 3));

        expect(result.current.isTargeting("e1", 3)).toBe(true);
        expect(result.current.isTargeting("e1", 4)).toBe(false);
        expect(result.current.isTargeting("e2", 3)).toBe(false);
    });

    it("keeps the target while the dialog is open, since that is what it is showing", () => {
        // The dialog covers the cell it was opened from, so the pointer leaves it immediately and
        // the cell reports that. Forgetting the target then made the open dialog ask for nothing.
        const {result} = renderHook(() => useTransactionsDialog());
        act(() => result.current.target("Bank", "210", 3));
        act(() => result.current.setOpen(true));

        act(() => result.current.clearTarget());

        expect(result.current.rowName).toBe("Bank");
        expect(result.current.rowId).toBe("210");
        expect(result.current.month).toBe(3);
    });

    it("forgets the target once the dialog is closed", () => {
        const {result} = renderHook(() => useTransactionsDialog());
        act(() => result.current.target("Bank", "210", 3));
        act(() => result.current.setOpen(true));

        act(() => result.current.close());

        expect(result.current.open).toBe(false);
        expect(result.current.rowId).toBeNull();
        expect(result.current.month).toBe(-1);
    });
});
