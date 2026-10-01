import React, {useEffect} from "react";
import {Typography} from "@mui/material";
import QueryStatsIcon from '@mui/icons-material/QueryStats';
import PostAddIcon from '@mui/icons-material/PostAdd';
import CurrencyExchangeIcon from '@mui/icons-material/CurrencyExchange';
import TravelExploreIcon from '@mui/icons-material/TravelExplore';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings';
import TroubleshootIcon from '@mui/icons-material/Troubleshoot';
import AccountBalanceIcon from '@mui/icons-material/AccountBalance';
import StorageIcon from '@mui/icons-material/Storage';
import BarChartIcon from '@mui/icons-material/BarChart';
import HomeSection from "../components/home/HomeSection";
import {useAppState} from "../state/appState";
import {useGoTo} from "../services/navigation";

/** Statement cards offer the same two spans of time. */
const spans = (goTo, path) => [
    {label: "Yearly", onSelect: goTo(path)},
    {label: "Overall", onSelect: goTo(path + "/overall")},
];

const sections = (goTo) => [
    {
        title: "Accounting", variant: "h4", height: 230,
        cards: [
            {
                title: "Accounting Chart", icon: <BarChartIcon/>, onOpen: goTo("/chart/accounting"),
                caption: "Data visualisation for all accounting statements.",
            },
            {
                title: "Balance Sheet", icon: <AccountBalanceIcon/>, actions: spans(goTo, "/accounting/balance"),
                caption: "A balance sheet is a financial statement that contains details of a company's assets or liabilities at a specific point in time.",
            },
            {
                title: "Income Statement", icon: <PostAddIcon/>, actions: spans(goTo, "/accounting/profit"),
                caption: "The income statement provides an overview of revenues, expenses, net income, operating profit and net profit.",
            },
            {
                title: "Cash Flow Statement", icon: <CurrencyExchangeIcon/>, actions: spans(goTo, "/accounting/cashflow"),
                caption: "The cash flow statement (CFS) measures how well a company generates cash to pay its debt obligations, fund its operating expenses, and fund investments.",
            },
        ],
    },
    {
        title: "Analytics", variant: "h4", height: 250,
        cards: [
            {
                title: "Budgeting", icon: <QueryStatsIcon/>, onOpen: goTo("/budgeting"),
                caption: "Budgeting is the process of allocating finite resources to the prioritized needs of an organization. A plan for estimating income and expenses for a set period. Primary goals of budgeting are planning, controlling, and evaluating performance.",
            },
            {
                title: "Vacations", icon: <TravelExploreIcon/>, onOpen: goTo("/view/vacation"),
                caption: "The action of leaving something one previously occupied. A period of time set aside for festivals or recreation.",
            },
            {
                title: "Views", icon: <TroubleshootIcon/>, onOpen: goTo("/view"),
                caption: "A view groups various transactions and accounts together for better visualization of specific interests.",
            },
            {
                title: "Financial Assets", icon: <TrendingUpIcon/>,
                actions: [
                    {label: "Yearly", onSelect: goTo("/financial/assets")},
                    {label: "Overall", onSelect: goTo("/financial/assets/all")},
                ],
                caption: "A financial asset is a liquid asset that gets its value from a contractual right or ownership claim. Cash, stocks, bonds, mutual funds, and bank deposits are all are examples of financial assets.",
            },
        ],
    },
    {
        title: "Other", variant: "h6", height: 150,
        cards: [
            {
                title: "Data", icon: <StorageIcon/>, onOpen: goTo("/data"),
                caption: "Structured accounting data. Transactions for an account from a schema.",
            },
            {
                title: "Admin", icon: <AdminPanelSettingsIcon/>, onOpen: goTo("/admin"),
                caption: "Operational tasks that are not part of reading the books: syncing the desktop application's data, and the back-end API reference.",
            },
        ],
    },
];

const Home = () => {
    const {setYearly} = useAppState();
    const goTo = useGoTo();

    useEffect(() => {
        setYearly(false)
    }, [setYearly]);

    return (
        <>
            {sections(goTo).map((section) => (
                <HomeSection key={section.title} {...section}/>
            ))}

            <Typography style={{width: '100%', position: 'fixed', bottom: 0, marginLeft: 5}} component="footer" align={"left"}>
                Copyright © {new Date().getFullYear()} Stanislav Kaleta
            </Typography>
        </>
    )
}


export default Home;
