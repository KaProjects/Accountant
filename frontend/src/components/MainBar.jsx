import React from "react";
import {AppBar, Box, Button, IconButton, MenuItem, Select, Toolbar, Typography} from "@mui/material";
import MenuIcon from '@mui/icons-material/Menu';
import ArrowLeftIcon from '@mui/icons-material/ArrowLeft';
import ArrowRightIcon from '@mui/icons-material/ArrowRight';
import {getChartConfigStyle} from "../theme/palette";
import {useAppState} from "../state/appState";
import {useGoHome} from "../services/navigation";

const spacerStyle = {flexGrow: 1};
const titleStyle = {display: {xs: 'none', sm: 'block'}};
const centreStyle = {display: {xs: 'none', md: 'flex'}};
const drawerButtonStyle = {mr: 2};

/**
 * An arrow that cannot be used is hidden but keeps its place, so that reaching the first or the
 * last year does not shift the year and the other arrow sideways. Hidden this way it is also out
 * of the tab order and cannot be clicked, so it is unavailable in every sense but the layout.
 */
const unavailableStepStyle = {visibility: "hidden"};

/** The selector sits on the coloured bar, so it has to be drawn in white rather than the default. */
const selectorStyle = {
    color: "white",
    '.MuiSvgIcon-root ': {fill: "white"},
    ':before': {borderBottomColor: 'white'},
    ':after': {borderBottomColor: 'white'},
};

const MainBar = () => {
    const {year, isYearly, selectValues, selectedValue, setYear, setSelectedValue} = useAppState();
    const goHome = useGoHome();

    return (
        <Box sx={spacerStyle}>
        <AppBar position="static">
            <Toolbar variant="dense">
                <IconButton size="large" edge="start" color="inherit" aria-label="open drawer" sx={drawerButtonStyle}
                            onClick={goHome}>
                    <MenuIcon />
                </IconButton>
                <Typography variant="h6" noWrap component="div" sx={titleStyle}>
                    Accountant
                </Typography>
                <Box sx={spacerStyle} />
                <Box sx={centreStyle}>

                    {isYearly &&
                    <>
                        <Button color="inherit"
                                style={2015 < year ? undefined : unavailableStepStyle}
                                onClick={() => setYear(year - 1)}>
                            <ArrowLeftIcon/>
                        </Button>
                        <Typography variant="h6" noWrap component="div">
                            {year}
                        </Typography>

                        <Button color="inherit"
                                style={year < new Date().getFullYear() ? undefined : unavailableStepStyle}
                                onClick={() => setYear(year + 1)}>
                            <ArrowRightIcon/>
                        </Button>
                    </>
                    }
                    {selectValues !== null &&
                        <Select
                            variant="standard"
                            sx={selectorStyle}
                            value={selectedValue}
                            onChange={event => setSelectedValue(event.target.value)}
                        >
                            {selectValues.map((value, index) => (
                                <MenuItem key={index} value={value} style={getChartConfigStyle(value.id)}>{value.name}</MenuItem>
                            ))}
                        </Select>
                    }
                </Box>
                <Box sx={spacerStyle} />
            </Toolbar>
        </AppBar>
        </Box>
    )
}

export default MainBar;