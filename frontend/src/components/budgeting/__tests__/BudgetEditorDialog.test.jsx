import {fireEvent, render, screen, waitFor, within} from "@testing-library/react";
import axios from "axios";
import BudgetEditorDialog from "../BudgetEditorDialog";

jest.mock("axios");

const storedRows = [
    {id: "i1", name: "Salary", debit: null, credit: null, description: null, planning: "all=100"},
    {id: "i1.1", name: "Employer", debit: "210.0", credit: "600.0", description: null, planning: null},
    {id: "e1", name: "Food", debit: "510%", credit: "510%", description: null, planning: "all=50"},
    {id: "of1", name: "Trips", debit: "5%", credit: "5%", description: "vac=", planning: "all=0", fixed: true},
];

// the year's accounts, which every debit and credit has to name
const yearAccounts = [
    {schemaId: "210", semanticId: "0"}, {schemaId: "600", semanticId: "0"}, {schemaId: "510", semanticId: "0"},
    {schemaId: "512", semanticId: "0"}, {schemaId: "601", semanticId: "2"}, {schemaId: "211", semanticId: "0"},
];

// the previous year's: one row already here, one to take over with its sub row, and a fixed one
const previousRows = [
    {id: "e1", name: "Food", debit: "510%", credit: "510%", description: null, planning: "all=40"},
    {id: "e2", name: "Fuel", debit: null, credit: null, description: null, planning: "all=30"},
    {id: "e2.1", name: "Diesel", debit: "512.0", credit: "512.0", description: "!vac=", planning: null},
    {id: "of1", name: "Trips", debit: "5%", credit: "5%", description: "vac=", planning: "all=0", fixed: true},
];

const yearSchema = {classes: [
    {id: "2", name: "Finance", groups: [{id: "21", name: "money", accounts: [{id: "210", name: "banks"}, {id: "211", name: "savings"}]}]},
    {id: "5", name: "Expenses", groups: [{id: "51", name: "consumption", accounts: [{id: "510", name: "everyday"}]}]},
]};

const openEditor = async () => {
    const onSaved = jest.fn();
    const onClose = jest.fn();
    render(<BudgetEditorDialog open year={2020} onClose={onClose} onSaved={onSaved}/>);
    await screen.findByDisplayValue("Salary");
    return {onSaved, onClose};
};

const section = (title) => screen.getByRole("region", {name: title});
const idsIn = (title) => within(section(title)).queryAllByRole("group")
    .map((group) => group.getAttribute("aria-label"))
    .filter((label) => /^(i|me|e|of)\d/.test(label));
const row = (id) => screen.getByRole("group", {name: id});
const input = (container, role, label) => within(container).getByRole(role, {name: new RegExp("^" + label)});

describe("BudgetEditorDialog", () => {
    beforeEach(() => {
        jest.clearAllMocks();
        axios.get.mockImplementation((url) => Promise.resolve({data:
            url.startsWith("/api/account/") ? yearAccounts
                : url.startsWith("/api/schema/") ? yearSchema
                    : url === "/api/budget/2019/schema" ? previousRows
                        : storedRows}));
    });

    it("reads the stored rows of the year, each in its part of the budget", async () => {
        await openEditor();

        expect(axios.get.mock.calls.map((call) => call[0])).toContain("/api/budget/2020/schema");
        expect(idsIn("Income")).toEqual(["i1", "i1.1"]);
        expect(idsIn("Mandatory Expenses")).toEqual([]);
        expect(idsIn("Expenses")).toEqual(["e1"]);
        expect(idsIn("Off-budget")).toEqual(["of1"]);
        expect(within(row("i1.1")).getByDisplayValue("210.0")).toBeInTheDocument();
    });

    it("reads nothing while it is closed", () => {
        render(<BudgetEditorDialog open={false} year={2020} onClose={jest.fn()} onSaved={jest.fn()}/>);

        expect(axios.get).not.toHaveBeenCalled();
    });

    const fill = (container, label, value) =>
        fireEvent.change(input(container, label === "Planning" ? "spinbutton" : "textbox", label), {target: {value}});
    const saveButton = () => screen.getByRole("button", {name: "Save"});

    it("adds a row numbered by itself, and saves only once it has what it needs", async () => {
        await openEditor();

        fireEvent.click(within(section("Expenses")).getByRole("button", {name: "Add row"}));

        expect(idsIn("Expenses")).toEqual(["e1", "e2"]);
        expect(within(row("e2")).getAllByRole("textbox").filter((box) => box.getAttribute("aria-invalid") === "true")).toHaveLength(3);
        expect(within(row("e2")).getByRole("spinbutton", {name: "Planning"})).toHaveAttribute("aria-invalid", "true");
        expect(saveButton()).toBeDisabled();
        expect(screen.getByText("Fill in what is marked to save")).toBeInTheDocument();

        fill(row("e2"), "Name", "Fuel");
        fill(row("e2"), "Planning", "300");
        fill(row("e2"), "Debit", "512%");
        expect(saveButton()).toBeDisabled();
        fill(row("e2"), "Credit", "512%");

        expect(saveButton()).toBeEnabled();
    });

    it("adds a sub row numbered by itself, with no planning, that needs its name and accounts", async () => {
        await openEditor();

        fireEvent.click(within(row("i1")).getByRole("button", {name: "Add subrow"}));

        expect(idsIn("Income")).toEqual(["i1", "i1.1", "i1.2"]);
        expect(within(row("i1.2")).queryByRole("spinbutton", {name: "Planning"})).not.toBeInTheDocument();
        expect(within(row("i1.2")).getAllByRole("textbox").filter((box) => box.getAttribute("aria-invalid") === "true")).toHaveLength(3);
        fill(row("i1.2"), "Name", "Bonus");
        fill(row("i1.2"), "Debit", "210.0");
        fill(row("i1.2"), "Credit", "601.2");
        expect(saveButton()).toBeEnabled();
    });

    it("makes what a row counted its first sub row, to be named, with an empty one after it", async () => {
        await openEditor();

        fireEvent.click(within(row("e1")).getByRole("button", {name: "Add subrow"}));

        expect(idsIn("Expenses")).toEqual(["e1", "e1.1", "e1.2"]);
        expect(input(row("e1.1"), "textbox", "Debit")).toHaveValue("510%");
        expect(input(row("e1.1"), "textbox", "Name")).toHaveValue("");
        expect(input(row("e1.2"), "textbox", "Debit")).toHaveValue("");
        // the row's own accounts are gone: the debits left in its card are its sub rows'
        expect(within(row("e1")).getAllByRole("textbox", {name: /^Debit/})).toHaveLength(2);
    });

    it("marks a debit or credit that names no account of the year, and says why", async () => {
        await openEditor();
        await waitFor(() => expect(axios.get.mock.calls.map((call) => call[0])).toContain("/api/account/2020"));

        fill(row("e1"), "Debit", "510.9");
        expect(await within(row("e1")).findByText("No such account")).toBeInTheDocument();
        fill(row("e1"), "Debit", "53%");
        expect(within(row("e1")).getByText("No account matches")).toBeInTheDocument();
        fill(row("e1"), "Debit", "510");
        expect(within(row("e1")).getByText(/^Like 210.0/)).toBeInTheDocument();
        expect(saveButton()).toBeDisabled();

        fill(row("e1"), "Debit", "51%");
        expect(saveButton()).toBeEnabled();
    });

    it("hints what a debit or credit can go on with while it is typed into, and puts in what is picked", async () => {
        await openEditor();
        await waitFor(() => expect(axios.get.mock.calls.map((call) => call[0])).toContain("/api/schema/2020"));
        const debit = input(row("e1"), "textbox", "Debit");
        const hints = () => screen.getByRole("region", {name: "Account hints"});

        fireEvent.focus(debit);
        fireEvent.change(debit, {target: {value: ""}});
        expect(await screen.findByText("Classes")).toBeInTheDocument();
        expect(within(hints()).getByText("Finance")).toBeInTheDocument();

        fireEvent.change(debit, {target: {value: "21"}});
        expect(within(hints()).getByText("Schema accounts of 21 money")).toBeInTheDocument();
        fireEvent.click(within(hints()).getByText("savings"));
        expect(debit).toHaveValue("211");

        fireEvent.change(debit, {target: {value: "211"}});
        fireEvent.click(within(hints()).getByText("211.0"));
        expect(debit).toHaveValue("211.0");

        fireEvent.blur(debit);
        expect(screen.queryByRole("region", {name: "Account hints"})).not.toBeInTheDocument();
    });

    it("lets only the planning of a fixed row be edited", async () => {
        await openEditor();

        ["Name", "Debit", "Credit"].forEach((label) => expect(input(row("of1"), "textbox", label)).toBeDisabled());
        expect(within(row("of1")).getByRole("spinbutton", {name: "Planning"})).toBeEnabled();
        expect(within(row("of1")).queryByRole("button", {name: "remove row"})).not.toBeInTheDocument();
        expect(within(row("of1")).queryByRole("button", {name: "Add subrow"})).not.toBeInTheDocument();
        // a row that is not fixed has them
        expect(within(row("e1")).getByRole("button", {name: "remove row"})).toBeInTheDocument();
    });

    it("takes a row of the previous year over, with its sub rows and planning", async () => {
        await openEditor();
        await waitFor(() => expect(axios.get.mock.calls.map((call) => call[0])).toContain("/api/budget/2019/schema"));

        fireEvent.click(await within(section("Expenses")).findByRole("button", {name: "Import row"}));
        const menu = await screen.findByRole("menu");
        // the row already here by its name cannot be taken twice
        expect(within(menu).getByRole("menuitem", {name: /Food/})).toHaveAttribute("aria-disabled", "true");
        fireEvent.click(within(menu).getByRole("menuitem", {name: /Fuel/}));

        await waitFor(() => expect(idsIn("Expenses")).toEqual(["e1", "e2", "e2.1"]));
        // the row's own name comes first in its card, before its sub row's
        expect(within(row("e2")).getAllByRole("textbox", {name: /^Name/})[0]).toHaveValue("Fuel");
        expect(within(row("e2")).getByRole("spinbutton", {name: "Planning"})).toHaveValue(30);
        expect(input(row("e2.1"), "textbox", "Debit")).toHaveValue("512.0");
        expect(input(row("e2.1"), "textbox", "required")).toHaveValue("!vac=");
    });

    it("can import nothing where the previous year has nothing that is not here already", async () => {
        await openEditor();
        const importButton = (title) => within(section(title)).getByRole("button", {name: "Import row"});
        await waitFor(() => expect(importButton("Expenses")).toBeEnabled());

        // a fixed row is never offered, and the previous year has no income
        expect(importButton("Off-budget")).toBeDisabled();
        expect(importButton("Income")).toBeDisabled();

        fireEvent.click(importButton("Expenses"));
        fireEvent.click(within(await screen.findByRole("menu")).getByRole("menuitem", {name: /Fuel/}));
        // Food is here already, and now Fuel is too
        await waitFor(() => expect(importButton("Expenses")).toBeDisabled());
    });

    it("shows no ids", async () => {
        await openEditor();

        expect(screen.queryByText("i1")).not.toBeInTheDocument();
        expect(screen.queryByText("i1.1")).not.toBeInTheDocument();
    });

    it("plans a row, never a sub row", async () => {
        await openEditor();

        expect(within(row("i1")).getByRole("spinbutton", {name: "Planning"})).toHaveValue(100);
        expect(within(row("i1.1")).queryByRole("spinbutton", {name: "Planning"})).not.toBeInTheDocument();
        expect(within(row("i1.1")).queryByRole("button", {name: "Monthly"})).not.toBeInTheDocument();
    });

    it("gives a row with sub rows no accounts of its own", async () => {
        await openEditor();

        // the only debit in the row's card is its sub row's
        expect(within(row("i1")).getAllByRole("textbox", {name: /^Debit/})).toHaveLength(1);
        expect(input(row("i1.1"), "textbox", "Debit")).toHaveValue("210.0");
        expect(input(row("e1"), "textbox", "Debit")).toHaveValue("510%");
    });

    it("takes a row out together with its sub rows", async () => {
        await openEditor();

        fireEvent.click(within(row("i1")).getByRole("button", {name: "remove row"}));

        expect(idsIn("Income")).toEqual([]);
    });

    it("saves every row as edited, added and removed, then says so", async () => {
        axios.put.mockResolvedValue({});
        const {onSaved} = await openEditor();

        fireEvent.change(within(row("i1")).getByRole("spinbutton", {name: "Planning"}), {target: {value: "200"}});
        fireEvent.click(within(row("e1")).getByRole("button", {name: "remove row"}));
        fireEvent.click(within(section("Off-budget")).getByRole("button", {name: "Add row"}));
        fill(row("of2"), "Name", "Savings");
        fill(row("of2"), "Planning", "10");
        fill(row("of2"), "Debit", "211.0");
        fill(row("of2"), "Credit", "211.0");
        fireEvent.click(screen.getByRole("button", {name: "Save"}));

        await waitFor(() => expect(onSaved).toHaveBeenCalled());
        const [url, body, config] = axios.put.mock.calls[0];
        expect(url).toBe("/api/budget/2020/schema");
        expect(body).toEqual([
            {id: "i1", name: "Salary", debit: "", credit: "", description: "", planning: "all=200"},
            {id: "i1.1", name: "Employer", debit: "210.0", credit: "600.0", description: "", planning: ""},
            {id: "of1", name: "Trips", debit: "5%", credit: "5%", description: "vac=", planning: "all=0"},
            {id: "of2", name: "Savings", debit: "211.0", credit: "211.0", description: "", planning: "all=10"},
        ]);
        expect(config.headers["X-Accountant-Client"]).toBe("web");
    });

    it("shows why the rows were refused, and keeps them as they are", async () => {
        axios.put.mockRejectedValue({response: {status: 422, statusText: "Unprocessable Content",
            data: {detail: "Row 'i1' has to have both its debit and its credit, or neither."}}});
        const {onSaved} = await openEditor();

        fireEvent.click(screen.getByRole("button", {name: "Save"}));

        expect(await screen.findByText(/both its debit and its credit/)).toBeInTheDocument();
        expect(onSaved).not.toHaveBeenCalled();
        expect(idsIn("Income")).toEqual(["i1", "i1.1"]);
    });

    it("closes without saving", async () => {
        const {onClose} = await openEditor();

        fireEvent.click(screen.getByRole("button", {name: "close"}));

        expect(onClose).toHaveBeenCalled();
        expect(axios.put).not.toHaveBeenCalled();
    });
});
