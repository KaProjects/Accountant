package org.kaleta.accountant.frontend.core;

import org.kaleta.accountant.backend.model.TransactionsModel;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.configuration.ConfigurationAction;
import org.kaleta.accountant.frontend.common.TransactionComparator;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionsOverview extends JPanel implements Configurable, DocumentListener {
    private Configuration configuration;

    private TransactionTableModel tableModel;
    private JTable table;

    private List<TransactionsModel.Transaction> allTransactionList;
    private String filter;
    private JTextField textFieldFilter;

    public TransactionsOverview(Configuration configuration){
        initComponents();
        setConfiguration(configuration);
        update();
    }

    private void initComponents(){
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

    private class TransactionTableModel extends AbstractTableModel {
        private List<TransactionsModel.Transaction> transactionList;

        TransactionTableModel(){
            transactionList = new ArrayList<>();
        }

        void setTransactionList(List<TransactionsModel.Transaction> newTransactionList) {
            transactionList.clear();
            transactionList.addAll(newTransactionList);
            transactionList.sort(new TransactionComparator());
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
                case 3: return Service.ACCOUNT.getFullAccountName(getConfiguration().getSelectedYear(), transaction.getDebit());
                case 4: return Service.ACCOUNT.getFullAccountName(getConfiguration().getSelectedYear(), transaction.getCredit());
                default: throw new IllegalArgumentException("columnIndex");
            }
        }
    }
}
