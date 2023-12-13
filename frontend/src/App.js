import React, {Component} from 'react';
import './App.css';
import {BrowserRouter, Route, Routes} from "react-router-dom";
import Budgeting from "./views/Budgeting";
import Vacation from "./views/Vacation";
import FinancialAssets from "./views/FinancialAssets";
import Login from "./components/Login";
import MainBar from "./components/MainBar";
import IncomeStatement from "./views/IncomeStatement";

class App extends Component {
    constructor(props) {
        super(props);
        this.state = {
            token: null,
            year: new Date().getFullYear(),
            isYearly: true, // toggles year's switch in MainBar
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

    render() {

        if (!this.getToken()) {
            return <Login setToken={this.setToken}/>
        }

        return (
            <div>
                <MainBar setYear={this.setYear} setToken={this.setToken} {...this.state} />
                <BrowserRouter>
                    <Routes>
                        {/*<Route exact path="/" element={<Menu {...this.state}/> }/>*/}
                        <Route exact path="/budgeting" element={<Budgeting {...this.state}/> }/>
                        <Route exact path="/view/vacation" element={<Vacation {...this.state}/> }/>
                        <Route exact path="/financial/assets/:all" element={<FinancialAssets {...this.state}/> }/>
                        <Route exact path="/financial/assets" element={<FinancialAssets {...this.state}/> }/>
                        <Route exact path="/profit" element={<IncomeStatement {...this.state}/> }/>
                    </Routes>
                </BrowserRouter>

                {/*<div style={{width: '100%', position: 'fixed', bottom: 0, justifyContent: "center"}}>*/}
                {/*    Copyright © {new Date().getFullYear()} Stanislav Kaleta*/}
                {/*</div>*/}
            </div>
        )
    }
}

export default App;
