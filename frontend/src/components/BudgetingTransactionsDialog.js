import React, {useEffect, useState} from "react";
import Loader from "./Loader";
import {Dialog, DialogTitle} from "@mui/material";
import {properties} from "../properties";
import axios from "axios";


const BudgetingTransactionsDialog = props => {

    const [data, setData] = useState(null);
    const [loaded, setLoaded] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        const path = "/budget/" + props.year + "/transaction/" + props.rowId + "/month/" + props.month

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
                setData(null)
            })
        };

        if (props.open){
            dataFetch()
        }

    }, [props.open]);


    return (
        <Dialog
            open={props.open}
            onClose={props.onClose}
            fullWidth={true}
        >
            <DialogTitle>Transactions for {props.row} {props.month}/{props.year}</DialogTitle>

            {!loaded &&
                <Loader error ={error}/>
            }
            {loaded &&
                <>

                {data.map((transaction, index) => (
                    <div>
                        {transaction.date} {transaction.amount} {transaction.debit} {transaction.credit} {transaction.description}
                    </div>
                    ))}
                </>
            }
        </Dialog>
    )
}

export default BudgetingTransactionsDialog;
