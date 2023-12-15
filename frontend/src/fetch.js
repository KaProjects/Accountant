import {useEffect, useState} from "react";
import axios from "axios";
import {properties} from "./properties";

export const useData = (path) => {

    const [data, setData] = useState(null);
    const [loaded, setLoaded] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        const dataFetch = async () => {
            const url = properties.protocol + "://" + properties.host + ":" + properties.port + path;
            const headers = { headers: {Authorization: 'Bearer ' + sessionStorage.getItem('token')}};
            await axios.get(url, headers).then(
                (response) => {
                    setData(response.data)
                    setError(null)
                    setLoaded(true)
                }).catch((error) => {
                    console.error(error)
                    setError(error)
                    setLoaded(false)
            })
        };

        dataFetch();
    }, [path]);

    return { data, loaded, error };
};