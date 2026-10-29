import {fireEvent, render, screen} from "@testing-library/react";
import {MemoryRouter, useLocation, useNavigate} from "react-router-dom";
import {useEffect} from "react";
import AppState, {AppStateProvider, useAppState, yearlyPath} from "../appState";

const YearOnly = () => <span>{useAppState().year}</span>;

const YearlyPage = () => {
    const {setYearly} = useAppState();
    useEffect(() => setYearly(true), [setYearly]);
    return <span>yearly page</span>;
};

const Reader = () => {
    const {year, setYear} = useAppState();
    return (
        <>
            <span>year {year}</span>
            <button onClick={() => setYear(year + 1)}>next</button>
        </>
    );
};

const renderAt = (path) => render(
    <MemoryRouter initialEntries={[path]}>
        <AppState><Reader/></AppState>
    </MemoryRouter>
);

describe("useAppState", () => {
    it("reads the state the provider holds", () => {
        render(<AppStateProvider value={{year: 2020}}><YearOnly/></AppStateProvider>);

        expect(screen.getByText("2020")).toBeInTheDocument();
    });

    it("fails loudly outside a provider, rather than reading undefined fields", () => {
        jest.spyOn(console, "error").mockImplementation(() => {});

        expect(() => render(<Reader/>)).toThrow(/outside an AppStateProvider/);

        console.error.mockRestore();
    });
});

describe("AppState", () => {
    it("takes the year from the address", () => {
        renderAt("/budgeting?year=2019");

        expect(screen.getByText("year 2019")).toBeInTheDocument();
    });

    it("shows the current year when the address names none", () => {
        renderAt("/budgeting");

        expect(screen.getByText("year " + new Date().getFullYear())).toBeInTheDocument();
    });

    it("shows the current year when the address names nonsense", () => {
        renderAt("/budgeting?year=lastTuesday");

        expect(screen.getByText("year " + new Date().getFullYear())).toBeInTheDocument();
    });

    it("writes a changed year into the address, so a reload keeps it", () => {
        renderAt("/budgeting?year=2019");

        fireEvent.click(screen.getByRole("button", {name: "next"}));

        expect(screen.getByText("year 2020")).toBeInTheDocument();
    });
});

const Address = () => <span data-testid="address">{useLocation().search}</span>;

describe("a yearly page", () => {
    it("puts the year it is showing into the address, even when nobody changed it", async () => {
        render(
            <MemoryRouter initialEntries={["/budgeting"]}>
                <AppState><YearlyPage/><Address/></AppState>
            </MemoryRouter>
        );

        expect(await screen.findByText("?year=" + new Date().getFullYear())).toBeInTheDocument();
    });

    it("leaves an address that already names a year alone", async () => {
        render(
            <MemoryRouter initialEntries={["/budgeting?year=2019"]}>
                <AppState><YearlyPage/><Address/></AppState>
            </MemoryRouter>
        );

        expect(await screen.findByText("?year=2019")).toBeInTheDocument();
    });

    it("writes no year into the address of a page it is left for", async () => {
        // leaving a single year for the overall view used to arrive at .../overall?year=2026: the
        // year was written back in before the overall view had said it was not yearly
        const OverallPage = () => {
            const {setYearly} = useAppState();
            useEffect(() => setYearly(false), [setYearly]);
            return <span>overall page</span>;
        };
        const Pages = () => {
            const navigate = useNavigate();
            return useLocation().pathname === "/accounting/profit"
                ? <><YearlyPage/><button onClick={() => navigate("/accounting/profit/overall")}>all years</button></>
                : <OverallPage/>;
        };
        render(
            <MemoryRouter initialEntries={["/accounting/profit?year=2024"]}>
                <AppState><Pages/><Address/></AppState>
            </MemoryRouter>
        );

        fireEvent.click(screen.getByText("all years"));

        expect(await screen.findByText("overall page")).toBeInTheDocument();
        expect(screen.getByTestId("address")).toBeEmptyDOMElement();
    });

    it("is yearly no longer once it has been left", async () => {
        const Pages = () => {
            const navigate = useNavigate();
            const {isYearly} = useAppState();
            return useLocation().pathname === "/budgeting"
                ? <><YearlyPage/><button onClick={() => navigate("/admin")}>leave</button></>
                : <span>{isYearly ? "still yearly" : "not yearly"}</span>;
        };
        render(
            <MemoryRouter initialEntries={["/budgeting?year=2024"]}>
                <AppState><Pages/></AppState>
            </MemoryRouter>
        );

        fireEvent.click(screen.getByText("leave"));

        expect(await screen.findByText("not yearly")).toBeInTheDocument();
    });

    it("leaves a page that is not yearly without one", () => {
        render(
            <MemoryRouter initialEntries={["/admin"]}>
                <AppState><Reader/><Address/></AppState>
            </MemoryRouter>
        );

        expect(screen.getByTestId("address")).toBeEmptyDOMElement();
    });
});

describe("the way to an overall view", () => {
    const Offering = () => {
        const {overallPath, setOverallPath} = useAppState();
        return (
            <>
                <span>overall {String(overallPath)}</span>
                <button onClick={() => setOverallPath("/accounting/balance/overall")}>offer</button>
            </>
        );
    };
    const renderOffering = () => render(
        <MemoryRouter initialEntries={["/accounting/balance"]}>
            <AppState><Offering/></AppState>
        </MemoryRouter>
    );

    it("is offered by no page until one says it has an overall view", () => {
        renderOffering();

        expect(screen.getByText("overall null")).toBeInTheDocument();
    });

    it("holds the overall view a page says it has", () => {
        renderOffering();

        fireEvent.click(screen.getByText("offer"));

        expect(screen.getByText("overall /accounting/balance/overall")).toBeInTheDocument();
    });
});

describe("yearlyPath", () => {
    it("addresses a page at a given year", () => {
        expect(yearlyPath("/accounting/balance", 2019)).toBe("/accounting/balance?year=2019");
    });
});
