import React from "react";
import PropTypes from "prop-types";
import {IconButton, TableCell, TableRow} from "@mui/material";
import KeyboardArrowDownIcon from "@mui/icons-material/KeyboardArrowDown";
import KeyboardArrowUpIcon from "@mui/icons-material/KeyboardArrowUp";
import ReceiptLongIcon from "@mui/icons-material/ReceiptLong";
import BarChartIcon from "@mui/icons-material/BarChart";
import {budgetPlannedRowStyle, budgetRowStyle} from "../../theme/tableStyles";

/** Opens the transactions dialog for the cell currently pointed at. */
const iconStyle = {width: 18};
const cellButtonStyle = {height: "2px", width: "25px"};
const expandButtonStyle = {height: "2px", width: "10px"};

const TransactionsButton = ({color, onOpen}) => (
    <IconButton style={{...cellButtonStyle, color}} onClick={onOpen}>
        <ReceiptLongIcon sx={iconStyle}/>
    </IconButton>
);

TransactionsButton.propTypes = {
    color: PropTypes.string,
    onOpen: PropTypes.func.isRequired,
};

/**
 * One budget line, plus the sub rows and the planned/difference breakdown it can
 * expand into.
 */
const BudgetRow = ({row, id, expansion, transactionsDialog, budgetChart}) => {
    const {showSubRows, showDeltas, onToggleSubRows, onToggleDeltas} = expansion;

    return (
    <React.Fragment>
        <TableRow key={id} onClick={() => {if (row.subRows.length !== 0) onToggleSubRows(id)}}>
            <TableCell style={budgetRowStyle(row.type, false, true)} key={-1}>
                <IconButton
                    aria-label="expand row"
                    style={expandButtonStyle}
                    onClick={(e) => {onToggleDeltas(id);e.stopPropagation();}}
                >
                    {showDeltas ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
                </IconButton>
                {" " + row.name}
                {(row.subRows.length !== 0) && <IconButton
                    aria-label="expand row"
                    style={cellButtonStyle}
                    onClick={(e) => {onToggleSubRows(id);e.stopPropagation();}}
                >
                    {showSubRows ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
                </IconButton>}
            </TableCell>
            {row.actual.map((month, index) => (
                (index < row.lastFilledMonth)
                    ? <TableCell
                        style={budgetRowStyle(row.type, false, false)} align="right" key={index}
                        onClick={() => {if (row.subRows.length === 0) transactionsDialog.target(row.name, row.id, index + 1)}}
                        onMouseLeave={() => transactionsDialog.clearTarget()}
                      >
                        {month}
                        {transactionsDialog.isTargeting(row.id, index + 1) &&
                            <TransactionsButton
                                color={budgetRowStyle(row.type).color}
                                onOpen={() => transactionsDialog.setOpen(true)}
                            />
                        }
                      </TableCell>
                    : <TableCell style={budgetPlannedRowStyle(row.type)} align="right" key={index}>{row.planned[index]}</TableCell>
            ))}
            <TableCell style={budgetRowStyle(row.type, true, false)} align="right" key={12}>{row.actualSum}</TableCell>
            <TableCell style={budgetRowStyle(row.type, true, true)} align="right" key={13}>{row.actualAvg}</TableCell>
            <TableCell style={budgetPlannedRowStyle(row.type)} align="right" key={14}>{row.plannedAvgToFilledMonth}</TableCell>
            <TableCell style={budgetPlannedRowStyle(row.type, false, row.deltaAvg)} align="right" key={15}>{row.deltaAvg}</TableCell>
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
                            onClick={() => transactionsDialog.target(subrow.name, subrow.id, monthIndex + 1)}
                            onMouseLeave={() => transactionsDialog.clearTarget()}
                        >
                            {month}
                            {transactionsDialog.isTargeting(subrow.id, monthIndex + 1) &&
                                <TransactionsButton onOpen={() => transactionsDialog.setOpen(true)}/>
                            }
                        </TableCell>
                        : <TableCell align="right" key={monthIndex}>{subrow.planned[monthIndex]}</TableCell>
                ))}
                <TableCell align="right" key={12}>{subrow.actualSum}</TableCell>
                <TableCell align="right" key={13}>{subrow.actualAvg}</TableCell>
                <TableCell align="right" key={14}>{subrow.plannedAvgToFilledMonth}</TableCell>
                <TableCell align="right" key={15}>{subrow.deltaAvg}</TableCell>
            </TableRow>
        ))}

        {showDeltas &&
            <>
                <TableRow key={id + "dp"}>
                    <TableCell style={budgetPlannedRowStyle(row.type)} component="th" scope="row" key={-1}>Planned</TableCell>
                    {row.planned.map((month, index) => (
                        (index < row.lastFilledMonth)
                            ? <TableCell style={budgetPlannedRowStyle(row.type)} align="right" key={index}>{month}</TableCell>
                            : <TableCell style={budgetPlannedRowStyle(row.type)} align="right" key={index}>-</TableCell>
                    ))}
                    <TableCell style={budgetPlannedRowStyle(row.type)} align="right" key={12}>{row.plannedSumToFilledMonth}</TableCell>
                    <TableCell style={budgetPlannedRowStyle(row.type)} align="right" key={13}>{row.plannedAvgToFilledMonth}</TableCell>
                    <TableCell style={budgetPlannedRowStyle(row.type)} align="right" key={14}>-</TableCell>
                    <TableCell style={budgetPlannedRowStyle(row.type)} align="right" key={15}>-</TableCell>
                </TableRow>
                <TableRow key={id + "dd"}>
                    <TableCell style={budgetPlannedRowStyle(row.type, true)} component="th" scope="row" key={-1}
                               onMouseEnter={() => budgetChart.preview(row)}
                               onMouseLeave={() => budgetChart.preview(null)}
                    >
                        Difference
                        {budgetChart.isPreviewing(row.name) &&
                            <IconButton
                                style={cellButtonStyle}
                                onClick={() => budgetChart.setOpen(true)}
                            >
                                <BarChartIcon sx={iconStyle}/>
                            </IconButton>
                        }
                    </TableCell>
                    {row.planned.map((month, index) => (
                        (index < row.lastFilledMonth)
                            ? <TableCell style={budgetPlannedRowStyle(row.type, true, row.actual[index] - month)} align="right" key={index}>{row.actual[index] - month}</TableCell>
                            : <TableCell style={budgetPlannedRowStyle(row.type, true)} align="right" key={index}>-</TableCell>
                    ))}
                    <TableCell style={budgetPlannedRowStyle(row.type, true, row.actualSum - row.plannedSumToFilledMonth)} align="right" key={12}>{row.actualSum - row.plannedSumToFilledMonth}</TableCell>
                    <TableCell style={budgetPlannedRowStyle(row.type, true, row.actualAvg - row.plannedAvgToFilledMonth)} align="right" key={13}>{row.actualAvg - row.plannedAvgToFilledMonth}</TableCell>
                    <TableCell style={budgetPlannedRowStyle(row.type, true)} align="right" key={14}>-</TableCell>
                    <TableCell style={budgetPlannedRowStyle(row.type, true)} align="right" key={15}>-</TableCell>
                </TableRow>
            </>
        }
    </React.Fragment>
    );
};

BudgetRow.propTypes = {
    row: PropTypes.object.isRequired,
    id: PropTypes.number.isRequired,
    /** Which of the row's breakdowns are open, and how to open them. */
    expansion: PropTypes.shape({
        showSubRows: PropTypes.bool,
        showDeltas: PropTypes.bool,
        onToggleSubRows: PropTypes.func.isRequired,
        onToggleDeltas: PropTypes.func.isRequired,
    }).isRequired,
    transactionsDialog: PropTypes.object.isRequired,
    budgetChart: PropTypes.object.isRequired,
};

export default BudgetRow;
