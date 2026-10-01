import {render, screen} from "@testing-library/react";
import Loader from "../Loader";

describe("Loader", () => {
    it("shows a progress spinner while there is no error", () => {
        render(<Loader error={null}/>);

        expect(screen.getByRole("progressbar")).toBeInTheDocument();
        expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    });

    it("shows the error message instead of the spinner once a request fails", () => {
        render(<Loader error={{message: "Token expired! Redirecting..."}}/>);

        expect(screen.getByRole("alert")).toHaveTextContent("Token expired! Redirecting...");
        expect(screen.queryByRole("progressbar")).not.toBeInTheDocument();
    });
});
