import {renderHook, waitFor} from "@testing-library/react";
import axios from "axios";
import {useData} from "../fetch";

jest.mock("axios");

describe("useData", () => {
    beforeEach(() => {
        jest.clearAllMocks();
        window.sessionStorage.clear();
    });

    it("requests the given path behind the /api prefix", async () => {
        axios.get.mockResolvedValue({data: {classes: []}});

        renderHook(() => useData("/schema/2020"));

        await waitFor(() => expect(axios.get).toHaveBeenCalled());
        expect(axios.get.mock.calls[0][0]).toBe("/api/schema/2020");
    });

    it("sends the stored token as a bearer header", async () => {
        window.sessionStorage.setItem("token", "abc-123");
        axios.get.mockResolvedValue({data: {}});

        renderHook(() => useData("/budget/2020"));

        await waitFor(() => expect(axios.get).toHaveBeenCalled());
        expect(axios.get.mock.calls[0][1]).toEqual({headers: {Authorization: "Bearer abc-123"}});
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

    it("reports an expired token when the backend answers 401", async () => {
        const unauthorized = new Error("Request failed with status code 401");
        unauthorized.response = {status: 401};
        axios.get.mockRejectedValue(unauthorized);
        jest.spyOn(console, "error").mockImplementation(() => {});

        const {result} = renderHook(() => useData("/budget/2020"));

        await waitFor(() => expect(result.current.error).not.toBeNull());
        expect(result.current.error.message).toBe("Token expired! Redirecting...");
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
