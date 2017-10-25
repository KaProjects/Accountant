package org.kaleta.accountant.frontend.dialog;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.Constants;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SelectAccountDialog extends Dialog {
    private Map<String, List<AccountsModel.Account>> accountMap;
    private List<SchemaModel.Class> classList;

    private String selectedAccountId;
    private String selectedAccountName;

    public SelectAccountDialog(Frame parent, Map<String, java.util.List<AccountsModel.Account>> accountMap, List<SchemaModel.Class> classList) {
        super(parent, "Selecting Account");
        this.accountMap = accountMap;
        this.classList = classList;
        selectedAccountId = "";
        buildDialog();
        this.setSize(300, 500);
    }

    public SelectAccountDialog(Frame parent, Map<String, java.util.List<AccountsModel.Account>> accountMap, SchemaModel.Class clazz) {
        super(parent, "Selecting Account");
        this.accountMap = accountMap;
        this.classList = new ArrayList<>();
        classList.add(clazz);
        selectedAccountId = "";
        buildDialog();
        this.setSize(300, 500);
    }

    @Override
    protected void buildDialog() {
        JButton buttonCancel = new JButton("Cancel");
        buttonCancel.addActionListener(a -> dispose());

        JButton buttonOk = new JButton("Add");
        buttonOk.addActionListener(a -> {
            if (selectedAccountId.trim().isEmpty()) {
                JOptionPane.showMessageDialog(SelectAccountDialog.this, "No account selected!", "Value Missing", JOptionPane.ERROR_MESSAGE);
                return;
            }
            result = true;
            dispose();
        });

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        for (SchemaModel.Class clazz : classList) {
            boolean classHasAccount = false;
            DefaultMutableTreeNode classNode = new DefaultMutableTreeNode(clazz.getName());
            for (SchemaModel.Class.Group group : clazz.getGroup()) {
                boolean groupHasAccount = false;
                DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(group.getName());
                for (SchemaModel.Class.Group.Account acc : group.getAccount()) {
                    DefaultMutableTreeNode accNode = new DefaultMutableTreeNode(acc.getName());
                    List<AccountsModel.Account> accountList = accountMap.get(clazz.getId() + group.getId() + acc.getId());
                    if (accountList != null) {
                        classHasAccount = true;
                        groupHasAccount = true;
                        for (AccountsModel.Account account : accountList) {
                            AccountTreeNode accountNode = new AccountTreeNode(account.getName());
                            accountNode.setSchemaId(account.getFullId());
                            accountNode.setFullName(acc.getName() + " - " + account.getName());
                            accNode.add(accountNode);
                        }
                        groupNode.add(accNode);
                    }
                }
                if (groupHasAccount) classNode.add(groupNode);
            }
            if (classHasAccount) root.add(classNode);
        }

        JTree tree = new JTree(root);
        tree.setRootVisible(false);
        tree.setToggleClickCount(1);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setCellRenderer(new DefaultTreeCellRenderer() {
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
        tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node instanceof SelectAccountDialog.AccountTreeNode) {
                selectedAccountId = ((SelectAccountDialog.AccountTreeNode) node).getSchemaId();
                selectedAccountName = ((SelectAccountDialog.AccountTreeNode) node).getFullName();
            } else {
                selectedAccountId = "";
                selectedAccountName = "";
            }
        });
        JScrollPane accountTreeScrollPane = new JScrollPane(tree);

        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup().addGap(5)
                .addGroup(layout.createParallelGroup()
                .addComponent(accountTreeScrollPane)
                .addGroup(layout.createSequentialGroup()
                        .addGap(5, 5, Short.MAX_VALUE).addComponent(buttonCancel).addGap(5).addComponent(buttonOk)))
                .addGap(5));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGap(5)
                .addComponent(accountTreeScrollPane)
                .addGap(5)
                .addGroup(layout.createParallelGroup()
                        .addComponent(buttonCancel).addComponent(buttonOk))
                .addGap(5));
    }

    public String getSelectedAccountId() {
        return selectedAccountId;
    }

    public String getSelectedAccountName() {
        return selectedAccountName;
    }

    private class AccountTreeNode extends DefaultMutableTreeNode {
        private String schemaId;
        private String fullName;

        AccountTreeNode(String title) {
            super(title);
        }

        public String getSchemaId() {
            return schemaId;
        }

        public void setSchemaId(String schemaId) {
            this.schemaId = schemaId;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
    }
}
