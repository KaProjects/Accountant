import {fireEvent, render, screen} from "@testing-library/react";
import Admin from "../Admin";
import {properties} from "../../properties";
import {isDevelopment} from "../../services/devLogin";
import {renderWithAppState} from "../../testUtils";

const mockNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
    ...jest.requireActual("react-router-dom"),
    useNavigate: () => mockNavigate,
}));


jest.mock("../../services/devLogin");

const mountView = () => {
    const setYearly = jest.fn();
    renderWithAppState(<Admin/>, {setYearly});
    return {setYearly};
};

describe("Admin", () => {
    beforeEach(() => {
        jest.clearAllMocks();
        isDevelopment.mockReturnValue(true);
    });

    it("offers the sync page and the API reference, grouped", () => {
        mountView();

        expect(screen.getByText("Data")).toBeInTheDocument();
        expect(screen.getByText("Reference")).toBeInTheDocument();
        expect(screen.getByText("Sync")).toBeInTheDocument();
        expect(screen.getByText("API Docs")).toBeInTheDocument();
    });

    it("hides the API reference outside development, where it is not built at all", () => {
        isDevelopment.mockReturnValue(false);

        mountView();

        expect(screen.queryByText("API Docs")).not.toBeInTheDocument();
        expect(screen.queryByText("Reference")).not.toBeInTheDocument();
        expect(screen.getByText("Sync")).toBeInTheDocument();
    });

    it("leaves yearly mode, because nothing here is reported per year", () => {
        const {setYearly} = mountView();

        expect(setYearly).toHaveBeenCalledWith(false);
    });

    it("navigates to the sync page", () => {
        mountView();

        fireEvent.click(screen.getByText("Sync"));

        expect(mockNavigate).toHaveBeenCalledWith("/admin/sync");
    });

    it("opens the API reference in a new tab, since it is served by the backend", () => {
        window.open = jest.fn();
        mountView();

        fireEvent.click(screen.getByText("API Docs"));

        expect(window.open).toHaveBeenCalledWith(properties.apiDocsUrl, "_blank");
    });
});
