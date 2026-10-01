import {cleanup, fireEvent, screen} from "@testing-library/react";
import MainBar from "../MainBar";
import {renderWithAppState} from "../../testUtils";

const mockNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
    ...jest.requireActual("react-router-dom"),
    useNavigate: () => mockNavigate,
}));


const currentYear = new Date().getFullYear();

const renderBar = (props = {}) => {
    const setYear = jest.fn();
    const setSelectedValue = jest.fn();
    renderWithAppState(<MainBar/>, {
        isYearly: true,
        year: currentYear - 1,
        setYear,
        selectValues: null,
        selectedValue: null,
        setSelectedValue,
        ...props,
    });
    return {setYear, setSelectedValue};
};

describe("MainBar", () => {

    it("steps the year back and forward", () => {
        const {setYear} = renderBar({year: 2020});

        fireEvent.click(screen.getByTestId("ArrowLeftIcon"));
        expect(setYear).toHaveBeenCalledWith(2019);

        fireEvent.click(screen.getByTestId("ArrowRightIcon"));
        expect(setYear).toHaveBeenCalledWith(2021);
    });

    it("hides the back arrow at the earliest supported year, without moving the rest", () => {
        renderBar({year: 2015});

        // Still laid out, so the year and the other arrow stay where they were.
        expect(screen.getByTestId("ArrowLeftIcon")).toBeInTheDocument();
        expect(screen.getByTestId("ArrowLeftIcon")).not.toBeVisible();
        expect(screen.getByTestId("ArrowRightIcon")).toBeVisible();
    });

    it("hides the forward arrow once the current year is reached, without moving the rest", () => {
        renderBar({year: currentYear});

        expect(screen.getByTestId("ArrowRightIcon")).toBeInTheDocument();
        expect(screen.getByTestId("ArrowRightIcon")).not.toBeVisible();
        expect(screen.getByTestId("ArrowLeftIcon")).toBeVisible();
    });

    it("keeps both arrows laid out at every year, so the selector never shifts", () => {
        const widths = [2015, 2016, currentYear - 1, currentYear].map((year) => {
            cleanup();
            renderBar({year});
            return screen.getAllByTestId(/Arrow(Left|Right)Icon/).length;
        });

        expect(widths).toEqual([2, 2, 2, 2]);
    });

    it("omits the year controls entirely when the view is not yearly", () => {
        renderBar({isYearly: false});

        expect(screen.queryByTestId("ArrowLeftIcon")).not.toBeInTheDocument();
        expect(screen.queryByTestId("ArrowRightIcon")).not.toBeInTheDocument();
    });

    it("returns to the start, which drops the year from the address", () => {
        renderBar({year: 2020});

        fireEvent.click(screen.getByLabelText("open drawer"));

        expect(mockNavigate).toHaveBeenCalledWith("/");
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
