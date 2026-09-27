import React, {useEffect, useState} from "react";
import PropTypes from "prop-types";
import {Grid} from "@mui/material";
import ChevronRightIcon from "@mui/icons-material/ChevronRight";
import {useData} from "../fetch";
import DataView from "../components/common/DataView";
import SchemaTree from "../components/data/SchemaTree";
import AccountTable from "../components/data/AccountTable";
import TransactionTable from "../components/data/TransactionTable";
import {keepReachableNodes} from "../services/schemaTree";

const columnStyle = {marginTop: "10px", marginBottom: "100px"};

const AccountingData = props => {

    const {data, loaded, error} = useData("/schema/" + props.year)

    useEffect(() => {
        props.setYearly(true)
        // eslint-disable-next-line
    }, []);

    const [expanded, setExpanded] = useState([]);
    const [schemaId, setSchemaId] = useState(null);
    const [accountId, setAccountId] = useState(null);

    const clearSelection = () => {
        setSchemaId(null)
        setAccountId(null)
    }

    const selectSchemaAccount = (id) => {
        setSchemaId(id)
        setAccountId(null)
    }

    return (
        <DataView loaded={loaded} error={error}>
            {() => (
                <Grid
                    container
                    direction="row"
                    justifyContent="flex-start"
                    alignItems="stretch"
                >
                    <SchemaTree
                        classes={data.classes}
                        expanded={expanded}
                        onToggle={(event, nodeIds) => setExpanded(keepReachableNodes(nodeIds))}
                        selectedSchemaId={schemaId}
                        onSelectAccount={selectSchemaAccount}
                        onClearSelection={clearSelection}
                    />

                    <div style={columnStyle}>
                        {schemaId !== null &&
                            <AccountTable
                                year={props.year}
                                schemaId={schemaId}
                                selectedAccountId={accountId}
                                onSelectAccount={setAccountId}
                            />
                        }
                    </div>
                    <div>
                        {accountId !== null && <ChevronRightIcon style={{marginTop: "18px"}}/>}
                    </div>
                    <div style={columnStyle}>
                        {accountId !== null && <TransactionTable year={props.year} accountId={accountId}/>}
                    </div>
                </Grid>
            )}
        </DataView>
    )
}

AccountingData.propTypes = {
    year: PropTypes.number.isRequired,
    setYearly: PropTypes.func.isRequired,
}

export default AccountingData;
