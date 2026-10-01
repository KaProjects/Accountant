import {render, screen} from "@testing-library/react";
import AccountingChart from "../AccountingChart";
import {useData} from "../../fetch";
import {renderWithAppState} from "../../testUtils";

jest.mock("../../fetch");
jest.mock("recharts", () => ({
    ...jest.requireActual("recharts"),
    ResponsiveContainer: ({children}) => <div data-testid="chart">{children}</div>,
}));

const configs = [
    {id: "60", name: "Revenues", type: "SUM"},
    {id: "51", name: "Consumption", type: "SUM"},
];

const respond = ({configLoaded = true} = {}) => {
    useData.mockImplementation((path) => {
        if (path === "/chart/config") return {data: configs, loaded: configLoaded, error: null};
        if (path.startsWith("/chart/data/")) return {data: {values: [{label: "2020", sum: 10}]}, loaded: true, error: null};
        return {data: null, loaded: false, error: null};
    });
};

const mountView = (props = {}) => {
    const setYearly = jest.fn();
    const setSelectedValue = jest.fn();
    const setSelectValues = jest.fn();
    renderWithAppState(<AccountingChart/>, {
        setYearly,
        selectedValue: null,
        setSelectedValue,
        setSelectValues,
        ...props,
    });
    return {setYearly, setSelectedValue, setSelectValues};
};

describe("AccountingChart", () => {
    beforeEach(() => jest.clearAllMocks());

    it("requests the chart configuration and leaves yearly mode off", () => {
        respond();
        const {setYearly} = mountView();

        expect(useData).toHaveBeenCalledWith("/chart/config");
        expect(setYearly).toHaveBeenCalledWith(false);
    });

    it("shows the loader until the configuration arrives", () => {
        respond({configLoaded: false});
        mountView();

        expect(screen.getByRole("progressbar")).toBeInTheDocument();
    });

    it("offers a dataset selector while nothing is selected", () => {
        respond();
        mountView();

        expect(screen.getByLabelText("Select a dataset")).toBeInTheDocument();
        expect(screen.queryByTestId("chart")).not.toBeInTheDocument();
    });

    it("draws the chart for the selected dataset instead of the selector", () => {
        respond();
        mountView({selectedValue: configs[0]});

        expect(useData).toHaveBeenCalledWith("/chart/data/60");
        expect(screen.getByTestId("chart")).toBeInTheDocument();
        expect(screen.queryByLabelText("Select a dataset")).not.toBeInTheDocument();
    });
});
