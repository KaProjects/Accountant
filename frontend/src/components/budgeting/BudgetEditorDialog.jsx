import {useEffect, useState} from "react";
import PropTypes from "prop-types";
import axios from "axios";
import {
    Alert, AppBar, Box, Button, Dialog, IconButton, ListItemText, Menu, MenuItem, Paper, TextField, Toolbar, Typography,
} from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import AddIcon from "@mui/icons-material/Add";
import DeleteIcon from "@mui/icons-material/Delete";
import HistoryIcon from "@mui/icons-material/History";
import {useData} from "../../fetch";
import {properties} from "../../properties";
import {mutatingJson} from "../../services/session";
import {describeResponseError} from "../../services/errors";
import Loader from "../Loader";
import {
    accountHints, ACCOUNT_FIELDS, importableRows, isSubRow, mainIdOf, nextRowId, rowProblems, rowsOfSection, SECTIONS,
    withImportedRow, withoutRow, withSubRows,
} from "../../services/budgetEditor";
import PlanningInput from "./PlanningInput";
import AccountHints from "./AccountHints";

const titleStyle = {ml: 2, flex: 1};
const incompleteStyle = {mr: 2, opacity: 0.85};
const contentStyle = {padding: "16px", display: "flex", gap: "24px", justifyContent: "center", alignItems: "flex-start"};
const mainStyle = {flex: "1 1 auto", maxWidth: "1100px", minWidth: 0};
// beside the rows, kept in view while they scroll, and its place kept so nothing moves when it shows
const asideStyle = {flex: "0 0 300px", position: "sticky", top: "64px"};
const sectionStyle = {marginBottom: "32px"};
const sectionHeadingStyle = {display: "flex", alignItems: "center", gap: "8px", marginBottom: "8px"};
const cardStyle = {padding: "12px 16px", marginBottom: "12px", display: "flex", flexDirection: "column", gap: "12px"};
// the values on one line, wrapping onto the next where the screen is narrow
// lined up by their tops, so a reason under one of them does not shift the others
const lineStyle = {display: "flex", flexWrap: "wrap", alignItems: "flex-start", gap: "8px 16px"};
const nameStyle = {flex: "1 1 260px"};
const growStyle = {flex: "1 1 160px"};
const removeStyle = {marginLeft: "auto", marginTop: "12px"};
const removeSubRowStyle = {marginTop: "12px"};
// under the row they make up, set off by a rule along their side
// a sub row stays on one line, its values narrowing rather than its remove button wrapping away
const subRowLineStyle = {...lineStyle, flexWrap: "nowrap"};
const shrinkingStyle = {flex: "1 1 0", minWidth: 0};
const subRowsStyle = {display: "flex", flexDirection: "column", gap: "8px", paddingLeft: "12px", borderLeft: "2px solid #e0e0e0"};
const addSubRowStyle = {alignSelf: "flex-start"};
// only where there is room beside the rows
const asideVisibility = {display: {xs: "none", lg: "block"}};
const emptyStyle = {color: "rgba(0, 0, 0, 0.6)", fontStyle: "italic"};

/** What a row or sub row counts - none for a row made of sub rows - and the width each value needs. */
/** The values whose options are hinted on the side while they are typed into. */
const HINTED = ["debit", "credit"];

const ACCOUNT_INPUTS = [
    {name: "debit", label: "Debit", style: {width: 110}},
    {name: "credit", label: "Credit", style: {width: 110}},
    // the text a transaction's description has to contain to be counted - or, after a !, must not
    {name: "description", label: "required description ('!' to exclude)", style: growStyle},
];

/** Whether a row of the previous year is here already, as a row of the same name. */
const isHere = (sectionRows, source) => sectionRows.some((row) => !isSubRow(row.id) && row.name === source.name);

const editable = (row) => ({
    id: row.id,
    ...Object.fromEntries(["name", "planning", ...ACCOUNT_FIELDS].map((name) => [name, row[name] ?? ""])),
    // one of the rows every year has: only its planning is up to the year
    fixed: Boolean(row.fixed),
});
const stored = ({fixed, ...row}) => row;

/**
 * The rows of a year's budget as they are stored, part by part - income, mandatory expenses,
 * expenses, off-budget - to edit, add to and take from, all saved together: the budget is only
 * read again once every row has been accepted.
 *
 * A row is its name, then what it counts - its debit, credit and description, or its sub rows, each
 * a name with those - and last its planning, which only a row has. The first sub row added to a row
 * takes over what the row counted, with an empty one after it. What is missing or wrong is marked
 * where it is, and the rows are saved once nothing is. Rows are numbered by themselves, so their ids
 * are never shown; taking a row out takes its sub rows with it.
 *
 * A row of the previous year can be taken over, with its sub rows and its planning, part by part.
 *
 * The rows every year has - the vacations, the investments - are fixed: what they count is given,
 * and they cannot be taken out or given sub rows, so only their planning is edited.
 */
const BudgetEditorDialog = ({open, year, onClose, onSaved}) => {
    const path = "/budget/" + year + "/schema";
    const {data, loaded, error} = useData(path, open);
    // the year's accounts, which every debit and credit has to name
    const accounts = useData("/account/" + year, open);
    const accountIds = accounts.loaded ? accounts.data.map((account) => account.schemaId + "." + account.semanticId) : null;
    const schema = useData("/schema/" + year, open);
    // the previous year's rows, to take over from; none where there is no previous year
    const previous = useData("/budget/" + (year - 1) + "/schema", open);
    const [importing, setImporting] = useState(null);
    // the debit or credit being typed into, which the hints on the side are for
    const [focused, setFocused] = useState(null);
    const [rows, setRows] = useState([]);
    const focusedRow = focused === null ? undefined : rows.find((row) => row.id === focused.id);
    const hints = focusedRow && schema.loaded && accounts.loaded
        ? accountHints(focusedRow[focused.field], schema.data, accounts.data)
        : null;
    const [saving, setSaving] = useState(false);
    const [saveError, setSaveError] = useState(null);

    // what is stored, each time the editor is opened on it
    useEffect(() => {
        if (open && loaded) setRows(data.map(editable));
    }, [open, loaded, data]);

    useEffect(() => {
        if (open) setSaveError(null);
    }, [open]);

    const change = (id, field, value) =>
        setRows((current) => current.map((row) => (row.id === id ? {...row, [field]: value} : row)));
    // marked when missing or wrong; only what is wrong is said, as the bar above says the rest once
    const valueInput = (row, name, label, style) => {
        const problem = problems[row.id]?.[name];
        return (
            <TextField key={name} variant="standard" size="small" label={label} style={style}
                       value={row[name]} error={problem !== undefined} disabled={row.fixed}
                       helperText={problem !== undefined && problem !== "Required" ? problem : undefined}
                       onFocus={HINTED.includes(name) ? () => setFocused({id: row.id, field: name}) : undefined}
                       onBlur={HINTED.includes(name) ? () => setFocused(null) : undefined}
                       onChange={(event) => change(row.id, name, event.target.value)}/>
        );
    };
    const remove = (id) => setRows((current) => withoutRow(current, id));
    const addRow = (prefix) => setRows((current) => [...current, editable({id: nextRowId(current, prefix)})]);
    const addSubRow = (mainId) => setRows((current) => withSubRows(current, mainId, (id) => editable({id})));
    const importRow = (sourceId) => {
        setRows((current) => withImportedRow(current, previous.data, sourceId, editable));
        setImporting(null);
    };
    const problems = rowProblems(rows, accountIds);
    const complete = Object.keys(problems).length === 0;

    const save = () => {
        setSaving(true);
        setSaveError(null);
        axios.put(properties.backend + path, rows.map(stored), mutatingJson)
            .then(() => onSaved())
            .catch((failure) => setSaveError(describeResponseError(failure)))
            .finally(() => setSaving(false));
    };

    return (
        <Dialog fullScreen open={open} onClose={onClose}>
            <AppBar position="sticky">
                <Toolbar variant="dense">
                    <IconButton edge="start" color="inherit" onClick={onClose} aria-label="close">
                        <CloseIcon/>
                    </IconButton>
                    <Typography sx={titleStyle} variant="h6" component="div">
                        Budget {year}
                    </Typography>
                    {loaded && !complete &&
                        <Typography variant="body2" sx={incompleteStyle}>Fill in what is marked to save</Typography>}
                    <Button color="inherit" onClick={save} disabled={!loaded || saving || !complete}>
                        Save
                    </Button>
                </Toolbar>
            </AppBar>
            <Box style={contentStyle}>
            <Box style={mainStyle}>
                {error && <Alert severity="error">{describeResponseError(error)}</Alert>}
                {saveError && <Alert severity="error">{saveError}</Alert>}
                {!loaded && !error && <Loader error={null}/>}
                {loaded && SECTIONS.map(({prefix, title}) => {
                    const sectionRows = rowsOfSection(rows, prefix);
                    return (
                        <section key={prefix} style={sectionStyle} aria-label={title}>
                            <Box style={sectionHeadingStyle}>
                                <Typography variant="h6" component="h2">{title}</Typography>
                                <Button size="small" startIcon={<AddIcon/>} onClick={() => addRow(prefix)}>
                                    Add row
                                </Button>
                                {/* there, but of no use, while the previous year has nothing here that is not here already */}
                                <Button size="small" startIcon={<HistoryIcon/>}
                                        disabled={!previous.loaded || importableRows(previous.data, prefix)
                                            .every((source) => isHere(sectionRows, source))}
                                        onClick={(event) => setImporting({prefix, anchor: event.currentTarget})}>
                                    Import row
                                </Button>
                            </Box>
                            {importing?.prefix === prefix &&
                                <Menu open anchorEl={importing.anchor} onClose={() => setImporting(null)}>
                                    <MenuItem disabled dense>From {year - 1}</MenuItem>
                                    {importableRows(previous.data, prefix).map((source) => {
                                        const here = isHere(sectionRows, source);
                                        const subRowCount = previous.data.filter((other) =>
                                            isSubRow(other.id) && mainIdOf(other.id) === source.id).length;
                                        return (
                                            <MenuItem key={source.id} disabled={here} onClick={() => importRow(source.id)}>
                                                <ListItemText primary={source.name}
                                                              secondary={here ? "Already here"
                                                                  : subRowCount > 0 ? subRowCount + " subrows" : source.debit + " / " + source.credit}/>
                                            </MenuItem>
                                        );
                                    })}
                                </Menu>
                            }
                            {sectionRows.length === 0 && <Typography variant="body2" style={emptyStyle}>No rows</Typography>}
                            {sectionRows.filter((row) => !isSubRow(row.id)).map((row) => {
                                const subRows = sectionRows.filter((other) => isSubRow(other.id) && mainIdOf(other.id) === row.id);
                                return (
                                    <Paper key={row.id} variant="outlined" style={cardStyle} role="group" aria-label={row.id}>
                                        <Box style={lineStyle}>
                                            {valueInput(row, "name", "Name", nameStyle)}
                                            {!row.fixed &&
                                                <IconButton size="small" style={removeStyle} aria-label="remove row"
                                                            title="Remove row" onClick={() => remove(row.id)}>
                                                    <DeleteIcon fontSize="small"/>
                                                </IconButton>
                                            }
                                        </Box>
                                        {/* a row counts transactions itself, or is made of its sub rows' */}
                                        {subRows.length === 0
                                            ? <Box style={lineStyle}>
                                                {ACCOUNT_INPUTS.map(({name, label, style}) => valueInput(row, name, label, style))}
                                            </Box>
                                            : <Box style={subRowsStyle}>
                                                {subRows.map((subRow) => (
                                                    <Box key={subRow.id} style={subRowLineStyle} role="group" aria-label={subRow.id}>
                                                        {valueInput(subRow, "name", "Name", shrinkingStyle)}
                                                        {ACCOUNT_INPUTS.map(({name, label, style}) =>
                                                            valueInput(subRow, name, label, name === "description" ? shrinkingStyle : style))}
                                                        <IconButton size="small" style={removeSubRowStyle} aria-label="remove sub row" title="Remove sub row"
                                                                    onClick={() => remove(subRow.id)}>
                                                            <DeleteIcon fontSize="small"/>
                                                        </IconButton>
                                                    </Box>
                                                ))}
                                            </Box>
                                        }
                                        {!row.fixed &&
                                            <Button size="small" style={addSubRowStyle} startIcon={<AddIcon/>}
                                                    onClick={() => addSubRow(row.id)}>
                                                Add subrow
                                            </Button>
                                        }
                                        <PlanningInput value={row.planning} error={problems[row.id]?.planning !== undefined}
                                                       onChange={(value) => change(row.id, "planning", value)}/>
                                    </Paper>
                                );
                            })}
                        </section>
                    );
                })}
            </Box>
            <Box component="aside" style={asideStyle} sx={asideVisibility}>
                {hints !== null &&
                    <AccountHints hints={hints} onPick={(id) => change(focused.id, focused.field, id)}/>}
            </Box>
            </Box>
        </Dialog>
    );
};

BudgetEditorDialog.propTypes = {
    open: PropTypes.bool.isRequired,
    year: PropTypes.number.isRequired,
    onClose: PropTypes.func.isRequired,
    /** Called once the rows are saved, for the budget to be read again. */
    onSaved: PropTypes.func.isRequired,
};

export default BudgetEditorDialog;
