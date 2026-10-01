import {render, screen} from "@testing-library/react";
import App from "../App";
import {devLogin, isDevelopment} from "../services/devLogin";
import {hasSession} from "../services/session";

jest.mock("../services/devLogin");
jest.mock("../services/session");

const application = () => screen.findByRole("button", {name: "open drawer"});

describe("App", () => {
    beforeEach(() => {
        jest.clearAllMocks();
        // The suite runs as a test build, where there is no automatic login.
        isDevelopment.mockReturnValue(false);
        devLogin.mockResolvedValue(false);
        hasSession.mockResolvedValue(false);
    });

    it("shows the login form when the backend reports no session", async () => {
        render(<App/>);

        expect(await screen.findByLabelText("Username")).toBeInTheDocument();
        expect(screen.getByLabelText("Password")).toBeInTheDocument();
        expect(screen.queryByRole("button", {name: "open drawer"})).not.toBeInTheDocument();
    });

    it("shows the application when the backend confirms a session", async () => {
        hasSession.mockResolvedValue(true);

        render(<App/>);

        expect(await application()).toBeInTheDocument();
        expect(screen.queryByLabelText("Username")).not.toBeInTheDocument();
    });

    it("shows neither while it is still asking, so the login form does not flash", () => {
        hasSession.mockReturnValue(new Promise(() => {}));

        render(<App/>);

        expect(screen.queryByLabelText("Username")).not.toBeInTheDocument();
        expect(screen.queryByRole("button", {name: "open drawer"})).not.toBeInTheDocument();
    });

    it("asks the backend again on every mount, since the cookie cannot be read here", async () => {
        hasSession.mockResolvedValue(true);
        const {unmount} = render(<App/>);
        expect(await application()).toBeInTheDocument();
        unmount();

        render(<App/>);

        expect(await application()).toBeInTheDocument();
        expect(hasSession).toHaveBeenCalledTimes(2);
    });

    it("logs a development build in by itself when there is no session yet", async () => {
        isDevelopment.mockReturnValue(true);
        devLogin.mockResolvedValue(true);

        render(<App/>);

        expect(await application()).toBeInTheDocument();
        expect(devLogin).toHaveBeenCalled();
    });

    it("does not log in automatically when the session is already good", async () => {
        isDevelopment.mockReturnValue(true);
        hasSession.mockResolvedValue(true);

        render(<App/>);

        expect(await application()).toBeInTheDocument();
        expect(devLogin).not.toHaveBeenCalled();
    });

    it("shows a not-found page for a route that matches nothing", async () => {
        hasSession.mockResolvedValue(true);
        window.history.pushState({}, "", "/no-such-page");

        render(<App/>);

        expect(await screen.findByText("404 Page not found")).toBeInTheDocument();
        window.history.pushState({}, "", "/");
    });

    it("falls back to the login form when the automatic login is refused", async () => {
        isDevelopment.mockReturnValue(true);
        devLogin.mockResolvedValue(false);

        render(<App/>);

        expect(await screen.findByLabelText("Username")).toBeInTheDocument();
    });
});
