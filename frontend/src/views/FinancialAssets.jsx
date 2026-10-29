import {useEffect, useState} from "react";
import {Box, ButtonBase, Collapse, Typography} from "@mui/material";
import {ExpandLess, ExpandMore, History} from "@mui/icons-material";
import {useData} from "../fetch";
import DataView from "../components/common/DataView";
import AssetPanel from "../components/financial/AssetPanel";
import {groupTitle, splitByActivity} from "../services/financialAssets";
import {useAppState} from "../state/appState";

const pageStyle = {padding: "16px", maxWidth: "1400px", margin: "0 auto"};
// on a surface of its own, as the panels are: the page around it is dark
const historicalToggleStyle = {
    width: "100%", justifyContent: "flex-start", gap: "8px", padding: "8px 12px", marginBottom: "16px",
    borderRadius: "4px", backgroundColor: "#f7f7f7", color: "rgba(0, 0, 0, 0.6)",
};

/**
 * Every financial asset ever held, over all the years, one group at a time: the groups are tabs
 * in the main bar, and the page shows the one chosen - the first, until another is.
 *
 * The chosen group's assets still held are all open. The ones sold off in earlier years come
 * first, folded away under a single line that opens them all: they are the group's history,
 * worth looking up but not worth scrolling past.
 *
 * There used to be a yearly view as well. The overall one covers every year since an asset keeps
 * its id from year to year, so it is the only one.
 */
const FinancialAssets = () => {
    const {setYearly, setTabs, selectedTab, setSelectedTab} = useAppState();
    const {data, loaded, error} = useData("/financial/assets");
    // the group whose history is open: another group's starts folded away
    const [historyOpenIn, setHistoryOpenIn] = useState(null);

    useEffect(() => {
        setYearly(false)
    }, [setYearly]);

    // the groups are offered as tabs once they are known, and withdrawn when the page is left, so
    // that no other page shows them
    const groupTitles = loaded && data ? data.groups.map((group) => groupTitle(group.name)) : null;
    const offered = groupTitles === null ? null : groupTitles.join("\n");
    useEffect(() => {
        if (offered === null) return undefined;
        const titles = offered === "" ? [] : offered.split("\n");
        setTabs(titles);
        setSelectedTab((selected) => titles.includes(selected) ? selected : (titles[0] ?? null));
        return () => {
            setTabs(null);
            setSelectedTab(null);
        };
    }, [offered, setTabs, setSelectedTab]);

    return (
        <DataView loaded={loaded} error={error}>
            {() => {
                const group = data.groups.find((each) => groupTitle(each.name) === selectedTab);
                const {active, historical} = splitByActivity(group ? group.accounts : []);
                const historyOpen = historyOpenIn !== null && historyOpenIn === selectedTab;
                return (
                    // keyed by the group, so that choosing another starts afresh rather than folding the history away with the next group's in it
                    <Box key={selectedTab} style={pageStyle}>
                        {historical.length > 0 && <>
                            <ButtonBase style={historicalToggleStyle} aria-expanded={historyOpen}
                                        onClick={() => setHistoryOpenIn(historyOpen ? null : selectedTab)}>
                                <History fontSize="small"/>
                                <Typography variant="body2">Historical ({historical.length})</Typography>
                                <Box sx={{flexGrow: 1}}/>
                                {historyOpen ? <ExpandLess/> : <ExpandMore/>}
                            </ButtonBase>
                            <Collapse in={historyOpen} timeout="auto" unmountOnExit>
                                {historical.map((account) => <AssetPanel key={account.id} account={account}/>)}
                            </Collapse>
                        </>}

                        {active.map((account) => <AssetPanel key={account.id} account={account}/>)}
                    </Box>
                );
            }}
        </DataView>
    );
};

export default FinancialAssets;
