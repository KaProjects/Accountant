import {renderHook, waitFor} from "@testing-library/react";
import axios from "axios";
import {useData, useEachData} from "../fetch";

jest.mock("axios");

describe("useData", () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    it("requests the given path behind the /api prefix", async () => {
        axios.get.mockResolvedValue({data: {classes: []}});

        renderHook(() => useData("/schema/2020"));

        await waitFor(() => expect(axios.get).toHaveBeenCalled());
        expect(axios.get.mock.calls[0][0]).toBe("/api/schema/2020");
    });

    it("sends the session cookie, which the page itself cannot read", async () => {
        axios.get.mockResolvedValue({data: {}});

        renderHook(() => useData("/budget/2020"));

        await waitFor(() => expect(axios.get).toHaveBeenCalled());
        expect(axios.get.mock.calls[0][1]).toEqual({withCredentials: true});
    });

    it("exposes the payload and marks the request loaded", async () => {
        const payload = {groups: [{name: "Expenses"}]};
        axios.get.mockResolvedValue({data: payload});

        const {result} = renderHook(() => useData("/budget/2020"));

        await waitFor(() => expect(result.current.loaded).toBe(true));
        expect(result.current.data).toEqual(payload);
        expect(result.current.error).toBeNull();
    });

    it("starts out empty before the request resolves", () => {
        axios.get.mockReturnValue(new Promise(() => {}));

        const {result} = renderHook(() => useData("/budget/2020"));

        expect(result.current.data).toBeNull();
        expect(result.current.loaded).toBe(false);
        expect(result.current.error).toBeNull();
    });

    it("surfaces a failure without marking the request loaded", async () => {
        const failure = new Error("Request failed");
        failure.response = {status: 500};
        axios.get.mockRejectedValue(failure);
        jest.spyOn(console, "error").mockImplementation(() => {});

        const {result} = renderHook(() => useData("/budget/2020"));

        await waitFor(() => expect(result.current.error).not.toBeNull());
        expect(result.current.loaded).toBe(false);
        expect(result.current.data).toBeNull();
    });

    it("reports an expired session when the backend answers 401", async () => {
        const unauthorized = new Error("Request failed with status code 401");
        unauthorized.response = {status: 401};
        axios.get.mockRejectedValue(unauthorized);
        jest.spyOn(console, "error").mockImplementation(() => {});

        const {result} = renderHook(() => useData("/budget/2020"));

        await waitFor(() => expect(result.current.error).not.toBeNull());
        expect(result.current.error.message).toBe("Session expired! Redirecting...");
    });

    it("fetches nothing while it is not wanted", () => {
        axios.get.mockResolvedValue({data: {}});

        renderHook(() => useData("/budget/2020", false));

        expect(axios.get).not.toHaveBeenCalled();
    });

    it("fetches once it becomes wanted", async () => {
        axios.get.mockResolvedValue({data: {}});
        const {rerender} = renderHook(({enabled}) => useData("/budget/2020", enabled),
            {initialProps: {enabled: false}});

        rerender({enabled: true});

        await waitFor(() => expect(axios.get).toHaveBeenCalledWith("/api/budget/2020", {withCredentials: true}));
    });

    it("stops being loaded while a new path is fetched, so no view renders stale data", async () => {
        // Otherwise a view whose address changed renders the previous payload against the new
        // parameters for one frame - which crashed the statement when it moved from the overall
        // view to a single year, because the rows it still held had no monthly values.
        axios.get.mockResolvedValue({data: {first: true}});
        const {result, rerender} = renderHook(({path}) => useData(path), {initialProps: {path: "/a"}});
        await waitFor(() => expect(result.current.loaded).toBe(true));

        let arrive;
        axios.get.mockReturnValue(new Promise((resolve) => {arrive = resolve}));
        rerender({path: "/b"});

        expect(result.current.loaded).toBe(false);

        arrive({data: {second: true}});
        await waitFor(() => expect(result.current.loaded).toBe(true));
        expect(result.current.data).toEqual({second: true});
    });

    it("refetches when the path changes", async () => {
        axios.get.mockResolvedValue({data: {}});

        const {rerender} = renderHook(({path}) => useData(path), {
            initialProps: {path: "/budget/2020"},
        });
        await waitFor(() => expect(axios.get).toHaveBeenCalledTimes(1));

        rerender({path: "/budget/2021"});

        await waitFor(() => expect(axios.get).toHaveBeenCalledTimes(2));
        expect(axios.get.mock.calls[1][0]).toBe("/api/budget/2021");
    });
});

describe("useEachData", () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    it("requests every path, and reports their answers together in the order asked", async () => {
        axios.get.mockImplementation((path) => Promise.resolve({data: {for: path}}));

        const {result} = renderHook(() => useEachData(["/cashflow/2019", "/cashflow/2020"]));

        await waitFor(() => expect(result.current.loaded).toBe(true));
        expect(result.current.data).toEqual([{for: "/api/cashflow/2019"}, {for: "/api/cashflow/2020"}]);
        expect(axios.get.mock.calls.map((call) => call[1])).toEqual([{withCredentials: true}, {withCredentials: true}]);
    });

    it("is not loaded until every one of them has come back", () => {
        axios.get.mockImplementation((path) => path.endsWith("2020")
            ? new Promise(() => {})
            : Promise.resolve({data: {}}));

        const {result} = renderHook(() => useEachData(["/cashflow/2019", "/cashflow/2020"]));

        expect(result.current.loaded).toBe(false);
        expect(result.current.data).toBeNull();
    });

    it("fails as soon as any one of them fails", async () => {
        const failure = new Error("Request failed");
        failure.response = {status: 500};
        axios.get.mockImplementation((path) => path.endsWith("2020")
            ? Promise.reject(failure)
            : Promise.resolve({data: {}}));
        jest.spyOn(console, "error").mockImplementation(() => {});

        const {result} = renderHook(() => useEachData(["/cashflow/2019", "/cashflow/2020"]));

        await waitFor(() => expect(result.current.error).toBe(failure));
        expect(result.current.loaded).toBe(false);
    });

    it("asks for nothing until it is wanted", () => {
        renderHook(() => useEachData(["/cashflow/2019"], false));

        expect(axios.get).not.toHaveBeenCalled();
    });

    it("asks for nothing when there is nothing to ask for, and is done at once", () => {
        // a statement with no years in it has no years to fetch, and must not wait for them for ever
        const {result} = renderHook(() => useEachData([]));

        expect(axios.get).not.toHaveBeenCalled();
        expect(result.current).toEqual({data: [], loaded: true, error: null});
    });
});
