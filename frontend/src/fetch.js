import {useEffect, useState} from "react";
import axios from "axios";
import {properties} from "./properties";

export const useData = (path) => {

    const [data, setData] = useState(null);
    const [loaded, setLoaded] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        const dataFetch = async () => {
            axios.get(properties.protocol + "://" + properties.host + ":" + properties.port + path).then(
                (response) => {
                    setData(response.data);
                    setError(null)
                    setLoaded(true);
                }).catch((error) => {
                console.error(error)
                setError(error)
            })
        };

        dataFetch();
    }, [path]);

    return { data, loaded, error };
};