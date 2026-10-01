import {fireEvent, render, screen, within} from "@testing-library/react";
import Budgeting from "../Budgeting";
import {useData} from "../../fetch";
import realBudget from "../../__tests__/fixtures/budget-2020.json";
import {renderWithAppState} from "../../testUtils";

jest.mock("../../fetch");

const months = (...values) => {
    const filled = [...values];
    while (filled.length < 12) filled.push(0);
    return filled;
};

const row = (overrides = {}) => ({
    id: "e1",
    name: "Groceries",
    type: "EXPENSE",
    actual: months(100, 200, 300),
    planned: months(150, 150, 150, 150, 150, 150, 150, 150, 150, 150, 150, 150),
    subRows: [],
    lastFilledMonth: 3,
    actualSum: 600,
    actualAvg: 200,
    plannedSumToFilledMonth: 450,
    plannedAvgToFilledMonth: 150,
    deltaAvg: 50,
    ...overrides,
});

const payload = (rows) => ({
    columns: ["Budget 2020", "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December",
        "actual", "actual/3", "budget/3", "delta/3"],
    lastFilledMonth: 3,
    rows,
});

const mountView = (data, {loaded = true, error = null} = {}) => {
    // The view and the transactions dialog inside it both fetch, so the mock answers by path
    // rather than returning the view's payload to whoever asks.
    useData.mockImplementation((path) =>
        path.startsWith("/budget/2020/transaction") ? {data: [], loaded: true, error: null} : {data, loaded, error});
    const setYearly = jest.fn();
    renderWithAppState(<Budgeting/>, {year: 2020, setYearly});
    return {setYearly};
};

describe("Budgeting", () => {
    beforeEach(() => jest.clearAllMocks());

    it("requests the budget for the selected year", () => {
        mountView(payload([row()]));

        expect(useData).toHaveBeenCalledWith("/budget/2020");
    });

    it("switches the main bar into yearly mode", () => {
        const {setYearly} = mountView(payload([row()]));

        expect(setYearly).toHaveBeenCalledWith(true);
    });

    it("shows the loader instead of the table until the data arrives", () => {
        mountView(null, {loaded: false});

        expect(screen.getByRole("progressbar")).toBeInTheDocument();
        expect(screen.queryByRole("table")).not.toBeInTheDocument();
    });

    it("renders every column heading", () => {
        mountView(payload([row()]));

        const headers = screen.getAllByRole("columnheader");
        expect(headers).toHaveLength(17);
        expect(headers[0]).toHaveTextContent("Budget 2020");
        expect(headers[1]).toHaveTextContent("January");
        expect(headers[16]).toHaveTextContent("delta/3");
    });

    it("shows actual figures up to the last filled month and planned ones after it", () => {
        mountView(payload([row()]));

        const cells = screen.getAllByRole("cell");
        // first cell is the row label, then 12 month cells
        expect(cells[1]).toHaveTextContent("100");
        expect(cells[2]).toHaveTextContent("200");
        expect(cells[3]).toHaveTextContent("300");
        expect(cells[4]).toHaveTextContent("150"); // April falls back to planned
    });

    it("shows the summary figures at the end of the row", () => {
        mountView(payload([row()]));

        const cells = screen.getAllByRole("cell");
        expect(cells[13]).toHaveTextContent("600"); // actualSum
        expect(cells[14]).toHaveTextContent("200"); // actualAvg
        expect(cells[15]).toHaveTextContent("150"); // plannedAvgToFilledMonth
        expect(cells[16]).toHaveTextContent("50");  // deltaAvg
    });

    it("keeps sub rows hidden until the row is clicked", () => {
        const parent = row({
            subRows: [row({id: "e1.1", name: "Supermarket", subRows: undefined})],
        });
        mountView(payload([parent]));

        expect(screen.queryByText("Supermarket")).not.toBeInTheDocument();

        fireEvent.click(screen.getByText(/Groceries/));

        expect(screen.getByText("Supermarket")).toBeInTheDocument();
    });

    it("reveals the planned and difference rows from the row toggle", () => {
        mountView(payload([row()]));

        expect(screen.queryByText("Planned")).not.toBeInTheDocument();

        fireEvent.click(screen.getAllByRole("button", {name: "expand row"})[0]);

        expect(screen.getByText("Planned")).toBeInTheDocument();
        expect(screen.getByText("Difference")).toBeInTheDocument();
    });

    it("computes the difference row as actual minus planned", () => {
        mountView(payload([row()]));
        fireEvent.click(screen.getAllByRole("button", {name: "expand row"})[0]);

        const differenceRow = screen.getAllByRole("row")
            .find((candidate) => within(candidate).queryByText("Difference"));
        const cells = within(differenceRow).getAllByRole("cell");
        expect(cells[0]).toHaveTextContent("-50");  // 100 - 150
        expect(cells[1]).toHaveTextContent("50");   // 200 - 150
        expect(cells[2]).toHaveTextContent("150");  // 300 - 150
        expect(cells[3]).toHaveTextContent("-");    // beyond the filled months
    });

    it("renders one row per budget line", () => {
        mountView(payload([row({id: "e1", name: "Groceries"}), row({id: "e2", name: "Fuel"})]));

        expect(screen.getByText(/Groceries/)).toBeInTheDocument();
        expect(screen.getByText(/Fuel/)).toBeInTheDocument();
    });

    it("renders the real backend payload without error", () => {
        mountView(realBudget);

        expect(screen.getAllByRole("columnheader")).toHaveLength(realBudget.columns.length);
        // one table row per budget line, before any expansion
        expect(screen.getAllByRole("row")).toHaveLength(realBudget.rows.length + 1);
    });
});
