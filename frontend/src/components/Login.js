import PropTypes from "prop-types";
import React, {useState} from "react";
import {properties} from "../properties";
import {Alert, Button, Slide, Snackbar, TextField} from "@mui/material";
import axios from "axios";

/** The login screen is the entire page until a token exists, so it centres itself. */
const centeredPage = {
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    minHeight: "100vh",
};

const loginForm = {
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    gap: "10px",
};

/**
 * Turns a failed login into something readable.
 *
 * The backend answers with plain text for the errors it raises itself, but an unexpected one is
 * answered by the container's own handler with a JSON object. Concatenating that into the
 * message rendered it as "[object Object]", which said nothing at all; its "details" carries
 * the error id and the exception, while its "stack" is far too long for a toast.
 */
export const describeLoginError = (error) => {
    if (!error.response) return error.code + " " + error.message

    const body = error.response.data
    let detail
    if (typeof body === "string") detail = body
    else if (body && typeof body === "object") detail = body.details || body.message || ""
    else detail = ""

    const status = error.response.status + " " + error.response.statusText
    return detail ? status + ": " + detail : status
}

export default function Login({ setToken}){

    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");

    const [errorToggle, setErrorToggle] = useState(false);
    const [error, setError] = useState("");

    const handleSubmit = async e => {
        e.preventDefault();
        setError("")

        axios({
            method: 'post',
            url: properties.backend + "/authenticate",
            headers: {'Content-Type': 'application/json'},
            data: {username, password}
        }).then(
            (response) => {
                setToken(response.data)
            }).catch(
                (error) => {
                    setError(describeLoginError(error))
                    setErrorToggle(true)
        })
    }

    return(
        <div style={centeredPage} data-testid="login-page">
            <form onSubmit={handleSubmit} style={loginForm}>
                <TextField label="Username" variant="outlined"
                           value={username}
                           onChange={(e) => setUsername(e.target.value)}/>
                <TextField label="Password" variant="outlined" type="password"
                           value={password}
                           onChange={(e) => setPassword(e.target.value)}/>
                <Button variant="contained" type="submit">Login</Button>
            </form>
            <Slide direction="up" in={errorToggle} mountOnEnter unmountOnExit>
                <Snackbar open={errorToggle}
                          autoHideDuration={5000}
                          onClose={() => setErrorToggle(false)}
                          anchorOrigin={{vertical: "top", horizontal: "center"}}>
                    <Alert onClose={() => setErrorToggle(false)} severity="error" sx={{ width: '100%' }}>
                        {error}
                    </Alert>
                </Snackbar>
            </Slide>
        </div>
    )
}

Login.propTypes = {
    setToken: PropTypes.func.isRequired
}