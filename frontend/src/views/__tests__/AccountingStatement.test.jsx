import {fireEvent, screen, within} from "@testing-library/react";
import AccountingStatement from "../AccountingStatement";
import {useData, useEachData} from "../../fetch";
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

const overallPayload = (rows) => ({
    columns: ["Yearly Balance Sheet", "2018", "2019", "2020"],
    rows,
});

const payload = (rows) => ({
    columns: ["Balance Sheet", "Initial", "January", "February", "March", "April",
        "May", "June", "July", "August", "September", "October", "November", "December", "Total"],
    rows,
});

const mountView = (data, {loaded = true, error = null, params = {type: "balance"}, each} = {}) => {
    useParams.mockReturnValue(params);
    // The view and the transactions dialog inside it both fetch, so the mock answers by path
    // rather than returning the view's payload to whoever asks.
    useData.mockImplementation((path) =>
        path.startsWith("/accounting/2020/transaction") ? {data: [], loaded: true, error: null} : {data, loaded, error});
    useEachData.mockImplementation((paths, enabled) => enabled && each !== undefined
        ? {data: each(paths), loaded: true, error: null}
        : {data: null, loaded: false, error: null});
    const setYearly = jest.fn();
    const setOverallPath = jest.fn();
    const view = renderWithAppState(<AccountingStatement/>, {year: 2020, setYearly, setOverallPath});
    return {setYearly, setOverallPath, unmount: view.unmount};
};

describe("AccountingStatement", () => {
    beforeEach(() => jest.clearAllMocks());

    describe("the overall cash flow", () => {
        const overallCashFlow = {
            columns: ["Yearly Cash Flow Statement", "2019", "2020"],
            rows: [
                {type: "CASH_FLOW_GROUP", schemaId: "20", name: "Cash", yearlyValues: [10, 20], children: []},
                {type: "CASH_FLOW_SUMMARY", schemaId: "cf", name: "Cash Flow", yearlyValues: [10, 20], children: []},
            ],
        };
        const yearOf = () => ({
            columns: ["Cash Flow Statement", "Initial", "January", "February", "March", "April", "May",
                "June", "July", "August", "September", "October", "November", "December", "Total"],
            rows: [
                {type: "CASH_FLOW_GROUP", schemaId: "20", name: "Cash", initial: 0, monthlyValues: months(10)},
                {type: "CASH_FLOW_SUMMARY", schemaId: "cf", name: "Cash Flow", initial: 0, monthlyValues: months(10)},
            ],
        });
        const mountCashFlow = (each) => mountView(overallCashFlow,
            {params: {type: "cashflow", overall: "overall"}, each});

        it("asks for every year's own statement, to chart the months of them all", () => {
            mountCashFlow((paths) => paths.map(yearOf));

            expect(useEachData).toHaveBeenCalledWith(["/accounting/cashflow/2019", "/accounting/cashflow/2020"], true);
        });

        it("charts the months below the chart of the years, neither of them titled", () => {
            mountCashFlow((paths) => paths.map(yearOf));

            // jsdom lays nothing out, so a chart shows only as its surface, which has no role
            // eslint-disable-next-line testing-library/no-node-access
            expect(document.querySelectorAll(".recharts-responsive-container")).toHaveLength(2);
            expect(screen.queryByRole("heading")).not.toBeInTheDocument();
        });

        it("shows no loader and no chart of months when the statement has no years", () => {
            // an empty database: the statement comes back with no years, so there are none to fetch,
            // and the hook answers an empty list of paths at once, with nothing
            mountView({columns: ["Yearly Cash Flow Statement"], rows: []},
                {params: {type: "cashflow", overall: "overall"}, each: () => []});

            expect(useEachData).toHaveBeenCalledWith([], true);
            expect(screen.queryByRole("progressbar")).not.toBeInTheDocument();
        });

        it("asks for no year's statement anywhere else", () => {
            mountView(overallPayload([row()]), {params: {type: "balance", overall: "overall"}});

            expect(useEachData).toHaveBeenCalledWith([], false);
        });
    });

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

    it("tells the main bar where the overall view of the same statement is", () => {
        const {setOverallPath} = mountView(payload([row()]), {params: {type: "cashflow"}});

        expect(setOverallPath).toHaveBeenLastCalledWith("/accounting/cashflow/overall");
    });

    it("offers no way to an overall view from the overall view itself", () => {
        const {setOverallPath} = mountView(overallPayload([row()]), {params: {type: "balance", overall: "overall"}});

        expect(setOverallPath).toHaveBeenLastCalledWith(null);
    });

    it("withdraws the way to its overall view on leaving, so no other page offers it", () => {
        const {setOverallPath, unmount} = mountView(payload([row()]));

        unmount();

        expect(setOverallPath).toHaveBeenLastCalledWith(null);
    });

    it("leaves yearly mode off for the overall view", () => {
        const {setYearly} = mountView(payload([row()]), {params: {type: "balance", overall: "overall"}});

        expect(setYearly).toHaveBeenCalledWith(false);
    });

    it("renders yearly figures instead of monthly ones on the overall view", () => {
        mountView(payload([row()]), {params: {type: "balance", overall: "overall"}});

        const cells = screen.getAllByRole("cell");
        expect(cells[2]).toHaveTextContent("1,000");
        expect(cells[3]).toHaveTextContent("2,000");
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
        expect(cells[1]).toHaveTextContent("1,000"); // initial
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

    describe("opening a single year from the overall view", () => {
        const mountOverall = () => mountView(overallPayload([row()]),
            {params: {type: "balance", overall: "overall"}});

        const header = (text) => screen.getByRole("columnheader", {name: new RegExp(text)});

        const mark = (text) => within(header(text)).queryByTestId("corner-mark");

        it("marks nothing until the reader points at a year", () => {
            mountOverall();

            expect(mark("2019")).not.toBeInTheDocument();
        });

        it("shades the year being pointed at, and marks its corner", () => {
            mountOverall();

            fireEvent.mouseEnter(header("2019"));

            // top right: the year is centred in its heading
            expect(mark("2019")).toHaveAttribute("data-corner", "top-right");
            expect(header("2019").style.boxShadow).toContain("inset 0 0 0 100vmax");
            expect(header("2019").style.cursor).toBe("pointer");
        });

        it("withdraws the shade and the mark when the reader points away", () => {
            mountOverall();
            fireEvent.mouseEnter(header("2019"));

            fireEvent.mouseLeave(header("2019"));

            expect(mark("2019")).not.toBeInTheDocument();
            expect(header("2019").style.boxShadow).not.toContain("100vmax");
        });

        it("opens that year's statement on a click anywhere on its header", () => {
            mountOverall();

            fireEvent.click(header("2019"));

            expect(mockNavigate).toHaveBeenCalledWith("/accounting/balance?year=2019");
        });

        it("opens it from the keyboard too", () => {
            mountOverall();

            fireEvent.keyDown(header("2019"), {key: "Enter"});

            expect(mockNavigate).toHaveBeenCalledWith("/accounting/balance?year=2019");
        });

        it("opens nothing from the heading, which names no year", () => {
            mountOverall();

            fireEvent.mouseEnter(header("Yearly Balance Sheet"));
            fireEvent.click(header("Yearly Balance Sheet"));

            expect(mark("Yearly Balance Sheet")).not.toBeInTheDocument();
            expect(mockNavigate).not.toHaveBeenCalled();
        });

        it("opens nothing from the income statement's Total, which is no year either", () => {
            mountView({columns: ["Yearly Income Statement", "2019", "Total"], rows: [row()]},
                {params: {type: "profit", overall: "overall"}});

            fireEvent.mouseEnter(header("Total"));
            fireEvent.click(header("Total"));

            expect(mark("Total")).not.toBeInTheDocument();
            expect(mockNavigate).not.toHaveBeenCalled();
        });

        it("opens nothing in the single-year view, which has no years to open", () => {
            mountView(payload([row()]));
            const january = screen.getByRole("columnheader", {name: /January/});

            fireEvent.mouseEnter(january);
            fireEvent.click(january);

            expect(within(january).queryByTestId("corner-mark")).not.toBeInTheDocument();
            expect(mockNavigate).not.toHaveBeenCalled();
        });
    });
});
