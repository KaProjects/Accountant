import {fireEvent, render, screen} from "@testing-library/react";
import App from "../App";

describe("App", () => {
    beforeEach(() => window.sessionStorage.clear());

    it("shows the login form when there is no token", () => {
        render(<App/>);

        expect(screen.getByLabelText("Username")).toBeInTheDocument();
        expect(screen.getByLabelText("Password")).toBeInTheDocument();
        expect(screen.queryByRole("button", {name: "Logout"})).not.toBeInTheDocument();
    });

    it("shows the application once a token is stored in the session", () => {
        window.sessionStorage.setItem("token", "a-token");

        render(<App/>);

        expect(screen.getByRole("button", {name: "Logout"})).toBeInTheDocument();
        expect(screen.queryByLabelText("Username")).not.toBeInTheDocument();
    });

    it("logging out clears the stored token and returns to the login form", () => {
        window.sessionStorage.setItem("token", "a-token");
        render(<App/>);

        fireEvent.click(screen.getByRole("button", {name: "Logout"}));

        expect(window.sessionStorage.getItem("token")).toBeNull();
        expect(screen.getByLabelText("Username")).toBeInTheDocument();
    });

    it("keeps using a token held only in the session storage across renders", () => {
        window.sessionStorage.setItem("token", "a-token");
        const {unmount} = render(<App/>);
        expect(screen.getByRole("button", {name: "Logout"})).toBeInTheDocument();
        unmount();

        render(<App/>);

        expect(screen.getByRole("button", {name: "Logout"})).toBeInTheDocument();
    });
});
