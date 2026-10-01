import {render, screen} from "@testing-library/react";
import AccountingBarChart from "../AccountingBarChart";
import {useData} from "../../../fetch";

jest.mock("../../../fetch");

const config = {id: "55b", name: "Other expenses", type: "BALANCE"};

describe("AccountingBarChart", () => {
    beforeEach(() => {
        jest.clearAllMocks();
        useData.mockReturnValue({
            data: {values: [{label: "2020", balance: 10}, {label: "2021", balance: 20}]},
            loaded: true,
            error: null,
        });
    });

    it("asks for the data of the chart it was configured with", () => {
        render(<AccountingBarChart config={config}/>);

        expect(useData).toHaveBeenCalledWith("/chart/data/55b");
    });

    it("renders the chart once the data has arrived", () => {
        const {container} = render(<AccountingBarChart config={config}/>);

        // eslint-disable-next-line testing-library/no-container, testing-library/no-node-access -- a chart container has no accessible role to query by
        expect(container.querySelector(".recharts-responsive-container")).toBeInTheDocument();
    });

    it("renders no chart until the data arrives", () => {
        useData.mockReturnValue({data: null, loaded: false, error: null});

        const {container} = render(<AccountingBarChart config={config}/>);

        // eslint-disable-next-line testing-library/no-container, testing-library/no-node-access -- a chart container has no accessible role to query by
        expect(container.querySelector(".recharts-responsive-container")).not.toBeInTheDocument();
    });

    it("surfaces a failure instead of the chart", () => {
        useData.mockReturnValue({data: null, loaded: false, error: {message: "boom"}});

        render(<AccountingBarChart config={config}/>);

        expect(screen.getByText(/boom/)).toBeInTheDocument();
    });
});
