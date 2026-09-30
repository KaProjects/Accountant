import React, {useEffect, useState} from "react";
import PropTypes from "prop-types";
import {Alert, AlertTitle, Box, Button, CircularProgress, Paper, Typography} from "@mui/material";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import {runSync, syncActions} from "../services/sync";

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
const AdminSync = props => {

    const [results, setResults] = useState({});
    const [running, setRunning] = useState(null);

    useEffect(() => {
        props.setYearly(false)
        // eslint-disable-next-line
    }, []);

    const run = (action) => async () => {
        setRunning(action.id)
        setResults((previous) => ({...previous, [action.id]: null}))

        const result = await runSync(action.path)

        setResults((previous) => ({...previous, [action.id]: result}))
        setRunning(null)
    };

    return (
        <Box sx={{width: "100%", maxWidth: 640, margin: {xs: "8px auto", sm: "24px auto"}, padding: "0 8px"}}>
            <Button startIcon={<ArrowBackIcon/>} size="small" sx={{marginBottom: 1}}
                    onClick={() => {window.location.href = "/admin"}}>
                Admin
            </Button>

            {syncActions(props.year).map((action) => (
                <Paper key={action.id} variant="outlined" sx={{padding: "12px 14px", marginBottom: 2}}>
                    <Typography sx={{fontSize: 15, fontWeight: 600}}>{action.label}</Typography>
                    <Typography color="text.secondary" sx={{fontSize: 13, marginBottom: 1}}>
                        {action.caption}
                    </Typography>

                    <Button variant="contained" size="small"
                            disabled={running !== null}
                            onClick={run(action)}
                            data-testid={"run-" + action.id}>
                        {running === action.id ? <CircularProgress size={18}/> : "Run"}
                    </Button>

                    {results[action.id] && (
                        <Alert severity={results[action.id].ok ? "success" : "error"}
                               sx={{marginTop: 1.5}}
                               data-testid={"result-" + action.id}>
                            <AlertTitle sx={{marginBottom: 0}}>{results[action.id].status}</AlertTitle>
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

AdminSync.propTypes = {
    year: PropTypes.number.isRequired,
    setYearly: PropTypes.func.isRequired,
}

export default AdminSync;
