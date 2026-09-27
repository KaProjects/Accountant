import {fireEvent, render, screen} from "@testing-library/react";
import FinancialAssets from "../FinancialAssets";
import {useData} from "../../fetch";
import {useParams} from "react-router-dom";

jest.mock("../../fetch");
jest.mock("react-router-dom", () => ({
    ...jest.requireActual("react-router-dom"),
    useParams: jest.fn(),
}));
// the chart itself is recharts; its rendering is not what this view is responsible for
jest.mock("../../components/FinancialChart", () => (props) => (
    <div data-testid="financial-chart" data-decomposed={String(props.decomposedFunding)}/>
));

const account = (overrides = {}) => ({
    name: "Pension fund",
    currentReturn: 12,
    currentValue: 1120,
    initialValue: 1000,
    withdrawalsSum: 0,
    depositsSum: 100,
    balances: [1000, 1100, 1120],
    funding: [1000, 1050, 1100],
    labels: ["1", "2", "3"],
    cumulativeDeposits: [0, 50, 100],
    cumulativeWithdrawals: [0, 0, 0],
    ...overrides,
});

const payload = (accounts) => ({groups: [{name: "Investments", accounts}]});

const renderView = (data, {loaded = true, params = {}} = {}) => {
    useParams.mockReturnValue(params);
    useData.mockReturnValue({data, loaded, error: null});
    const setYearly = jest.fn();
    render(<FinancialAssets year={2020} setYearly={setYearly}/>);
    return {setYearly};
};

describe("FinancialAssets", () => {
    beforeEach(() => jest.clearAllMocks());

    it("requests the year's assets and switches to yearly mode", () => {
        const {setYearly} = renderView(payload([account()]));

        expect(useData).toHaveBeenCalledWith("/financial/assets/2020");
        expect(setYearly).toHaveBeenCalledWith(true);
    });

    it("requests all years without a year on the overall route", () => {
        const {setYearly} = renderView(payload([account()]), {params: {all: "all"}});

        expect(useData).toHaveBeenCalledWith("/financial/assets/");
        expect(setYearly).toHaveBeenCalledWith(false);
    });

    it("shows the loader until the data arrives", () => {
        renderView(null, {loaded: false});

        expect(screen.getByRole("progressbar")).toBeInTheDocument();
    });

    it("lists each group and the accounts inside it", () => {
        renderView(payload([account(), account({name: "Brokerage"})]));

        expect(screen.getByText("Investments")).toBeInTheDocument();
        expect(screen.getByText("Pension fund")).toBeInTheDocument();
        expect(screen.getByText("Brokerage")).toBeInTheDocument();
    });

    it("keeps the chart collapsed until the account is opened", () => {
        renderView(payload([account()]));

        expect(screen.queryByTestId("financial-chart")).not.toBeInTheDocument();
    });

    it("opens the chart and the summary figures for the chosen account", () => {
        renderView(payload([account()]));

        fireEvent.click(screen.getByText("Pension fund"));

        expect(screen.getByTestId("financial-chart")).toBeInTheDocument();
        expect(screen.getByText("Current Return")).toBeInTheDocument();
        expect(screen.getByText("+12%")).toBeInTheDocument();
        expect(screen.getByText("1120")).toBeInTheDocument();
    });

    it("shows a negative return without a plus sign", () => {
        renderView(payload([account({currentReturn: -5})]));

        fireEvent.click(screen.getByText("Pension fund"));

        expect(screen.getByText("-5%")).toBeInTheDocument();
    });

    it("opening one account marks any other as closed", () => {
        // the collapse animates out, so the expand icons are what update
        // synchronously - exactly one account is ever marked open
        renderView(payload([account(), account({name: "Brokerage"})]));
        expect(screen.queryAllByTestId("ExpandLessIcon")).toHaveLength(0);

        fireEvent.click(screen.getByText("Pension fund"));
        expect(screen.getAllByTestId("ExpandLessIcon")).toHaveLength(1);

        fireEvent.click(screen.getByText("Brokerage"));
        expect(screen.getAllByTestId("ExpandLessIcon")).toHaveLength(1);
        expect(screen.getAllByTestId("ExpandMoreIcon")).toHaveLength(1);
    });

    it("toggles the funding decomposition", () => {
        renderView(payload([account()]));
        fireEvent.click(screen.getByText("Pension fund"));

        const chart = screen.getByTestId("financial-chart");
        const initial = chart.getAttribute("data-decomposed");

        fireEvent.click(screen.getByRole("checkbox"));

        expect(screen.getByTestId("financial-chart").getAttribute("data-decomposed")).not.toBe(initial);
    });

    it("defaults the decomposition on when the account is fully withdrawn", () => {
        renderView(payload([account({balances: [1000, 500, 0]})]));

        fireEvent.click(screen.getByText("Pension fund"));

        expect(screen.getByRole("checkbox")).toBeChecked();
    });
});
