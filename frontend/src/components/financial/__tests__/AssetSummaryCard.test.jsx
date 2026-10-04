import {render, screen} from "@testing-library/react";
import AssetSummaryCard from "../AssetSummaryCard";

const figures = {
    totalReturn: 12,
    annualReturn: 4.21,
    endValue: 5600,
    startValue: 5000,
    withdrawalsSum: 100,
    depositsSum: 700,
};

const returnText = () => screen.getByText(/^[+-]?[\d.]+%$/).textContent;

describe("AssetSummaryCard", () => {
    it("shows every headline figure, with thousands separated as the statements write them", () => {
        render(<AssetSummaryCard figures={figures}/>);

        expect(screen.getByText("5,600")).toBeInTheDocument();
        expect(screen.getByText("5,000")).toBeInTheDocument();
        expect(screen.getByText("100")).toBeInTheDocument();
        expect(screen.getByText("700")).toBeInTheDocument();
    });

    it("heads the card with its stretch, and names the figures after its start and end", () => {
        render(<AssetSummaryCard figures={figures} period="2/21 – 10/26"/>);

        expect(screen.getByText("2/21 – 10/26")).toBeInTheDocument();
        expect(screen.getByText("Return")).toBeInTheDocument();
        expect(screen.getByText("End Value")).toBeInTheDocument();
        expect(screen.getByText("Start Value")).toBeInTheDocument();
        expect(screen.queryByText(/Current|Initial/)).not.toBeInTheDocument();
    });

    it("signs a gain explicitly, so it cannot be mistaken for a loss", () => {
        render(<AssetSummaryCard figures={figures}/>);

        expect(returnText()).toBe("+12%");
    });

    it("leaves a loss and a flat return unsigned beyond their own minus", () => {
        const {rerender} = render(<AssetSummaryCard figures={{...figures, totalReturn: -8}}/>);
        expect(returnText()).toBe("-8%");

        rerender(<AssetSummaryCard figures={{...figures, totalReturn: 0}}/>);
        expect(returnText()).toBe("0%");
    });

    it("shows the annual return beside the total, signed and coloured the same way", () => {
        render(<AssetSummaryCard figures={figures}/>);

        expect(screen.getByText("Annual Return")).toBeInTheDocument();
        expect(screen.getByText("+4.21% p.a.")).toHaveStyle({color: "#158615"});
    });

    it("shows a loss per year in red", () => {
        render(<AssetSummaryCard figures={{...figures, annualReturn: -3.5}}/>);

        expect(screen.getByText("-3.5% p.a.")).toHaveStyle({color: "#b93333"});
    });

    it("shows no annual return for a stretch shorter than a year", () => {
        render(<AssetSummaryCard figures={{...figures, annualReturn: null}}/>);

        expect(screen.getByText("—")).toBeInTheDocument();
    });
});
