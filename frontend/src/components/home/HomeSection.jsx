import PropTypes from "prop-types";
import {Stack, Typography} from "@mui/material";
import HomeCard from "./HomeCard";

/** A titled row of menu tiles. */
const HomeSection = ({title, variant, height, cards}) => (
    <>
        <Typography variant={variant} component="div" align={"center"} marginTop={2} marginBottom={1}>
            {title}
        </Typography>
        <Stack direction="row" justifyContent="center" alignItems="flex-start" spacing={0.5}>
            {cards.map((card) => (
                <HomeCard key={card.title} height={height} {...card}/>
            ))}
        </Stack>
    </>
);

HomeSection.propTypes = {
    title: PropTypes.string.isRequired,
    variant: PropTypes.string.isRequired,
    height: PropTypes.number.isRequired,
    cards: PropTypes.array.isRequired,
};

export default HomeSection;
