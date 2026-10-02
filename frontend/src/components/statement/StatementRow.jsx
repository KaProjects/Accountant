import React from "react";
import PropTypes from "prop-types";
import {TableCell, TableRow} from "@mui/material";
import ReceiptLongIcon from "@mui/icons-material/ReceiptLong";
import {highlightedCell, pinnedFirstColumn, shadedStatementRowStyle, statementRowStyle} from "../../theme/tableStyles";
import CornerMark from "../common/CornerMark";
import {formatAmount} from "../../services/amount";

/**
 * The corner mark is positioned against this, not against the cell.
 *
 * The lines between cells are box shadows, which bleed into the neighbouring cell. A positioned
 * cell paints above its siblings, so its own background covered the shadows they were casting onto
 * it and the table lost its borders. An inner block leaves the cell itself unpositioned, so the
 * painting order is untouched.
 */
const offerHostStyle = {position: "relative", display: "block"};

/**
 * One statement line, with the child and grandchild levels it expands into.
 *
 * Only the balance sheet goes three levels deep; the other statements stop at
 * children.
 */
const StatementRow = ({row, id, columns, expansion, transactionsDialog, shade = null}) => {
    const {type, isOverall, hasInitial, hasTotal} = columns;
    const {showChildren, onToggleChildren, showGrandChild, onToggleGrandChild} = expansion;

    /** The row's own cells, in a shade of their own when it is given one. */
    const rowStyle = (hasLeftBorder, hasRightBorder) =>
        shadedStatementRowStyle(statementRowStyle(row.type, hasLeftBorder, hasRightBorder), shade);

    /** The rows it expands into, in the fainter shade that goes with the row's own. */
    const accountStyle = (node, hasLeftBorder, hasRightBorder) => shadedStatementRowStyle(
        statementRowStyle(node.type, hasLeftBorder, hasRightBorder), shade === null ? null : shade.accounts);

    /** An overall statement reports years where a yearly one reports months. */
    const valuesOf = (node) => isOverall ? node.yearlyValues : node.monthlyValues;

    /**
     * Whether a month cell of a node opens the transactions behind it. Only an account's cells do -
     * a node with children of its own is the sum of them, not a set of transactions - and only on
     * the yearly statements: a year's worth already fills the dialog, and every year at once would
     * be no use to anybody.
     */
    const opensTransactions = (node) => !isOverall && node.children.length === 0;

    /** Whether the pointer is on the month cell of a node, which is then shaded and marked. */
    const pointedAt = (node, index) => opensTransactions(node) && transactionsDialog.isTargeting(node.schemaId, index + 1);

    /**
     * What a month cell of an account does: pointing at it makes it the one the dialog would open
     * for, and a click anywhere on it - or Enter, from the keyboard - opens the dialog.
     */
    const monthCellProps = (node, index) => {
        if (!opensTransactions(node)) return {};
        const target = () => transactionsDialog.target(node.name, node.schemaId, index + 1);
        const open = (event) => {
            // the row's own click expands the row, which a click meant for the cell must not do too
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

    const monthCellStyle = (node, index, style) => pointedAt(node, index) ? highlightedCell(style) : style;

    return (
            <React.Fragment>
                <TableRow key={id} onClick={() => onToggleChildren(id)}>
                    <TableCell key={-1} style={pinnedFirstColumn(rowStyle(true, true))}>
                        {" " + row.name}
                    </TableCell>
                    {hasInitial &&
                        <TableCell key={-2} align="right" style={rowStyle(false, true)}>
                            {formatAmount(row.initial)}
                        </TableCell>
                    }
                    {!isOverall && row.monthlyValues.map((month, index) => (
                        <TableCell
                            align="right" key={index}
                            style={rowStyle(false, row.monthlyValues.length -1 === index)}
                        >
                            {formatAmount(month)}
                        </TableCell>
                    ))}
                    {isOverall && row.yearlyValues.map((year, index) => (
                        <TableCell
                            align="right" key={index}
                            style={rowStyle(false, row.yearlyValues.length -1 === index)}
                        >
                            {formatAmount(year)}
                        </TableCell>
                    ))}
                    {hasTotal &&
                        <TableCell
                            align="right" key={-3}
                            style={rowStyle(false, true)}
                        >
                            {formatAmount(row.total)}
                        </TableCell>
                    }
                </TableRow>
                {showChildren && row.children.map((child, index) => (
                    <React.Fragment key={child.schemaId + "f" + index}>
                    <TableRow key={child.schemaId + "x" + index} onClick={() => onToggleGrandChild(child.schemaId)}>
                        <TableCell component="th" scope="row" key={-1} style={pinnedFirstColumn(accountStyle(child, true, true))}>
                            {child.name}
                        </TableCell>
                        {hasInitial &&
                            <TableCell key={-2} align="right" style={accountStyle(child, false, true)}>
                                {formatAmount(child.initial)}
                            </TableCell>
                        }
                        {valuesOf(child).map((value, index) => (
                            <TableCell
                                align="right" key={index}
                                style={monthCellStyle(child, index, accountStyle(child, false, valuesOf(child).length -1 === index))}
                                {...monthCellProps(child, index)}
                            >
                                <span style={offerHostStyle}>
                                    {pointedAt(child, index) && <CornerMark icon={ReceiptLongIcon} corner="top-left"/>}
                                    {formatAmount(value)}
                                </span>
                            </TableCell>
                        ))}
                        {hasTotal &&
                            <TableCell align="right" key={-3} style={accountStyle(child, true, true)}>{formatAmount(child.total)}</TableCell>
                        }
                    </TableRow>
                    {type === "balance" && (showGrandChild === child.schemaId) && child.children.map((grandchild, index) => (
                        <TableRow key={grandchild.schemaId + "x" + index}>
                            <TableCell component="th" scope="row" key={-1} style={pinnedFirstColumn(accountStyle(grandchild, true, true))}>
                                {grandchild.name}
                            </TableCell>
                            {hasInitial &&
                                <TableCell key={-2} align="right" style={accountStyle(grandchild, false, true)}>
                                    {formatAmount(grandchild.initial)}
                                </TableCell>
                            }
                            {valuesOf(grandchild).map((value, index) => (
                                <TableCell
                                    align="right" key={index}
                                    style={monthCellStyle(grandchild, index, accountStyle(grandchild, false, valuesOf(grandchild).length -1 === index))}
                                    {...monthCellProps(grandchild, index)}
                                >
                                    <span style={offerHostStyle}>
                                        {pointedAt(grandchild, index) && <CornerMark icon={ReceiptLongIcon} corner="top-left"/>}
                                        {formatAmount(value)}
                                    </span>
                                </TableCell>
                            ))}
                            {hasTotal &&
                                <TableCell align="right" key={-3} style={accountStyle(grandchild, true, true)}>{formatAmount(grandchild.total)}</TableCell>
                            }
                        </TableRow>
                    ))}
                    </React.Fragment>
                ))}
            </React.Fragment>
    );
};

StatementRow.propTypes = {
    row: PropTypes.object.isRequired,
    id: PropTypes.number.isRequired,
    /** Which columns this statement has, and whether it reports months or years. */
    columns: PropTypes.shape({
        type: PropTypes.string.isRequired,
        isOverall: PropTypes.bool,
        hasInitial: PropTypes.bool,
        hasTotal: PropTypes.bool,
    }).isRequired,
    /** Which levels are open, and how to open them. */
    expansion: PropTypes.shape({
        showChildren: PropTypes.bool,
        onToggleChildren: PropTypes.func.isRequired,
        showGrandChild: PropTypes.string,
        onToggleGrandChild: PropTypes.func.isRequired,
    }).isRequired,
    transactionsDialog: PropTypes.object.isRequired,
    /**
     * A shade to paint the row in instead of the colour of its type: {fill, ink, edge}, with the
     * fainter shade for the rows it expands into under `accounts`.
     */
    shade: PropTypes.shape({
        fill: PropTypes.string.isRequired,
        ink: PropTypes.string.isRequired,
        edge: PropTypes.string.isRequired,
        accounts: PropTypes.object,
    }),
};

export default StatementRow;
