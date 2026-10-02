import {useEffect, useState} from "react";
import axios from "axios";
import {properties} from "./properties";
import {credentialed} from "./services/session";

/**
 * One request to the backend, answering with what came back.
 *
 * A failure is logged and passed on. A refused session is also turned into a message the page can
 * show, and the page is reloaded a moment later: it asks the backend for its session on startup,
 * and will be shown the login form.
 */
const request = (path) => axios.get(properties.backend + path, credentialed)
    .then((response) => response.data)
    .catch((error) => {
        console.error(error)
        // A transport failure has no response at all, and reading a status off it threw, which
        // replaced the real error with a TypeError.
        if (error.response && error.response.status === 401) {
            error.message = "Session expired! Redirecting..."
            setTimeout(() => window.location.reload(), 1000)
        }
        throw error;
    });

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

        request(path)
            .then((data) => {
                if (awaited) setFetched({path, data, error: null})
            })
            .catch((error) => {
                if (awaited) setFetched({path, data: null, error})
            });

        return () => {awaited = false};
    }, [path, enabled]);

    const answersThisPath = fetched.path === path;

    return {
        data: answersThisPath ? fetched.data : null,
        loaded: answersThisPath && fetched.error === null && fetched.data !== null,
        error: answersThisPath ? fetched.error : null,
    };
};

/**
 * Fetches several paths at once, for a view built from more than one answer, and reports them
 * together: loaded once every one of them has come back, in the order the paths were given, and
 * failed as soon as any one of them has.
 *
 * What came back is remembered with the paths it came back for, exactly as useData remembers its
 * one path, and for the same reason.
 *
 * Asked for no paths at all, it is loaded at once, with nothing: there is nothing to wait for. It
 * used to wait for answers that were never asked for, and a view whose statement had no years in it
 * showed its loader for ever.
 */
export const useEachData = (paths, enabled = true) => {

    const key = paths.join("\n");
    const [fetched, setFetched] = useState({key: null, data: null, error: null});

    useEffect(() => {
        if (!enabled || key === "") return;

        let awaited = true;

        Promise.all(key.split("\n").map(request))
            .then((data) => {
                if (awaited) setFetched({key, data, error: null})
            })
            .catch((error) => {
                if (awaited) setFetched({key, data: null, error})
            });

        return () => {awaited = false};
    }, [key, enabled]);

    if (enabled && key === "") return {data: [], loaded: true, error: null};

    const answersThesePaths = fetched.key === key;

    return {
        data: answersThesePaths ? fetched.data : null,
        loaded: answersThesePaths && fetched.error === null && fetched.data !== null,
        error: answersThesePaths ? fetched.error : null,
    };
};
