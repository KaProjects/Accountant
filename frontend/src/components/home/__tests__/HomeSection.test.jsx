import {render, screen, within} from "@testing-library/react";
import HomeSection from "../HomeSection";

describe("HomeSection", () => {
    it("titles the section and renders one tile per card, at the section's height", () => {
        render(<HomeSection
            title="Accounting"
            variant="h4"
            height={230}
            cards={[
                {title: "Balance Sheet", icon: <span/>, caption: "owned and owed", onOpen: jest.fn()},
                {title: "Income Statement", icon: <span/>, caption: "revenue and cost", onOpen: jest.fn()},
            ]}
        />);

        expect(screen.getByText("Accounting")).toBeInTheDocument();
        expect(screen.getByText("Balance Sheet")).toBeInTheDocument();
        expect(screen.getByText("Income Statement")).toBeInTheDocument();
    });

    it("renders nothing but its title when it has no cards", () => {
        const {container} = render(
            <HomeSection title="Other" variant="h6" height={150} cards={[]}/>);

        expect(screen.getByText("Other")).toBeInTheDocument();
        expect(within(container).queryByRole("button")).not.toBeInTheDocument();
    });
});
