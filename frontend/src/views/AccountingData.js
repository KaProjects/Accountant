import {useData} from "../fetch";
import React, {useEffect, useState} from "react";
import Loader from "../components/Loader";
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import {TreeView} from '@mui/x-tree-view/TreeView';
import {TreeItem} from '@mui/x-tree-view/TreeItem';
import {
    Box,
    Grid,
    styled,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Typography
} from "@mui/material";
import Paper from "@mui/material/Paper";

const AccountingData = props => {

    const {data, loaded, error} = useData("/schema/" + props.year)

    useEffect(() => {
        props.setYearly(true)
        // eslint-disable-next-line
    }, []);

    const ClassTreeItem = styled(TreeItem)`
      & > .MuiTreeItem-content > .MuiTreeItem-label {font-weight: bold; color: #017901;}, 
      & > .MuiTreeItem-content.Mui-selected {background: transparent;}, 
      & > .MuiTreeItem-content.Mui-selected:hover {background: #efefef;}
    `;

    const GroupTreeItem = styled(TreeItem)`
      & > .MuiTreeItem-content > .MuiTreeItem-label {font-weight: bold; color: #014779;},
      & > .MuiTreeItem-content {margin-left: -10px;},
      & > .MuiTreeItem-content.Mui-selected {background: transparent;},
      & > .MuiTreeItem-content.Mui-selected:hover {background: #efefef;}
    `;

    const AccountTreeItem = styled(TreeItem)`
      & > .MuiTreeItem-content > .MuiTreeItem-label {font-weight: bold; color: #61279f;},
      & > .MuiTreeItem-content {margin-left: -20px;},
      & > .MuiTreeItem-content.Mui-selected {background: #f6edc5;},
      & > .MuiTreeItem-content.Mui-selected:hover {background: #f6edc5;}
    `;

    const [expanded, setExpanded] = React.useState([]);

    const handleToggle = (event, nodeIds) => {
        const newNodeIds = []
        nodeIds.forEach(nodeId => {
            if (nodeId.includes("g")){
                if (nodeIds.includes(nodeId.split("g")[0])) newNodeIds.push(nodeId)
            } else {
                newNodeIds.push(nodeId)
            }
        })
        setExpanded(newNodeIds);
    };

    const [schemaId, setSchemaId] = useState(null);
    const [accountId, setAccountId] = useState(null);

    function AccountTable(props) {
        const {year, schemaId} = props;
        const {data, loaded, error} = useData("/account/" + year + "/" + schemaId)
        const columns = ["Id", "Name", "Initial", "Turnover", new Date().getFullYear() === year ? "Balance" : "Closure"]

        return (
            <React.Fragment>
                {!loaded &&
                    <Loader error ={error}/>
                }
                {loaded &&
                    <TableContainer component={Paper}>
                        <Table sx={{ minWidth: 100}} size="small" aria-label="a dense table">
                            <TableHead>
                                <TableRow>
                                    {columns.map((column, index) => (
                                        <TableCell key={index}>{column}</TableCell>
                                    ))}
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {data.map((account, index) => (
                                    <TableRow hover key={index} onClick={() => setAccountId(account.id)} selected={account.id === accountId}>
                                        <TableCell align="left">{account.id}</TableCell>
                                        <TableCell align="left">{account.name}</TableCell>
                                        <TableCell align="right">{account.initial}</TableCell>
                                        <TableCell align="right">{account.turnover}</TableCell>
                                        <TableCell align="right">{account.balance}</TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                }
            </React.Fragment>
        );
    }

    function TransactionTable(props) {
        const {year, accountId} = props;
        const {data, loaded, error} = useData("/transaction/" + year + "/" + accountId)
        const columns = ["Date", "Debit", "Credit", "Account Pair", "Description"]

        return (
            <React.Fragment>
                {!loaded &&
                    <Loader error ={error}/>
                }
                {loaded &&
                    <TableContainer component={Paper}>
                        <Table sx={{ minWidth: 100}} size="small" aria-label="a dense table">
                            <TableHead>
                                <TableRow>
                                    {columns.map((column, index) => (
                                        <TableCell key={index}>{column}</TableCell>
                                    ))}
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {data.map((transaction, index) => (
                                    <TableRow hover key={index}>
                                        <TableCell align="left">{transaction.date}</TableCell>
                                        <TableCell align="right">{transaction.debit}</TableCell>
                                        <TableCell align="right">{transaction.credit}</TableCell>
                                        <TableCell align="left">{transaction.pair}</TableCell>
                                        <TableCell align="left">{transaction.description}</TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                }
            </React.Fragment>
        );
    }

    function AccountLabel(props) {
        const {account} = props;

        return (
            <React.Fragment>
                <Box sx={{display: 'flex', alignItems: 'center'}}>
                    <Box color="inherit" sx={{ mr: 1 }} />
                    <Typography variant="body2" sx={{ fontWeight: 'inherit', fontSize: 'inherit', flexGrow: 1 }}>
                        {account.name}
                    </Typography>
                    {schemaId === account.id && <ChevronRightIcon />}
                </Box>
            </React.Fragment>
        );
    }

    return (
        <>
            {!loaded &&
                <Loader error ={error}/>
            }
            {loaded &&
                <Grid
                    container
                    direction="row"
                    justifyContent="flex-start"
                    alignItems="stretch"
                >
                    <TreeView
                        aria-label="rich object"
                        defaultCollapseIcon={<ExpandMoreIcon />}
                        defaultExpandIcon={<ChevronRightIcon />}
                        expanded={expanded}
                        onNodeToggle={handleToggle}
                        style={{marginTop: "10px", marginBottom: "100px", width: "300px"}}
                    >
                        {data.classes.map((clazz, cIndex) => (
                            <ClassTreeItem nodeId={"c" + cIndex}
                                           key={"c" + cIndex}
                                           label={clazz.name}
                                           onClick={() => {setSchemaId(null);setAccountId(null);}}
                            >
                                {clazz.groups.map((group, gIndex) => (
                                    <GroupTreeItem nodeId={"c" + cIndex + "g" + gIndex}
                                                   key={"c" + cIndex + "g" + gIndex}
                                                   label={group.name}
                                                   onClick={() => {setSchemaId(null);setAccountId(null);}}
                                    >
                                        {group.accounts.map((account, aIndex) => (
                                            <AccountTreeItem nodeId={"c" + cIndex + "g" + gIndex + "a" + aIndex}
                                                             key={"c" + cIndex + "g" + gIndex + "a" + aIndex}
                                                             label={<AccountLabel account={account}/>}
                                                             onClick={() => {setSchemaId(account.id);setAccountId(null);}}
                                            />
                                        ))}
                                    </GroupTreeItem>
                                ))}
                            </ClassTreeItem>
                        ))}
                    </TreeView>

                    <div style={{marginTop: "10px", marginBottom: "100px"}}>
                        {schemaId !== null && <AccountTable year={props.year} schemaId={schemaId}/>}
                    </div>
                    <div>
                        {accountId !== null && <ChevronRightIcon style={{marginTop: "18px"}}/>}
                    </div>
                    <div style={{marginTop: "10px", marginBottom: "100px"}}>
                        {accountId !== null && <TransactionTable year={props.year} accountId={accountId}/>}
                    </div>
                </Grid>
            }
        </>
    )
}

export default AccountingData;