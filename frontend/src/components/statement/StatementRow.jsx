import React from "react";
import PropTypes from "prop-types";
import {IconButton, TableCell, TableRow} from "@mui/material";
import ReceiptLongIcon from "@mui/icons-material/ReceiptLong";
import {statementRowStyle} from "../../theme/tableStyles";

const iconStyle = {width: 18};

/**
 * Laid out over the cell rather than inside it: appearing in the flow widened the column under the
 * pointer, which shifted every column after it.
 */
const cellButtonStyle = {
    position: "absolute",
    left: 0,
    top: "50%",
    transform: "translateY(-50%)",
    height: "2px",
    width: "25px",
};
/**
 * The offer is positioned against this, not against the cell.
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
const StatementRow = ({row, id, columns, expansion, transactionsDialog}) => {
    const {type, isOverall, hasInitial, hasTotal} = columns;
    const {showChildren, onToggleChildren, showGrandChild, onToggleGrandChild} = expansion;

    return (
            <React.Fragment>
                <TableRow key={id} onClick={() => onToggleChildren(id)}>
                    <TableCell key={-1} style={statementRowStyle(row.type, true, true)}>
                        {" " + row.name}
                    </TableCell>
                    {hasInitial &&
                        <TableCell key={-2} align="right" style={statementRowStyle(row.type, false, true)}>
                            {row.initial}
                        </TableCell>
                    }
                    {!isOverall && row.monthlyValues.map((month, index) => (
                        <TableCell
                            align="right" key={index}
                            style={statementRowStyle(row.type, false, row.monthlyValues.length -1 === index)}
                        >
                            {month}
                        </TableCell>
                    ))}
                    {isOverall && row.yearlyValues.map((year, index) => (
                        <TableCell
                            align="right" key={index}
                            style={statementRowStyle(row.type, false, row.yearlyValues.length -1 === index)}
                        >
                            {year}
                        </TableCell>
                    ))}
                    {hasTotal &&
                        <TableCell
                            align="right" key={-3}
                            style={statementRowStyle(row.type, false, true)}
                        >
                            {row.total}
                        </TableCell>
                    }
                </TableRow>
                {showChildren && row.children.map((child, index) => (
                    <React.Fragment key={child.schemaId + "f" + index}>
                    <TableRow key={child.schemaId + "x" + index} onClick={() => onToggleGrandChild(child.schemaId)}>
                        <TableCell component="th" scope="row" key={-1} style={statementRowStyle(child.type, true, true)}>
                            {child.name}
                        </TableCell>
                        {hasInitial &&
                            <TableCell key={-2} align="right" style={statementRowStyle(child.type, false, true)}>
                                {child.initial}
                            </TableCell>
                        }
                        {child.monthlyValues.map((month, index) => (
                            <TableCell
                                align="right" key={index}
                                style={statementRowStyle(child.type, false, child.monthlyValues.length -1 === index)}
                                onMouseEnter={() => transactionsDialog.target(child.name, child.schemaId, index + 1)}
                                onMouseLeave={() => transactionsDialog.clearTarget()}
                            >
                                <span style={offerHostStyle}>
                                    {child.children.length === 0 && transactionsDialog.isTargeting(child.schemaId, index + 1) &&
                                        <IconButton
                                            style={{...cellButtonStyle, color: statementRowStyle(child.type).color}}
                                            onClick={() => transactionsDialog.setOpen(true)}
                                        >
                                            <ReceiptLongIcon sx={iconStyle}/>
                                        </IconButton>
                                    }
                                    {month}
                                </span>
                            </TableCell>
                        ))}
                        {hasTotal &&
                            <TableCell align="right" key={-3} style={statementRowStyle(child.type, true, true)}>{child.total}</TableCell>
                        }
                    </TableRow>
                    {type === "balance" && (showGrandChild === child.schemaId) && child.children.map((grandchild, index) => (
                        <TableRow key={grandchild.schemaId + "x" + index}>
                            <TableCell component="th" scope="row" key={-1} style={statementRowStyle(grandchild.type, true, true)}>
                                {grandchild.name}
                            </TableCell>
                            <TableCell key={-2} align="right" style={statementRowStyle(grandchild.type, false, true)}>
                                {grandchild.initial}
                            </TableCell>
                            {grandchild.monthlyValues.map((month, index) => (
                                <TableCell
                                    align="right" key={index}
                                    style={statementRowStyle(grandchild.type, false, grandchild.monthlyValues.length -1 === index)}
                                    onMouseEnter={() => transactionsDialog.target(grandchild.name, grandchild.schemaId, index + 1)}
                                    onMouseLeave={() => transactionsDialog.clearTarget()}
                                >
                                    <span style={offerHostStyle}>
                                        {grandchild.children.length === 0 && transactionsDialog.isTargeting(grandchild.schemaId, index + 1) &&
                                            <IconButton
                                                style={{...cellButtonStyle, color: statementRowStyle(grandchild.type).color}}
                                                onClick={() => transactionsDialog.setOpen(true)}
                                            >
                                                <ReceiptLongIcon sx={iconStyle}/>
                                            </IconButton>
                                        }
                                        {month}
                                    </span>
                                </TableCell>
                            ))}
                            <TableCell align="right" key={-3} style={statementRowStyle(grandchild.type, true, true)}>{grandchild.total}</TableCell>
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
};

export default StatementRow;
