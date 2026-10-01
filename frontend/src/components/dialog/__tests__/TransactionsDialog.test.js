import {render, screen, waitFor, within} from "@testing-library/react";
import axios from "axios";
import TransactionsDialog from "../TransactionsDialog";

jest.mock("axios");

const transactions = [
    {date: "0101", amount: 100, debit: "500.0", credit: "210.0", description: "hotel"},
    {date: "0202", amount: 250, debit: "500.1", credit: "210.0", description: "flights"},
];

const props = (overrides = {}) => ({
    open: true, onClose: jest.fn(), type: "BUDGET",
    year: 2020, row: "Travel", rowId: "7", month: 3,
    ...overrides,
});

describe("TransactionsDialog", () => {
    beforeEach(() => {
        jest.clearAllMocks();
        axios.get.mockResolvedValue({data: transactions});
    });

    it("asks for the budget row's transactions for that month", async () => {
        render(<TransactionsDialog {...props()}/>);

        await waitFor(() => expect(axios.get).toHaveBeenCalled());
        expect(axios.get.mock.calls[0][0]).toBe("/api/budget/2020/transaction/7/month/3");
    });

    it("asks a different endpoint for an accounting row", async () => {
        render(<TransactionsDialog {...props({type: "ACCOUNTING"})}/>);

        await waitFor(() => expect(axios.get).toHaveBeenCalled());
        expect(axios.get.mock.calls[0][0]).toBe("/api/accounting/2020/transaction/7/month/3");
    });

    it("sends the session cookie", async () => {
        render(<TransactionsDialog {...props()}/>);

        await waitFor(() => expect(axios.get).toHaveBeenCalled());
        expect(axios.get.mock.calls[0][1]).toEqual({withCredentials: true});
    });

    it("says which row and month it is showing", async () => {
        render(<TransactionsDialog {...props()}/>);

        expect(await screen.findByText(/Transactions for/)).toHaveTextContent("Travel 3/2020");
    });

    it("lists every transaction", async () => {
        render(<TransactionsDialog {...props()}/>);

        const row = await screen.findByRole("row", {name: "hotel" instanceof RegExp ? "hotel" : new RegExp("hotel")});
        expect(within(row).getAllByRole("cell").map((cell) => cell.textContent))
            .toEqual(["0101", "100", "500.0", "210.0", "hotel"]);
    });

    it("totals the amounts", async () => {
        render(<TransactionsDialog {...props()}/>);

        const total = await screen.findByRole("row", {name: /Total:/ instanceof RegExp ? /Total:/ : new RegExp(/Total:/)});
        expect(within(total).getAllByRole("cell")[1]).toHaveTextContent("350");
    });

    it("fetches nothing while it is closed", () => {
        render(<TransactionsDialog {...props({open: false})}/>);

        expect(axios.get).not.toHaveBeenCalled();
    });

    it("refuses a type it does not know, instead of requesting a nonsense path", async () => {
        render(<TransactionsDialog {...props({type: "NONSENSE"})}/>);

        expect(await screen.findByText(/INVALID TRANSACTION DIALOG TYPE/)).toBeInTheDocument();
        expect(axios.get).not.toHaveBeenCalled();
    });
});
