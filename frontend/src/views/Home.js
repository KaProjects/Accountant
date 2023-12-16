import React, {useEffect} from "react";
import {Button, ButtonBase, Card, CardActions, CardContent, Stack, Typography} from "@mui/material";
import QueryStatsIcon from '@mui/icons-material/QueryStats';
import PostAddIcon from '@mui/icons-material/PostAdd';
import CurrencyExchangeIcon from '@mui/icons-material/CurrencyExchange';
import TravelExploreIcon from '@mui/icons-material/TravelExplore';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import {properties} from "../properties";
import SettingsSuggestIcon from '@mui/icons-material/SettingsSuggest';


const Home = props => {

    useEffect(() => {
        props.setYearly(false)
    }, []);

    return (
        <>
        <Typography variant="h4" component="div" align={"center"} margin={2}>
            Accounting
        </Typography>
        <Stack direction="row" justifyContent="center" alignItems="flex-start" spacing={0.5}>
            <Card sx={{ width: 300, height: 200}} raised style={{backgroundColor:"#ffc107"}}>
                <ButtonBase onClick={event => window.location.href='/accounting/profit'}>
                    <CardContent>
                        <Typography variant="h5" component="div" align={"center"}>
                            Income Statement
                        </Typography>
                        <Typography align={"center"}>
                            <PostAddIcon/>
                        </Typography>
                        <Typography variant="caption">
                            The income statement provides an overview of revenues, expenses, net income, operating profit and net profit.
                        </Typography>
                    </CardContent>
                </ButtonBase>
            </Card>
            <Card sx={{ width: 300, height: 200 }} raised style={{backgroundColor:"#ffc107"}}>
                <ButtonBase onClick={event => window.location.href='/accounting/cashflow'}>
                    <CardContent>
                        <Typography variant="h5" component="div" align={"center"}>
                            Cash Flow Statement
                        </Typography>
                        <Typography align={"center"}>
                            <CurrencyExchangeIcon/>
                        </Typography>
                        <Typography variant="caption">
                            The cash flow statement (CFS) measures how well a company generates cash to pay its debt obligations, fund its operating expenses, and fund investments.
                        </Typography>
                    </CardContent>
                </ButtonBase>
            </Card>

            {/*import AccountBalanceIcon from '@mui/icons-material/AccountBalance';*/}

        </Stack>

        <Typography variant="h4" component="div" align={"center"} margin={2}>
            Analytics
        </Typography>
        <Stack direction="row" justifyContent="center" alignItems="flex-start" spacing={0.5}>
            <Card sx={{ width: 300, height: 250 }} raised style={{backgroundColor:"#ffc107"}}>
                <ButtonBase onClick={event => window.location.href='/budgeting'}>
                    <CardContent>
                        <Typography variant="h5" component="div" align={"center"}>
                            Budgeting
                        </Typography>
                        <Typography align={"center"}>
                            <QueryStatsIcon/>
                        </Typography>
                        <Typography variant="caption">
                            Budgeting is the process of allocating finite resources to the prioritized needs of an organization. A plan for estimating income and expenses for a set period. Primary goals of budgeting are planning, controlling, and evaluating performance.
                        </Typography>
                    </CardContent>
                </ButtonBase>
            </Card>
            <Card sx={{ width: 300, height: 250 }} raised style={{backgroundColor:"#ffc107"}}>
                <ButtonBase onClick={event => window.location.href='/view/vacation'}>
                    <CardContent>
                        <Typography variant="h5" component="div" align={"center"}>
                            Vacations
                        </Typography>
                        <Typography align={"center"}>
                            <TravelExploreIcon/>
                        </Typography>
                        <Typography variant="caption">
                            The action of leaving something one previously occupied. A period of time set aside for festivals or recreation.
                        </Typography>
                    </CardContent>
                </ButtonBase>
            </Card>
            <Card sx={{ width: 300, height: 250 }} raised style={{backgroundColor:"#ffc107"}}>
                <CardContent>
                    <Typography variant="h5" component="div" align={"center"}>
                        Financial Assets
                    </Typography>
                    <Typography align={"center"}>
                        <TrendingUpIcon/>
                    </Typography>
                    <Typography variant="caption">
                        A financial asset is a liquid asset that gets its value from a contractual right or ownership claim. Cash, stocks, bonds, mutual funds, and bank deposits are all are examples of financial assets.
                    </Typography>
                </CardContent>
                <CardActions style={{justifyContent: 'center'}}>
                    <Button size="small" onClick={event => window.location.href='/financial/assets'}>Yearly</Button>
                    <Button size="small" onClick={event => window.location.href='/financial/assets/all'}>Overall</Button>
                </CardActions>
            </Card>
        </Stack>

        <Typography variant="h6" component="div" align={"center"} marginTop={3} marginBottom={2}>
            Other
        </Typography>
        <Stack direction="row" justifyContent="center" alignItems="flex-start" spacing={0.5}>
            <Card sx={{ width: 300 }} raised style={{backgroundColor:"#ffc107"}}>
                <ButtonBase onClick={event => window.location.href=properties.protocol + "://" + properties.host + ":" + properties.port + '/api/docs'}>
                    <CardContent>
                        <Typography variant="h5" component="div" align={"center"}>
                            API
                        </Typography>
                        <Typography align={"center"}>
                            <SettingsSuggestIcon/>
                        </Typography>
                        <Typography variant="caption">
                            A back-end API is a programming interface that helps developers to interact with back-end services.
                        </Typography>
                    </CardContent>
                </ButtonBase>
            </Card>
        </Stack>

        <Typography style={{width: '100%', position: 'fixed', bottom: 0}} component="footer" align={"center"}>
            Copyright © {new Date().getFullYear()} Stanislav Kaleta
        </Typography>
        </>
    )
}

export default Home;