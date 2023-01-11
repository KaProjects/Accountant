import React, {Component} from 'react';
import './App.css';
import {BrowserRouter, Route, Routes} from "react-router-dom";
import Budgeting from "./Budgeting";
import {properties} from "./properties";
import Vacation from "./Vacation";
import FinancialAssets from "./FinancialAssets";

class App extends Component {
    constructor(props) {
        super(props);
        this.state = {
            host: properties.host,
            port: properties.port,
        }
        // this.componentDidMount = this.componentDidMount.bind(this);
    }
    render() {
        return (
            <div>
                {/*<MainBar {...this.state} />*/}
                <BrowserRouter>
                    <Routes>
                        {/*<Route exact path="/" element={<Menu {...this.state}/> }/>*/}
                        <Route exact path="/budget/:year" element={<Budgeting {...this.state}/> }/>
                        <Route exact path="/view/:year/vacation" element={<Vacation {...this.state}/> }/>
                        <Route exact path="/financial/assets/:year" element={<FinancialAssets {...this.state}/> }/>
                        <Route exact path="/financial/assets" element={<FinancialAssets {...this.state}/> }/>

                    </Routes>
                </BrowserRouter>


            </div>
        )
    }
}

export default App;
