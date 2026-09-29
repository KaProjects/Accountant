import React, {useEffect, useState} from "react";
import Loader from "./Loader";
import {Dialog, DialogTitle, Table, TableBody, TableCell, TableContainer, TableHead, TableRow} from "@mui/material";
import {properties} from "../properties";
import axios from "axios";
import Paper from "@mui/material/Paper";


const TransactionsDialog = props => {

    const [data, setData] = useState(null);
    const [loaded, setLoaded] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        let path;
        if (props.type === "BUDGET") {
            path = "/budget/" + props.year + "/transaction/" + props.rowId + "/month/" + props.month
        } else if (props.type === "ACCOUNTING") {
            path = "/accounting/" + props.year + "/transaction/" + props.rowId + "/month/" + props.month
        } else {
            const error = {message: "INVALID TRANSACTION DIALOG TYPE"}
            console.error(error)
            setError(error)
            setLoaded(false)
            setData(null)
            return;
        }

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
        // eslint-disable-next-line
    }, [props.open]);

    const columns = ["Date", "Amount", "Debit", "Credit", "Description"]

    function getTotal(){
        let sum = 0;
        data.forEach((transaction) => sum += parseInt(transaction.amount))
        return sum
    }

    return (
        <Dialog
            open={props.open}
            onClose={props.onClose}
            fullWidth={false}
            maxWidth={'lg'}
        >
            <DialogTitle>Transactions for {props.row} {props.month}/{props.year}</DialogTitle>

            {!loaded &&
                <Loader error ={error}/>
            }
            {loaded &&
                <>
                <TableContainer component={Paper}>
                    <Table sx={{ minWidth: 700 }} size="small" aria-label="a dense table">
                        <TableHead>
                            <TableRow>
                                {columns.map((column, index) => (
                                    <TableCell key={index}>{column}</TableCell>
                                ))}
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {data.map((transaction, index) => (
                                <TableRow key={index}>
                                    <TableCell align="center">{transaction.date}</TableCell>
                                    <TableCell align="right">{transaction.amount}</TableCell>
                                    <TableCell align="left" style={{minWidth: "150px"}}>{transaction.debit}</TableCell>
                                    <TableCell align="left" style={{minWidth: "150px"}}>{transaction.credit}</TableCell>
                                    <TableCell align="left" style={{minWidth: "150px"}}>{transaction.description}</TableCell>
                                </TableRow>
                            ))}
                            <TableRow key={-1}>
                                <TableCell align="center" style={{fontWeight: "bold"}}>Total: </TableCell>
                                <TableCell align="right" style={{fontWeight: "bold"}}>{getTotal()}</TableCell>
                                <TableCell/>
                                <TableCell/>
                                <TableCell/>
                            </TableRow>
                        </TableBody>
                    </Table>
                </TableContainer>
                </>
            }
        </Dialog>
    )
}

export default TransactionsDialog;
