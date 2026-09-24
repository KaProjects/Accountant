import {fireEvent, render, screen} from "@testing-library/react";
import MainBar from "../MainBar";

const currentYear = new Date().getFullYear();

const renderBar = (props = {}) => {
    const setYear = jest.fn();
    const setToken = jest.fn();
    const setSelectedValue = jest.fn();
    render(<MainBar
        isYearly={true}
        year={currentYear - 1}
        setYear={setYear}
        setToken={setToken}
        selectValues={null}
        selectedValue={null}
        setSelectedValue={setSelectedValue}
        {...props}/>);
    return {setYear, setToken, setSelectedValue};
};

describe("MainBar", () => {
    const originalLocation = window.location;

    beforeEach(() => window.sessionStorage.clear());

    afterEach(() => {
        Object.defineProperty(window, "location", {
            value: originalLocation, writable: true, configurable: true,
        });
    });

    it("steps the year back and forward", () => {
        const {setYear} = renderBar({year: 2020});

        fireEvent.click(screen.getByTestId("ArrowLeftIcon"));
        expect(setYear).toHaveBeenCalledWith(2019);

        fireEvent.click(screen.getByTestId("ArrowRightIcon"));
        expect(setYear).toHaveBeenCalledWith(2021);
    });

    it("hides the back arrow at the earliest supported year", () => {
        renderBar({year: 2015});

        expect(screen.queryByTestId("ArrowLeftIcon")).not.toBeInTheDocument();
        expect(screen.getByTestId("ArrowRightIcon")).toBeInTheDocument();
    });

    it("hides the forward arrow once the current year is reached", () => {
        renderBar({year: currentYear});

        expect(screen.queryByTestId("ArrowRightIcon")).not.toBeInTheDocument();
        expect(screen.getByTestId("ArrowLeftIcon")).toBeInTheDocument();
    });

    it("omits the year controls entirely when the view is not yearly", () => {
        renderBar({isYearly: false});

        expect(screen.queryByTestId("ArrowLeftIcon")).not.toBeInTheDocument();
        expect(screen.queryByTestId("ArrowRightIcon")).not.toBeInTheDocument();
    });

    it("clears the token when logging out", () => {
        const {setToken} = renderBar();

        fireEvent.click(screen.getByRole("button", {name: "Logout"}));

        expect(setToken).toHaveBeenCalledWith(null);
    });

    it("forgets the remembered year when the menu button is used", () => {
        window.sessionStorage.setItem("year", "2020");
        Object.defineProperty(window, "location", {
            value: {href: ""}, writable: true, configurable: true,
        });
        renderBar();

        fireEvent.click(screen.getByLabelText("open drawer"));

        expect(window.sessionStorage.getItem("year")).toBeNull();
        expect(window.location.href).toBe("/");
    });

    it("renders no selector when no values are supplied", () => {
        renderBar({selectValues: null});

        expect(screen.queryByRole("combobox")).not.toBeInTheDocument();
    });

    it("renders the selector showing the current selection", () => {
        // the selected value must be the same object instance as the one in
        // selectValues - MUI matches the Select value against its MenuItems by
        // reference, so an equal-looking copy renders an empty selector
        const values = [{id: "60", name: "Revenues"}, {id: "51", name: "Consumption"}];
        renderBar({selectValues: values, selectedValue: values[0]});

        expect(screen.getByRole("combobox")).toHaveTextContent("Revenues");
    });

    it("shows nothing selected when the selected value is a copy rather than the same instance", () => {
        const values = [{id: "60", name: "Revenues"}];
        renderBar({selectValues: values, selectedValue: {id: "60", name: "Revenues"}});

        expect(screen.getByRole("combobox")).not.toHaveTextContent("Revenues");
    });
});
