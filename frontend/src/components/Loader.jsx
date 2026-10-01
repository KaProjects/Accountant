import PropTypes from "prop-types";
import {Alert, CircularProgress} from "@mui/material";
import React from "react";


const Loader = props => {

    return (
        <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "100vh"}}>
            {props.error === null ? <CircularProgress/>
                : <Alert severity="error">{props.error.message}</Alert> }
        </div>
    )
}

Loader.propTypes = {
    /** Null while the request is still in flight; the failure once it is not. */
    error: PropTypes.shape({message: PropTypes.string}),
};

export default Loader;