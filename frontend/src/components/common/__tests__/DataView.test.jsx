import {render, screen} from "@testing-library/react";
import DataView from "../DataView";

describe("DataView", () => {
    it("shows the spinner while loading and does not build the content", () => {
        const content = jest.fn(() => <p>loaded content</p>);

        render(<DataView loaded={false} error={null}>{content}</DataView>);

        expect(screen.getByRole("progressbar")).toBeInTheDocument();
        expect(content).not.toHaveBeenCalled();
    });

    it("shows the failure message instead of the spinner", () => {
        render(<DataView loaded={false} error={{message: "Request failed"}}>{() => <p>x</p>}</DataView>);

        expect(screen.getByRole("alert")).toHaveTextContent("Request failed");
    });

    it("renders the content once loaded", () => {
        render(<DataView loaded={true} error={null}>{() => <p>loaded content</p>}</DataView>);

        expect(screen.getByText("loaded content")).toBeInTheDocument();
        expect(screen.queryByRole("progressbar")).not.toBeInTheDocument();
    });
});
