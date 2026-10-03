import PropTypes from "prop-types";
import {Button, ButtonBase, Card, CardActionArea, CardActions, CardContent, Typography} from "@mui/material";

const CARD_BACKGROUND = "#ffc107";

const plainCardStyle = {backgroundColor: CARD_BACKGROUND};
const actionCardStyle = {
    backgroundColor: CARD_BACKGROUND,
    display: "flex",
    flexDirection: "column",
    justifyContent: "space-between",
};
const actionsStyle = {justifyContent: "center"};
// the part of a tile above its actions, filling it down to them, so it opens wherever it is clicked
const openingAreaStyle = {flexGrow: 1, display: "flex", flexDirection: "column", alignItems: "stretch", justifyContent: "flex-start"};

/**
 * One menu tile.
 *
 * A card either opens a single destination, in which case the whole tile is the
 * button, or offers several named destinations as actions along its foot. A card
 * with actions can also be opened itself, at the destination it opens by default -
 * a statement at its single year - in which case everything above the actions is
 * the button for it. The actions sit outside it, as a button cannot hold buttons.
 */
const HomeCard = ({title, icon, caption, height, onOpen, actions}) => {
    const body = (
        <CardContent>
            <Typography variant="h5" component="div" align={"center"}>
                {title}
            </Typography>
            <Typography align={"center"}>
                {icon}
            </Typography>
            <Typography variant="caption">
                {caption}
            </Typography>
        </CardContent>
    );

    if (actions) {
        return (
            <Card sx={{width: 300, height}} raised
                  style={actionCardStyle}>
                {onOpen ? <CardActionArea onClick={onOpen} style={openingAreaStyle}>{body}</CardActionArea> : body}
                <CardActions style={actionsStyle}>
                    {actions.map((action) => (
                        <Button key={action.label} size="small" onClick={action.onSelect}>
                            {action.label}
                        </Button>
                    ))}
                </CardActions>
            </Card>
        );
    }

    return (
        <ButtonBase onClick={onOpen}>
            <Card sx={{width: 300, height}} raised style={plainCardStyle}>
                {body}
            </Card>
        </ButtonBase>
    );
};

HomeCard.propTypes = {
    title: PropTypes.string.isRequired,
    icon: PropTypes.node.isRequired,
    caption: PropTypes.string.isRequired,
    height: PropTypes.number.isRequired,
    onOpen: PropTypes.func,
    actions: PropTypes.arrayOf(PropTypes.shape({
        label: PropTypes.string.isRequired,
        onSelect: PropTypes.func.isRequired,
    })),
};

export default HomeCard;
