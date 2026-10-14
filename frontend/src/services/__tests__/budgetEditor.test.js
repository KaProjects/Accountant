import {accountHints, accountProblem, compareIds, hasSubRows, importableRows, nextRowId, nextSubRowId, planningProblem, rowProblems, rowsOfSection, sectionOf, withImportedRow, withoutRow, withSubRows} from "../budgetEditor";

const rows = (...ids) => ids.map((id) => ({id}));

describe("sectionOf", () => {
    it("tells the part of the budget a row belongs to by its id", () => {
        expect(sectionOf("i1.2")).toBe("i");
        expect(sectionOf("me3")).toBe("me");
        expect(sectionOf("e1.1")).toBe("e");
        expect(sectionOf("of2")).toBe("of");
        expect(sectionOf("x1")).toBeNull();
    });
});

describe("compareIds", () => {
    it("orders number by number, not as text", () => {
        expect(["e1.10", "e10", "e1", "e1.2"].sort(compareIds)).toEqual(["e1", "e1.2", "e1.10", "e10"]);
    });
});

describe("nextRowId", () => {
    it("numbers a new row after the highest of its part", () => {
        expect(nextRowId(rows("e1", "e1.1", "e3", "me7"), "e")).toBe("e4");
    });

    it("starts a part with nothing in it at one", () => {
        expect(nextRowId(rows("e1"), "of")).toBe("of1");
    });
});

describe("nextSubRowId", () => {
    it("numbers a new sub row after the highest of its row's", () => {
        expect(nextSubRowId(rows("e1", "e1.1", "e1.2", "e2.5"), "e1")).toBe("e1.3");
    });

    it("starts a row with no sub rows at one", () => {
        expect(nextSubRowId(rows("e1", "e2.5"), "e1")).toBe("e1.1");
    });

    it("writes the number as wide as the sub rows already there", () => {
        expect(nextSubRowId(rows("me5", "me5.01", "me5.02"), "me5")).toBe("me5.03");
    });
});

describe("rowsOfSection", () => {
    it("lists the rows of one part, each followed by its sub rows, in order", () => {
        const listed = rowsOfSection(rows("e2", "e1.10", "me1", "e1", "e1.2", "e2.1"), "e");

        expect(listed.map((row) => row.id)).toEqual(["e1", "e1.2", "e1.10", "e2", "e2.1"]);
    });
});

describe("withoutRow", () => {
    it("takes a row out with its sub rows, and a sub row alone", () => {
        expect(withoutRow(rows("e1", "e1.1", "e1.2", "e10.1", "e2"), "e1").map((row) => row.id)).toEqual(["e10.1", "e2"]);
        expect(withoutRow(rows("e1", "e1.1", "e1.2"), "e1.1").map((row) => row.id)).toEqual(["e1", "e1.2"]);
    });
});

describe("withSubRows", () => {
    const empty = (id) => ({id, name: "", debit: "", credit: "", description: ""});

    it("makes what a row counted its first sub row, to be named, with an empty one after it", () => {
        const rows = [{id: "e1", name: "Food", debit: "510%", credit: "510%", description: "!vac="}];

        expect(withSubRows(rows, "e1", empty)).toEqual([
            {id: "e1", name: "Food", debit: "", credit: "", description: ""},
            {id: "e1.1", name: "", debit: "510%", credit: "510%", description: "!vac="},
            empty("e1.2"),
        ]);
    });

    it("adds one empty sub row to a row that has them already", () => {
        const rows = [empty("e1"), {...empty("e1.1"), debit: "510%", credit: "510%"}];

        expect(withSubRows(rows, "e1", empty)).toEqual([...rows, empty("e1.2")]);
        expect(hasSubRows(rows, "e1")).toBe(true);
        expect(hasSubRows(rows, "e1.1")).toBe(false);
    });
});

describe("planningProblem", () => {
    it("accepts the forms the budget reads", () => {
        expect(planningProblem("all=1500")).toBeNull();
        expect(planningProblem("1|2|3|4|5|6|7|8|9|10|11|-12")).toBeNull();
    });

    it("says what is wrong with any other", () => {
        expect(planningProblem("all=x")).toMatch(/amount/);
        expect(planningProblem("1|2|3")).toMatch(/Twelve/);
        expect(planningProblem("1|2|3|4|5|6|7|8|9|10|11|x")).toMatch(/Every month/);
    });
});

describe("rowProblems", () => {
    const row = (id, values = {}) => ({id, name: "", planning: "", debit: "", credit: "", description: "", ...values});

    it("asks a row for its name, planning, debit and credit", () => {
        expect(rowProblems([row("e1")])).toEqual({e1: {name: "Required", planning: "Required", debit: "Required", credit: "Required"}});
    });

    it("asks a row made of sub rows for no accounts, and each sub row for its name and accounts but no planning", () => {
        const rows = [row("e1", {name: "Food", planning: "all=1"}), row("e1.1", {name: "Groceries", debit: "510%"})];

        expect(rowProblems(rows)).toEqual({"e1.1": {credit: "Required"}});
    });

    it("says what is wrong with a planning given", () => {
        expect(rowProblems([row("e1", {name: "a", planning: "1|2", debit: "5%", credit: "5%"})]).e1.planning).toMatch(/Twelve/);
    });

    it("finds nothing in complete rows", () => {
        expect(rowProblems([row("e1", {name: "a", planning: "all=1", debit: "5%", credit: "5%"})])).toEqual({});
    });
});

describe("accountProblem", () => {
    const accounts = ["210.0", "510.0-1", "549.0-10", "230.10"];

    it("takes an account of the year, and a beginning of one followed by %", () => {
        ["210.0", "549.0-10", "5%", "51%", "510%", "510.%", "510.0%", "510.0-%", "549.0-1%"]
            .forEach((pattern) => expect(accountProblem(pattern, accounts)).toBeNull());
    });

    it("refuses an account the year does not have, or a beginning of none", () => {
        expect(accountProblem("210.1", accounts)).toBe("No such account");
        expect(accountProblem("549.0-1", accounts)).toBe("No such account");
        expect(accountProblem("53%", accounts)).toBe("No account matches");
    });

    it("refuses anything that is neither", () => {
        ["510", "510.", "510.0-", "51.0", "5100.0", "a10.0", "510.0-1-2", "%", "510.0-x%"]
            .forEach((pattern) => expect(accountProblem(pattern, accounts)).toMatch(/^Like 210.0/));
    });

    it("checks only the form without the year's accounts", () => {
        expect(accountProblem("999.9")).toBeNull();
        expect(accountProblem("999")).toMatch(/^Like 210.0/);
    });
});

describe("accountHints", () => {
    const schema = {classes: [
        {id: "5", name: "Expenses", groups: [
            {id: "51", name: "consumption", accounts: [{id: "511", name: "other"}, {id: "510", name: "everyday"}]},
            {id: "50", name: "depreciation", accounts: []},
        ]},
        {id: "2", name: "Finance", groups: [{id: "23", name: "assets", accounts: [{id: "230", name: "funds"}]}]},
    ]};
    const accounts = [
        {schemaId: "510", semanticId: "0-1", name: "fruit"}, {schemaId: "510", semanticId: "0-0", name: "food"},
        {schemaId: "510", semanticId: "1-0", name: "snacks"}, {schemaId: "230", semanticId: "10", name: "Indigo"},
        {schemaId: "230", semanticId: "2", name: "Pioneer"},
    ];
    const ids = (value) => accountHints(value, schema, accounts).options.map((option) => option.id);

    it("offers the classes of the schema to begin with", () => {
        expect(accountHints("", schema, accounts)).toEqual({title: "Classes",
            options: [{id: "2", name: "Finance"}, {id: "5", name: "Expenses"}]});
    });

    it("offers a class's groups, then a group's schema accounts, each in order", () => {
        expect(accountHints("5", schema, accounts).title).toBe("Groups of 5 Expenses");
        expect(ids("5")).toEqual(["50", "51"]);
        expect(accountHints("51", schema, accounts).title).toBe("Schema accounts of 51 consumption");
        expect(ids("51")).toEqual(["510", "511"]);
    });

    it("offers the year's accounts of a schema account, and those that begin as typed", () => {
        expect(accountHints("510", schema, accounts).title).toBe("Accounts of 510 everyday");
        expect(ids("510")).toEqual(["510.0-0", "510.0-1", "510.1-0"]);
        expect(ids("510.0")).toEqual(["510.0-0", "510.0-1"]);
        expect(ids("230.")).toEqual(["230.2", "230.10"]);
    });

    it("looks past a % at the end", () => {
        expect(ids("51%")).toEqual(["510", "511"]);
    });

    it("offers nothing for what begins no account", () => {
        expect(ids("9")).toEqual([]);
        expect(accountHints("5x", schema, accounts)).toBeNull();
    });
});

describe("importableRows", () => {
    it("offers the rows of a part of the budget, but no sub rows and no fixed rows", () => {
        const source = [{id: "of1", fixed: true}, {id: "of2"}, {id: "of2.1"}, {id: "e1"}];

        expect(importableRows(source, "of").map((row) => row.id)).toEqual(["of2"]);
    });
});

describe("withImportedRow", () => {
    const copy = (row) => ({...row});

    it("takes a row over with its sub rows and planning, numbered after the rows of its part here", () => {
        const here = [{id: "e1", name: "Fuel"}, {id: "e3", name: "Bars"}];
        const source = [
            {id: "e2", name: "Food", planning: "all=50", debit: "", credit: "", description: ""},
            {id: "e2.01", name: "Groceries", debit: "510%", credit: "510%", description: "!vac="},
            {id: "e2.02", name: "Snacks", debit: "510.1-%", credit: "510.1-%", description: ""},
            {id: "e20", name: "Other"},
        ];

        expect(withImportedRow(here, source, "e2", copy).slice(2)).toEqual([
            {id: "e4", name: "Food", planning: "all=50", debit: "", credit: "", description: "", fixed: false},
            {id: "e4.01", name: "Groceries", debit: "510%", credit: "510%", description: "!vac=", fixed: false},
            {id: "e4.02", name: "Snacks", debit: "510.1-%", credit: "510.1-%", description: "", fixed: false},
        ]);
    });
});
