import {fireEvent, render, screen, waitFor} from "@testing-library/react";
import axios from "axios";
import Login from "../Login";

jest.mock("axios");

const submitCredentials = (username, password) => {
    fireEvent.change(screen.getByLabelText("Username"), {target: {value: username}});
    fireEvent.change(screen.getByLabelText("Password"), {target: {value: password}});
    fireEvent.click(screen.getByRole("button", {name: "Login"}));
};

describe("Login", () => {
    beforeEach(() => jest.clearAllMocks());

    it("posts the credentials to the authenticate endpoint", async () => {
        axios.mockResolvedValue({status: 204});
        render(<Login onAuthenticated={jest.fn()}/>);

        submitCredentials("stanley", "secret");

        await waitFor(() => expect(axios).toHaveBeenCalled());
        expect(axios).toHaveBeenCalledWith(expect.objectContaining({
            method: "post",
            url: "/api/authenticate",
            data: {username: "stanley", password: "secret"},
            // The session travels as a cookie, and the header is what tells the backend the
            // request came from this page rather than from somebody else's.
            withCredentials: true,
            headers: {"Content-Type": "application/json", "X-Accountant-Client": "web"},
        }));
    });

    it("tells the parent it is authenticated, there being no token to hand over", async () => {
        const onAuthenticated = jest.fn();
        axios.mockResolvedValue({status: 204});
        render(<Login onAuthenticated={onAuthenticated}/>);

        submitCredentials("stanley", "secret");

        await waitFor(() => expect(onAuthenticated).toHaveBeenCalled());
    });

    it("reports a rejected login using the response status and body", async () => {
        const onAuthenticated = jest.fn();
        axios.mockRejectedValue({
            response: {status: 401, statusText: "Unauthorized", data: "bad credentials"},
        });
        render(<Login onAuthenticated={onAuthenticated}/>);

        submitCredentials("stanley", "wrong");

        expect(await screen.findByRole("alert"))
            .toHaveTextContent("401 Unauthorized: bad credentials");
        expect(onAuthenticated).not.toHaveBeenCalled();
    });

    it("reports a transport failure when there is no response at all", async () => {
        axios.mockRejectedValue({code: "ERR_NETWORK", message: "Network Error"});
        render(<Login onAuthenticated={jest.fn()}/>);

        submitCredentials("stanley", "secret");

        expect(await screen.findByRole("alert")).toHaveTextContent("ERR_NETWORK Network Error");
    });

    it("centres the page on both axes over the full viewport height", () => {
        // jsdom does not lay out, so this guards the styling intent rather than the
        // rendered geometry - enough to catch a reintroduced left margin
        render(<Login onAuthenticated={jest.fn()}/>);

        expect(screen.getByTestId("login-page")).toHaveStyle({
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            minHeight: "100vh",
        });
    });

    it("does not submit anything until the form is submitted", () => {
        render(<Login onAuthenticated={jest.fn()}/>);

        fireEvent.change(screen.getByLabelText("Username"), {target: {value: "stanley"}});

        expect(axios).not.toHaveBeenCalled();
    });
});
