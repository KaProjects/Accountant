package org.kaleta.accountant.frontend.component;

import org.kaleta.accountant.backend.entity.Journal;
import org.kaleta.accountant.backend.entity.Schema;
import org.kaleta.accountant.backend.entity.Transaction;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stanislav Kaleta on 10.08.2016.
 */
public class ProfitTableModel extends AbstractTableModel {
    private List<BalanceRow> expenses;
    private List<BalanceRow> revenues;

    public ProfitTableModel(){
        expenses = new ArrayList<>();
        revenues = new ArrayList<>();
    }

    @Override
    public int getRowCount() {
        return (expenses.size() > revenues.size()) ? expenses.size() : revenues.size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public Object getValueAt(int row, int column) {
        switch (column){
            case 0:
                if (row < expenses.size()){
                    return expenses.get(row).getName();
                } else {
                    return null;
                }
            case 1:
                if (row < expenses.size()){
                    return expenses.get(row).getValue();
                } else {
                    return null;
                }
            case 2:
                if (row < revenues.size()){
                    return revenues.get(row).getName();
                } else {
                    return null;
                }
            case 3:
                if (row < revenues.size()){
                    return revenues.get(row).getValue();
                } else {
                    return null;
                }
            default: throw new IllegalArgumentException("columnIndex");
        }
    }

    public void update(Schema schemaR, Schema schemaE, Journal journal){
        expenses.clear();
        //int expensesBalance = 0;
        for (Schema.Class clazz : schemaE.getClazz()){
            List<BalanceRow> groups = new ArrayList<>();
            int classBalance = 0;
            for (Schema.Class.Group group : clazz.getGroup()) {
                List<BalanceRow> accounts = new ArrayList<>();
                int groupBalance = 0;
                for (Schema.Class.Group.Account account : group.getAccount()) {
                    int accBalance = 0;
                    for (Transaction transaction : journal.getTransaction()) {
                        String schemaId = clazz.getId() + group.getId() + account.getId();
                        if (transaction.getDebit().startsWith(schemaId)) {
                            accBalance += Integer.parseInt(transaction.getAmount());
                        }
                        if (transaction.getCredit().startsWith(schemaId)) {
                            accBalance -= Integer.parseInt(transaction.getAmount());
                        }
                    }
                    accounts.add(new BalanceRow(account.getName(), String.valueOf(accBalance), BalanceRow.ACCOUNT));
                    groupBalance += accBalance;
                }
                groups.add(new BalanceRow(group.getName(), String.valueOf(groupBalance), BalanceRow.GROUP));
                groups.addAll(accounts);
                classBalance += groupBalance;
            }
            //expensesBalance += classBalance;
            expenses.add(new BalanceRow(clazz.getName(), String.valueOf(classBalance), BalanceRow.CLASS));
            expenses.addAll(groups);
        }

        revenues.clear();
        //int revenuesBalance = 0;
        for (Schema.Class clazz : schemaR.getClazz()){
            List<BalanceRow> groups = new ArrayList<>();
            int classBalance = 0;
            for (Schema.Class.Group group : clazz.getGroup()) {
                List<BalanceRow> accounts = new ArrayList<>();
                int groupBalance = 0;
                for (Schema.Class.Group.Account account : group.getAccount()) {
                    int accBalance = 0;
                    for (Transaction transaction : journal.getTransaction()) {
                        String schemaId = clazz.getId() + group.getId() + account.getId();
                        if (transaction.getDebit().startsWith(schemaId)) {
                            accBalance -= Integer.parseInt(transaction.getAmount());
                        }
                        if (transaction.getCredit().startsWith(schemaId)) {
                            accBalance += Integer.parseInt(transaction.getAmount());
                        }
                    }
                    accounts.add(new BalanceRow(account.getName(), String.valueOf(accBalance), BalanceRow.ACCOUNT));
                    groupBalance += accBalance;
                }
                groups.add(new BalanceRow(group.getName(), String.valueOf(groupBalance), BalanceRow.GROUP));
                groups.addAll(accounts);
                classBalance += groupBalance;
            }
            //revenuesBalance += classBalance;
            revenues.add(new BalanceRow(clazz.getName(), String.valueOf(classBalance), BalanceRow.CLASS));
            revenues.addAll(groups);
        }
        // TODO: 8/10/16 profit not showed here (is only in balance) for now
    }

    public String getCellType(int row, int column){
        switch (column){
            case 0:
            case 1:
                if (row < expenses.size()){
                    return expenses.get(row).getType();
                } else {
                    return "";
                }
            case 2:
            case 3:
                if (row < revenues.size()){
                    return revenues.get(row).getType();
                } else {
                    return "";
                }
            default: return null;
        }
    }

    @Override
    public String getColumnName(int column) {
        return "";
    }
}
