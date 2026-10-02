import React, {useEffect, useRef} from "react";
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
import {useColumnLayout} from "../hooks/useColumnLayout";
import {useAppState, yearlyPath} from "../state/appState";
import {useGoTo} from "../services/navigation";
import {statementCharts, statementRowShades} from "../services/statementCharts";

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

    // A chart lined up with the table is drawn from where the table actually put its columns.
    const tableRef = useRef(null);
    const measured = useColumnLayout(tableRef, data);

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
    // Below an overall table sits its chart, and a row painted like its part of the chart is one
    // less thing to match up by eye.
    const rowShades = () => statementRowShades(type, data, isOverall)
    const charts = () => statementCharts(type, data, isOverall)
    const tableLayout = () => measured === null ? null : {
        width: measured.width,
        columns: measured.columns.map((column, index) => ({...column, name: data.columns[index]})),
    }

    return (
        <DataView loaded={loaded} error={error}>
            {() => (
                <>
                <TableContainer component={Paper}>
                    <Table ref={tableRef} sx={{ minWidth: 650 }} size="small" aria-label="a dense table">
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
                                    shade={rowShades()[index]}
                                />
                            ))}
                        </TableBody>
                    </Table>
                    {/* A chart lined up with the table sits inside its scroll, so that the two
                        move together when the table is wider than the window. */}
                    {tableLayout() !== null && charts().filter((chart) => chart.alignToTable).map((chart) => (
                        <StatementChart key={chart.key} chart={chart} layout={tableLayout()} titled={charts().length > 1}/>
                    ))}
                </TableContainer>

                {/* The page is half empty below the table, which is where the shape of the
                    figures is easiest to read. */}
                {charts().filter((chart) => !chart.alignToTable).map((chart) => (
                    <StatementChart key={chart.key} chart={chart} titled={charts().length > 1}/>
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
