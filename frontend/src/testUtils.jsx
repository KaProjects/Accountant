import {render} from "@testing-library/react";
import {AppStateProvider} from "./state/appState";

/** Everything the shared state holds, so a test only has to name what it cares about. */
const DEFAULT_APP_STATE = {
    year: 2020,
    isYearly: false,
    selectValues: null,
    selectedValue: "",
    overallPath: null,
    tabs: null,
    selectedTab: null,
    setYear: () => {},
    setYearly: () => {},
    setSelectedValue: () => {},
    setSelectValues: () => {},
    setOverallPath: () => {},
    setTabs: () => {},
    setSelectedTab: () => {},
};

/** Renders a component together with the shared state it reads. */
export const renderWithAppState = (ui, overrides = {}) =>
    render(<AppStateProvider value={{...DEFAULT_APP_STATE, ...overrides}}>{ui}</AppStateProvider>);
