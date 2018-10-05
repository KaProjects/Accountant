package org.kaleta.accountant.frontend.dialog;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.listener.AccountsEditorAccountAction;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SelectAccountDialog extends Dialog {
    private final Map<String, List<AccountsModel.Account>> accountMap;
    private final List<SchemaModel.Class> classList;

    private String selectedAccountId;
    private String selectedAccountName;

    private DefaultTreeModel accountTreeModel;
    private JTree accountTree;

    public SelectAccountDialog(Configuration configuration, Map<String, java.util.List<AccountsModel.Account>> accountMap, List<SchemaModel.Class> classList) {
        super(configuration, "Selecting Account", "Select");
        this.accountMap = accountMap;
        this.classList = classList;
        selectedAccountId = "";
        buildDialogContent();
        updateTree("");
        this.setSize(350, (int) (0.8f * Toolkit.getDefaultToolkit().getScreenSize().height));
    }

    private void buildDialogContent() {
        JButton buttonAddAccount = new JButton("Add Account");
        buttonAddAccount.setEnabled(false);

        accountTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode("root"));
        accountTree = new JTree(accountTreeModel);
        accountTree.setRootVisible(false);
        accountTree.setToggleClickCount(1);
        accountTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        accountTree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                c.setFont(new Font(c.getFont().getName(), Font.BOLD, 15));
                setIcon(null);
                switch (((DefaultMutableTreeNode) value).getLevel()) {
                    case 1: {
                        c.setForeground(Constants.Color.OVERVIEW_CLASS);
                        break;
                    }
                    case 2: {
                        c.setForeground(Constants.Color.OVERVIEW_GROUP);
                        break;
                    }
                    case 3: {
                        c.setForeground(Constants.Color.OVERVIEW_ACCOUNT);
                        break;
                    }
                }
                return c;
            }
        });
        accountTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) accountTree.getLastSelectedPathComponent();
            if (node instanceof SelectAccountDialog.AccountTreeNode) {
                selectedAccountId = ((SelectAccountDialog.AccountTreeNode) node).getSchemaFullId();
                selectedAccountName = ((SelectAccountDialog.AccountTreeNode) node).getFullName();
                SelectAccountDialog.this.setDialogValid(null);
            } else {
                selectedAccountId = "";
                selectedAccountName = "";
                SelectAccountDialog.this.setDialogValid("No account selected");
            }
            buttonAddAccount.setEnabled(node instanceof SelectAccountDialog.SchemaAccountTreeNode);
        });
        JScrollPane accountTreeScrollPane = new JScrollPane(accountTree);

        buttonAddAccount.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(getParent(), "Set Account Name");
            if (name != null && !name.trim().isEmpty()) {
                String schemaId = ((SchemaAccountTreeNode) accountTree.getLastSelectedPathComponent()).getSchemaId();
                List<AccountsModel.Account> accountList = accountMap.get(schemaId);
                if (accountList == null) accountList = new ArrayList<>();
                AccountsEditorAccountAction action = new AccountsEditorAccountAction(this, schemaId, null);
                AccountsModel.Account createdAccount = action.subactionPerformed(name);
                accountList.add(createdAccount);
                accountMap.put(schemaId, accountList);
                updateTree(schemaId);
            }
        });

        SelectAccountDialog.this.setDialogValid("No account selected");

        setContent(layout -> {
            layout.setHorizontalGroup(layout.createParallelGroup().addComponent(accountTreeScrollPane));
            layout.setVerticalGroup(layout.createSequentialGroup().addComponent(accountTreeScrollPane));
        });

        setButtons(jPanel -> jPanel.add(buttonAddAccount));
    }

    private void updateTree(String updatedSchemaId){
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) accountTreeModel.getRoot();
        root.removeAllChildren();
        DefaultMutableTreeNode updatedSchemaAccountNode = null;
        for (SchemaModel.Class clazz : classList) {
            DefaultMutableTreeNode classNode = new DefaultMutableTreeNode(clazz.getName());
            for (SchemaModel.Class.Group group : clazz.getGroup()) {
                DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(group.getName());
                for (SchemaModel.Class.Group.Account acc : group.getAccount()) {
                    SchemaAccountTreeNode schemaAccountNode = new SchemaAccountTreeNode(acc.getName());
                    String schemaId = clazz.getId() + group.getId() + acc.getId();
                    schemaAccountNode.setSchemaId(schemaId);
                    List<AccountsModel.Account> accountList = accountMap.get(schemaId);
                    if (accountList != null) {
                        for (AccountsModel.Account account : accountList) {
                            AccountTreeNode accountNode = new AccountTreeNode(account.getName());
                            accountNode.setSchemaFullId(account.getFullId());
                            if (account.getName().equals(Constants.Account.GENERAL_ACCOUNT_NAME)){
                                accountNode.setFullName(acc.getName());
                            } else {
                                accountNode.setFullName(acc.getName() + " - " + account.getName());
                            }
                            schemaAccountNode.add(accountNode);
                        }
                    }
                    groupNode.add(schemaAccountNode);
                    if (schemaId.equals(updatedSchemaId)) updatedSchemaAccountNode = schemaAccountNode;
                }
                classNode.add(groupNode);
            }
            root.add(classNode);
        }
        accountTreeModel.setRoot(root);

        for (int i = 0; i < root.getChildCount(); i++) {
            accountTree.expandPath(new TreePath(((DefaultMutableTreeNode)root.getChildAt(i)).getPath()));
        }

        if (updatedSchemaAccountNode != null) accountTree.expandPath(new TreePath(updatedSchemaAccountNode.getPath()));
    }

    public String getSelectedAccountId() {
        return selectedAccountId;
    }

    public String getSelectedAccountName() {
        return selectedAccountName;
    }

    private class AccountTreeNode extends DefaultMutableTreeNode {
        private String schemaFullId;
        private String fullName;

        AccountTreeNode(String title) {
            super(title);
        }

        String getSchemaFullId() {
            return schemaFullId;
        }

        void setSchemaFullId(String schemaId) {
            this.schemaFullId = schemaId;
        }

        String getFullName() {
            return fullName;
        }

        void setFullName(String fullName) {
            this.fullName = fullName;
        }
    }

    private class SchemaAccountTreeNode extends DefaultMutableTreeNode {
        private String schemaId;

        SchemaAccountTreeNode(String title) {
            super(title);
        }

        String getSchemaId() {
            return schemaId;
        }

        void setSchemaId(String schemaId) {
            this.schemaId = schemaId;
        }
    }
}
