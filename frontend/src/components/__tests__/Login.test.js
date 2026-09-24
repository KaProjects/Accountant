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
        axios.mockResolvedValue({data: "a-token"});
        render(<Login setToken={jest.fn()}/>);

        submitCredentials("stanley", "secret");

        await waitFor(() => expect(axios).toHaveBeenCalled());
        expect(axios).toHaveBeenCalledWith(expect.objectContaining({
            method: "post",
            url: "/api/authenticate",
            data: {username: "stanley", password: "secret"},
        }));
    });

    it("hands the returned token to the parent", async () => {
        const setToken = jest.fn();
        axios.mockResolvedValue({data: "a-token"});
        render(<Login setToken={setToken}/>);

        submitCredentials("stanley", "secret");

        await waitFor(() => expect(setToken).toHaveBeenCalledWith("a-token"));
    });

    it("reports a rejected login using the response status and body", async () => {
        const setToken = jest.fn();
        axios.mockRejectedValue({
            response: {status: 401, statusText: "Unauthorized", data: "bad credentials"},
        });
        render(<Login setToken={setToken}/>);

        submitCredentials("stanley", "wrong");

        expect(await screen.findByRole("alert"))
            .toHaveTextContent("401 Unauthorized: bad credentials");
        expect(setToken).not.toHaveBeenCalled();
    });

    it("reports a transport failure when there is no response at all", async () => {
        axios.mockRejectedValue({code: "ERR_NETWORK", message: "Network Error"});
        render(<Login setToken={jest.fn()}/>);

        submitCredentials("stanley", "secret");

        expect(await screen.findByRole("alert")).toHaveTextContent("ERR_NETWORK Network Error");
    });

    it("does not submit anything until the form is submitted", () => {
        render(<Login setToken={jest.fn()}/>);

        fireEvent.change(screen.getByLabelText("Username"), {target: {value: "stanley"}});

        expect(axios).not.toHaveBeenCalled();
    });
});
