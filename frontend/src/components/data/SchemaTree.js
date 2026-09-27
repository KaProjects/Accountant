import PropTypes from "prop-types";
import {Box, styled, Typography} from "@mui/material";
import ChevronRightIcon from "@mui/icons-material/ChevronRight";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import {TreeView} from "@mui/x-tree-view/TreeView";
import {TreeItem} from "@mui/x-tree-view/TreeItem";

// Declared at module scope: styled() inside a component body rebuilds the
// component on every render, which defeats emotion's style cache and remounts
// the whole tree.
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

/** An account node, marked with a chevron while it is the selected one. */
const AccountLabel = ({account, isSelected}) => (
    <Box sx={{display: "flex", alignItems: "center"}}>
        <Box color="inherit" sx={{mr: 1}}/>
        <Typography variant="body2" sx={{fontWeight: "inherit", fontSize: "inherit", flexGrow: 1}}>
            {account.name}
        </Typography>
        {isSelected && <ChevronRightIcon/>}
    </Box>
);

AccountLabel.propTypes = {
    account: PropTypes.object.isRequired,
    isSelected: PropTypes.bool,
};

/** The chart of accounts as a class / group / account tree. */
const SchemaTree = ({classes, expanded, onToggle, selectedSchemaId, onSelectAccount, onClearSelection}) => (
    <TreeView
        aria-label="rich object"
        defaultCollapseIcon={<ExpandMoreIcon/>}
        defaultExpandIcon={<ChevronRightIcon/>}
        expanded={expanded}
        onNodeToggle={onToggle}
        style={{marginTop: "10px", marginBottom: "100px", width: "300px"}}
    >
        {classes.map((clazz, cIndex) => (
            <ClassTreeItem nodeId={"c" + cIndex}
                           key={"c" + cIndex}
                           label={clazz.name}
                           onClick={onClearSelection}
            >
                {clazz.groups.map((group, gIndex) => (
                    <GroupTreeItem nodeId={"c" + cIndex + "g" + gIndex}
                                   key={"c" + cIndex + "g" + gIndex}
                                   label={group.name}
                                   onClick={onClearSelection}
                    >
                        {group.accounts.map((account, aIndex) => (
                            <AccountTreeItem nodeId={"c" + cIndex + "g" + gIndex + "a" + aIndex}
                                             key={"c" + cIndex + "g" + gIndex + "a" + aIndex}
                                             label={<AccountLabel account={account}
                                                                  isSelected={selectedSchemaId === account.id}/>}
                                             onClick={() => onSelectAccount(account.id)}
                            />
                        ))}
                    </GroupTreeItem>
                ))}
            </ClassTreeItem>
        ))}
    </TreeView>
);

SchemaTree.propTypes = {
    classes: PropTypes.array.isRequired,
    expanded: PropTypes.array.isRequired,
    onToggle: PropTypes.func.isRequired,
    selectedSchemaId: PropTypes.string,
    onSelectAccount: PropTypes.func.isRequired,
    onClearSelection: PropTypes.func.isRequired,
};

export {AccountLabel};
export default SchemaTree;
