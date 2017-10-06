package org.kaleta.accountant.frontend.dep;

import org.kaleta.accountant.backend.comparator.TransactionComparator;
import org.kaleta.accountant.backend.entity.Journal;
import org.kaleta.accountant.backend.entity.Schema;
import org.kaleta.accountant.backend.entity.Semantic;
import org.kaleta.accountant.backend.entity.Transaction;

import javax.swing.table.AbstractTableModel;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Stanislav Kaleta on 08.08.2016.
 */
public class JournalTableModel extends AbstractTableModel {
    private Schema schema;
    private Semantic semantic;
    private String year;
    private List<Transaction> transactionList;


    public JournalTableModel(Schema schema, Semantic semantic){
        this.schema = schema;
        this.semantic = semantic;
        this.year = "-1";
        transactionList = new LinkedList<>();
    }

    public void updateJournal(Journal journal){
        transactionList.clear();
        transactionList.addAll(journal.getTransaction());
        Collections.sort(transactionList, new TransactionComparator());
        year = journal.getYear();
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
            default: throw new IllegalArgumentException("columnIndex");
        }
    }

    @Override
    public Object getValueAt(int row, int column) {
        Transaction transaction = transactionList.get(row);
        switch (column){
            case 0: return transaction.getDate().substring(0,2) + "." + transaction.getDate().substring(2,4) + "." + year;
            case 1: return transaction.getDescription();
            case 2: return transaction.getAmount();
            case 3: return getFullAccountName(transaction.getDebit());
            case 4: return getFullAccountName(transaction.getCredit());
            default: throw new IllegalArgumentException("columnIndex");
        }
    }

    private String getFullAccountName(String accountId){
        String schemaId = accountId.split("-")[0];
        final String[] schemaName = new String[]{""};
        schema.getClazz().stream().filter(clazz -> clazz.getId().equals(schemaId.substring(0, 1))).forEach(clazz -> {
            clazz.getGroup().stream().filter(group -> group.getId().equals(schemaId.substring(1, 2))).forEach(group -> {
                group.getAccount().stream().filter(acc -> acc.getId().equals(schemaId.substring(2, 3))).forEach(acc -> {
                    schemaName[0] = acc.getName();
                });
            });
        });
        if (accountId.contains("-")){
            String semanticId = accountId.split("-")[1];
            final String[] semanticName = {""};
            semantic.getAccount().stream().filter(acc -> acc.getSchemaId().equals(schemaId)).forEach(acc -> {
                if (acc.getId().equals(semanticId)){
                    semanticName[0] = acc.getName();
                }
            });
            return accountId + " " + schemaName[0] + " - " + semanticName[0];
        } else {
            return accountId + " " + schemaName[0];
        }
    }
}
