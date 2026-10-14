import {useState} from "react";
import {fireEvent, render, screen} from "@testing-library/react";
import PlanningInput from "../PlanningInput";

const renderInput = (value) => {
    const onChange = jest.fn();
    render(<PlanningInput value={value} onChange={onChange}/>);
    return onChange;
};

describe("PlanningInput", () => {
    it("shows a fixed planning as one amount", () => {
        renderInput("all=1500");

        expect(screen.getByRole("button", {name: "Fixed"})).toHaveAttribute("aria-pressed", "true");
        expect(screen.getByRole("spinbutton", {name: "Planning"})).toHaveValue(1500);
    });

    it("writes one amount as a fixed planning, and none as no planning", () => {
        const onChange = renderInput("all=1500");

        fireEvent.change(screen.getByRole("spinbutton", {name: "Planning"}), {target: {value: "2000"}});
        expect(onChange).toHaveBeenLastCalledWith("all=2000");

        fireEvent.change(screen.getByRole("spinbutton", {name: "Planning"}), {target: {value: ""}});
        expect(onChange).toHaveBeenLastCalledWith("");
    });

    it("shows an amount for each month, month by month", () => {
        renderInput("1|2|3|4|5|6|7|8|9|10|11|12");

        expect(screen.getByRole("button", {name: "Monthly"})).toHaveAttribute("aria-pressed", "true");
        expect(screen.getByRole("spinbutton", {name: "Mar"})).toHaveValue(3);
        expect(screen.getByRole("spinbutton", {name: "Dec"})).toHaveValue(12);
    });

    it("writes a month's amount in its place among the others", () => {
        const onChange = renderInput("1|2|3|4|5|6|7|8|9|10|11|12");

        fireEvent.change(screen.getByRole("spinbutton", {name: "Feb"}), {target: {value: "20"}});

        expect(onChange).toHaveBeenLastCalledWith("1|20|3|4|5|6|7|8|9|10|11|12");
    });

    it("carries the fixed amount over to every month when switched to monthly", () => {
        const onChange = renderInput("all=100");
        fireEvent.click(screen.getByRole("button", {name: "Monthly"}));
        expect(onChange).toHaveBeenLastCalledWith("100|100|100|100|100|100|100|100|100|100|100|100");
    });

    it("carries the first month's amount over when switched to fixed", () => {
        const onChange = renderInput("7|2|3|4|5|6|7|8|9|10|11|12");
        fireEvent.click(screen.getByRole("button", {name: "Fixed"}));
        expect(onChange).toHaveBeenLastCalledWith("all=7");
    });

    it("keeps each form as it was left while the other is shown", () => {
        // the input as it is used: given back what it writes
        const Controlled = () => {
            const [value, setValue] = useState("all=100");
            return <PlanningInput value={value} onChange={setValue}/>;
        };
        render(<Controlled/>);

        fireEvent.click(screen.getByRole("button", {name: "Monthly"}));
        fireEvent.change(screen.getByRole("spinbutton", {name: "Mar"}), {target: {value: "300"}});
        fireEvent.click(screen.getByRole("button", {name: "Fixed"}));
        expect(screen.getByRole("spinbutton", {name: "Planning"})).toHaveValue(100);

        fireEvent.change(screen.getByRole("spinbutton", {name: "Planning"}), {target: {value: "150"}});
        fireEvent.click(screen.getByRole("button", {name: "Monthly"}));
        expect(screen.getByRole("spinbutton", {name: "Jan"})).toHaveValue(100);
        expect(screen.getByRole("spinbutton", {name: "Mar"})).toHaveValue(300);

        fireEvent.click(screen.getByRole("button", {name: "Fixed"}));
        expect(screen.getByRole("spinbutton", {name: "Planning"})).toHaveValue(150);
    });
});
