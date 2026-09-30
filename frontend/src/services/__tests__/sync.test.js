import axios from "axios";
import {runSync, syncActions} from "../sync";

jest.mock("axios");

describe("syncActions", () => {
    it("names the year of the single-year action", () => {
        const actions = syncActions(2019);

        expect(actions.map((action) => action.path))
            .toEqual(["/sync/2019", "/sync/all", "/sync/all/validate"]);
        expect(actions[0].label).toBe("Sync 2019");
    });
});

describe("runSync", () => {
    beforeEach(() => {
        jest.clearAllMocks();
        sessionStorage.setItem("token", "a-token");
    });

    it("sends the stored token, because every endpoint now requires one", async () => {
        axios.get.mockResolvedValue({status: 200, statusText: "OK", data: "done"});

        await runSync("/sync/all");

        expect(axios.get).toHaveBeenCalledWith("/api/sync/all",
            {headers: {Authorization: "Bearer a-token"}});
    });

    it("reports the status and body of a successful run", async () => {
        axios.get.mockResolvedValue({status: 200, statusText: "OK", data: "2020 synced"});

        expect(await runSync("/sync/2020")).toEqual({ok: true, status: "200 OK", body: "2020 synced"});
    });

    it("reports a rejected run without throwing, keeping the body", async () => {
        axios.get.mockRejectedValue({
            response: {status: 406, statusText: "Not Acceptable", data: "2019: data invalid"},
        });

        expect(await runSync("/sync/all/validate"))
            .toEqual({ok: false, status: "406 Not Acceptable", body: "2019: data invalid"});
    });

    it("reports a transport failure when there was no response at all", async () => {
        axios.get.mockRejectedValue({code: "ERR_NETWORK", message: "Network Error"});

        expect(await runSync("/sync/all"))
            .toEqual({ok: false, status: "no response", body: "ERR_NETWORK Network Error"});
    });

    it("reads the details out of the container's own error object", async () => {
        axios.get.mockRejectedValue({
            response: {status: 500, statusText: "Internal Server Error", data: {details: "NullPointerException"}},
        });

        expect(await runSync("/sync/all"))
            .toEqual({ok: false, status: "500 Internal Server Error", body: "NullPointerException"});
    });
});
