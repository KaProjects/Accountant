import React, {useEffect, useState} from "react";
import {Alert, AlertTitle, Box, Button, CircularProgress, MenuItem, Paper, TextField, Typography} from "@mui/material";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import {runSync, syncActions} from "../services/sync";
import {useAppState} from "../state/appState";
import {useGoTo} from "../services/navigation";

const pageStyle = {width: "100%", maxWidth: 640, margin: {xs: "8px auto", sm: "24px auto"}, padding: "0 8px"};
const backStyle = {marginBottom: 1};
const actionStyle = {padding: "12px 14px", marginBottom: 2};
const actionLabelStyle = {fontSize: 15, fontWeight: 600};
const actionCaptionStyle = {fontSize: 13, marginBottom: 1};
const resultStyle = {marginTop: 1.5};
const controlsStyle = {display: "flex", alignItems: "center", gap: "12px"};
const yearSelectStyle = {width: 110};

/** The years the books can hold, as the sync accepts them: from the first one up to this one. */
const FIRST_YEAR = 2015;
const years = () => {
    const latest = new Date().getFullYear();
    return Array.from({length: latest - FIRST_YEAR + 1}, (_, index) => latest - index);
};
const statusStyle = {marginBottom: 0};

/** The response bodies are plain text and the validation one is a report, so it is shown as written. */
const responseStyle = {
    margin: "8px 0 0",
    padding: "8px 10px",
    maxHeight: 260,
    overflow: "auto",
    fontFamily: "monospace",
    fontSize: 12,
    whiteSpace: "pre-wrap",
    wordBreak: "break-word",
    backgroundColor: "action.hover",
    borderRadius: 1,
};

/**
 * Runs the data sync endpoints and shows what each one answered.
 *
 * They used to be reachable without a token, which made a browser or a single curl enough to
 * trigger one. Now that every endpoint requires a session, the application itself is the place
 * to run them from, because it already has one.
 */
const AdminSync = () => {
    const {setYearly} = useAppState();
    // the year synced on its own: the latest, unless another is chosen
    const [syncedYear, setSyncedYear] = useState(() => new Date().getFullYear());
    const goTo = useGoTo();

    const [results, setResults] = useState({});
    const [running, setRunning] = useState(null);

    useEffect(() => {
        setYearly(false)
    }, [setYearly]);

    const run = (action) => async () => {
        setRunning(action.id)
        setResults((previous) => ({...previous, [action.id]: null}))

        const result = await runSync(action.path)

        setResults((previous) => ({...previous, [action.id]: result}))
        setRunning(null)
    };

    return (
        <Box sx={pageStyle}>
            <Button startIcon={<ArrowBackIcon/>} size="small" sx={backStyle}
                    onClick={goTo("/admin")}>
                Admin
            </Button>

            {syncActions(syncedYear).map((action) => (
                <Paper key={action.id} variant="outlined" sx={actionStyle}>
                    <Typography sx={actionLabelStyle}>{action.label}</Typography>
                    <Typography color="text.secondary" sx={actionCaptionStyle}>
                        {action.caption}
                    </Typography>

                    <Box sx={controlsStyle}>
                        {action.id === "year" &&
                            <TextField select size="small" label="Year" sx={yearSelectStyle}
                                       value={syncedYear} disabled={running !== null}
                                       onChange={(event) => setSyncedYear(Number(event.target.value))}>
                                {years().map((one) => <MenuItem key={one} value={one}>{one}</MenuItem>)}
                            </TextField>
                        }
                        <Button variant="contained" size="small"
                                disabled={running !== null}
                                onClick={run(action)}
                                data-testid={"run-" + action.id}>
                            {running === action.id ? <CircularProgress size={18}/> : "Run"}
                        </Button>
                    </Box>

                    {results[action.id] && (
                        <Alert severity={results[action.id].ok ? "success" : "error"}
                               sx={resultStyle}
                               data-testid={"result-" + action.id}>
                            <AlertTitle sx={statusStyle}>{results[action.id].status}</AlertTitle>
                            {results[action.id].body && (
                                <Box sx={responseStyle}>{results[action.id].body}</Box>
                            )}
                        </Alert>
                    )}
                </Paper>
            ))}
        </Box>
    )
}


export default AdminSync;
