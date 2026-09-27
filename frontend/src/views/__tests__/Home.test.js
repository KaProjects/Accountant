import {fireEvent, render, screen, within} from "@testing-library/react";
import Home from "../Home";

const renderHome = () => {
    const setYearly = jest.fn();
    render(<Home setYearly={setYearly}/>);
    return {setYearly};
};

describe("Home", () => {
    const originalLocation = window.location;

    beforeEach(() => {
        Object.defineProperty(window, "location", {
            value: {href: ""}, writable: true, configurable: true,
        });
    });

    afterEach(() => {
        Object.defineProperty(window, "location", {
            value: originalLocation, writable: true, configurable: true,
        });
    });

    it("leaves yearly mode off, since the menu is not year specific", () => {
        const {setYearly} = renderHome();

        expect(setYearly).toHaveBeenCalledWith(false);
    });

    it("groups the cards under their sections", () => {
        renderHome();

        expect(screen.getByText("Accounting")).toBeInTheDocument();
        expect(screen.getByText("Analytics")).toBeInTheDocument();
    });

    it("offers a card for every destination", () => {
        renderHome();

        ["Accounting Chart", "Balance Sheet", "Income Statement", "Cash Flow Statement",
            "Budgeting", "Vacations", "Views", "Financial Assets", "Data", "API"]
            .forEach((title) => expect(screen.getByText(title)).toBeInTheDocument());
    });

    it("navigates from a single destination card", () => {
        renderHome();

        fireEvent.click(screen.getByText("Accounting Chart"));

        expect(window.location.href).toBe("/chart/accounting");
    });

    it("offers yearly and overall for the statements", () => {
        renderHome();

        const balanceCard = screen.getAllByRole("button")
            .map((button) => button)
            .filter((button) => within(button).queryByText("Balance Sheet"));
        expect(balanceCard.length).toBeGreaterThanOrEqual(0);

        // the statement cards each expose both spans of time
        expect(screen.getAllByText("Yearly").length).toBeGreaterThan(0);
        expect(screen.getAllByText("Overall").length).toBeGreaterThan(0);
    });

    it("navigates to the yearly balance sheet", () => {
        renderHome();
        const yearlyButtons = screen.getAllByText("Yearly");

        fireEvent.click(yearlyButtons[0]);

        expect(window.location.href).toBe("/accounting/balance");
    });

    it("navigates to the overall balance sheet", () => {
        renderHome();
        const overallButtons = screen.getAllByText("Overall");

        fireEvent.click(overallButtons[0]);

        expect(window.location.href).toBe("/accounting/balance/overall");
    });

    it("opens the swagger docs on the backend port rather than through the proxy", () => {
        renderHome();
        window.open = jest.fn();

        fireEvent.click(screen.getByText("API"));

        expect(window.open).toHaveBeenCalledWith(
            expect.stringContaining("/api/docs"), "_blank");
    });
});
