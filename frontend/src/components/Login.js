import PropTypes from "prop-types";
import {useState} from "react";
import {properties as props} from "../properties";
import {Alert, Button, Slide, Snackbar, TextField} from "@mui/material";
import axios from "axios";

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
            url: "http://" + props.host + ":" + props.port + "/authenticate",
            headers: {'Content-Type': 'application/json'},
            data: {username, password}
        }).then(
            (response) => {
                setToken(response.data)
            }).catch(
                (error) => {
                    const message = error.response
                        ? error.response.status + " " + error.response.statusText + ": " + error.response.data
                        : error.code + " " + error.message
                    setError(message)
                    setErrorToggle(true)
        })
    }

    return(
        <div style={{display: "flex", flexDirection: "column", alignItems: "left", marginLeft: "50px"}}>
            <form onSubmit={handleSubmit}>
                <div style={{marginTop: "50px"}}>
                    <TextField label="Username" variant="outlined"
                               value={username}
                               onChange={(e) => setUsername(e.target.value)}/>
                </div>
                <div style={{marginTop: "10px"}}>
                    <TextField label="Password" variant="outlined" type="password"
                               value={password}
                               onChange={(e) => setPassword(e.target.value)}/>
                </div>
                <div style={{marginTop: "10px"}}>
                    <Button variant="contained" type="submit">Login</Button>
                </div>
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