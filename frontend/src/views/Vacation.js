import React, {useEffect, useState} from "react";
import {Collapse, List, ListItem, ListItemText} from "@mui/material";
import {ExpandLess, ExpandMore} from "@mui/icons-material";
import {Table, TableBody, TableCell, TableContainer, TableHead, TableRow} from "@material-ui/core";
import Paper from "@mui/material/Paper";
import Loader from "../components/Loader";
import {useData} from "../fetch";


const Vacation = props => {

    const [transactionFlags, setTransactionFlags] = useState([])

    const {data, loaded, error} = useData("http://" + props.host + ":" + props.port + "/view/" + props.year + "/vacation")

    useEffect(() => {
        props.setYearly(true)
    }, []);

    function toggleTransactions(index){
        const newFlag = !transactionFlags[index]
        const newFlags = Array(transactionFlags.length).fill(false)
        newFlags[index] = newFlag
        setTransactionFlags(newFlags)
    }

    function getHeaderStyle(index) {
        let width = null
        if (index <= 1) {
            width = "60px"
        } else if (index <= 3) {
            width = "300px"
        }

        return {width: width, fontWeight: "bold"}
    }

    function getTitleStyle(index) {
        let boxShadow = "0 0 8px 0";
        let background = transactionFlags[index] ? "#87befc" : "#b6d8ff";
        let color = "#3361bb";
        return {boxShadow: boxShadow, background: background, color: color};
    }

    return (
        <>
        {!loaded &&
            <Loader error ={error}/>
        }
        {loaded &&
            <List
            component="nav"
            aria-labelledby="nested-list-subheader"
            >
            {data.vacations.map((vacation, index) => (
                <div key={index}>
                <ListItem button onClick={() => toggleTransactions(index)}
                            style={getTitleStyle(index)}>
                    <ListItemText primary={vacation.name} primaryTypographyProps={{ style: {fontWeight: "bold"} }}/>
                    {transactionFlags[index] ? <ExpandLess /> : <ExpandMore />}
                </ListItem>
                <Collapse in={transactionFlags[index]} timeout="auto" unmountOnExit>
                    <TableContainer component={Paper}>
                        <Table sx={{ minWidth: 650 }} size="small" aria-label="a dense table">
                            <TableHead>
                                <TableRow>
                                    {data.columns.map((column, index) => (
                                        <TableCell key={index} style={getHeaderStyle(index)}>{column}</TableCell>
                                    ))}
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {vacation.transactions.map((transaction, index) => (
                                    <TableRow key={index}>
                                        <TableCell>{transaction.date}</TableCell>
                                        <TableCell style={{textAlign: "right"}}>{transaction.amount}</TableCell>
                                        <TableCell>{transaction.debit}</TableCell>
                                        <TableCell>{transaction.credit}</TableCell>
                                        <TableCell>{transaction.description}</TableCell>
                                    </TableRow>
                                ))}
                                <TableRow key="-1">
                                    <TableCell style={{fontWeight: "bold"}}>Expenses:</TableCell>
                                    <TableCell style={{textAlign: "right", fontWeight: "bold"}}>{vacation.expenses}</TableCell>
                                    <TableCell></TableCell>
                                    <TableCell></TableCell>
                                    <TableCell></TableCell>
                                </TableRow>
                            </TableBody>
                        </Table>
                    </TableContainer>
                </Collapse>
                </div>
            ))}

            </List>
        }
        </>
    )
}

export default Vacation;