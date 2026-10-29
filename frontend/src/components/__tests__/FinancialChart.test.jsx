import {render} from "@testing-library/react";
import FinancialChart from "../FinancialChart";

const series = [
    {month: "0", valuation: 1000, funding: 1000, deposits: 0, withdrawals: 0},
    {month: "1", valuation: 1100, funding: 1050, deposits: 50, withdrawals: 0},
];

// eslint-disable-next-line testing-library/no-node-access -- a chart container has no accessible role
const chartOf = (container) => container.querySelector(".recharts-responsive-container");

describe("FinancialChart", () => {
    // jsdom has no layout, so a responsive chart draws no series. What is assertable is that the
    // component renders for both shapes it supports, and for no data at all.
    it("renders the combined funding view", () => {
        const {container} = render(<FinancialChart data={series} width={400} decomposedFunding={false}/>);

        expect(chartOf(container)).toBeInTheDocument();
    });

    it("renders the decomposed view, where funding is split into deposits and withdrawals", () => {
        const {container} = render(<FinancialChart data={series} width={400} decomposedFunding={true}/>);

        expect(chartOf(container)).toBeInTheDocument();
    });

    it("renders an empty series without failing", () => {
        const {container} = render(<FinancialChart data={[]} width={400} decomposedFunding={false}/>);

        expect(chartOf(container)).toBeInTheDocument();
    });
});
