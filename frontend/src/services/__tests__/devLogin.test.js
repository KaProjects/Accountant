import axios from "axios";
import {devLogin, isDevelopment} from "../devLogin";

jest.mock("axios");

/** Jest runs with NODE_ENV=test, so a development build has to be simulated. */
const asNodeEnv = async (value, run) => {
    const original = process.env.NODE_ENV;
    process.env.NODE_ENV = value;
    try {
        return await run();
    } finally {
        process.env.NODE_ENV = original;
    }
};

describe("devLogin", () => {
    beforeEach(() => jest.clearAllMocks());

    it("does nothing at all outside a development build", async () => {
        expect(await asNodeEnv("production", devLogin)).toBe(false);
        expect(axios.post).not.toHaveBeenCalled();
    });

    it("logs in with the development credentials, letting the backend set the cookie", async () => {
        axios.post.mockResolvedValue({status: 204});

        expect(await asNodeEnv("development", devLogin)).toBe(true);
        expect(axios.post).toHaveBeenCalledWith(
            "/api/authenticate",
            {username: "dev", password: "dev"},
            {
                withCredentials: true,
                headers: {"Content-Type": "application/json", "X-Accountant-Client": "web"},
            });
    });

    it("gives up quietly when the backend refuses, so the login screen is shown", async () => {
        // What happens when a development frontend is pointed at a real backend: the development
        // credentials are not there, and the developer should simply be asked to log in.
        axios.post.mockRejectedValue({response: {status: 401, statusText: "Unauthorized"}});

        expect(await asNodeEnv("development", devLogin)).toBe(false);
    });

    it("reports which build it is", async () => {
        expect(await asNodeEnv("development", async () => isDevelopment())).toBe(true);
        expect(await asNodeEnv("test", async () => isDevelopment())).toBe(false);
    });
});
