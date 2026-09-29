import React, {useEffect, useState} from "react";
import '../viewsContainer.css';
import {
    Collapse,
    List,
    ListItem,
    ListItemText,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow, Typography
} from "@mui/material";
import {ExpandLess, ExpandMore} from "@mui/icons-material";
import Paper from "@mui/material/Paper";
import Loader from "../components/Loader";
import {useData} from "../fetch";
import VacationChart from "../components/VacationChart";
import {useParams} from "react-router-dom";


const Views = props => {
    let { vacation } = useParams();

    const [transactionFlags, setTransactionFlags] = useState([])

    const {data, loaded, error} = useData("/view/" + props.year + (vacation === undefined ? "" : "/vacation"))

    useEffect(() => {
        props.setYearly(true)
        setTransactionFlags([])
        // eslint-disable-next-line
    }, [data]);

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

    function formatTitle(title) {
        return title.split(/(?=[A-Z]|[0-9])/).join(" ")
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
            {data.views.map((view, index) => (
                <div key={index}>
                <ListItem button onClick={() => toggleTransactions(index)}
                            style={getTitleStyle(index)}>
                    <ListItemText primary={formatTitle(view.name)} primaryTypographyProps={{ style: {fontWeight: "bold", fontFamily: "Copperplate", marginLeft: "10px"} }}/>
                    {transactionFlags[index] ? <ExpandLess /> : <ExpandMore />}
                </ListItem>
                <Collapse in={transactionFlags[index]} timeout="auto" unmountOnExit>
                    <div className={"parent"}>
                        <div>
                            <TableContainer component={Paper} style={{ height: 350 }}>
                                <Table sx={{ minWidth: 650 }} size="small" aria-label="a dense table" stickyHeader>
                                    <TableHead >
                                        <TableRow>
                                            {data.columns.map((column, index) => (
                                                <TableCell key={index} style={getHeaderStyle(index)}>{column}</TableCell>
                                            ))}
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {view.transactions.map((transaction, index) => (
                                            <TableRow key={index}>
                                                <TableCell>{transaction.date}</TableCell>
                                                <TableCell style={{textAlign: "right"}}>{transaction.amount}</TableCell>
                                                <TableCell>{transaction.debit}</TableCell>
                                                <TableCell>{transaction.credit}</TableCell>
                                                <TableCell>{transaction.description}</TableCell>
                                            </TableRow>
                                        ))}
                                    </TableBody>
                                </Table>
                            </TableContainer>
                            <Typography style={{fontWeight: "bold", margin: 15}}>
                                Total Expenses: {view.expenses}
                            </Typography>
                        </div>
                        <div className={"chartBottom"}>
                            <VacationChart data={view.chartData} isBottom={true}/>
                        </div>
                        <div className={"chartRight"}>
                            <VacationChart data={view.chartData} isBottom={false}/>
                        </div>
                    </div>
                </Collapse>
                </div>
            ))}
            </List>
        }
        </>
    )
}

export default Views;