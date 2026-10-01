import {createContext, useCallback, useContext, useEffect, useState} from "react";
import PropTypes from "prop-types";
import {useSearchParams} from "react-router-dom";

const AppStateContext = createContext(null);

const YEAR = "year";

export const AppStateProvider = AppStateContext.Provider;

/**
 * The state the main bar and the views share.
 *
 * It is context rather than props because it is not passed in one direction: the chart view
 * publishes the values the main bar offers in its selector, and the main bar publishes the year
 * every view reads. Handing the whole of it to every route as props - which is what this replaced
 * - meant each view's signature claimed state it never touched, and adding one field changed
 * every signature.
 */
export const useAppState = () => {
    const state = useContext(AppStateContext);

    if (state === null) {
        throw new Error("useAppState was called outside an AppStateProvider");
    }
    return state;
};

/** The address of the given page, showing the given year. */
export const yearlyPath = (path, year) => path + "?" + YEAR + "=" + year;

/**
 * Holds that state, reading the year from the address.
 *
 * The year lives in the address rather than in a variable, so reloading the page, bookmarking it
 * or sharing it all keep the year being looked at. It used to be kept in session storage, which
 * survived a reload but was invisible in the address and belonged to the tab rather than the page.
 *
 * It is written only when it is changed, so a page opened without one simply shows the current
 * year, and the views with no year selector - the overall statements, the chart, the admin pages -
 * never put one in their address.
 *
 * This lives inside the router, because that is the only place the address can be read.
 */
const AppState = ({children}) => {
    const [searchParams, setSearchParams] = useSearchParams();
    const [isYearly, setIsYearly] = useState(false);
    const [selectValues, rememberSelectValues] = useState(null);
    const [selectedValue, setSelectedValue] = useState("");

    const requestedYear = parseInt(searchParams.get(YEAR));
    const year = Number.isNaN(requestedYear) ? new Date().getFullYear() : requestedYear;

    // Replaced rather than pushed: stepping through years should not fill the back button with
    // every year passed on the way.
    const setYear = useCallback((value) => {
        const next = new URLSearchParams(searchParams);
        next.set(YEAR, value);
        setSearchParams(next, {replace: true});
    }, [searchParams, setSearchParams]);

    // Stable, so that the views announcing what kind of page they are can declare this as the
    // dependency it is. They used to silence the dependency warning instead, with a bare disable
    // that switched off every rule on the line rather than the one they meant.
    // A yearly page says which year it is showing, so its address says so too - even when the
    // year was never changed and is simply the current one. Without this the address only gained a
    // year once the selector was used, so the same page had two different addresses depending on
    // how the reader got there.
    useEffect(() => {
        if (isYearly && searchParams.get(YEAR) === null) {
            setYear(year);
        }
    }, [isYearly, searchParams, setYear, year]);

    const setYearly = useCallback((yearly) => {
        rememberSelectValues(null);
        setSelectedValue("");
        setIsYearly(yearly);
    }, []);

    const setSelectValues = useCallback((values) => {
        setIsYearly(false);
        rememberSelectValues(values);
    }, []);

    return (
        <AppStateProvider value={{
            year, isYearly, selectValues, selectedValue,
            setYear, setYearly, setSelectedValue, setSelectValues,
        }}>
            {children}
        </AppStateProvider>
    );
};

AppState.propTypes = {
    children: PropTypes.node,
};

export default AppState;
