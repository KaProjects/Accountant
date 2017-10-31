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

public class ProfitOverview extends JPanel implements Configurable {
    private Configuration configuration;
    private ProfitTableModel tableModel;

    public ProfitOverview() {
        tableModel = new ProfitTableModel();
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
        columnModel.getColumn(0).setMinWidth(350);
        columnModel.getColumn(1).setMinWidth(140);
        columnModel.getColumn(1).setMaxWidth(140);
        columnModel.getColumn(2).setMinWidth(350);
        columnModel.getColumn(3).setMinWidth(140);
        columnModel.getColumn(3).setMaxWidth(140);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String type = ((ProfitTableModel) table.getModel()).getCellType(row, column);
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
                if (column == 0 || column == 2){
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
        this.add(table);    }

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

    private class ProfitTableModel extends AbstractTableModel {
        private java.util.List<BalanceRow> expenses;
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

        public void updateModel(){
            String year = getConfiguration().getSelectedYear();
            Map<String, List<AccountsModel.Account>> accountMap =  Service.ACCOUNT.getAccountsViaSchemaMap(year);

            expenses.clear();
            for (SchemaModel.Class clazz : Service.SCHEMA.getSchemaClassListByAccountType(year, Constants.AccountType.EXPENSE)){
                List<BalanceRow> groups = new ArrayList<>();
                int classBalance = 0;
                for (SchemaModel.Class.Group group : clazz.getGroup()) {
                    List<BalanceRow> accounts = new ArrayList<>();
                    int groupBalance = 0;
                    for (SchemaModel.Class.Group.Account schemaAccount : group.getAccount()) {
                        int accBalance = 0;
                        String schemaId = clazz.getId() + group.getId() + schemaAccount.getId();
                        if (accountMap.get(schemaId) != null) {
                            for (AccountsModel.Account account : accountMap.get(schemaId)){
                                accBalance += Integer.parseInt(Service.ACCOUNT.getAccountBalance(year, account));
                            }
                        }
                        accounts.add(new BalanceRow(schemaAccount.getName(), "T", String.valueOf(accBalance), schemaId, BalanceRow.ACCOUNT));
                        groupBalance += accBalance;
                    }
                    groups.add(new BalanceRow(group.getName(), "T", String.valueOf(groupBalance), BalanceRow.GROUP));
                    groups.addAll(accounts);
                    classBalance += groupBalance;
                }
                expenses.add(new BalanceRow(clazz.getName(), "T", String.valueOf(classBalance), BalanceRow.CLASS));
                expenses.addAll(groups);
            }

            revenues.clear();
            for (SchemaModel.Class clazz : Service.SCHEMA.getSchemaClassListByAccountType(year, Constants.AccountType.REVENUE)){
                List<BalanceRow> groups = new ArrayList<>();
                int classBalance = 0;
                for (SchemaModel.Class.Group group : clazz.getGroup()) {
                    List<BalanceRow> accounts = new ArrayList<>();
                    int groupBalance = 0;
                    for (SchemaModel.Class.Group.Account schemaAccount : group.getAccount()) {
                        int accBalance = 0;
                        String schemaId = clazz.getId() + group.getId() + schemaAccount.getId();
                        if (accountMap.get(schemaId) != null) {
                            for (AccountsModel.Account account : accountMap.get(schemaId)){
                                accBalance += Integer.parseInt(Service.ACCOUNT.getAccountBalance(year, account));
                            }
                        }
                        accounts.add(new BalanceRow(schemaAccount.getName() ,"T", String.valueOf(accBalance), schemaId, BalanceRow.ACCOUNT));
                        groupBalance += accBalance;
                    }
                    groups.add(new BalanceRow(group.getName(), "T", String.valueOf(groupBalance), BalanceRow.GROUP));
                    groups.addAll(accounts);
                    classBalance += groupBalance;
                }
                revenues.add(new BalanceRow(clazz.getName(), "T", String.valueOf(classBalance), BalanceRow.CLASS));
                revenues.addAll(groups);
            }
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

        public String getCellSchemaId(int row, int column) {
            switch (column){
                case 0:
                case 1:
                    if (row < expenses.size()){
                        return expenses.get(row).getSchemaId();
                    } else {
                        return null;
                    }
                case 2:
                case 3:
                    if (row < revenues.size()){
                        return revenues.get(row).getSchemaId();
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
}
