import React, {Component} from 'react';
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

class App extends Component {
    constructor(props) {
        super(props);

        function retrieveYear() {
            let sessionYear = sessionStorage.getItem('year')
            if (sessionYear !== null){
                return parseInt(sessionYear);
            } else {
                return new Date().getFullYear()
            }
        }

        this.state = {
            token: null,
            year: retrieveYear(),
            isYearly: false, // toggles year's switch in MainBar
            setYearly: this.setYearly.bind(this),
            selectValues: null,
            selectedValue: "",
            setSelectedValue: this.setSelectedValue.bind(this),
            setSelectValues: this.setSelectValues.bind(this)
        }

        this.setToken = this.setToken.bind(this);
        this.getToken = this.getToken.bind(this);
        this.setYear = this.setYear.bind(this);
        this.setSelectedValue = this.setSelectedValue.bind(this);
        this.setSelectValues = this.setSelectValues.bind(this);
    }

    setToken(token){
        if (token == null){
            sessionStorage.removeItem('token')
        } else {
            sessionStorage.setItem('token', token);
        }
        this.setState({token: token})
    }

    getToken() {
        if (this.state.token != null) {
            return this.state.token
        } else {
            return sessionStorage.getItem('token');
        }
    }

    setYear(year){
        sessionStorage.setItem('year', year);
        this.setState({year: year})
    }

    setYearly(yearly){
        this.setState({selectValues: null})
        this.setState({selectedValue: ""})
        this.setState({isYearly: yearly})
    }

    setSelectValues(values){
        this.setState({isYearly: false})
        this.setState({selectValues: values})
    }

    setSelectedValue(value){
        this.setState({selectedValue: value})
    }

    PageNotFound() {
        return (
            <div style={{position: "absolute", top: "25%", left: "50%", transform: "translate(-50%, -50%)"}}>
                <h2>404 Page not found</h2>
            </div>
        );
    }

    render() {

        if (!this.getToken()) {
            return <Login setToken={this.setToken}/>
        }

        return (
            <div>
                <MainBar setYear={this.setYear} setToken={this.setToken} {...this.state} />
                <BrowserRouter>
                    <Routes>
                        <Route exact path="/" element={<Home {...this.state}/> }/>
                        <Route exact path="/budgeting" element={<Budgeting {...this.state}/> }/>
                        <Route exact path="/view/:vacation" element={<Views {...this.state}/> }/>
                        <Route exact path="/view" element={<Views {...this.state}/> }/>
                        <Route exact path="/financial/assets/:all" element={<FinancialAssets {...this.state}/> }/>
                        <Route exact path="/financial/assets" element={<FinancialAssets {...this.state}/> }/>
                        <Route exact path="/accounting/:type" element={<AccountingStatement {...this.state}/> }/>
                        <Route exact path="/accounting/:type/:overall" element={<AccountingStatement {...this.state}/> }/>
                        <Route exact path="/chart/accounting" element={<AccountingChart {...this.state}/> }/>
                        <Route exact path="/data" element={<AccountingData {...this.state}/> }/>
                        <Route path="*" element={this.PageNotFound()} />
                    </Routes>
                </BrowserRouter>
            </div>
        )
    }
}

export default App;
