import {fireEvent, render, screen} from "@testing-library/react";
import SchemaTree, {AccountLabel} from "../SchemaTree";

const classes = [{
    name: "Class 2",
    groups: [{
        name: "Group 21",
        accounts: [{id: "210", name: "Bank"}, {id: "211", name: "Cash"}],
    }],
}];

const mountTree = (props = {}) => {
    const merged = {
        classes,
        // Everything expanded, so the accounts are reachable without driving the tree open first.
        expanded: ["c0", "c0g0"],
        onToggle: jest.fn(),
        onSelectAccount: jest.fn(),
        onClearSelection: jest.fn(),
        ...props,
    };
    render(<SchemaTree {...merged}/>);
    return merged;
};

describe("SchemaTree", () => {
    beforeEach(() => jest.clearAllMocks());

    it("renders the chart of accounts as class, group and account levels", () => {
        mountTree();

        expect(screen.getByText("Class 2")).toBeInTheDocument();
        expect(screen.getByText("Group 21")).toBeInTheDocument();
        expect(screen.getByText("Bank")).toBeInTheDocument();
        expect(screen.getByText("Cash")).toBeInTheDocument();
    });

    it("shows nothing below a level that is not expanded", () => {
        mountTree({expanded: []});

        expect(screen.getByText("Class 2")).toBeInTheDocument();
        expect(screen.queryByText("Group 21")).not.toBeInTheDocument();
        expect(screen.queryByText("Bank")).not.toBeInTheDocument();
    });

    it("selects the account that was clicked", () => {
        const props = mountTree();

        fireEvent.click(screen.getByText("Cash"));

        expect(props.onSelectAccount).toHaveBeenCalledWith("211");
    });

    it("clears the selection when a class or a group is clicked, since neither is an account", () => {
        const props = mountTree();

        fireEvent.click(screen.getByText("Class 2"));
        fireEvent.click(screen.getByText("Group 21"));

        expect(props.onClearSelection).toHaveBeenCalledTimes(2);
        expect(props.onSelectAccount).not.toHaveBeenCalled();
    });

    it("marks only the selected account", () => {
        render(
            <>
                <AccountLabel account={{id: "210", name: "Bank"}} isSelected={true}/>
                <AccountLabel account={{id: "211", name: "Cash"}} isSelected={false}/>
            </>
        );

        expect(screen.getAllByTestId("ChevronRightIcon")).toHaveLength(1);
        expect(screen.getByText("Bank")).toBeInTheDocument();
        expect(screen.getByText("Cash")).toBeInTheDocument();
    });
});
