package org.kaleta.accountant.frontend.dialog;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.listener.AccountsEditorAccountAction;
import org.kaleta.accountant.frontend.component.SelectAccountTextField;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
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
        this(configuration, accountMap, classList, false, true);
    }

    public SelectAccountDialog(Configuration configuration, Map<String, java.util.List<AccountsModel.Account>> accountMap, List<SchemaModel.Class> classList, boolean expanded) {
        this(configuration, accountMap, classList, expanded, true);
    }

    public SelectAccountDialog(Configuration configuration, Map<String, java.util.List<AccountsModel.Account>> accountMap, List<SchemaModel.Class> classList, boolean expanded, boolean modal) {
        super(configuration, modal? "Selecting Account" : "Showing Accounts", modal? "Select" : "Close");
        this.accountMap = accountMap;
        this.classList = classList;
        selectedAccountId = "";
        buildDialogContent(expanded, modal);
        updateTree("", expanded);
        setModal(modal);
        this.setSize(350, (int) (0.8f * Toolkit.getDefaultToolkit().getScreenSize().height));
    }

    private void buildDialogContent(boolean expanded, boolean modal) {
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
                c.setBackground(Color.LIGHT_GRAY);
                ((JLabel) c).setOpaque(value.toString().equals(Constants.Account.GENERAL_ACCOUNT_NAME) && !hasFocus);
                return c;
            }
        });
        accountTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) accountTree.getLastSelectedPathComponent();
            if (node instanceof SelectAccountDialog.AccountTreeNode) {
                selectedAccountId = ((SelectAccountDialog.AccountTreeNode) node).getSchemaFullId();
                selectedAccountName = ((SelectAccountDialog.AccountTreeNode) node).getFullName();
                SelectAccountDialog.this.setDialogValid(null);
                if (!modal) accountTree.setDragEnabled(true);
            } else {
                selectedAccountId = "";
                selectedAccountName = "";
                SelectAccountDialog.this.setDialogValid("No account selected");
                if (!modal) accountTree.setDragEnabled(false);
            }
            buttonAddAccount.setEnabled(node instanceof SelectAccountDialog.SchemaAccountTreeNode);
        });

        if (!modal) accountTree.setTransferHandler(new SelectAccountDialogTransferHandler(this));

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
                updateTree(schemaId, expanded);
            }
        });

        SelectAccountDialog.this.setDialogValid("No account selected");

        setContent(layout -> {
            layout.setHorizontalGroup(layout.createParallelGroup().addComponent(accountTreeScrollPane));
            layout.setVerticalGroup(layout.createSequentialGroup().addComponent(accountTreeScrollPane));
        });

        setButtons(jPanel -> jPanel.add(buttonAddAccount));
    }

    private void updateTree(String updatedSchemaId, boolean expanded){
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
            DefaultMutableTreeNode nodeClasses = (DefaultMutableTreeNode) root.getChildAt(i);
            accountTree.expandPath(new TreePath(nodeClasses.getPath()));
            if (expanded) {
                for (int j = 0; j < nodeClasses.getChildCount(); j++) {
                    DefaultMutableTreeNode nodeGroups = (DefaultMutableTreeNode) nodeClasses.getChildAt(j);
                    accountTree.expandPath(new TreePath(nodeGroups.getPath()));

                    for (int k = 0; k < nodeGroups.getChildCount(); k++) {
                        DefaultMutableTreeNode nodeAccounts = (DefaultMutableTreeNode) nodeGroups.getChildAt(k);
                        accountTree.expandPath(new TreePath(nodeAccounts.getPath()));
                    }
                }
            }
        }
        if (updatedSchemaAccountNode != null && !expanded) accountTree.expandPath(new TreePath(updatedSchemaAccountNode.getPath()));


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


    private class SelectAccountDialogTransferHandler extends TransferHandler {
        private SelectAccountDialog dialog;
        SelectAccountDialogTransferHandler(SelectAccountDialog dialog){
            super();
            this.dialog = dialog;
        }
        public int getSourceActions(JComponent c) {
            return COPY;
        }

        public Transferable createTransferable(JComponent c) {
            return new StringSelection(dialog.getSelectedAccountId());
        }

        public void exportDone(JComponent c, Transferable t, int action) {

        }

        public boolean canImport(TransferSupport ts) {
            return ts.getComponent() instanceof SelectAccountTextField;
        }

        public boolean importData(TransferSupport ts) {
            try {
                ((SelectAccountTextField) ts.getComponent())
                        .setSelectedAccount((String) ts.getTransferable().getTransferData(DataFlavor.stringFlavor));
                return true;
            } catch (UnsupportedFlavorException | IOException e) {
                return false;
            }
        }
    }
}
