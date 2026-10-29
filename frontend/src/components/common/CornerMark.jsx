import PropTypes from "prop-types";

// Over the cell's padding, just in from one of its top corners: positioned against the block that
// holds the figure, which sits inside the padding.
const markStyle = {
    position: "absolute", top: "-4px",
    fontSize: "14px", color: "inherit", opacity: 0.8, pointerEvents: "none",
};
const corners = {
    "top-left": {left: "-13px"},
    "top-right": {right: "-14px"},
};

/**
 * The small sign in the corner of a cell that does something when clicked, saying what a click
 * will do. It is not a button - the whole cell is - and it is laid over the cell rather than put in
 * its flow, so that its coming and going moves neither the figure nor the column.
 *
 * Which corner is the caller's: away from where the cell's text sits. A figure is set to the right,
 * so its mark goes top left; a year heading is centred, and its mark goes top right.
 */
const CornerMark = ({icon: Icon, corner}) =>
    <Icon aria-hidden="true" data-testid="corner-mark" data-corner={corner} sx={{...markStyle, ...corners[corner]}}/>;

CornerMark.propTypes = {
    /** An icon component, such as one of @mui/icons-material. */
    icon: PropTypes.elementType.isRequired,
    corner: PropTypes.oneOf(["top-left", "top-right"]).isRequired,
};

export default CornerMark;
