import {fireEvent, screen, within} from "@testing-library/react";
import AccountingData from "../AccountingData";
import {useData} from "../../fetch";
import {renderWithAppState} from "../../testUtils";

jest.mock("../../fetch");

const schema = {
    classes: [{
        name: "Class 2",
        groups: [{
            name: "Group 21",
            accounts: [{id: "210", name: "Bank"}, {id: "211", name: "Cash"}],
        }],
    }],
};

const accounts = [
    {id: "210.0", name: "Current account", initial: 100, turnover: 50, balance: 150},
    {id: "210.1", name: "Savings", initial: 200, turnover: 0, balance: 200},
];

const transactions = [
    {date: "0101", debit: 10, credit: 0, pair: "600.0", description: "salary"},
];

/** Routes each request to its own payload, as the real hook would. */
const respond = ({schemaLoaded = true} = {}) => {
    useData.mockImplementation((path) => {
        if (path.startsWith("/schema/")) return {data: schema, loaded: schemaLoaded, error: null};
        if (path.startsWith("/account/")) return {data: accounts, loaded: true, error: null};
        if (path.startsWith("/transaction/")) return {data: transactions, loaded: true, error: null};
        return {data: null, loaded: false, error: null};
    });
};

const mountView = () => {
    const setYearly = jest.fn();
    renderWithAppState(<AccountingData/>, {year: 2020, setYearly});
    return {setYearly};
};

describe("AccountingData", () => {
    beforeEach(() => jest.clearAllMocks());

    it("requests the schema for the selected year and switches to yearly mode", () => {
        respond();
        const {setYearly} = mountView();

        expect(useData).toHaveBeenCalledWith("/schema/2020");
        expect(setYearly).toHaveBeenCalledWith(true);
    });

    it("shows the loader until the schema arrives", () => {
        respond({schemaLoaded: false});
        mountView();

        expect(screen.getByRole("progressbar")).toBeInTheDocument();
        expect(screen.queryByRole("tree")).not.toBeInTheDocument();
    });

    it("renders the schema classes as a tree", () => {
        respond();
        mountView();

        expect(screen.getByRole("tree")).toBeInTheDocument();
        expect(screen.getByText("Class 2")).toBeInTheDocument();
    });

    it("reveals groups and accounts as the tree is expanded", () => {
        respond();
        mountView();

        fireEvent.click(screen.getByText("Class 2"));
        expect(screen.getByText("Group 21")).toBeInTheDocument();

        fireEvent.click(screen.getByText("Group 21"));
        expect(screen.getByText("Bank")).toBeInTheDocument();
        expect(screen.getByText("Cash")).toBeInTheDocument();
    });

    it("shows no account table until an account is picked", () => {
        respond();
        mountView();

        expect(screen.queryByText("Current account")).not.toBeInTheDocument();
    });

    it("loads the accounts for the picked schema account", () => {
        respond();
        mountView();

        fireEvent.click(screen.getByText("Class 2"));
        fireEvent.click(screen.getByText("Group 21"));
        fireEvent.click(screen.getByText("Bank"));

        expect(useData).toHaveBeenCalledWith("/account/2020/210");
        expect(screen.getByText("Current account")).toBeInTheDocument();
        expect(screen.getByText("Savings")).toBeInTheDocument();
    });

    it("labels the account table with a balance column for the current year", () => {
        respond();
        renderWithAppState(<AccountingData/>, {year: new Date().getFullYear()});

        fireEvent.click(screen.getByText("Class 2"));
        fireEvent.click(screen.getByText("Group 21"));
        fireEvent.click(screen.getByText("Bank"));

        expect(screen.getByText("Balance")).toBeInTheDocument();
        expect(screen.queryByText("Closure")).not.toBeInTheDocument();
    });

    it("labels it a closure column for a past year", () => {
        respond();
        mountView();

        fireEvent.click(screen.getByText("Class 2"));
        fireEvent.click(screen.getByText("Group 21"));
        fireEvent.click(screen.getByText("Bank"));

        expect(screen.getByText("Closure")).toBeInTheDocument();
        expect(screen.queryByText("Balance")).not.toBeInTheDocument();
    });

    it("loads the transactions for the selected account row", () => {
        respond();
        mountView();

        fireEvent.click(screen.getByText("Class 2"));
        fireEvent.click(screen.getByText("Group 21"));
        fireEvent.click(screen.getByText("Bank"));
        fireEvent.click(screen.getByText("Current account"));

        expect(useData).toHaveBeenCalledWith("/transaction/2020/210.0");
        expect(screen.getByText("salary")).toBeInTheDocument();
    });

    it("renders an off-balance account with blank turnover and balance", () => {
        // the backend omits both figures for off-balance accounts, since neither is
        // meaningful without a debit or credit orientation
        useData.mockImplementation((path) => {
            if (path.startsWith("/schema/")) return {data: schema, loaded: true, error: null};
            if (path.startsWith("/account/")) {
                return {data: [{id: "700.0", name: "general", initial: 0}], loaded: true, error: null};
            }
            return {data: [], loaded: true, error: null};
        });
        mountView();

        fireEvent.click(screen.getByText("Class 2"));
        fireEvent.click(screen.getByText("Group 21"));
        fireEvent.click(screen.getByText("Bank"));

        const row = screen.getAllByRole("row")
            .find((candidate) => within(candidate).queryByText("general"));
        const cells = within(row).getAllByRole("cell");
        expect(cells[0]).toHaveTextContent("700.0");
        expect(cells[3]).toHaveTextContent("");
        expect(cells[4]).toHaveTextContent("");
    });

    it("renders each account row with its figures", () => {
        respond();
        mountView();
        fireEvent.click(screen.getByText("Class 2"));
        fireEvent.click(screen.getByText("Group 21"));
        fireEvent.click(screen.getByText("Bank"));

        const accountRow = screen.getAllByRole("row")
            .find((candidate) => within(candidate).queryByText("Current account"));
        const cells = within(accountRow).getAllByRole("cell");
        expect(cells[0]).toHaveTextContent("210.0");
        expect(cells[2]).toHaveTextContent("100");
        expect(cells[3]).toHaveTextContent("50");
        expect(cells[4]).toHaveTextContent("150");
    });
});
