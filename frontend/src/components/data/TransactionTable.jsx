import PropTypes from "prop-types";
import {Table, TableBody, TableCell, TableContainer, TableHead, TableRow} from "@mui/material";
import Paper from "@mui/material/Paper";
import {useData} from "../../fetch";
import DataView from "../common/DataView";

const COLUMNS = ["Date", "Debit", "Credit", "Account Pair", "Description"];

/** Every transaction posted against one account in the given year. */
const TransactionTable = ({year, accountId}) => {
    const {data, loaded, error} = useData("/transaction/" + year + "/" + accountId);

    return (
        <DataView loaded={loaded} error={error}>
            {() => (
                <TableContainer component={Paper}>
                    <Table sx={{minWidth: 100}} size="small" aria-label="a dense table">
                        <TableHead>
                            <TableRow>
                                {COLUMNS.map((column, index) => (
                                    <TableCell key={index}>{column}</TableCell>
                                ))}
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {data.map((transaction, index) => (
                                <TableRow hover key={index}>
                                    <TableCell align="left">{transaction.date}</TableCell>
                                    <TableCell align="right">{transaction.debit}</TableCell>
                                    <TableCell align="right">{transaction.credit}</TableCell>
                                    <TableCell align="left">{transaction.pair}</TableCell>
                                    <TableCell align="left">{transaction.description}</TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>
            )}
        </DataView>
    );
};

TransactionTable.propTypes = {
    year: PropTypes.number.isRequired,
    accountId: PropTypes.string.isRequired,
};

export default TransactionTable;
