import {cleanup, fireEvent, render, screen} from "@testing-library/react";
import {MemoryRouter, Route, Routes} from "react-router-dom";
import {useGoHome, useGoTo} from "../navigation";
import {AppStateProvider} from "../../state/appState";

const Start = () => {
    const goTo = useGoTo();
    const goHome = useGoHome();
    return (
        <>
            <span>start</span>
            <button onClick={goTo("/elsewhere")}>go</button>
            <button onClick={goHome}>home</button>
        </>
    );
};

const renderAt = (path, state = {}) => render(
    <AppStateProvider value={{setYear: jest.fn(), ...state}}>
        <MemoryRouter initialEntries={[path]}>
            <Routes>
                <Route path="/" element={<Start/>}/>
                <Route path="/elsewhere" element={<span>elsewhere</span>}/>
            </Routes>
        </MemoryRouter>
    </AppStateProvider>
);

describe("useGoTo", () => {
    it("moves to the destination without reloading the page", () => {
        renderAt("/");

        fireEvent.click(screen.getByRole("button", {name: "go"}));

        expect(screen.getByText("elsewhere")).toBeInTheDocument();
        expect(screen.queryByText("start")).not.toBeInTheDocument();
    });

    it("does nothing until the returned handler is called", () => {
        renderAt("/");

        expect(screen.getByText("start")).toBeInTheDocument();
    });
});

describe("useGoHome", () => {
    it("returns to the start", () => {
        renderAt("/elsewhere");
        expect(screen.getByText("elsewhere")).toBeInTheDocument();

        cleanup();
        renderAt("/");
        fireEvent.click(screen.getByRole("button", {name: "home"}));
        expect(screen.getByText("start")).toBeInTheDocument();
    });

    it("drops the year from the address, since the start carries none", () => {
        renderAt("/elsewhere?year=2019");

        expect(screen.getByText("elsewhere")).toBeInTheDocument();
    });
});
