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

const titleTypography = {
    style: {fontWeight: "bold", fontFamily: "Copperplate", marginLeft: "10px"},
};

/** One named view: its transactions, total and the two chart placements. */
const ViewPanel = ({view, columns, isOpen, onToggle}) => (
    <div>
        <ListItem button onClick={onToggle} style={viewTitleStyle(isOpen)}>
            <ListItemText primary={formatViewTitle(view.name)} primaryTypographyProps={titleTypography}/>
            {isOpen ? <ExpandLess/> : <ExpandMore/>}
        </ListItem>
        <Collapse in={isOpen} timeout="auto" unmountOnExit>
            <div className={"parent"}>
                <div>
                    <TableContainer component={Paper} style={{height: 350}}>
                        <Table sx={{minWidth: 650}} size="small" aria-label="a dense table" stickyHeader>
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
                                        <TableCell style={{textAlign: "right"}}>{transaction.amount}</TableCell>
                                        <TableCell>{transaction.debit}</TableCell>
                                        <TableCell>{transaction.credit}</TableCell>
                                        <TableCell>{transaction.description}</TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                    <Typography style={{fontWeight: "bold", margin: 15}}>
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
