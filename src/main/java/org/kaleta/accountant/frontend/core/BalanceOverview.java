package org.kaleta.accountant.frontend.core;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.BalanceRow;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BalanceOverview extends JPanel implements Configurable {
    private Configuration configuration;
    private BalanceTableModel tableModel;

    public BalanceOverview() {
        tableModel = new BalanceTableModel();
        JTable table = new JTable();
        table.setModel(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.setPreferredScrollableViewportSize(new Dimension(1200,800));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(30);

        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setMinWidth(320);
        columnModel.getColumn(1).setMinWidth(140);
        columnModel.getColumn(1).setMaxWidth(140);
        columnModel.getColumn(2).setMinWidth(140);
        columnModel.getColumn(2).setMaxWidth(140);

        columnModel.getColumn(3).setMinWidth(320);
        columnModel.getColumn(4).setMinWidth(140);
        columnModel.getColumn(4).setMaxWidth(140);
        columnModel.getColumn(5).setMinWidth(140);
        columnModel.getColumn(5).setMaxWidth(140);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String type = ((BalanceTableModel) table.getModel()).getCellType(row, column);
                if (type.equals("")){
                    c.setBackground(null);
                }
                if (type.equals(BalanceRow.SUM) || type.equals(BalanceRow.CLASS)){
                    c.setFont(new Font(c.getFont().getName(), Font.BOLD, 25));
                    c.setBackground(Color.LIGHT_GRAY.darker());
                }
                if (type.equals(BalanceRow.GROUP)){
                    c.setFont(new Font(c.getFont().getName(), Font.PLAIN, 20));
                    c.setBackground(Color.LIGHT_GRAY);
                }
                if (type.equals(BalanceRow.ACCOUNT)){
                    c.setFont(new Font(c.getFont().getName(), Font.PLAIN, 15));
                    c.setBackground(Color.WHITE);
                }
                if (column == 0 || column == 3){
                    setHorizontalAlignment(JLabel.LEFT);
                    setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
                } else {
                    setHorizontalAlignment(JLabel.RIGHT);
                    setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
                }
                return c;
            }
        });

        this.getActionMap().put(Configuration.SCHEMA_UPDATED, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableModel.updateModel();
            }
        });
        this.getActionMap().put(Configuration.ACCOUNT_UPDATED, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableModel.updateModel();
            }
        });
        this.getActionMap().put(Configuration.TRANSACTION_UPDATED, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableModel.updateModel();
            }
        });

        this.setLayout(new GridLayout(1,1));
        this.add(table);
    }

    public void update(){
        tableModel.updateModel();
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    private class BalanceTableModel extends AbstractTableModel {
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

        public void updateModel(){
            String year = getConfiguration().getSelectedYear();
            Map<String, List<AccountsModel.Account>> accountMap =  Service.ACCOUNT.getAccountsViaSchemaMap(year);

            assets.clear();
            int assetsBalance = 0;
            int assetsTurnover = 0;
            for (SchemaModel.Class clazz : Service.SCHEMA.getSchemaClassListByAccountType(year, Constants.AccountType.ASSET)){
                List<BalanceRow> groups = new ArrayList<>();
                int classBalance = 0;
                int classTurnover = 0;
                for (SchemaModel.Class.Group group : clazz.getGroup()) {
                    List<BalanceRow> accounts = new ArrayList<>();
                    int groupBalance = 0;
                    int groupTurnover = 0;
                    for (SchemaModel.Class.Group.Account schemaAccount : group.getAccount()) {
                        int accBalance = 0;
                        int accTurnover = 0;
                        String schemaId = clazz.getId() + group.getId() + schemaAccount.getId();
                        if (accountMap.get(schemaId) != null) {
                            for (AccountsModel.Account account : accountMap.get(schemaId)){
                                accTurnover += Integer.parseInt(Service.ACCOUNT.getAccountTurnover(year, account));
                                accBalance += Integer.parseInt(Service.ACCOUNT.getAccountBalance(year, account));
                            }
                        }
                        accounts.add(new BalanceRow(schemaAccount.getName(), String.valueOf(accTurnover), String.valueOf(accBalance), schemaId, BalanceRow.ACCOUNT));
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
            for (SchemaModel.Class clazz : Service.SCHEMA.getSchemaClassListByAccountType(year, Constants.AccountType.LIABILITY)){
                List<BalanceRow> groups = new ArrayList<>();
                int classBalance = 0;
                int classTurnover = 0;
                for (SchemaModel.Class.Group group : clazz.getGroup()) {
                    List<BalanceRow> accounts = new ArrayList<>();
                    int groupBalance = 0;
                    int groupTurnover = 0;
                    for (SchemaModel.Class.Group.Account schemaAccount : group.getAccount()) {
                        int accBalance = 0;
                        int accTurnover = 0;
                        String schemaId = clazz.getId() + group.getId() + schemaAccount.getId();
                        if (accountMap.get(schemaId) != null) {
                            for (AccountsModel.Account account : accountMap.get(schemaId)){
                                accTurnover += Integer.parseInt(Service.ACCOUNT.getAccountTurnover(year, account));
                                accBalance += Integer.parseInt(Service.ACCOUNT.getAccountBalance(year, account));
                            }
                        }
                        accounts.add(new BalanceRow(schemaAccount.getName(), String.valueOf(accTurnover), String.valueOf(accBalance), schemaId, BalanceRow.ACCOUNT));
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

        @Override
        public String getColumnName(int column) {
            return "";
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
    }
}
