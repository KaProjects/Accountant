import PropTypes from "prop-types";
import {Table, TableBody, TableCell, TableContainer, TableHead, TableRow} from "@mui/material";
import Paper from "@mui/material/Paper";
import {useData} from "../../fetch";
import DataView from "../common/DataView";

/**
 * The accounts belonging to one schema account, with the closing figure labelled
 * as a running balance only while the year is still open.
 */
const AccountTable = ({year, schemaId, selectedAccountId, onSelectAccount}) => {
    const {data, loaded, error} = useData("/account/" + year + "/" + schemaId);
    const closingColumn = new Date().getFullYear() === year ? "Balance" : "Closure";
    const columns = ["Id", "Name", "Initial", "Turnover", closingColumn];

    return (
        <DataView loaded={loaded} error={error}>
            {() => (
                <TableContainer component={Paper}>
                    <Table sx={{minWidth: 100}} size="small" aria-label="a dense table">
                        <TableHead>
                            <TableRow>
                                {columns.map((column, index) => (
                                    <TableCell key={index}>{column}</TableCell>
                                ))}
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {data.map((account, index) => (
                                <TableRow hover key={index}
                                          onClick={() => onSelectAccount(account.id)}
                                          selected={account.id === selectedAccountId}>
                                    <TableCell align="left">{account.id}</TableCell>
                                    <TableCell align="left">{account.name}</TableCell>
                                    <TableCell align="right">{account.initial}</TableCell>
                                    <TableCell align="right">{account.turnover}</TableCell>
                                    <TableCell align="right">{account.balance}</TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>
            )}
        </DataView>
    );
};

AccountTable.propTypes = {
    year: PropTypes.number.isRequired,
    schemaId: PropTypes.string.isRequired,
    selectedAccountId: PropTypes.string,
    onSelectAccount: PropTypes.func.isRequired,
};

export default AccountTable;
