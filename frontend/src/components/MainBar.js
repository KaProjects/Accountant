import React from "react";
import {AppBar, Box, Button, IconButton, MenuItem, Select, Toolbar, Typography} from "@mui/material";
import MenuIcon from '@mui/icons-material/Menu';
import ArrowLeftIcon from '@mui/icons-material/ArrowLeft';
import ArrowRightIcon from '@mui/icons-material/ArrowRight';
import {getChartConfigStyle} from "../constants";

const MainBar = props => {

    return (
        <Box sx={{ flexGrow: 1 }}>
        <AppBar position="static">
            <Toolbar variant="dense">
                <IconButton size="large" edge="start" color="inherit" aria-label="open drawer" sx={{ mr: 2 }}
                            onClick={event => {sessionStorage.removeItem('year');window.location.href='/'}}>
                    <MenuIcon />
                </IconButton>
                <Typography variant="h6" noWrap component="div" sx={{ display: { xs: 'none', sm: 'block' } }}>
                    Accountant
                </Typography>
                <Box sx={{ flexGrow: 1 }} />
                <Box sx={{ display: { xs: 'none', md: 'flex' } }}>

                    {props.isYearly &&
                    <>
                        {2015 < props.year &&
                            <Button color="inherit" onClick={() => props.setYear(props.year - 1)}>
                                <ArrowLeftIcon/>
                            </Button>
                        }
                        <Typography variant="h6" noWrap component="div">
                            {props.year}
                        </Typography>

                        {props.year < new Date().getFullYear() &&
                            <Button color="inherit" onClick={() => props.setYear(props.year + 1)}>
                                <ArrowRightIcon/>
                            </Button>
                        }
                    </>
                    }
                    {props.selectValues !== null &&
                        <Select
                            variant="standard"
                            sx={{
                                color: "white",
                                '.MuiSvgIcon-root ': {fill: "white"},
                                ':before': { borderBottomColor: 'white' },
                                ':after': { borderBottomColor: 'white' }
                            }}
                            value={props.selectedValue}
                            onChange={event => props.setSelectedValue(event.target.value)}
                        >
                            {props.selectValues.map((value, index) => (
                                <MenuItem key={index} value={value} style={getChartConfigStyle(value.id)}>{value.name}</MenuItem>
                            ))}
                        </Select>
                    }
                </Box>
                <Box sx={{ flexGrow: 1 }} />
                <Box sx={{ display: { xs: 'none', md: 'flex' } }}>
                    <Button color="inherit" onClick={() => props.setToken(null)}>
                        Logout
                    </Button>
                </Box>
            </Toolbar>
        </AppBar>
        </Box>
    )
}

export default MainBar;