package org.kaleta.accountant.frontend.core;

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
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionsOverview extends JPanel implements Configurable {
    private Configuration configuration;
    private final TransactionTableModel tableModel;
    private final JTable table;

    public TransactionsOverview(){
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

        this.setLayout(new GridLayout(1,1));
        this.add(table);
    }


    public void update(){
        tableModel.setTransactionList(Service.TRANSACTIONS.getTransactions(getConfiguration().getSelectedYear(),null,null));
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

    private class TransactionTableModel extends AbstractTableModel {
        private List<TransactionsModel.Transaction> transactionList;

        // TODO post 1.0 : transaction filter

        TransactionTableModel(){
            transactionList = new ArrayList<>();
        }

        void setTransactionList(List<TransactionsModel.Transaction> transactionList) {
            this.transactionList = transactionList;
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
