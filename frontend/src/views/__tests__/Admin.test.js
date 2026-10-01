import {fireEvent, render, screen} from "@testing-library/react";
import Admin from "../Admin";
import {properties} from "../../properties";
import {isDevelopment} from "../../services/devLogin";

jest.mock("../../services/devLogin");

const renderView = () => {
    const setYearly = jest.fn();
    render(<Admin setYearly={setYearly}/>);
    return {setYearly};
};

describe("Admin", () => {
    beforeEach(() => {
        jest.clearAllMocks();
        isDevelopment.mockReturnValue(true);
    });

    it("offers the sync page and the API reference, grouped", () => {
        renderView();

        expect(screen.getByText("Data")).toBeInTheDocument();
        expect(screen.getByText("Reference")).toBeInTheDocument();
        expect(screen.getByText("Sync")).toBeInTheDocument();
        expect(screen.getByText("API Docs")).toBeInTheDocument();
    });

    it("hides the API reference outside development, where it is not built at all", () => {
        isDevelopment.mockReturnValue(false);

        renderView();

        expect(screen.queryByText("API Docs")).not.toBeInTheDocument();
        expect(screen.queryByText("Reference")).not.toBeInTheDocument();
        expect(screen.getByText("Sync")).toBeInTheDocument();
    });

    it("leaves yearly mode, because nothing here is reported per year", () => {
        const {setYearly} = renderView();

        expect(setYearly).toHaveBeenCalledWith(false);
    });

    it("navigates to the sync page", () => {
        delete window.location;
        window.location = {href: ""};
        renderView();

        fireEvent.click(screen.getByText("Sync"));

        expect(window.location.href).toBe("/admin/sync");
    });

    it("opens the API reference in a new tab, since it is served by the backend", () => {
        window.open = jest.fn();
        renderView();

        fireEvent.click(screen.getByText("API Docs"));

        expect(window.open).toHaveBeenCalledWith(properties.apiDocsUrl, "_blank");
    });
});
