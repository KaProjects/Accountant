import {render, screen, within} from "@testing-library/react";
import TransactionTable from "../TransactionTable";
import {useData} from "../../../fetch";

jest.mock("../../../fetch");

const transactions = [
    {date: "0101", debit: 10, credit: 0, pair: "600.0", description: "salary"},
    {date: "0202", debit: 0, credit: 25, pair: "500.1", description: "rent"},
];

describe("TransactionTable", () => {
    beforeEach(() => {
        jest.clearAllMocks();
        useData.mockReturnValue({data: transactions, loaded: true, error: null});
    });

    it("asks for the transactions of the given account and year", () => {
        render(<TransactionTable year={2020} accountId="210.0"/>);

        expect(useData).toHaveBeenCalledWith("/transaction/2020/210.0");
    });

    it("lists every transaction in full", () => {
        render(<TransactionTable year={2020} accountId="210.0"/>);
        const row = screen.getByRole("row", {name: new RegExp("salary")});

        expect(within(row).getAllByRole("cell").map((cell) => cell.textContent))
            .toEqual(["0101", "10", "0", "600.0", "salary"]);
        expect(screen.getByText("rent")).toBeInTheDocument();
    });

    it("renders nothing but the loader until the transactions arrive", () => {
        useData.mockReturnValue({data: null, loaded: false, error: null});

        render(<TransactionTable year={2020} accountId="210.0"/>);

        expect(screen.queryByText("salary")).not.toBeInTheDocument();
    });
});
