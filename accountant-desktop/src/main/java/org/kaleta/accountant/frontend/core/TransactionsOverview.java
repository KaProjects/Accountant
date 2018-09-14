package org.kaleta.accountant.frontend.core;

import org.kaleta.accountant.backend.model.TransactionsModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.configuration.ConfigurationAction;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TransactionsOverview extends JPanel implements Configurable, DocumentListener {
    private Configuration configuration;

    private final TransactionTableModel tableModel;
    private final JTable table;

    private List<TransactionsModel.Transaction> allTransactionList;
    private String filter;
    private JTextField textFieldFilter;

    public TransactionsOverview(){
        allTransactionList = new ArrayList<>();
        filter = "-1";
        tableModel = new TransactionTableModel();
        table = new JTable();
        table.setModel(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.setPreferredScrollableViewportSize(new Dimension(1200,800));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setMinWidth(90);
        columnModel.getColumn(0).setMaxWidth(90);
        columnModel.getColumn(0).setCellRenderer(new DefaultTableCellRenderer());
        ((DefaultTableCellRenderer)columnModel.getColumn(0).getCellRenderer()).setHorizontalAlignment(JLabel.CENTER);
        columnModel.getColumn(1).setMinWidth(300);
        columnModel.getColumn(2).setMinWidth(80);
        columnModel.getColumn(2).setMaxWidth(80);
        columnModel.getColumn(2).setCellRenderer(new DefaultTableCellRenderer());
        ((DefaultTableCellRenderer)columnModel.getColumn(2).getCellRenderer()).setHorizontalAlignment(JLabel.RIGHT);
        columnModel.getColumn(3).setMinWidth(300);
        columnModel.getColumn(4).setMinWidth(300);
        // TODO post 1.0 : play more with column renderers

        this.getActionMap().put(Configuration.TRANSACTION_UPDATED, new ConfigurationAction(this) {
            @Override
            protected void actionPerformed() {
                TransactionsOverview.this.update();
            }
        });

        textFieldFilter = new JTextField();
        textFieldFilter.getDocument().addDocumentListener(this);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup().addComponent(textFieldFilter).addComponent(table));
        layout.setVerticalGroup(layout.createSequentialGroup().addComponent(textFieldFilter,20,20,20).addComponent(table));
    }


    public void update(){
        allTransactionList.clear();
        allTransactionList.addAll(Service.TRANSACTIONS.getTransactions(getConfiguration().getSelectedYear(),null,null));
        filterTransactions();
    }

    private void filterTransactions(){
        if (filter.equals("-1")){
            tableModel.setTransactionList(allTransactionList);
        } else {
            List<TransactionsModel.Transaction> filteredTransactions = new ArrayList<>();
            for (TransactionsModel.Transaction tr : allTransactionList){
                if (tr.getCredit().startsWith(filter) || tr.getDebit().startsWith(filter)) {
                    filteredTransactions.add(tr);
                }
            }
            tableModel.setTransactionList(filteredTransactions);
        }
        table.repaint();
        table.revalidate();
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public void insertUpdate(DocumentEvent documentEvent) {
        proceedFilter();
    }

    @Override
    public void removeUpdate(DocumentEvent documentEvent) {
        proceedFilter();
    }

    @Override
    public void changedUpdate(DocumentEvent documentEvent) {
        proceedFilter();
    }

    private void proceedFilter(){
        if (textFieldFilter.getText().trim().isEmpty()) {
            filter = "-1";
        } else {
            filter = textFieldFilter.getText();
        }
        filterTransactions();
    }

    private class TransactionTableModel extends AbstractTableModel implements Comparator<TransactionsModel.Transaction> {
        private List<TransactionsModel.Transaction> transactionList;

        TransactionTableModel(){
            transactionList = new ArrayList<>();
        }

        void setTransactionList(List<TransactionsModel.Transaction> newTransactionList) {
            transactionList.clear();
            transactionList.addAll(newTransactionList);
            transactionList.sort(this);
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
                case 1: return "Description";
                case 2: return "Amount";
                case 3: return "Debit";
                case 4: return "Credit";
                default: throw new IllegalArgumentException("invalid column index");
            }
        }

        @Override
        public Object getValueAt(int row, int column) {
            TransactionsModel.Transaction transaction = transactionList.get(row);
            switch (column){
                case 0: return transaction.getDate().substring(0,2) + "." + transaction.getDate().substring(2,4) + "." + getConfiguration().getSelectedYear();
                case 1: return transaction.getDescription();
                case 2: return transaction.getAmount();
                case 3: return getFullAccountName(transaction.getDebit());
                case 4: return getFullAccountName(transaction.getCredit());
                default: throw new IllegalArgumentException("columnIndex");
            }
        }

        @Override
        public int compare(TransactionsModel.Transaction tr1, TransactionsModel.Transaction tr2) {
            int monthDiff = Integer.parseInt(tr1.getDate().substring(2,4)) - Integer.parseInt(tr2.getDate().substring(2,4));
            if (monthDiff != 0) {
                return monthDiff;
            } else {
                return Integer.parseInt(tr1.getDate().substring(0,2)) - Integer.parseInt(tr2.getDate().substring(0,2));
            }
        }

        private String getFullAccountName(String accountId){
            String schemaName = Service.SCHEMA.getAccountName(getConfiguration().getSelectedYear(), accountId.substring(0,1),
                    accountId.substring(1,2), accountId.substring(2,3));
            String semanticName = Service.ACCOUNT.getAccountName(getConfiguration().getSelectedYear(), accountId);
            if (semanticName.equals(Constants.Account.GENERAL_ACCOUNT_NAME)){
                return accountId + " " + schemaName;
            } else {
                return accountId + " " + schemaName + " - " + semanticName;
            }

        }
    }
}
