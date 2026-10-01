import React, {useEffect, useState} from 'react';
import './App.css';
import {BrowserRouter, Route, Routes} from "react-router-dom";
import Budgeting from "./views/Budgeting";
import Views from "./views/Views";
import FinancialAssets from "./views/FinancialAssets";
import Login from "./components/Login";
import MainBar from "./components/MainBar";
import AccountingStatement from "./views/AccountingStatement";
import Home from "./views/Home";
import AccountingData from "./views/AccountingData";
import AccountingChart from "./views/AccountingChart";
import Admin from "./views/Admin";
import AdminSync from "./views/AdminSync";
import {devLogin, isDevelopment} from "./services/devLogin";
import {hasSession} from "./services/session";
import {AppStateProvider} from "./state/appState";

/** The year last looked at in this tab, or the current one on a first visit. */
const retrieveYear = () => {
    const storedYear = sessionStorage.getItem('year');
    return storedYear !== null ? parseInt(storedYear) : new Date().getFullYear();
};

const PageNotFound = () => (
    <div style={{position: "absolute", top: "25%", left: "50%", transform: "translate(-50%, -50%)"}}>
        <h2>404 Page not found</h2>
    </div>
);

const App = () => {
    const [authenticated, setAuthenticated] = useState(false);
    // Nothing is rendered while this is set, so the login form does not flash for somebody who
    // already has a session.
    const [checkingSession, setCheckingSession] = useState(true);
    const [year, rememberYear] = useState(retrieveYear);
    const [isYearly, setIsYearly] = useState(false); // toggles year's switch in MainBar
    const [selectValues, rememberSelectValues] = useState(null);
    const [selectedValue, setSelectedValue] = useState("");

    /**
     * The session is an HttpOnly cookie, so the page cannot look at it and has to ask. Asking also
     * settles the two cases a stored token used to get wrong: a session the backend forgot because
     * it restarted, and one that has expired.
     */
    useEffect(() => {
        const establishSession = async () => {
            let session = await hasSession();

            if (!session && isDevelopment()) {
                session = await devLogin();
            }

            setAuthenticated(session);
            setCheckingSession(false);
        };

        establishSession();
    }, []);

    const setYear = (value) => {
        sessionStorage.setItem('year', value);
        rememberYear(value);
    };

    const setYearly = (yearly) => {
        rememberSelectValues(null);
        setSelectedValue("");
        setIsYearly(yearly);
    };

    const setSelectValues = (values) => {
        setIsYearly(false);
        rememberSelectValues(values);
    };

    const state = {
        year, isYearly, selectValues, selectedValue,
        setYear, setYearly, setSelectedValue, setSelectValues,
    };

    if (checkingSession) {
        return null;
    }

    if (!authenticated) {
        return <Login onAuthenticated={() => setAuthenticated(true)}/>;
    }

    return (
        <div>
            {/* Inside the router, so the bar can navigate without reloading the page. */}
            <BrowserRouter>
                <AppStateProvider value={state}>
                <MainBar/>
                <Routes>
                    <Route exact path="/" element={<Home/> }/>
                    <Route exact path="/budgeting" element={<Budgeting/> }/>
                    <Route exact path="/view/:vacation" element={<Views/> }/>
                    <Route exact path="/view" element={<Views/> }/>
                    <Route exact path="/financial/assets/:all" element={<FinancialAssets/> }/>
                    <Route exact path="/financial/assets" element={<FinancialAssets/> }/>
                    <Route exact path="/accounting/:type" element={<AccountingStatement/> }/>
                    <Route exact path="/accounting/:type/:overall" element={<AccountingStatement/> }/>
                    <Route exact path="/chart/accounting" element={<AccountingChart/> }/>
                    <Route exact path="/data" element={<AccountingData/> }/>
                    <Route exact path="/admin" element={<Admin/> }/>
                    <Route exact path="/admin/sync" element={<AdminSync/> }/>
                    <Route path="*" element={<PageNotFound/>} />
                </Routes>
                </AppStateProvider>
            </BrowserRouter>
        </div>
    );
};

export default App;
