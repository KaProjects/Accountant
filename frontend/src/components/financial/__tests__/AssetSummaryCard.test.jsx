import {render, screen} from "@testing-library/react";
import AssetSummaryCard from "../AssetSummaryCard";

const account = {
    currentReturn: 12,
    currentValue: 5600,
    initialValue: 5000,
    withdrawalsSum: 100,
    depositsSum: 700,
};

const returnText = () => screen.getByText(/%$/).textContent;

describe("AssetSummaryCard", () => {
    it("shows every headline figure", () => {
        render(<AssetSummaryCard account={account}/>);

        expect(screen.getByText("5600")).toBeInTheDocument();
        expect(screen.getByText("5000")).toBeInTheDocument();
        expect(screen.getByText("100")).toBeInTheDocument();
        expect(screen.getByText("700")).toBeInTheDocument();
        expect(screen.getByText("Current Value")).toBeInTheDocument();
        expect(screen.getByText("Deposits")).toBeInTheDocument();
    });

    it("signs a gain explicitly, so it cannot be mistaken for a loss", () => {
        render(<AssetSummaryCard account={account}/>);

        expect(returnText()).toBe("+12%");
    });

    it("leaves a loss and a flat return unsigned beyond their own minus", () => {
        const {rerender} = render(<AssetSummaryCard account={{...account, currentReturn: -8}}/>);
        expect(returnText()).toBe("-8%");

        rerender(<AssetSummaryCard account={{...account, currentReturn: 0}}/>);
        expect(returnText()).toBe("0%");
    });

    it("colours the return by its direction", () => {
        const colourFor = (currentReturn) => {
            const {unmount} = render(<AssetSummaryCard account={{...account, currentReturn}}/>);
            const colour = screen.getByText(/%$/).style.color;
            unmount();
            return colour;
        };

        expect(colourFor(12)).not.toBe(colourFor(-8));
        expect(colourFor(0)).not.toBe(colourFor(12));
        expect(colourFor(0)).not.toBe(colourFor(-8));
    });
});
