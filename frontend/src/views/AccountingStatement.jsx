import React, {useEffect} from "react";
import {useData} from "../fetch";
import DataView from "../components/common/DataView";
import Paper from "@mui/material/Paper";
import {IconButton, Table, TableBody, TableCell, TableContainer, TableHead, TableRow} from "@mui/material";
import TransactionsDialog from "../components/dialog/TransactionsDialog";
import {useParams} from "react-router-dom";
import LaunchIcon from '@mui/icons-material/Launch';
import StatementRow from "../components/statement/StatementRow";
import StatementChart from "../components/chart/StatementChart";
import {statementHeaderStyle} from "../theme/tableStyles";
import {useTransactionsDialog} from "../hooks/useTransactionsDialog";
import {useAppState, yearlyPath} from "../state/appState";
import {useGoTo} from "../services/navigation";
import {statementCharts} from "../services/statementCharts";

/**
 * Offered on hover, to the left of the year, so it reads as "open this year".
 *
 * It is laid out over the cell rather than inside it: appearing and disappearing in the flow made
 * the column widen and narrow under the pointer, which moved every column after it.
 */
const openYearStyle = {
    position: "absolute",
    left: 0,
    top: "50%",
    transform: "translateY(-50%)",
    height: "2px",
    width: "25px",
};
const openYearIconStyle = {width: 18};
/**
 * The offer is positioned against this block, not against the header cell: the lines between cells
 * are box shadows, and a positioned cell paints over the ones its neighbours cast onto it.
 */
const yearHeaderStyle = {position: "relative", display: "block"};

const AccountingStatement = () => {
    const {year, setYearly} = useAppState();
    const goTo = useGoTo();
    const {type, overall} = useParams();
    const isOverall = overall !== undefined;

    const {data, loaded, error} = useData("/accounting/" + type + "/" + (isOverall ? "" : year))

    useEffect(() => {
        setYearly(!isOverall)
    }, [isOverall, setYearly]);

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
        // Setting the year used to mean writing it down and letting the reload read it back.
        goTo(yearlyPath('/accounting/' + type, data.columns[redirectYearIndex]))()
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
                                               onMouseEnter={() => {if (index !== 0) setRedirectYearIndex(index)}}
                                               onMouseLeave={() => setRedirectYearIndex(-1)}
                                    >
                                        <span style={yearHeaderStyle}>
                                            {isOverall && index === redirectYearIndex &&
                                                <IconButton
                                                    style={openYearStyle}
                                                    onClick={() => redirectToYear()}
                                                >
                                                    <LaunchIcon sx={openYearIconStyle}/>
                                                </IconButton>
                                            }
                                            {column}
                                        </span>
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
                                    columns={{
                                        type,
                                        isOverall,
                                        hasInitial: hasInitial(),
                                        hasTotal: hasTotal(),
                                    }}
                                    expansion={{
                                        showChildren: Boolean(showChildren[index]),
                                        onToggleChildren: toggleChildren,
                                        showGrandChild,
                                        onToggleGrandChild: toggleGrandChild,
                                    }}
                                    transactionsDialog={transactionsDialog}
                                />
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>

                {/* The overall views leave the page half empty below the table, which is where
                    the shape of all those years is easiest to read. */}
                {isOverall && statementCharts(type, data).map((chart) => (
                    <StatementChart key={chart.key} chart={chart}/>
                ))}

                <TransactionsDialog
                    open={transactionsDialog.open}
                    onClose={transactionsDialog.close}
                    year={year}
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


export default AccountingStatement;
