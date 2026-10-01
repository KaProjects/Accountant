import {useEffect, useState} from "react";
import axios from "axios";
import {properties} from "./properties";
import {credentialed} from "./services/session";

/**
 * Fetches a path, optionally only once it is wanted.
 *
 * The flag exists for a caller that knows the path before it knows whether it will be used - a
 * dialog that has been pointed at a row but not yet opened. Such a caller used to be forced to
 * write its own request instead, which is how one of them came to miss a change to how the session
 * is sent.
 *
 * What came back is remembered together with the path it came back for, and is only reported as an
 * answer to that same path. Anything else is still loading. This has to be decided while
 * rendering, not in an effect afterwards: a view reads its parameters from the address, which
 * changes during the render, so clearing the previous payload one effect later is a frame too
 * late. That frame is what crashed the statement on the way from the overall view to a single
 * year - it rendered the rows it still held, which carry yearly values, against parameters that
 * said to read monthly ones.
 */
export const useData = (path, enabled = true) => {

    const [fetched, setFetched] = useState({path: null, data: null, error: null});

    useEffect(() => {
        if (!enabled) return;

        // Set while this request is the current one, so a slow answer that arrives after the path
        // has moved on is discarded rather than shown.
        let awaited = true;

        const dataFetch = async () => {
            await axios.get(properties.backend + path, credentialed)
                .then((response) => {
                    if (awaited) setFetched({path, data: response.data, error: null})
                }).catch((error) => {
                    console.error(error)
                    // A transport failure has no response at all, and reading a status off it
                    // threw, which replaced the real error with a TypeError.
                    if (error.response && error.response.status === 401) {
                        error.message = "Session expired! Redirecting..."
                        // Reloading is enough: the page asks the backend for its session on
                        // startup, and will be shown the login form.
                        setTimeout(() => window.location.reload(), 1000)
                    }
                    if (awaited) setFetched({path, data: null, error})
                })
        };

        dataFetch();

        return () => {awaited = false};
    }, [path, enabled]);

    const answersThisPath = fetched.path === path;

    return {
        data: answersThisPath ? fetched.data : null,
        loaded: answersThisPath && fetched.error === null && fetched.data !== null,
        error: answersThisPath ? fetched.error : null,
    };
};
