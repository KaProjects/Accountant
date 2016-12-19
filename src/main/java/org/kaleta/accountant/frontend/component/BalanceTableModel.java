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
public class BalanceTableModel extends AbstractTableModel{
    private List<BalanceRow> assets;
    private List<BalanceRow> liabilities;
    private BalanceRow assetsBalanceRow;
    private BalanceRow liabilitiesBalanceRow;

    public BalanceTableModel(){
        assets = new ArrayList<>();
        liabilities = new ArrayList<>();
        assetsBalanceRow = new BalanceRow("Assets", "T", "X", BalanceRow.SUM);
        liabilitiesBalanceRow = new BalanceRow("Liabilities", "T", "X", BalanceRow.SUM);
    }

    @Override
    public int getRowCount() {
        return 1 + ((assets.size() > liabilities.size()) ? assets.size() : liabilities.size());
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (row == getRowCount() - 1){
            switch (column){
                case 0: return assetsBalanceRow.getName();
                case 1: return assetsBalanceRow.getTurnover();
                case 2: return assetsBalanceRow.getValue();
                case 3: return liabilitiesBalanceRow.getName();
                case 4: return liabilitiesBalanceRow.getTurnover();
                case 5: return liabilitiesBalanceRow.getValue();
                default: throw new IllegalArgumentException("columnIndex");
            }
        }
        switch (column){
            case 0:
                if (row < assets.size()){
                    return assets.get(row).getName();
                } else {
                    return null;
                }
            case 1:
                if (row < assets.size()){
                    return assets.get(row).getTurnover();
                } else {
                    return null;
                }
            case 2:
                if (row < assets.size()){
                    return assets.get(row).getValue();
                } else {
                    return null;
                }
            case 3:
                if (row < liabilities.size()){
                    return liabilities.get(row).getName();
                } else {
                    return null;
                }
            case 4:
                if (row < liabilities.size()){
                    return liabilities.get(row).getTurnover();
                } else {
                    return null;
                }
            case 5:
                if (row < liabilities.size()){
                    return liabilities.get(row).getValue();
                } else {
                    return null;
                }
            default: throw new IllegalArgumentException("columnIndex");
        }
    }

    public void update(Schema schemaA, Schema schemaL, Journal journal){
        assets.clear();
        int assetsBalance = 0;
        int assetsTurnover = 0;
        for (Schema.Class clazz : schemaA.getClazz()){
            List<BalanceRow> groups = new ArrayList<>();
            int classBalance = 0;
            int classTurnover = 0;
            for (Schema.Class.Group group : clazz.getGroup()) {
                List<BalanceRow> accounts = new ArrayList<>();
                int groupBalance = 0;
                int groupTurnover = 0;
                for (Schema.Class.Group.Account account : group.getAccount()) {
                    int accBalance = 0;
                    int accTurnover = 0;
                    String schemaId = clazz.getId() + group.getId() + account.getId();
                    for (Transaction transaction : journal.getTransaction()) {
                        if (transaction.getDebit().startsWith(schemaId)) {
                            accBalance += Integer.parseInt(transaction.getAmount());
                            accTurnover += Integer.parseInt(transaction.getAmount());
                        }
                        if (transaction.getCredit().startsWith(schemaId)) {
                            accBalance -= Integer.parseInt(transaction.getAmount());
                        }
                    }
                    accounts.add(new BalanceRow(account.getName(), String.valueOf(accTurnover), String.valueOf(accBalance), schemaId, BalanceRow.ACCOUNT));
                    groupBalance += accBalance;
                    groupTurnover += accTurnover;
                }
                groups.add(new BalanceRow(group.getName(), String.valueOf(groupTurnover), String.valueOf(groupBalance), BalanceRow.GROUP));
                groups.addAll(accounts);
                classBalance += groupBalance;
                classTurnover += groupTurnover;
            }
            assetsBalance += classBalance;
            assetsTurnover += classTurnover;
            assets.add(new BalanceRow(clazz.getName(), String.valueOf(classTurnover), String.valueOf(classBalance), BalanceRow.CLASS));
            assets.addAll(groups);
        }
        assetsBalanceRow.setValue(String.valueOf(assetsBalance));
        assetsBalanceRow.setTurnover(String.valueOf(assetsTurnover));

        liabilities.clear();
        int liabilitiesBalance = 0;
        int liabilitiesTurnover = 0;
        for (Schema.Class clazz : schemaL.getClazz()){
            List<BalanceRow> groups = new ArrayList<>();
            int classBalance = 0;
            int classTurnover = 0;
            for (Schema.Class.Group group : clazz.getGroup()) {
                List<BalanceRow> accounts = new ArrayList<>();
                int groupBalance = 0;
                int groupTurnover = 0;
                for (Schema.Class.Group.Account account : group.getAccount()) {
                    int accBalance = 0;
                    int accTurnover = 0;
                    String schemaId = clazz.getId() + group.getId() + account.getId();
                    for (Transaction transaction : journal.getTransaction()) {
                        if (transaction.getDebit().startsWith(schemaId)) {
                            accBalance -= Integer.parseInt(transaction.getAmount());
                        }
                        if (transaction.getCredit().startsWith(schemaId)) {
                            accBalance += Integer.parseInt(transaction.getAmount());
                            accTurnover += Integer.parseInt(transaction.getAmount());
                        }
                    }
                    accounts.add(new BalanceRow(account.getName(), String.valueOf(accTurnover), String.valueOf(accBalance), schemaId, BalanceRow.ACCOUNT));
                    groupBalance += accBalance;
                    groupTurnover += accTurnover;
                }
                groups.add(new BalanceRow(group.getName(), String.valueOf(groupTurnover), String.valueOf(groupBalance), BalanceRow.GROUP));
                groups.addAll(accounts);
                classBalance += groupBalance;
                classTurnover += groupTurnover;
            }
            liabilitiesBalance += classBalance;
            liabilitiesTurnover += classTurnover;
            liabilities.add(new BalanceRow(clazz.getName(), String.valueOf(classTurnover), String.valueOf(classBalance), BalanceRow.CLASS));
            liabilities.addAll(groups);
        }
        int profit = assetsBalance - liabilitiesBalance;
        liabilities.add(new BalanceRow("Profit", "", String.valueOf(profit), BalanceRow.SUM));
        liabilitiesBalance += profit;
        liabilitiesBalanceRow.setValue(String.valueOf(liabilitiesBalance));
        liabilitiesBalanceRow.setTurnover(String.valueOf(liabilitiesTurnover));
    }

    public String getCellType(int row, int column){
        if (row == getRowCount() - 1){
            return BalanceRow.SUM;
        }
        switch (column){
            case 0:
            case 1:
            case 2:
                if (row < assets.size()){
                    return assets.get(row).getType();
                } else {
                    return "";
                }
            case 3:
            case 4:
            case 5:
                if (row < liabilities.size()){
                    return liabilities.get(row).getType();
                } else {
                    return "";
                }
            default: return null;
        }
    }

    public String getCellSchemaId(int row, int column) {
        switch (column){
            case 0:
            case 1:
            case 2:
                if (row < assets.size()){
                    return assets.get(row).getSchemaId();
                } else {
                    return null;
                }
            case 3:
            case 4:
            case 5:
                if (row < liabilities.size()){
                    return liabilities.get(row).getSchemaId();
                } else {
                    return null;
                }
            default: return null;
        }
    }

    @Override
    public String getColumnName(int column) {
        return "";
    }
}
