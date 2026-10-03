import {fireEvent, render, screen} from "@testing-library/react";
import HomeCard from "../HomeCard";

const card = {
    title: "Balance Sheet",
    icon: <span data-testid="icon"/>,
    caption: "What is owned and what is owed.",
    height: 230,
};

describe("HomeCard", () => {
    it("shows its title, icon and caption", () => {
        render(<HomeCard {...card} onOpen={jest.fn()}/>);

        expect(screen.getByText("Balance Sheet")).toBeInTheDocument();
        expect(screen.getByTestId("icon")).toBeInTheDocument();
        expect(screen.getByText("What is owned and what is owed.")).toBeInTheDocument();
    });

    it("makes the whole tile the button when it has a single destination", () => {
        const onOpen = jest.fn();
        render(<HomeCard {...card} onOpen={onOpen}/>);

        fireEvent.click(screen.getByText("Balance Sheet"));

        expect(onOpen).toHaveBeenCalled();
    });

    it("offers each destination as its own action when there are several", () => {
        const yearly = jest.fn();
        const overall = jest.fn();
        render(<HomeCard {...card} actions={[
            {label: "Yearly", onSelect: yearly},
            {label: "Overall", onSelect: overall},
        ]}/>);

        fireEvent.click(screen.getByRole("button", {name: "Overall"}));

        expect(overall).toHaveBeenCalled();
        expect(yearly).not.toHaveBeenCalled();
    });

    it("opens its own destination when the body of a tile with actions is clicked", () => {
        const onOpen = jest.fn();
        const yearly = jest.fn();
        render(<HomeCard {...card} onOpen={onOpen} actions={[{label: "Yearly", onSelect: yearly}]}/>);

        fireEvent.click(screen.getByText("What is owned and what is owed."));

        expect(onOpen).toHaveBeenCalled();
        expect(yearly).not.toHaveBeenCalled();
    });

    it("opens only an action's destination when the action is clicked, not the tile's as well", () => {
        const onOpen = jest.fn();
        const overall = jest.fn();
        render(<HomeCard {...card} onOpen={onOpen} actions={[{label: "Overall", onSelect: overall}]}/>);

        fireEvent.click(screen.getByRole("button", {name: "Overall"}));

        expect(overall).toHaveBeenCalled();
        expect(onOpen).not.toHaveBeenCalled();
    });

    it("does not open anything when the body of a tile with only actions is clicked", () => {
        // with no destination of its own, the tile itself is not a button
        render(<HomeCard {...card} actions={[{label: "Yearly", onSelect: jest.fn()}]}/>);

        expect(screen.getAllByRole("button").map((button) => button.textContent)).toEqual(["Yearly"]);
    });
});
