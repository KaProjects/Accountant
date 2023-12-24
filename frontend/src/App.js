import React, {Component} from 'react';
import './App.css';
import {BrowserRouter, Route, Routes} from "react-router-dom";
import Budgeting from "./views/Budgeting";
import Vacation from "./views/Vacation";
import FinancialAssets from "./views/FinancialAssets";
import Login from "./components/Login";
import MainBar from "./components/MainBar";
import AccountingStatement from "./views/AccountingStatement";
import Home from "./views/Home";

class App extends Component {
    constructor(props) {
        super(props);
        this.state = {
            token: null,
            year: new Date().getFullYear(),
            isYearly: false, // toggles year's switch in MainBar
            setYearly: this.setYearly.bind(this)
        }

        this.setToken = this.setToken.bind(this);
        this.getToken = this.getToken.bind(this);
        this.setYear = this.setYear.bind(this);
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
        this.setState({year: year})
    }

    setYearly(yearly){
        this.setState({isYearly: yearly})
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
                        <Route exact path="/view/vacation" element={<Vacation {...this.state}/> }/>
                        <Route exact path="/financial/assets/:all" element={<FinancialAssets {...this.state}/> }/>
                        <Route exact path="/financial/assets" element={<FinancialAssets {...this.state}/> }/>
                        <Route exact path="/accounting/:type" element={<AccountingStatement {...this.state}/> }/>
                        <Route exact path="/accounting/:type/:year" element={<AccountingStatement setYear={this.setYear} {...this.state}/> }/>
                        <Route path="*" element={this.PageNotFound()} />
                    </Routes>
                </BrowserRouter>
            </div>
        )
    }
}

export default App;
