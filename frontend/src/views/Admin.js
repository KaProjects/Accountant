import React, {useEffect} from "react";
import {Box, Divider, List, ListItemButton, ListItemIcon, ListItemText, Paper, Typography} from "@mui/material";
import CloudSyncIcon from "@mui/icons-material/CloudSync";
import ApiIcon from "@mui/icons-material/Api";
import ChevronRightIcon from "@mui/icons-material/ChevronRight";
import OpenInNewIcon from "@mui/icons-material/OpenInNew";
import {properties} from "../properties";
import {isDevelopment} from "../services/devLogin";
import {useAppState} from "../state/appState";
import {useGoTo} from "../services/navigation";

const pageStyle = {
    width: {xs: "calc(100% + 16px)", sm: "100%"},
    maxWidth: {sm: 420},
    margin: {xs: "0 -8px", sm: "24px auto"},
    padding: {xs: 0, sm: "0 8px"},
};
const panelStyle = {borderRadius: {xs: 0, sm: 1}, borderWidth: {xs: "1px 0", sm: "1px"}};
const groupTitleStyle = {
    padding: "10px 16px 4px",
    fontSize: 11,
    fontWeight: 700,
    letterSpacing: "0.08em",
    textTransform: "uppercase",
    color: "text.secondary",
};
const pageIconStyle = {minWidth: 38, color: "text.secondary"};
const chevronStyle = {color: "text.disabled"};

const DATA_GROUP = {
    title: "Data",
    pages: [
        {title: "Sync", icon: CloudSyncIcon, path: "/admin/sync"},
    ],
};

const REFERENCE_GROUP = {
    title: "Reference",
    pages: [
        {title: "API Docs", icon: ApiIcon, external: true},
    ],
};

/**
 * The API description is a development tool. It is deliberately left out of the production build,
 * because it was a complete, unauthenticated map of every endpoint - so the link to it is only
 * offered where it leads somewhere.
 */
const adminGroups = () => isDevelopment() ? [DATA_GROUP, REFERENCE_GROUP] : [DATA_GROUP];

/** Operational tasks, kept off the main view because none of them is part of reading the books. */
const Admin = () => {
    const {setYearly} = useAppState();
    const goTo = useGoTo();

    useEffect(() => {
        setYearly(false)
        // eslint-disable-next-line
    }, []);

    const open = (page) => (event) => {
        if (page.external) {
            // The API description is served by the backend, not by this application.
            window.open(properties.apiDocsUrl, "_blank")
        } else {
            goTo(page.path)(event)
        }
    };

    return (
        <Box sx={pageStyle}>
            <Paper variant="outlined" sx={panelStyle}>
                {adminGroups().map((group, groupIndex) => (
                    <Box key={group.title}>
                        {groupIndex > 0 && <Divider/>}
                        <Typography sx={groupTitleStyle}>
                            {group.title}
                        </Typography>
                        <List disablePadding>
                            {group.pages.map(page => (
                                <ListItemButton key={page.title} onClick={open(page)}>
                                    <ListItemIcon sx={pageIconStyle}>
                                        <page.icon fontSize="small"/>
                                    </ListItemIcon>
                                    <ListItemText primary={page.title} slotProps={{primary: {fontSize: 15}}}/>
                                    {page.external
                                        ? <OpenInNewIcon fontSize="small" sx={chevronStyle}/>
                                        : <ChevronRightIcon fontSize="small" sx={chevronStyle}/>}
                                </ListItemButton>
                            ))}
                        </List>
                    </Box>
                ))}
            </Paper>
        </Box>
    )
}


export default Admin;
