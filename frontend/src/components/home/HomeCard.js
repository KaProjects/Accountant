import PropTypes from "prop-types";
import {Button, ButtonBase, Card, CardActions, CardContent, Typography} from "@mui/material";

const CARD_BACKGROUND = "#ffc107";

/**
 * One menu tile.
 *
 * A card either opens a single destination, in which case the whole tile is the
 * button, or offers several named destinations as actions along its foot.
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
                  style={{backgroundColor: CARD_BACKGROUND, display: "flex", flexDirection: "column", justifyContent: "space-between"}}>
                {body}
                <CardActions style={{justifyContent: "center"}}>
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
            <Card sx={{width: 300, height}} raised style={{backgroundColor: CARD_BACKGROUND}}>
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
