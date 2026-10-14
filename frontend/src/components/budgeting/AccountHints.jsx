import PropTypes from "prop-types";
import {List, ListItemButton, ListItemText, Paper, Typography} from "@mui/material";

const panelStyle = {padding: "8px 0", maxHeight: "calc(100vh - 96px)", overflowY: "auto"};
const titleStyle = {padding: "0 16px 4px"};
const idStyle = {display: "inline-block", minWidth: 64, fontWeight: 600};
const emptyStyle = {padding: "4px 16px", color: "rgba(0, 0, 0, 0.6)", fontStyle: "italic"};

/**
 * What the debit or credit being typed can go on with - the classes, groups, schema accounts or
 * accounts its beginning leads to - each with its name, to be put in with a click.
 */
const AccountHints = ({hints, onPick}) => (
    <Paper variant="outlined" style={panelStyle} role="region" aria-label="Account hints">
        <Typography variant="subtitle2" style={titleStyle}>{hints.title}</Typography>
        {hints.options.length === 0
            ? <Typography variant="body2" style={emptyStyle}>Nothing matches</Typography>
            : <List dense disablePadding>
                {hints.options.map((option) => (
                    // kept from taking the focus, so the value it is picked for stays the one typed into
                    <ListItemButton key={option.id} onMouseDown={(event) => event.preventDefault()}
                                    onClick={() => onPick(option.id)}>
                        <ListItemText primary={<><span style={idStyle}>{option.id}</span>{option.name}</>}/>
                    </ListItemButton>
                ))}
            </List>
        }
    </Paper>
);

AccountHints.propTypes = {
    hints: PropTypes.shape({
        title: PropTypes.string.isRequired,
        options: PropTypes.arrayOf(PropTypes.shape({id: PropTypes.string, name: PropTypes.string})).isRequired,
    }).isRequired,
    onPick: PropTypes.func.isRequired,
};

export default AccountHints;
