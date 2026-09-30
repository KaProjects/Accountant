import {fireEvent, render, screen, waitFor, within} from "@testing-library/react";
import AdminSync from "../AdminSync";
import {runSync} from "../../services/sync";

jest.mock("../../services/sync", () => ({
    ...jest.requireActual("../../services/sync"),
    runSync: jest.fn(),
}));

const renderView = () => {
    const setYearly = jest.fn();
    render(<AdminSync year={2020} setYearly={setYearly}/>);
    return {setYearly};
};

describe("AdminSync", () => {
    beforeEach(() => jest.clearAllMocks());

    it("offers each sync action, naming the selected year", () => {
        renderView();

        expect(screen.getByText("Sync 2020")).toBeInTheDocument();
        expect(screen.getByText("Sync All Years")).toBeInTheDocument();
        expect(screen.getByText("Sync All Years & Validate")).toBeInTheDocument();
    });

    it("shows nothing until an action has been run", () => {
        renderView();

        expect(screen.queryByTestId("result-all")).not.toBeInTheDocument();
    });

    it("runs the endpoint of the action that was clicked", async () => {
        runSync.mockResolvedValue({ok: true, status: "200 OK", body: "2020 synced"});
        renderView();

        fireEvent.click(screen.getByTestId("run-year"));

        await waitFor(() => expect(runSync).toHaveBeenCalledWith("/sync/2020"));
    });

    it("reports the status and the response body of a successful run", async () => {
        runSync.mockResolvedValue({ok: true, status: "200 OK", body: "2020 synced\n2021 synced"});
        renderView();

        fireEvent.click(screen.getByTestId("run-all"));

        const result = await screen.findByTestId("result-all");
        expect(within(result).getByText("200 OK")).toBeInTheDocument();
        expect(within(result).getByText(/2021 synced/)).toBeInTheDocument();
    });

    it("reports a failure as an error, keeping the body that explains it", async () => {
        // A year that fails its checks arrives as 406 whose body is the report, so the body
        // matters more than the status and must not be swallowed.
        runSync.mockResolvedValue({ok: false, status: "406 Not Acceptable", body: "2019: data invalid"});
        renderView();

        fireEvent.click(screen.getByTestId("run-validate"));

        const result = await screen.findByTestId("result-validate");
        expect(within(result).getByText("406 Not Acceptable")).toBeInTheDocument();
        expect(within(result).getByText(/data invalid/)).toBeInTheDocument();
        expect(result.className).toMatch(/colorError|standardError/);
    });

    it("keeps the results of different actions apart", async () => {
        runSync.mockResolvedValueOnce({ok: true, status: "200 OK", body: "all synced"});
        renderView();

        fireEvent.click(screen.getByTestId("run-all"));
        await screen.findByTestId("result-all");

        expect(screen.queryByTestId("result-year")).not.toBeInTheDocument();
        expect(screen.queryByTestId("result-validate")).not.toBeInTheDocument();
    });

    it("blocks the other actions while one is running, so two do not rebuild at once", async () => {
        let finish;
        runSync.mockReturnValue(new Promise((resolve) => {finish = resolve}));
        renderView();

        fireEvent.click(screen.getByTestId("run-all"));

        await waitFor(() => expect(screen.getByTestId("run-year")).toBeDisabled());
        finish({ok: true, status: "200 OK", body: ""});
        await waitFor(() => expect(screen.getByTestId("run-year")).toBeEnabled());
    });
});
