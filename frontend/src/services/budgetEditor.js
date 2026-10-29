/**
 * The parts of a budget, in the order it shows them, each with the prefix of its rows' ids: a
 * row's id is the prefix and a number - e1 - and a sub row's is its row's id, a dot and a number
 * of its own - e1.1, e1.2.
 */
export const SECTIONS = [
    {prefix: "i", title: "Income"},
    {prefix: "me", title: "Mandatory Expenses"},
    {prefix: "e", title: "Expenses"},
    {prefix: "of", title: "Off-budget"},
];

/** The prefix of the part of the budget a row belongs to, or null for an id of none. */
export const sectionOf = (id) => {
    const match = /^(me|of|i|e)\d/.exec(id);
    return match === null ? null : match[1];
};

/** The id of the row a sub row belongs to; a row's own id for a row. */
export const mainIdOf = (id) => id.split(".")[0];

export const isSubRow = (id) => id.includes(".");

/** Ids in the order of their parts, number by number: e1.2 before e1.10, as the budget orders them. */
export const compareIds = (one, other) => {
    const parts = (id) => id.split(/(?<=\D)(?=\d)|\./);
    const ones = parts(one);
    const others = parts(other);
    for (let i = 0; i < Math.min(ones.length, others.length); i++) {
        const numbers = /^\d+$/.test(ones[i]) && /^\d+$/.test(others[i]);
        const compared = numbers ? Number(ones[i]) - Number(others[i]) : ones[i].localeCompare(others[i]);
        if (compared !== 0) return compared;
    }
    return ones.length - others.length;
};

const lastNumber = (id) => Number(id.slice(id.search(/\d+$/)));

/** The id of a new row of the given part of the budget: the number after the highest there. */
export const nextRowId = (rows, prefix) => {
    const numbers = rows.map((row) => row.id)
        .filter((id) => !isSubRow(id) && sectionOf(id) === prefix)
        .map(lastNumber);
    return prefix + (Math.max(0, ...numbers) + 1);
};

/**
 * The id of a new sub row of the given row: the number after the highest of its sub rows, written
 * as wide as theirs, so that rows numbered 01, 02 go on with 03.
 */
export const nextSubRowId = (rows, mainId) => {
    const own = rows.map((row) => row.id)
        .filter((id) => id.startsWith(mainId + ".") && !id.slice(mainId.length + 1).includes("."))
        .map((id) => id.slice(mainId.length + 1));
    const next = String(Math.max(0, ...own.map(Number)) + 1);
    const width = Math.max(0, ...own.map((number) => number.length));
    return mainId + "." + next.padStart(width, "0");
};

/** The rows of one part of the budget, as the budget shows them: each row followed by its sub rows. */
export const rowsOfSection = (rows, prefix) => {
    const ofSection = rows.filter((row) => sectionOf(row.id) === prefix);
    const mains = ofSection.filter((row) => !isSubRow(row.id)).sort((a, b) => compareIds(a.id, b.id));
    return mains.flatMap((main) => [main, ...ofSection
        .filter((row) => isSubRow(row.id) && mainIdOf(row.id) === main.id)
        .sort((a, b) => compareIds(a.id, b.id))]);
};

/** The rows left once a row is taken out - with every sub row of it, for a row that has them. */
export const withoutRow = (rows, id) =>
    rows.filter((row) => row.id !== id && !(!isSubRow(id) && isSubRow(row.id) && mainIdOf(row.id) === id));

/** The values of a row that say which transactions it counts: none on a row made of sub rows. */
export const ACCOUNT_FIELDS = ["debit", "credit", "description"];

export const hasSubRows = (rows, id) => !isSubRow(id) && rows.some((row) => isSubRow(row.id) && mainIdOf(row.id) === id);

/**
 * The rows with more sub rows of the given row, each numbered after its row's others. A row made of
 * sub rows counts nothing itself, so the first time it is given them, what it counted becomes the
 * first of them - to be named - and an empty one follows; after that, each time adds one empty.
 */
export const withSubRows = (rows, mainId, emptyRow) => {
    if (hasSubRows(rows, mainId)) return [...rows, emptyRow(nextSubRowId(rows, mainId))];

    const main = rows.find((row) => row.id === mainId);
    const counted = Object.fromEntries(ACCOUNT_FIELDS.map((field) => [field, main[field] ?? ""]));
    const cleared = Object.fromEntries(ACCOUNT_FIELDS.map((field) => [field, ""]));
    const first = {...emptyRow(nextSubRowId(rows, mainId)), ...counted};
    const withFirst = [...rows.map((row) => (row.id === mainId ? {...row, ...cleared} : row)), first];
    return [...withFirst, emptyRow(nextSubRowId(withFirst, mainId))];
};

/** What is wrong with a planning as the budget reads it, or null: all=amount, or twelve amounts separated by |. */
export const planningProblem = (planning) => {
    const amount = /^-?\d+$/;
    if (planning.startsWith("all=")) return amount.test(planning.slice(4)) ? null : "all= has to be followed by an amount";
    const months = planning.split("|");
    if (months.length !== 12) return "Twelve amounts separated by |, or all=amount";
    return months.every((month) => amount.test(month.trim())) ? null : "Every month has to be an amount";
};

/**
 * What is missing or wrong in each row, by id and then by value, or nothing for a row that is
 * complete. A row has its name and planning, and counts transactions itself - its debit and
 * credit - unless it is made of sub rows; a sub row has its name, debit and credit. A debit or
 * credit has to name the year's accounts, as accountProblem says.
 */
/** An account: its schema id, a dot and its number - and for an asset's, a dash and another. */
const ACCOUNT = /^\d{3}\.\d+(-\d+)?$/;
/** The beginning of an account, and a % for every account it begins. */
const ACCOUNT_PREFIX = /^(\d{1,3}|\d{3}\.(\d+(-\d*)?)?)%$/;

/**
 * What is wrong with a row's debit or credit, or null. It is either an account - 210.0, or
 * 549.0-10 for an asset's - which has to be one of the year's; or the beginning of one followed by
 * %, meaning every account it begins - 5%, 510%, 511.0-% - of which the year has to have at least
 * one. Without the year's accounts, only the form is checked.
 */
export const accountProblem = (pattern, accountIds = null) => {
    if (ACCOUNT.test(pattern)) {
        return accountIds === null || accountIds.includes(pattern) ? null : "No such account";
    }
    if (ACCOUNT_PREFIX.test(pattern)) {
        const prefix = pattern.slice(0, -1);
        return accountIds === null || accountIds.some((id) => id.startsWith(prefix)) ? null : "No account matches";
    }
    return "Like 210.0 or 510%";
};

export const rowProblems = (rows, accountIds = null) => {
    const problems = {};
    for (const row of rows) {
        const own = {};
        const required = isSubRow(row.id)
            ? ["name", "debit", "credit"]
            : ["name", "planning", ...(hasSubRows(rows, row.id) ? [] : ["debit", "credit"])];
        for (const field of required) {
            if ((row[field] ?? "").trim() === "") own[field] = "Required";
        }
        for (const field of ["debit", "credit"]) {
            const pattern = (row[field] ?? "").trim();
            if (own[field] === undefined && pattern !== "") {
                const problem = accountProblem(pattern, accountIds);
                if (problem !== null) own[field] = problem;
            }
        }
        if (!isSubRow(row.id) && own.planning === undefined) {
            const problem = planningProblem(row.planning.trim());
            if (problem !== null) own.planning = problem;
        }
        if (Object.keys(own).length > 0) problems[row.id] = own;
    }
    return problems;
};

const byNumber = (one, other) => compareIds("x" + one.id, "x" + other.id);

/**
 * What a debit or credit can go on with, as it is typed: with nothing yet, the classes of the
 * schema; with a class, its groups; with a group, its schema accounts; with a schema account, the
 * year's accounts of it; and further, the year's accounts that begin so. A % at the end - every
 * account it begins - is looked past. Each option is an id to put in, with its name; null when
 * what is typed is no beginning of an account at all.
 */
export const accountHints = (value, schema, accounts) => {
    const typed = value.trim().replace(/%$/, "");
    const named = (id, name) => ({id, name});
    const accountsBeginning = (prefix) => accounts
        .map((account) => named(account.schemaId + "." + account.semanticId, account.name))
        .filter((account) => account.id.startsWith(prefix))
        .sort(byNumber);

    if (typed === "") {
        return {title: "Classes", options: schema.classes.map((clazz) => named(clazz.id, clazz.name)).sort(byNumber)};
    }
    if (/^\d$/.test(typed)) {
        const clazz = schema.classes.find((one) => one.id === typed);
        return {title: "Groups of " + typed + (clazz ? " " + clazz.name : ""),
            options: (clazz?.groups ?? []).map((group) => named(group.id, group.name)).sort(byNumber)};
    }
    if (/^\d{2}$/.test(typed)) {
        const group = schema.classes.flatMap((clazz) => clazz.groups).find((one) => one.id === typed);
        return {title: "Schema accounts of " + typed + (group ? " " + group.name : ""),
            options: (group?.accounts ?? []).map((account) => named(account.id, account.name)).sort(byNumber)};
    }
    if (/^\d{3}(\.(\d+(-\d*)?)?)?$/.test(typed)) {
        const schemaAccount = schema.classes.flatMap((clazz) => clazz.groups).flatMap((group) => group.accounts)
            .find((one) => one.id === typed.slice(0, 3));
        return {title: "Accounts of " + typed.slice(0, 3) + (schemaAccount ? " " + schemaAccount.name : ""),
            options: accountsBeginning(typed.length === 3 ? typed + "." : typed)};
    }
    return null;
};

/** The rows of a part of the budget that can be taken over from another year: all but the fixed ones. */
export const importableRows = (sourceRows, prefix) =>
    rowsOfSection(sourceRows, prefix).filter((row) => !isSubRow(row.id) && !row.fixed);

/**
 * The rows with a row of another year taken over - its name, what it counts and its planning, and
 * its sub rows with theirs - numbered after the highest row of its part here. Its sub rows keep the
 * numbers they had under it.
 */
export const withImportedRow = (rows, sourceRows, sourceId, editable) => {
    const source = sourceRows.find((row) => row.id === sourceId);
    const id = nextRowId(rows, sectionOf(sourceId));
    const subRows = sourceRows.filter((row) => isSubRow(row.id) && mainIdOf(row.id) === sourceId);
    return [
        ...rows,
        editable({...source, id, fixed: false}),
        ...subRows.map((subRow) => editable({...subRow, id: id + subRow.id.slice(sourceId.length), fixed: false})),
    ];
};
