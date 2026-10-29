import SvgIcon from "@mui/material/SvgIcon";

/**
 * A calendar holding a span of time: every year at once, for the way from a single year to the
 * overall view.
 *
 * Drawn on a grid of its own, 48 wide, and in outline. The icon wrapper fills whatever it draws by
 * default, which would have turned the outline into a solid block, so the outlined part says
 * explicitly that it is not filled.
 */
const AllYearsIcon = (props) => (
    <SvgIcon viewBox="0 0 48 48" data-testid="AllYearsIcon" {...props}>
        <g fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
            <rect x="6" y="9" width="36" height="33" rx="3"/>
            <line x1="6" y1="17" x2="42" y2="17"/>
            <line x1="15" y1="5" x2="15" y2="12"/>
            <line x1="33" y1="5" x2="33" y2="12"/>
        </g>
        <rect x="11" y="24" width="26" height="5" rx="2.5" fill="currentColor"/>
        <circle cx="13" cy="35" r="2" fill="currentColor"/>
        <circle cx="20" cy="35" r="2" fill="currentColor" opacity="0.5"/>
    </SvgIcon>
);

export default AllYearsIcon;
