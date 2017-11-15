package org.kaleta.accountant.frontend.core;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.configuration.ConfigurationAction;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AccountsOverview extends JPanel implements Configurable {
    private Configuration configuration;

    private final DefaultTreeModel accountTreeModel;
    private final AccountTableModel accountTableModel;
    private final JScrollPane accountPane;
    private final JLabel noAccountLabel;
    private final JTable accountTable;

    private String selectedSchemaAccount;

    public AccountsOverview() {
        accountTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode("root"));
        JTree tree = new JTree(accountTreeModel);
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
            if (node instanceof SchemaAccountTreeNode) {
                selectedSchemaAccount = ((SchemaAccountTreeNode) node).getSchemaId();
            } else {
                selectedSchemaAccount = "-1";
            }
            updateAccountOverview();
        });
        JScrollPane treePane = new JScrollPane(tree);

        noAccountLabel = new JLabel("Selected Schema Account has no accounts assigned.");

        accountTableModel = new AccountTableModel();
        accountTable = new JTable();
        accountPane = new JScrollPane(accountTable);
        accountTable.setModel(accountTableModel);
        accountTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        accountTable.setRowSelectionAllowed(true);
        accountTable.setColumnSelectionAllowed(false);
        accountTable.getTableHeader().setReorderingAllowed(false);
        accountTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        TableColumnModel columnModel = accountTable.getColumnModel();
        columnModel.getColumn(0).setMinWidth(60);
        columnModel.getColumn(0).setMaxWidth(60);
        columnModel.getColumn(1).setMinWidth(200);
        columnModel.getColumn(2).setMinWidth(60);
        columnModel.getColumn(2).setMaxWidth(60);
        columnModel.getColumn(2).setCellRenderer(new DefaultTableCellRenderer());
        ((DefaultTableCellRenderer)columnModel.getColumn(2).getCellRenderer()).setHorizontalAlignment(JLabel.RIGHT);
        columnModel.getColumn(3).setMinWidth(60);
        columnModel.getColumn(3).setMaxWidth(60);
        columnModel.getColumn(3).setCellRenderer(new DefaultTableCellRenderer());
        ((DefaultTableCellRenderer)columnModel.getColumn(3).getCellRenderer()).setHorizontalAlignment(JLabel.RIGHT);
        columnModel.getColumn(4).setMinWidth(60);
        columnModel.getColumn(4).setMaxWidth(60);
        columnModel.getColumn(4).setCellRenderer(new DefaultTableCellRenderer());
        ((DefaultTableCellRenderer)columnModel.getColumn(4).getCellRenderer()).setHorizontalAlignment(JLabel.RIGHT);
        // TODO post 1.0 : play more with column renderers



        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(treePane)
                .addComponent(noAccountLabel)
                .addComponent(accountPane));
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addComponent(treePane,300,300,300)
                .addComponent(noAccountLabel)
                .addComponent(accountPane));

        this.getActionMap().put(Configuration.SCHEMA_UPDATED, new ConfigurationAction(this) {
            @Override
            protected void actionPerformed() {
                AccountsOverview.this.update();
            }
        });

        this.getActionMap().put(Configuration.ACCOUNT_UPDATED, new ConfigurationAction(this) {
            @Override
            protected void actionPerformed() {
                AccountsOverview.this.updateAccountOverview();
            }
        });
    }

    public void update() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) accountTreeModel.getRoot();
        root.removeAllChildren();
        for (SchemaModel.Class clazz : Service.SCHEMA.getSchemaClassList(getConfiguration().getSelectedYear())) {
            if (clazz.getId().equals("7")) continue;
            DefaultMutableTreeNode classNode = new DefaultMutableTreeNode(clazz.getName());
            root.add(classNode);
            for (SchemaModel.Class.Group group : clazz.getGroup()) {
                DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(group.getName());
                classNode.add(groupNode);
                for (SchemaModel.Class.Group.Account account : group.getAccount()) {
                    SchemaAccountTreeNode accountNode = new SchemaAccountTreeNode(account.getName());
                    accountNode.setSchemaId(clazz.getId() + group.getId() + account.getId());
                    groupNode.add(accountNode);
                }
            }
        }
        accountTreeModel.setRoot(root);

        selectedSchemaAccount = "-1";
        updateAccountOverview();

        this.revalidate();
        this.repaint();
    }

    private void updateAccountOverview() {
        if (selectedSchemaAccount.equals("-1")) {
            accountPane.setVisible(false);
            noAccountLabel.setVisible(false);
            return;
        }
        List<AccountsModel.Account> accountList = Service.ACCOUNT.getAccountsBySchemaId(getConfiguration().getSelectedYear(),selectedSchemaAccount);
        if (accountList.size() == 0) {
            accountPane.setVisible(false);
            noAccountLabel.setVisible(true);
        } else {
            accountPane.setVisible(true);
            noAccountLabel.setVisible(false);
            accountTableModel.setAccountList(accountList);
            this.repaint();
            this.revalidate();
        }
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
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

    private class AccountTableModel extends AbstractTableModel {
        private List<AccountsModel.Account> accountList;

        AccountTableModel(){
            accountList = new ArrayList<>();
        }

        void setAccountList(List<AccountsModel.Account> accountList) {
            this.accountList.clear();
            this.accountList.addAll(accountList);
        }

        @Override
        public int getRowCount() {
            return accountList.size();
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public String getColumnName(int column) {
            switch (column){
                case 0: return "Id";
                case 1: return "Name";
                case 2: return "Initial";
                case 3: return "Turnover";
                case 4: return "Balance";
                default: throw new IllegalArgumentException("invalid column index");
            }
        }

        @Override
        public Object getValueAt(int row, int column) {
            AccountsModel.Account account = accountList.get(row);
            switch (column){
                case 0: return account.getFullId();
                case 1: return account.getName();
                case 2: {
                    String accountType = Service.SCHEMA.getSchemaAccountType(getConfiguration().getSelectedYear(), account.getSchemaId());
                    if (accountType.equals(Constants.AccountType.EXPENSE) || accountType.equals(Constants.AccountType.REVENUE)){
                        return "-";
                    } else {
                        return Service.TRANSACTIONS.getAccountInitValue(getConfiguration().getSelectedYear(), account);
                    }
                }
                case 3: return Service.TRANSACTIONS.getAccountTurnover(getConfiguration().getSelectedYear(), account);
                case 4: return Service.TRANSACTIONS.getAccountBalance(getConfiguration().getSelectedYear(), account);
                default: throw new IllegalArgumentException("columnIndex");
            }
        }
    }
}
