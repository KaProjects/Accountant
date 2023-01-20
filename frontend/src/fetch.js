import {useEffect, useState} from "react";
import axios from "axios";

export const useData = (url) => {

    const [data, setData] = useState(null);
    const [loaded, setLoaded] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        const dataFetch = async () => {
            axios.get(url).then(
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
    }, [url]);

    return { data, loaded, error };
};