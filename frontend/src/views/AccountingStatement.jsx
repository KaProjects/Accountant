import React, {useEffect, useRef} from "react";
import {useData, useEachData} from "../fetch";
import DataView from "../components/common/DataView";
import Paper from "@mui/material/Paper";
import {Table, TableBody, TableCell, TableContainer, TableHead, TableRow} from "@mui/material";
import TransactionsDialog from "../components/dialog/TransactionsDialog";
import {useParams} from "react-router-dom";
import LaunchIcon from '@mui/icons-material/Launch';
import StatementRow from "../components/statement/StatementRow";
import StatementChart from "../components/chart/StatementChart";
import VariantChart from "../components/chart/VariantChart";
import {highlightedCell, statementHeaderStyle} from "../theme/tableStyles";
import CornerMark from "../components/common/CornerMark";
import {useTransactionsDialog} from "../hooks/useTransactionsDialog";
import {useColumnLayout} from "../hooks/useColumnLayout";
import {useAppState, yearlyPath} from "../state/appState";
import {useGoTo} from "../services/navigation";
import {monthlyBalanceCharts, monthlyCashFlowChart, monthlyProfitCharts, statementCharts, statementRowShades} from "../services/statementCharts";

/**
 * The corner mark of a year is positioned against this block, not against the header cell: the
 * lines between cells are box shadows, and a positioned cell paints over the ones its neighbours
 * cast onto it.
 */
const yearHeaderStyle = {position: "relative", display: "block"};

const AccountingStatement = () => {
    const {year, setYearly, setOverallPath} = useAppState();
    const goTo = useGoTo();
    const {type, overall} = useParams();
    const isOverall = overall !== undefined;

    const {data, loaded, error} = useData("/accounting/" + type + "/" + (isOverall ? "" : year))

    useEffect(() => {
        setYearly(!isOverall)
    }, [isOverall, setYearly]);

    // A single year can be left for the overall view of the same statement from the main bar, which
    // the overall view only offered the other way round, from its headers. Withdrawn on leaving, so
    // no other page offers a way to a statement it is not showing.
    useEffect(() => {
        setOverallPath(isOverall ? null : "/accounting/" + type + "/overall");
        return () => setOverallPath(null);
    }, [isOverall, type, setOverallPath]);

    const [showChildren, setShowChildren] = React.useState([]);
    const [showGrandChild, setShowGrandChild] = React.useState(null);
    const [redirectYearIndex, setRedirectYearIndex] = React.useState(-1);

    const transactionsDialog = useTransactionsDialog();

    // Every overall statement is also charted month by month, which takes every year's own statement.
    const monthsOfEveryYear = isOverall && ["balance", "cashflow", "profit"].includes(type) && loaded;
    const everyYear = monthsOfEveryYear ? data.columns.slice(1).filter((column) => /^\d{4}$/.test(column)) : [];
    const yearly = useEachData(everyYear.map((each) => "/accounting/" + type + "/" + each), monthsOfEveryYear);

    // An overall statement too wide for the screen opens scrolled to its latest year, at the right.
    // A single year is left at its start: its right end is the total and the months still to come.
    const containerRef = useRef(null);
    useEffect(() => {
        if (!isOverall || !loaded || containerRef.current === null) return;
        containerRef.current.scrollLeft = containerRef.current.scrollWidth;
    }, [isOverall, loaded, data]);

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

    // Only a column that is a year opens one: not the statement's name, nor the income statement's
    // Total, which used to be offered too and led to a year called "Total".
    const opensYear = (index) => isOverall && /^\d{4}$/.test(String(data.columns[index]))

    const openYear = (index) => {
        // Setting the year used to mean writing it down and letting the reload read it back.
        goTo(yearlyPath('/accounting/' + type, data.columns[index]))()
    }

    /**
     * What the header of a year does on the overall view: pointing at it shades and marks it, and a
     * click anywhere on it - or Enter, from the keyboard - opens that year.
     */
    const yearHeaderProps = (index) => !opensYear(index) ? {} : {
        tabIndex: 0,
        onMouseEnter: () => setRedirectYearIndex(index),
        onMouseLeave: () => setRedirectYearIndex(-1),
        onClick: () => openYear(index),
        onKeyDown: (event) => {
            if (event.key === "Enter") openYear(index);
        },
    }
    const headerStyle = (index) => {
        const style = statementHeaderStyle(index, {columnCount: data.columns.length, hasInitial: hasInitial(), hasTotal: hasTotal()})
        return opensYear(index) && index === redirectYearIndex ? highlightedCell(style) : style
    }

    const hasInitial = () => data.columns[1] === "Initial"
    const hasTotal = () => data.columns[data.columns.length - 1] === "Total"
    // Below an overall table sits its chart, and a row painted like its part of the chart is one
    // less thing to match up by eye.
    const rowShades = () => statementRowShades(type, data, isOverall)
    const monthly = () => {
        if (!yearly.loaded) return [];
        const years = everyYear.map((each, index) => ({year: each, data: yearly.data[index]}));
        if (type === "profit") return monthlyProfitCharts(data, years);
        if (type === "balance") return monthlyBalanceCharts(data, years);
        const cashFlow = monthlyCashFlowChart(data, years);
        return cashFlow === null ? [] : [cashFlow];
    }
    const charts = () => statementCharts(type, data, isOverall)
    // A view showing two charts names them so they can be told apart - the two sides of the balance
    // sheet, say. The cash flow's one is plain enough without.
    const titled = () => type !== "cashflow" && charts().length > 1
    const tableLayout = () => measured === null ? null : {
        width: measured.width,
        columns: measured.columns.map((column, index) => ({...column, name: data.columns[index]})),
    }

    return (
        <DataView loaded={loaded} error={error}>
            {() => (
                <>
                <TableContainer component={Paper} ref={containerRef}>
                    <Table ref={tableRef} sx={{ minWidth: 650 }} size="small" aria-label="a dense table">
                        <TableHead>
                            <TableRow key={-1}>
                                {data.columns.map((column, index) => (
                                    <TableCell key={index} style={headerStyle(index)} {...yearHeaderProps(index)}>
                                        <span style={yearHeaderStyle}>
                                            {opensYear(index) && index === redirectYearIndex && <CornerMark icon={LaunchIcon} corner="top-right"/>}
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
                        <StatementChart key={chart.key} chart={chart} layout={tableLayout()} titled={titled()}/>
                    ))}
                </TableContainer>

                {/* The page is half empty below the table, which is where the shape of the
                    figures is easiest to read. An overall chart can also be switched to every
                    year's months, once those have come. */}
                {charts().filter((chart) => !chart.alignToTable).map((chart) => (
                    <VariantChart
                        key={chart.key} chart={chart} titled={titled()}
                        monthly={monthly().find((each) => each.key === chart.key + "-monthly") ?? null}
                    />
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
