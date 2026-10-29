import React from "react";
import PropTypes from "prop-types";
import {IconButton, TableCell, TableRow} from "@mui/material";
import KeyboardArrowDownIcon from "@mui/icons-material/KeyboardArrowDown";
import KeyboardArrowUpIcon from "@mui/icons-material/KeyboardArrowUp";
import BarChartIcon from "@mui/icons-material/BarChart";
import ReceiptLongIcon from "@mui/icons-material/ReceiptLong";
import {budgetDeltaStyle, budgetPlannedRowStyle, budgetRowStyle, highlightedCell} from "../../theme/tableStyles";
import {formatAmount} from "../../services/amount";
import CornerMark from "../common/CornerMark";

const iconStyle = {width: 18};
/** Laid out over the cell, so appearing under the pointer does not widen the column. */
const cellButtonStyle = {
    position: "absolute",
    left: 0,
    top: "50%",
    transform: "translateY(-50%)",
    height: "2px",
    width: "25px",
};
/** Positioned inside the cell, so the cell keeps painting behind its neighbours' shadows. */
const offerHostStyle = {position: "relative", display: "block"};
const expandButtonStyle = {height: "2px", width: "10px"};
// on the right of the name, so it does not cover it or the sub rows' toggle on its left
const chartButtonStyle = {...cellButtonStyle, left: "auto", right: 0};
// under the figure, on a line of its own
const deltaHostStyle = {display: "block"};

/** The difference from the planning, signed. */
const signed = (delta) => (delta > 0 ? "+" : "") + formatAmount(delta);

/** Under an actual figure, how far it is from what was planned. */
const Delta = ({type, value}) => (
    <span style={deltaHostStyle}>
        <span style={budgetDeltaStyle(type, value)}>({signed(value)})</span>
    </span>
);

Delta.propTypes = {
    type: PropTypes.string,
    value: PropTypes.number.isRequired,
};

/**
 * One budget line, with the sub rows it can expand into. Each actual figure of the line has, under
 * it, how far it is from what was planned; a sub row is not planned, so it has none.
 */
const BudgetRow = ({row, id, expansion, fold, transactionsDialog, budgetChart}) => {
    const {showSubRows, onToggleSubRows} = expansion;

    /**
     * What a month cell of a line with transactions of its own does, as in the statements: pointing
     * at it shades it as the one the dialog would open for, and a click anywhere on it - or Enter,
     * from the keyboard - opens the dialog. A line made of sub rows shows all of theirs; only one
     * adding up other lines - a total, a balance - has none of its own to show.
     */
    const opensTransactions = (line) => transactionsDialog.opensFor(line.id);
    const monthCellProps = (line, index) => {
        if (!opensTransactions(line)) return {};
        const target = () => transactionsDialog.target(line.name, line.id, index + 1);
        const open = (event) => {
            // the row's own click opens its sub rows or folds its part, which this one must not do too
            event.stopPropagation();
            target();
            transactionsDialog.setOpen(true);
        };
        return {
            tabIndex: 0,
            onMouseEnter: target,
            onMouseLeave: () => transactionsDialog.clearTarget(),
            onClick: open,
            onKeyDown: (event) => {
                if (event.key === "Enter") open(event);
            },
        };
    };
    const pointedAt = (line, index) => opensTransactions(line) && transactionsDialog.isTargeting(line.id, index + 1);
    const monthCellStyle = (line, index, style) => (pointedAt(line, index) ? highlightedCell(style ?? {}) : style);
    const toggle = () => {
        if (row.subRows.length !== 0) onToggleSubRows(id);
        else if (fold) fold.onToggle();
    };

    return (
    <React.Fragment>
        {/* the chart is offered wherever on the row the pointer is */}
        <TableRow key={id} onClick={toggle}
                  onMouseEnter={() => budgetChart.preview(row)}
                  onMouseLeave={() => budgetChart.preview(null)}
        >
            <TableCell style={budgetRowStyle(row.type, false, true)} key={-1}>
                <span style={offerHostStyle}>
                    {/* before the name, in line, rather than over its first letters */}
                    {(row.subRows.length !== 0) && <IconButton
                        aria-label="expand row"
                        style={expandButtonStyle}
                        onClick={(e) => {onToggleSubRows(id);e.stopPropagation();}}
                    >
                        {showSubRows ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
                    </IconButton>}
                    {/* a total folds up the rows it adds up, which are above it */}
                    {fold && <IconButton
                        aria-label={fold.folded ? "unfold rows" : "fold rows"}
                        style={expandButtonStyle}
                        onClick={(e) => {fold.onToggle();e.stopPropagation();}}
                    >
                        {fold.folded ? <KeyboardArrowDownIcon /> : <KeyboardArrowUpIcon />}
                    </IconButton>}
                    {(row.subRows.length !== 0 || fold ? " " : "") + row.name}
                    {budgetChart.isPreviewing(row.name) &&
                        <IconButton
                            aria-label="budget chart"
                            style={chartButtonStyle}
                            onClick={(e) => {budgetChart.setOpen(true);e.stopPropagation();}}
                        >
                            <BarChartIcon sx={iconStyle}/>
                        </IconButton>
                    }
                </span>
            </TableCell>
            {row.actual.map((month, index) => (
                (index < row.lastFilledMonth)
                    ? <TableCell
                        style={monthCellStyle(row, index, budgetRowStyle(row.type, false, false))} align="right" key={index}
                        {...monthCellProps(row, index)}
                      >
                        <span style={offerHostStyle}>
                            {pointedAt(row, index) && <CornerMark icon={ReceiptLongIcon} corner="top-left"/>}
                            {formatAmount(month)}
                        </span>
                        <Delta type={row.type} value={month - row.planned[index]}/>
                      </TableCell>
                    : <TableCell style={budgetPlannedRowStyle(row.type)} align="right" key={index}>{formatAmount(row.planned[index])}</TableCell>
            ))}
            <TableCell style={budgetRowStyle(row.type, true, false)} align="right" key={12}>
                {formatAmount(row.actualSum)}
                <Delta type={row.type} value={row.actualSum - row.plannedSumToFilledMonth}/>
            </TableCell>
            <TableCell style={budgetRowStyle(row.type, true, false)} align="right" key={13}>
                {formatAmount(row.actualAvg)}
                <Delta type={row.type} value={row.actualAvg - row.plannedAvgToFilledMonth}/>
            </TableCell>
        </TableRow>

        {showSubRows && row.subRows.map((subrow, index) => (
            <TableRow key={id + "s" + index}>
                <TableCell component="th" scope="row" key={-1}>
                    {subrow.name}
                </TableCell>
                {subrow.actual.map((month, monthIndex) => (
                    (monthIndex < row.lastFilledMonth)
                        ? <TableCell
                            align="right" key={monthIndex}
                            style={monthCellStyle(subrow, monthIndex, undefined)}
                            {...monthCellProps(subrow, monthIndex)}
                        >
                            <span style={offerHostStyle}>
                                {pointedAt(subrow, monthIndex) && <CornerMark icon={ReceiptLongIcon} corner="top-left"/>}
                                {formatAmount(month)}
                            </span>
                        </TableCell>
                        : <TableCell align="right" key={monthIndex}>{formatAmount(subrow.planned[monthIndex])}</TableCell>
                ))}
                <TableCell align="right" key={12}>{formatAmount(subrow.actualSum)}</TableCell>
                <TableCell align="right" key={13}>{formatAmount(subrow.actualAvg)}</TableCell>
            </TableRow>
        ))}
    </React.Fragment>
    );
};

BudgetRow.propTypes = {
    row: PropTypes.object.isRequired,
    id: PropTypes.number.isRequired,
    /** Whether the row's sub rows are open, and how to open them. */
    expansion: PropTypes.shape({
        showSubRows: PropTypes.bool,
        onToggleSubRows: PropTypes.func.isRequired,
    }).isRequired,
    /** For a total that can fold up the rows it adds up: whether they are folded, and how to toggle them. */
    fold: PropTypes.shape({
        folded: PropTypes.bool.isRequired,
        onToggle: PropTypes.func.isRequired,
    }),
    transactionsDialog: PropTypes.object.isRequired,
    budgetChart: PropTypes.object.isRequired,
};

export default BudgetRow;
