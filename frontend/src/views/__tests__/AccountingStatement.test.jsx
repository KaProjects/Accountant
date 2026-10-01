import {fireEvent, screen, within} from "@testing-library/react";
import AccountingStatement from "../AccountingStatement";
import {useData} from "../../fetch";
import {useParams} from "react-router-dom";
import realBalance from "../../__tests__/fixtures/accounting-balance-2020.json";
import {renderWithAppState} from "../../testUtils";

jest.mock("../../fetch");
const mockNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
    ...jest.requireActual("react-router-dom"),
    useNavigate: () => mockNavigate,
    useParams: jest.fn(),
}));

const months = (...values) => {
    const filled = [...values];
    while (filled.length < 12) filled.push(0);
    return filled;
};

const child = (overrides = {}) => ({
    schemaId: "21",
    name: "Bank accounts",
    type: "BALANCE_GROUP",
    initial: 500,
    monthlyValues: months(10, 20, 30),
    total: 60,
    children: [],
    ...overrides,
});

const row = (overrides = {}) => ({
    schemaId: "2",
    name: "Assets",
    type: "BALANCE_CLASS",
    initial: 1000,
    monthlyValues: months(100, 200, 300),
    // the overall route renders yearlyValues instead of monthlyValues
    yearlyValues: [1000, 2000, 3000],
    total: 600,
    children: [],
    ...overrides,
});

const payload = (rows) => ({
    columns: ["Balance Sheet", "Initial", "January", "February", "March", "April",
        "May", "June", "July", "August", "September", "October", "November", "December", "Total"],
    rows,
});

const mountView = (data, {loaded = true, error = null, params = {type: "balance"}} = {}) => {
    useParams.mockReturnValue(params);
    // The view and the transactions dialog inside it both fetch, so the mock answers by path
    // rather than returning the view's payload to whoever asks.
    useData.mockImplementation((path) =>
        path.startsWith("/accounting/2020/transaction") ? {data: [], loaded: true, error: null} : {data, loaded, error});
    const setYearly = jest.fn();
    renderWithAppState(<AccountingStatement/>, {year: 2020, setYearly});
    return {setYearly};
};

describe("AccountingStatement", () => {
    beforeEach(() => jest.clearAllMocks());

    it("requests the statement for the routed type and selected year", () => {
        mountView(payload([row()]));

        expect(useData).toHaveBeenCalledWith("/accounting/balance/2020");
    });

    it("requests the overall statement without a year when the overall route is used", () => {
        mountView(payload([row()]), {params: {type: "balance", overall: "overall"}});

        expect(useData).toHaveBeenCalledWith("/accounting/balance/");
    });

    it("switches the main bar into yearly mode for a single year", () => {
        const {setYearly} = mountView(payload([row()]));

        expect(setYearly).toHaveBeenCalledWith(true);
    });

    it("leaves yearly mode off for the overall view", () => {
        const {setYearly} = mountView(payload([row()]), {params: {type: "balance", overall: "overall"}});

        expect(setYearly).toHaveBeenCalledWith(false);
    });

    it("renders yearly figures instead of monthly ones on the overall view", () => {
        mountView(payload([row()]), {params: {type: "balance", overall: "overall"}});

        const cells = screen.getAllByRole("cell");
        expect(cells[2]).toHaveTextContent("1000");
        expect(cells[3]).toHaveTextContent("2000");
    });

    it("shows the loader until the data arrives", () => {
        mountView(null, {loaded: false});

        expect(screen.getByRole("progressbar")).toBeInTheDocument();
        expect(screen.queryByRole("table")).not.toBeInTheDocument();
    });

    it("renders every column heading", () => {
        mountView(payload([row()]));

        const headers = screen.getAllByRole("columnheader");
        expect(headers).toHaveLength(15);
        // The year is in the bar and in the address, so the title does not repeat it.
        expect(headers[0]).toHaveTextContent("Balance Sheet");
        expect(headers[1]).toHaveTextContent("Initial");
        expect(headers[14]).toHaveTextContent("Total");
    });

    it("renders the initial column, the monthly values and the total", () => {
        mountView(payload([row()]));

        const cells = screen.getAllByRole("cell");
        expect(cells[0]).toHaveTextContent("Assets");
        expect(cells[1]).toHaveTextContent("1000"); // initial
        expect(cells[2]).toHaveTextContent("100");  // January
        expect(cells[14]).toHaveTextContent("600"); // total
    });

    it("keeps children hidden until the row is clicked", () => {
        mountView(payload([row({children: [child()]})]));

        expect(screen.queryByText("Bank accounts")).not.toBeInTheDocument();

        fireEvent.click(screen.getByText(/Assets/));

        expect(screen.getByText("Bank accounts")).toBeInTheDocument();
    });

    it("renders a child row with its own initial, months and total", () => {
        mountView(payload([row({children: [child()]})]));
        fireEvent.click(screen.getByText(/Assets/));

        const childRow = screen.getAllByRole("row")
            .find((candidate) => within(candidate).queryByText("Bank accounts"));
        const cells = within(childRow).getAllByRole("cell");
        expect(cells[0]).toHaveTextContent("500"); // initial
        expect(cells[1]).toHaveTextContent("10");  // January
    });

    it("expands a balance sheet child down to its grandchildren", () => {
        const grandchild = child({schemaId: "210", name: "Current account", type: "BALANCE_ACCOUNT"});
        const parent = child({children: [grandchild]});
        mountView(payload([row({children: [parent]})]));

        fireEvent.click(screen.getByText(/Assets/));
        expect(screen.queryByText("Current account")).not.toBeInTheDocument();

        fireEvent.click(screen.getByText("Bank accounts"));

        expect(screen.getByText("Current account")).toBeInTheDocument();
    });

    it("keeps grandchildren collapsed on statements other than the balance sheet", () => {
        const grandchild = child({schemaId: "510", name: "Consumption detail"});
        const parent = child({name: "Consumption", children: [grandchild]});
        mountView(payload([row({children: [parent]})]), {params: {type: "profit"}});

        fireEvent.click(screen.getByText(/Assets/));
        fireEvent.click(screen.getByText("Consumption"));

        expect(screen.queryByText("Consumption detail")).not.toBeInTheDocument();
    });

    it("renders the real backend payload without error", () => {
        mountView(realBalance);

        expect(screen.getAllByRole("columnheader")).toHaveLength(realBalance.columns.length);
        expect(screen.getAllByRole("row")).toHaveLength(realBalance.rows.length + 1);
    });
});
