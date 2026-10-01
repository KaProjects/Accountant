import {fireEvent, render, screen, within} from "@testing-library/react";
import AccountTable from "../AccountTable";
import {useData} from "../../../fetch";

jest.mock("../../../fetch");

const accounts = [
    {id: "210.0", name: "Current account", initial: 100, turnover: 50, balance: 150},
    {id: "210.1", name: "Savings", initial: 200, turnover: 0, balance: 200},
];

const mountTable = (props = {}) => {
    const merged = {year: 2020, schemaId: "210", onSelectAccount: jest.fn(), ...props};
    render(<AccountTable {...merged}/>);
    return merged;
};

describe("AccountTable", () => {
    beforeEach(() => {
        jest.clearAllMocks();
        useData.mockReturnValue({data: accounts, loaded: true, error: null});
    });

    it("asks for the accounts of the given year and schema account", () => {
        mountTable();

        expect(useData).toHaveBeenCalledWith("/account/2020/210");
    });

    it("lists every account with its figures", () => {
        mountTable();
        const row = screen.getByRole("row", {name: new RegExp("Current account")});

        expect(within(row).getAllByRole("cell").map((cell) => cell.textContent))
            .toEqual(["210.0", "Current account", "100", "50", "150"]);
        expect(screen.getByText("Savings")).toBeInTheDocument();
    });

    it("calls the closing figure a running balance only while the year is still open", () => {
        const thisYear = new Date().getFullYear();

        const {unmount} = render(<AccountTable year={thisYear} schemaId="210" onSelectAccount={jest.fn()}/>);
        expect(screen.getByText("Balance")).toBeInTheDocument();
        unmount();

        render(<AccountTable year={thisYear - 1} schemaId="210" onSelectAccount={jest.fn()}/>);
        expect(screen.getByText("Closure")).toBeInTheDocument();
    });

    it("selects the account that was clicked", () => {
        const props = mountTable();

        fireEvent.click(screen.getByText("Savings"));

        expect(props.onSelectAccount).toHaveBeenCalledWith("210.1");
    });

    it("renders nothing but the loader until the accounts arrive", () => {
        useData.mockReturnValue({data: null, loaded: false, error: null});

        mountTable();

        expect(screen.queryByText("Current account")).not.toBeInTheDocument();
    });
});
