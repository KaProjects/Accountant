import {fireEvent, screen, within} from "@testing-library/react";
import Home from "../Home";
import {renderWithAppState} from "../../testUtils";

const mockNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
    ...jest.requireActual("react-router-dom"),
    useNavigate: () => mockNavigate,
}));


const renderHome = () => {
    const setYearly = jest.fn();
    renderWithAppState(<Home/>, {setYearly});
    return {setYearly};
};

describe("Home", () => {
    beforeEach(() => jest.clearAllMocks());

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
            "Budgeting", "Vacations", "Views", "Financial Assets", "Data", "Admin"]
            .forEach((title) => expect(screen.getByText(title)).toBeInTheDocument());
    });

    it("navigates from a single destination card", () => {
        renderHome();

        fireEvent.click(screen.getByText("Accounting Chart"));

        expect(mockNavigate).toHaveBeenCalledWith("/chart/accounting");
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

        expect(mockNavigate).toHaveBeenCalledWith("/accounting/balance");
    });

    it("navigates to the overall balance sheet", () => {
        renderHome();
        const overallButtons = screen.getAllByText("Overall");

        fireEvent.click(overallButtons[0]);

        expect(mockNavigate).toHaveBeenCalledWith("/accounting/balance/overall");
    });

    // The swagger link moved onto the admin page, which is covered by Admin.test.js.
    it("opens the admin page", () => {
        renderHome();

        fireEvent.click(screen.getByText("Admin"));

        expect(mockNavigate).toHaveBeenCalledWith("/admin");
    });
});
