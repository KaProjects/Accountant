import React, {useEffect, useState} from "react";
import '../viewsContainer.css';
import {List} from "@mui/material";
import {useParams} from "react-router-dom";
import {useData} from "../fetch";
import DataView from "../components/common/DataView";
import ViewPanel from "../components/views/ViewPanel";
import {useAppState} from "../state/appState";

// the views right under the main bar, with room between one and the next
const listStyle = {"& > * + *": {marginTop: "12px"}};

const Views = () => {
    const {year, setYearly} = useAppState();
    const {vacation} = useParams();

    const {data, loaded, error} = useData("/view/" + year + (vacation === undefined ? "" : "/vacation"))

    useEffect(() => {
        setYearly(true)
    }, [setYearly]);

    // every view closed to begin with, for each answer read - its title says what it spent; each
    // opened and closed again on its own
    const [opened, setOpened] = useState({views: null, indices: []});
    const openIndices = opened.views === data ? opened.indices : [];
    const toggle = (index) => setOpened({
        views: data,
        indices: openIndices.includes(index) ? openIndices.filter((one) => one !== index) : [...openIndices, index],
    });

    return (
        <DataView loaded={loaded} error={error}>
            {() => (
                <List component="nav" aria-labelledby="nested-list-subheader" disablePadding sx={listStyle}>
                    {data.views.map((view, index) => (
                        <ViewPanel
                            key={index}
                            view={view}
                            columns={data.columns}
                            isOpen={openIndices.includes(index)}
                            onToggle={() => toggle(index)}
                        />
                    ))}
                </List>
            )}
        </DataView>
    )
}


export default Views;
