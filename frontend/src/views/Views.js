import React, {useEffect, useState} from "react";
import '../viewsContainer.css';
import {List} from "@mui/material";
import {useParams} from "react-router-dom";
import {useData} from "../fetch";
import DataView from "../components/common/DataView";
import ViewPanel from "../components/views/ViewPanel";
import {useAppState} from "../state/appState";

const Views = () => {
    const {year, setYearly} = useAppState();
    const {vacation} = useParams();

    const [openIndex, setOpenIndex] = useState(null)

    const {data, loaded, error} = useData("/view/" + year + (vacation === undefined ? "" : "/vacation"))

    useEffect(() => {
        setYearly(true)
        setOpenIndex(null)
        // eslint-disable-next-line
    }, [data]);

    /** Opening a view closes any other. */
    const toggle = (index) => setOpenIndex(openIndex === index ? null : index)

    return (
        <DataView loaded={loaded} error={error}>
            {() => (
                <List component="nav" aria-labelledby="nested-list-subheader">
                    {data.views.map((view, index) => (
                        <ViewPanel
                            key={index}
                            view={view}
                            columns={data.columns}
                            isOpen={openIndex === index}
                            onToggle={() => toggle(index)}
                        />
                    ))}
                </List>
            )}
        </DataView>
    )
}


export default Views;
