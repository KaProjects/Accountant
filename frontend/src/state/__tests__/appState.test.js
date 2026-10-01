import {render, screen} from "@testing-library/react";
import {AppStateProvider, useAppState} from "../appState";

const Reader = () => {
    const {year} = useAppState();
    return <span>{year}</span>;
};

describe("useAppState", () => {
    it("reads the state the provider holds", () => {
        render(<AppStateProvider value={{year: 2020}}><Reader/></AppStateProvider>);

        expect(screen.getByText("2020")).toBeInTheDocument();
    });

    it("fails loudly outside a provider, rather than reading undefined fields", () => {
        // Without this the component would read `year` off null and fail somewhere less obvious.
        jest.spyOn(console, "error").mockImplementation(() => {});

        expect(() => render(<Reader/>)).toThrow(/outside an AppStateProvider/);

        console.error.mockRestore();
    });
});
