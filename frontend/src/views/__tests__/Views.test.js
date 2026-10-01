import {fireEvent, render, screen, within} from "@testing-library/react";
import Views from "../Views";
import {useData} from "../../fetch";
import {useParams} from "react-router-dom";
import {renderWithAppState} from "../../testUtils";

jest.mock("../../fetch");
jest.mock("react-router-dom", () => ({
    ...jest.requireActual("react-router-dom"),
    useParams: jest.fn(),
}));
jest.mock("../../components/VacationChart", () => () => <div data-testid="vacation-chart"/>);

const view = (overrides = {}) => ({
    name: "SummerTrip2020",
    expenses: 1234,
    chartData: [],
    transactions: [
        {date: "0107", amount: 500, debit: "510.0", credit: "210.0", description: "hotel"},
    ],
    ...overrides,
});

const payload = (views) => ({
    columns: ["Date", "Amount", "Debit", "Credit", "Description"],
    views,
});

const mountView = (data, {loaded = true, params = {}} = {}) => {
    useParams.mockReturnValue(params);
    useData.mockReturnValue({data, loaded, error: null});
    const setYearly = jest.fn();
    renderWithAppState(<Views/>, {year: 2020, setYearly});
    return {setYearly};
};

describe("Views", () => {
    beforeEach(() => jest.clearAllMocks());

    it("requests the year's views and switches to yearly mode", () => {
        const {setYearly} = mountView(payload([view()]));

        expect(useData).toHaveBeenCalledWith("/view/2020");
        expect(setYearly).toHaveBeenCalledWith(true);
    });

    it("requests the vacation views on the vacation route", () => {
        mountView(payload([view()]), {params: {vacation: "vacation"}});

        expect(useData).toHaveBeenCalledWith("/view/2020/vacation");
    });

    it("shows the loader until the data arrives", () => {
        mountView(null, {loaded: false});

        expect(screen.getByRole("progressbar")).toBeInTheDocument();
    });

    it("splits a run-together view name into words", () => {
        mountView(payload([view()]));

        expect(screen.getByText("Summer Trip 2 0 2 0")).toBeInTheDocument();
    });

    it("keeps the transactions collapsed until the view is opened", () => {
        mountView(payload([view()]));

        expect(screen.queryByText("hotel")).not.toBeInTheDocument();
    });

    it("opens the transactions, total and charts for a view", () => {
        mountView(payload([view()]));

        fireEvent.click(screen.getByText("Summer Trip 2 0 2 0"));

        expect(screen.getByText("hotel")).toBeInTheDocument();
        expect(screen.getByText(/Total Expenses: 1234/)).toBeInTheDocument();
        expect(screen.getAllByTestId("vacation-chart")).toHaveLength(2);
    });

    it("renders the column headings and a row per transaction", () => {
        mountView(payload([view()]));
        fireEvent.click(screen.getByText("Summer Trip 2 0 2 0"));

        expect(screen.getAllByRole("columnheader")).toHaveLength(5);
        const row = screen.getAllByRole("row")
            .find((candidate) => within(candidate).queryByText("hotel"));
        const cells = within(row).getAllByRole("cell");
        expect(cells[0]).toHaveTextContent("0107");
        expect(cells[1]).toHaveTextContent("500");
    });

    it("opening one view marks any other as closed", () => {
        mountView(payload([view(), view({name: "WinterTrip"})]));

        fireEvent.click(screen.getByText("Summer Trip 2 0 2 0"));
        expect(screen.getAllByTestId("ExpandLessIcon")).toHaveLength(1);

        fireEvent.click(screen.getByText("Winter Trip"));
        expect(screen.getAllByTestId("ExpandLessIcon")).toHaveLength(1);
    });
});
