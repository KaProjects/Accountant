import PropTypes from "prop-types";
import {
    Collapse, ListItem, ListItemText, Table, TableBody, TableCell,
    TableContainer, TableHead, TableRow, Typography,
} from "@mui/material";
import {ExpandLess, ExpandMore} from "@mui/icons-material";
import Paper from "@mui/material/Paper";
import VacationChart from "../VacationChart";
import {viewHeaderStyle, viewTitleStyle} from "../../theme/tableStyles";
import {formatViewTitle} from "../../services/viewTitle";
import {formatAmount} from "../../services/amount";

// the table takes all the room the chart beside it leaves, the same in every view
const tableColumnStyle = {flex: "1 1 0", minWidth: 0};
const tableBoxStyle = {height: 350};
const tableStyle = {minWidth: 650};
const amountStyle = {textAlign: "right"};
const totalStyle = {fontWeight: "bold", margin: 15};

const titleTypography = {style: {fontWeight: 600, fontSize: "16px"}};
// what the view spent, on the right of its title, so it reads while the view is closed - which the
// backend sends as text
const titleTotalStyle = {fontWeight: 600, fontSize: "15px", marginRight: "12px", whiteSpace: "nowrap"};

/** One named view: its transactions, total and the two chart placements. */
const ViewPanel = ({view, columns, isOpen, onToggle}) => (
    <div>
        <ListItem button onClick={onToggle} style={viewTitleStyle(isOpen)}>
            <ListItemText primary={formatViewTitle(view.name)} primaryTypographyProps={titleTypography}/>
            <Typography component="span" style={titleTotalStyle}>{formatAmount(Number(view.expenses))}</Typography>
            {isOpen ? <ExpandLess/> : <ExpandMore/>}
        </ListItem>
        <Collapse in={isOpen} timeout="auto" unmountOnExit>
            <div className={"parent"}>
                <div style={tableColumnStyle}>
                    <TableContainer component={Paper} style={tableBoxStyle}>
                        <Table sx={tableStyle} size="small" aria-label="a dense table" stickyHeader>
                            <TableHead>
                                <TableRow>
                                    {columns.map((column, index) => (
                                        <TableCell key={index} style={viewHeaderStyle(index)}>{column}</TableCell>
                                    ))}
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {view.transactions.map((transaction, index) => (
                                    <TableRow key={index}>
                                        <TableCell>{transaction.date}</TableCell>
                                        <TableCell style={amountStyle}>{transaction.amount}</TableCell>
                                        <TableCell>{transaction.debit}</TableCell>
                                        <TableCell>{transaction.credit}</TableCell>
                                        <TableCell>{transaction.description}</TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                    <Typography style={totalStyle}>
                        Total Expenses: {view.expenses}
                    </Typography>
                </div>
                <div className={"chartBottom"}>
                    <VacationChart data={view.chartData} isBottom={true}/>
                </div>
                <div className={"chartRight"}>
                    <VacationChart data={view.chartData} isBottom={false}/>
                </div>
            </div>
        </Collapse>
    </div>
);

ViewPanel.propTypes = {
    view: PropTypes.object.isRequired,
    columns: PropTypes.array.isRequired,
    isOpen: PropTypes.bool,
    onToggle: PropTypes.func.isRequired,
};

export default ViewPanel;
