import React, {useEffect} from "react";
import {useData} from "../fetch";
import DataView from "../components/common/DataView";
import Paper from "@mui/material/Paper";
import PropTypes from "prop-types";
import {IconButton, Table, TableBody, TableCell, TableContainer, TableHead, TableRow} from "@mui/material";
import TransactionsDialog from "../components/TransactionsDialog";
import {useParams} from "react-router-dom";
import LaunchIcon from '@mui/icons-material/Launch';
import StatementRow from "../components/statement/StatementRow";
import {statementHeaderStyle} from "../theme/tableStyles";
import {useTransactionsDialog} from "../hooks/useTransactionsDialog";

const AccountingStatement = props => {
    const {type, overall} = useParams();
    const isOverall = overall !== undefined;

    const {data, loaded, error} = useData("/accounting/" + type + "/" + (isOverall ? "" : props.year))

    useEffect(() => {
        props.setYearly(!isOverall)
        // eslint-disable-next-line
    }, []);

    const [showChildren, setShowChildren] = React.useState([]);
    const [showGrandChild, setShowGrandChild] = React.useState(null);
    const [redirectYearIndex, setRedirectYearIndex] = React.useState(-1);

    const transactionsDialog = useTransactionsDialog();

    const toggleChildren = (id) => {
        const updated = showChildren.slice()
        updated[id] = !showChildren[id]
        setShowChildren(updated)
    }

    const toggleGrandChild = (schemaId) => {
        setShowGrandChild(showGrandChild === schemaId ? null : schemaId)
    }

    const redirectToYear = () => {
        sessionStorage.setItem('year', data.columns[redirectYearIndex])
        window.location.href='/accounting/' + type
    }

    const hasInitial = () => data.columns[1] === "Initial"
    const hasTotal = () => data.columns[data.columns.length - 1] === "Total"

    return (
        <DataView loaded={loaded} error={error}>
            {() => (
                <>
                <TableContainer component={Paper}>
                    <Table sx={{ minWidth: 650 }} size="small" aria-label="a dense table">
                        <TableHead>
                            <TableRow key={-1}>
                                {data.columns.map((column, index) => (
                                    <TableCell key={index}
                                               style={statementHeaderStyle(index, {columnCount: data.columns.length, hasInitial: hasInitial(), hasTotal: hasTotal()})}
                                               onClick={() => {if (index !== 0) setRedirectYearIndex(index)}}
                                               onMouseLeave={() => setRedirectYearIndex(-1)}
                                    >
                                        {column}
                                        {isOverall && index === redirectYearIndex &&
                                            <IconButton
                                                style={{height: "2px", width: "25px"}}
                                                onClick={() => redirectToYear()}
                                            >
                                                <LaunchIcon sx={{width: 18}}/>
                                            </IconButton>
                                        }
                                    </TableCell>
                                ))}
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {data.rows.map((row, index) => (
                                <StatementRow
                                    key={index}
                                    row={row}
                                    id={index}
                                    type={type}
                                    isOverall={isOverall}
                                    hasInitial={hasInitial()}
                                    hasTotal={hasTotal()}
                                    showChildren={Boolean(showChildren[index])}
                                    onToggleChildren={toggleChildren}
                                    showGrandChild={showGrandChild}
                                    onToggleGrandChild={toggleGrandChild}
                                    transactionsDialog={transactionsDialog}
                                />
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>
                <TransactionsDialog
                    open={transactionsDialog.open}
                    onClose={transactionsDialog.close}
                    year={props.year}
                    row={transactionsDialog.rowName}
                    rowId={transactionsDialog.rowId}
                    month={transactionsDialog.month}
                    type="ACCOUNTING"
                />
                </>
            )}
        </DataView>
    )
}

AccountingStatement.propTypes = {
    year: PropTypes.number.isRequired,
    setYearly: PropTypes.func.isRequired,
}

export default AccountingStatement;
