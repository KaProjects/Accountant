import {fireEvent, render, screen, within} from "@testing-library/react";
import {MemoryRouter} from "react-router-dom";
import FinancialAssets from "../FinancialAssets";
import MainBar from "../../components/MainBar";
import AppState from "../../state/appState";
import {useData} from "../../fetch";

jest.mock("../../fetch");
// the chart itself is recharts; its rendering is not what this view is responsible for
// the slider is recharts' too; a button stands in for dragging it to the last two months, from the point before them
jest.mock("../../components/FinancialChart", () => (props) => (
    <div data-testid="financial-chart" data-decomposed={String(props.decomposedFunding)}
        >
        <button onClick={() => props.onRangeChange({from: props.data.length - 3, to: props.data.length - 1})}>
            narrow
        </button>
    </div>
));

const account = (overrides = {}) => ({
    id: "230.1",
    name: "Pension fund",
    active: true,
    currentReturn: 12,
    currentValue: 1120,
    initialValue: 1000,
    withdrawalsSum: 0,
    depositsSum: 100,
    balances: [1000, 1100, 1120],
    funding: [1000, 1050, 1100],
    labels: ["1", "2", "3"],
    deposits: [0, 50, 50],
    withdrawals: [0, 0, 0],
    cumulativeDeposits: [0, 50, 100],
    cumulativeWithdrawals: [0, 0, 0],
    ...overrides,
});

const payload = (accounts) => ({groups: [{name: "INVESTMENTS", accounts}]});

/**
 * The view with the main bar and the real shared state, as the app has them: the groups are tabs
 * in the bar, so the two are tested together.
 */
const mountView = (data, {loaded = true} = {}) => {
    useData.mockReturnValue({data, loaded, error: null});
    return render(
        <MemoryRouter initialEntries={["/financial/assets"]}>
            <AppState><MainBar/><FinancialAssets/></AppState>
        </MemoryRouter>
    );
};

const panels = () => screen.queryAllByTestId("asset-panel");
const panelOf = (name) => panels().find((panel) => within(panel).queryByText(name));

describe("FinancialAssets", () => {
    beforeEach(() => jest.clearAllMocks());

    it("requests every year's assets, and is no yearly page", () => {
        mountView(payload([account()]));

        expect(useData).toHaveBeenCalledWith("/financial/assets");
        // a yearly page would show its year and the arrows to step through them
        expect(screen.queryByText(String(new Date().getFullYear()))).not.toBeInTheDocument();
    });

    it("shows the loader until the data arrives", () => {
        mountView(null, {loaded: false});

        expect(screen.getByRole("progressbar")).toBeInTheDocument();
    });

    it("puts no heading over the group: its tab names it", () => {
        mountView(payload([account(), account({id: "230.0", name: "Old fund", active: false})]));

        expect(screen.queryByRole("heading", {name: "Investments"})).not.toBeInTheDocument();
        expect(screen.queryByText(/held/)).not.toBeInTheDocument();
    });

    it("shows every asset still held open, with its figures and chart, in the order given", () => {
        mountView(payload([account(), account({id: "230.2", name: "Brokerage"})]));

        expect(panels()).toHaveLength(2);
        expect(within(panels()[0]).getByText("Pension fund")).toBeInTheDocument();
        expect(within(panels()[1]).getByText("Brokerage")).toBeInTheDocument();
        expect(screen.getAllByTestId("financial-chart")).toHaveLength(2);
        expect(within(panelOf("Pension fund")).getAllByText("+12%").length).toBeGreaterThan(0);
        expect(within(panelOf("Pension fund")).getByText("230.1")).toBeInTheDocument();
    });

    it("keeps the historical assets folded away until asked for", () => {
        mountView(payload([account(), account({id: "230.0", name: "Old fund", active: false})]));

        expect(panelOf("Old fund")).toBeUndefined();
        const toggle = screen.getByRole("button", {name: /Historical \(1\)/});
        expect(toggle).toHaveAttribute("aria-expanded", "false");

        fireEvent.click(toggle);

        expect(toggle).toHaveAttribute("aria-expanded", "true");
        expect(panelOf("Old fund")).toBeDefined();
    });

    it("puts the historical assets before the ones still held", () => {
        mountView(payload([account(), account({id: "230.0", name: "Old fund", active: false})]));
        fireEvent.click(screen.getByRole("button", {name: /Historical/}));

        expect(within(panels()[0]).getByText("Old fund")).toBeInTheDocument();
        expect(within(panels()[1]).getByText("Pension fund")).toBeInTheDocument();
    });

    it("offers no historical section in a group with nothing sold", () => {
        mountView(payload([account()]));

        expect(screen.queryByRole("button", {name: /Historical/})).not.toBeInTheDocument();
    });

    it("shows a negative return without a plus sign", () => {
        mountView(payload([account({currentReturn: -5})]));

        expect(screen.getAllByText("-5%").length).toBeGreaterThan(0);
    });

    it("toggles the funding decomposition of one asset only", () => {
        mountView(payload([account(), account({id: "230.2", name: "Brokerage"})]));

        fireEvent.click(within(panelOf("Pension fund")).getByRole("checkbox"));

        expect(within(panelOf("Pension fund")).getByTestId("financial-chart")).toHaveAttribute("data-decomposed", "true");
        expect(within(panelOf("Brokerage")).getByTestId("financial-chart")).toHaveAttribute("data-decomposed", "false");
    });

    it("opens a fully withdrawn asset with its funding decomposed", () => {
        mountView(payload([account({balances: [1000, 500, 0]})]));

        expect(screen.getByRole("checkbox")).toBeChecked();
    });

    describe("one group at a time", () => {
        const twoGroups = {groups: [
            {name: "INVESTMENTS", accounts: [account()]},
            {name: "CRYPTO", accounts: [account({id: "233.0", name: "Bitcoin"})]},
        ]};

        it("offers the groups as tabs in the main bar, and shows the first", () => {
            mountView(twoGroups);

            const tabs = screen.getAllByRole("tab");
            expect(tabs.map((tab) => tab.textContent)).toEqual(["Investments", "Crypto"]);
            expect(tabs[0]).toHaveAttribute("aria-selected", "true");
            expect(screen.getByText("Pension fund")).toBeInTheDocument();
            expect(screen.queryByText("Bitcoin")).not.toBeInTheDocument();
        });

        it("shows the group whose tab is chosen", () => {
            mountView(twoGroups);

            fireEvent.click(screen.getByRole("tab", {name: "Crypto"}));

            expect(screen.getByRole("tab", {name: "Crypto"})).toHaveAttribute("aria-selected", "true");
            expect(screen.getByText("Bitcoin")).toBeInTheDocument();
            expect(screen.queryByText("Pension fund")).not.toBeInTheDocument();
        });

        it("folds the history away again when another group is chosen", () => {
            const withHistory = {groups: [
                {name: "INVESTMENTS", accounts: [account(), account({id: "230.0", name: "Old fund", active: false})]},
                {name: "CRYPTO", accounts: [account({id: "233.0", name: "Bitcoin"}), account({id: "233.1", name: "Old coin", active: false})]},
            ]};
            mountView(withHistory);
            fireEvent.click(screen.getByRole("button", {name: /Historical/}));

            fireEvent.click(screen.getByRole("tab", {name: "Crypto"}));

            expect(screen.getByRole("button", {name: /Historical/})).toHaveAttribute("aria-expanded", "false");
            expect(screen.queryByText("Old coin")).not.toBeInTheDocument();
        });

        it("offers no tabs until the groups are known", () => {
            mountView(null, {loaded: false});

            expect(screen.queryAllByRole("tab")).toHaveLength(0);
        });

        it("withdraws the tabs when the page is left", () => {
            useData.mockReturnValue({data: twoGroups, loaded: true, error: null});
            const Pages = ({showAssets}) => (
                <MemoryRouter><AppState><MainBar/>{showAssets && <FinancialAssets/>}</AppState></MemoryRouter>
            );
            const {rerender} = render(<Pages showAssets={true}/>);
            expect(screen.getAllByRole("tab")).toHaveLength(2);

            rerender(<Pages showAssets={false}/>);

            expect(screen.queryAllByRole("tab")).toHaveLength(0);
        });
    });

    describe("the slider under a chart", () => {
        it("starts on the asset's whole history, and heads its figures with it", () => {
            mountView(payload([account()]));

            // from the 1000 it opened with to the 1120 it is worth at the end of month 3, 100 put in
            expect(screen.getByText("0 – 3")).toBeInTheDocument();
            const card = screen.getByText("Start Value").closest(".MuiCard-root");
            expect(within(card).getByText("+1.82%")).toBeInTheDocument();
        });

        it("works the figures out again for the stretch it picks", () => {
            mountView(payload([account()]));

            fireEvent.click(screen.getByRole("button", {name: "narrow"}));

            // from the end of month 1 (worth 1000) to the end of month 3 (worth 1120), 100 put in
            expect(screen.getByText("1 – 3")).toBeInTheDocument();
            expect(screen.getByText("Start Value")).toBeInTheDocument();
            const card = screen.getByText("Start Value").closest(".MuiCard-root");
            expect(within(card).getByText("+1.82%")).toBeInTheDocument();
        });
    });
});
