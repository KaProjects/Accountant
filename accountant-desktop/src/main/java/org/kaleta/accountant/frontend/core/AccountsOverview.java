package org.kaleta.accountant.frontend.core;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.backend.model.TransactionsModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.configuration.ConfigurationAction;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AccountsOverview extends JPanel implements Configurable {
    private Configuration configuration;

    private DefaultTreeModel accountTreeModel;
    private JTree accountTree;

    private AccountTableModel accountTableModel;
    private JScrollPane accountPane;
    private JLabel noAccountLabel;
    private JTable accountTable;

    private String selectedSchemaAccount;

    private TransactionTableModel transactionTableModel;
    private JScrollPane transactionPane;
    private JTable transactionTable;

    public AccountsOverview(Configuration configuration){
        initComponents();
        setConfiguration(configuration);
        update();
    }

    private void initComponents(){
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
            if (node instanceof SchemaAccountTreeNode) {
                selectedSchemaAccount = ((SchemaAccountTreeNode) node).getSchemaId();
            } else {
                selectedSchemaAccount = "-1";
            }
            updateAccountOverview();
        });
        JScrollPane treePane = new JScrollPane(accountTree);

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

        accountTable.getSelectionModel().addListSelectionListener(event -> updateTransactionOverview(accountTable.getSelectedRow()));

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

        transactionTableModel = new TransactionTableModel();
        transactionTable = new JTable();
        transactionPane = new JScrollPane(transactionTable);
        transactionTable.setModel(transactionTableModel);
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionTable.setRowSelectionAllowed(false);
        transactionTable.setColumnSelectionAllowed(false);
        transactionTable.getTableHeader().setReorderingAllowed(false);
        transactionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        TableColumnModel trColumnModel = transactionTable.getColumnModel();
        trColumnModel.getColumn(0).setMinWidth(90);
        trColumnModel.getColumn(0).setMaxWidth(90);
        trColumnModel.getColumn(0).setCellRenderer(new DefaultTableCellRenderer());
        ((DefaultTableCellRenderer)trColumnModel.getColumn(0).getCellRenderer()).setHorizontalAlignment(JLabel.CENTER);

        trColumnModel.getColumn(1).setMinWidth(60);
        trColumnModel.getColumn(1).setMaxWidth(60);
        trColumnModel.getColumn(1).setCellRenderer(new DefaultTableCellRenderer());
        ((DefaultTableCellRenderer)trColumnModel.getColumn(1).getCellRenderer()).setHorizontalAlignment(JLabel.RIGHT);
        trColumnModel.getColumn(2).setMinWidth(60);
        trColumnModel.getColumn(2).setMaxWidth(60);
        trColumnModel.getColumn(2).setCellRenderer(new DefaultTableCellRenderer());
        ((DefaultTableCellRenderer)trColumnModel.getColumn(2).getCellRenderer()).setHorizontalAlignment(JLabel.RIGHT);

        trColumnModel.getColumn(3).setMinWidth(200);
        trColumnModel.getColumn(4).setMinWidth(200);
        // TODO post 1.0 : play more with column renderers


        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(treePane)
                .addComponent(noAccountLabel)
                .addComponent(accountPane)
                .addComponent(transactionPane));
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addComponent(treePane,300,300,300)
                .addComponent(noAccountLabel)
                .addComponent(accountPane,500,500,500)
                .addComponent(transactionPane));

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

        this.getActionMap().put(Configuration.TRANSACTION_UPDATED, new ConfigurationAction(this) {
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

        for (int i = 0; i < root.getChildCount(); i++) {
            accountTree.expandPath(new TreePath(((DefaultMutableTreeNode)root.getChildAt(i)).getPath()));
        }

        selectedSchemaAccount = "-1";
        updateAccountOverview();

        this.revalidate();
        this.repaint();
    }

    private void updateAccountOverview() {
        if (selectedSchemaAccount.equals("-1")) {
            noAccountLabel.setVisible(false);
            accountPane.setVisible(false);
            transactionPane.setVisible(false);
            return;
        }
        List<AccountsModel.Account> accountList = Service.ACCOUNT.getAccountsBySchemaId(getConfiguration().getSelectedYear(),selectedSchemaAccount);
        if (accountList.size() == 0) {
            noAccountLabel.setVisible(true);
            accountPane.setVisible(false);
            transactionPane.setVisible(false);
        } else {
            noAccountLabel.setVisible(false);

            accountPane.setVisible(true);
            accountTableModel.setAccountList(accountList);
            accountTable.clearSelection();
            accountTable.repaint();
            accountTable.revalidate();

            transactionPane.setVisible(true);
            transactionTableModel.setTransactionList(new ArrayList<>(), "-1");
            transactionTable.repaint();
            transactionTable.revalidate();

            this.repaint();
            this.revalidate();
        }
    }

    private void updateTransactionOverview(int row) {
        if (row < 0) return;
        String selectedAccId = String.valueOf(accountTableModel.getValueAt(row, 0));
        List<TransactionsModel.Transaction> transactionList = Service.TRANSACTIONS.getTransactionsForAccount(getConfiguration().getSelectedYear(), selectedAccId);
        transactionTableModel.setTransactionList(transactionList, selectedAccId);
        transactionTable.repaint();
        transactionTable.revalidate();
        this.repaint();
        this.revalidate();
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
                        return Service.TRANSACTIONS.getAccountInitialValue(getConfiguration().getSelectedYear(), account);
                    }
                }
                case 3: return Service.TRANSACTIONS.getAccountTurnover(getConfiguration().getSelectedYear(), account);
                case 4: return Service.TRANSACTIONS.getAccountBalance(getConfiguration().getSelectedYear(), account);
                default: throw new IllegalArgumentException("columnIndex");
            }
        }
    }

    private class TransactionTableModel extends AbstractTableModel {
        private List<TransactionsModel.Transaction> transactionList;
        private String accountId;

        TransactionTableModel(){
            transactionList = new ArrayList<>();
            accountId = "-1";
        }

        void setTransactionList(List<TransactionsModel.Transaction> transactionList, String accountId) {
            this.transactionList.clear();
            this.transactionList.addAll(transactionList);
            this.accountId = accountId;
        }

        @Override
        public int getRowCount() {
            return transactionList.size();
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public String getColumnName(int column) {
            switch (column){
                case 0: return "Date";
                case 1: return "Debit";
                case 2: return "Credit";
                case 3: return "Account Pair";
                case 4: return "Description";
                default: throw new IllegalArgumentException("invalid column index");
            }
        }

        @Override
        public Object getValueAt(int row, int column) {
            TransactionsModel.Transaction transaction = transactionList.get(row);
            switch (column){
                case 0: return transaction.getDate().substring(0,2) + "." + transaction.getDate().substring(2,4) + "." + getConfiguration().getSelectedYear();
                case 1: return transaction.getDebit().equals(accountId) ? transaction.getAmount() + " " : "";
                case 2: return transaction.getCredit().equals(accountId) ? transaction.getAmount() + " " : "";
                case 3: return transaction.getDebit().equals(accountId) ? Service.ACCOUNT.getFullAccountName(getConfiguration().getSelectedYear(), transaction.getCredit()) : Service.ACCOUNT.getFullAccountName(getConfiguration().getSelectedYear(), transaction.getDebit());
                case 4: return transaction.getDescription();
                default: throw new IllegalArgumentException("columnIndex");
            }
        }
    }
}
