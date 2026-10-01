import {useEffect, useState} from "react";
import axios from "axios";
import {properties} from "./properties";
import {credentialed} from "./services/session";

export const useData = (path) => {

    const [data, setData] = useState(null);
    const [loaded, setLoaded] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        const dataFetch = async () => {
            await axios.get(properties.backend + path, credentialed)
                .then((response) => {
                    setData(response.data)
                    setError(null)
                    setLoaded(true)
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
                    setError(error)
                    setLoaded(false)
                })
        };

        dataFetch();
    }, [path]);

    return { data, loaded, error };
};
