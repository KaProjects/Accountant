import axios from "axios";
import {credentialed, mutatingJson, hasSession} from "../session";

jest.mock("axios");

describe("hasSession", () => {
    beforeEach(() => jest.clearAllMocks());

    it("asks the backend, sending the cookie the page cannot read", async () => {
        axios.get.mockResolvedValue({status: 204});

        expect(await hasSession()).toBe(true);
        expect(axios.get).toHaveBeenCalledWith("/api/authenticate", {withCredentials: true});
    });

    it("reports no session when the backend refuses", async () => {
        axios.get.mockRejectedValue({response: {status: 401, statusText: "Unauthorized"}});

        expect(await hasSession()).toBe(false);
    });

    it("reports no session when the backend cannot be reached at all", async () => {
        axios.get.mockRejectedValue({code: "ERR_NETWORK", message: "Network Error"});

        expect(await hasSession()).toBe(false);
    });

    it("sends credentials on every request that uses it", () => {
        expect(credentialed).toEqual({withCredentials: true});
    });

    it("adds the client header to anything that changes something", () => {
        expect(mutatingJson).toEqual({
            withCredentials: true,
            headers: {"Content-Type": "application/json", "X-Accountant-Client": "web"},
        });
    });
});
