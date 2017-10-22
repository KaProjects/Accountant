package org.kaleta.accountant.frontend.dep.dialog.schema;

import org.kaleta.accountant.backend.entity.Schema;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.common.IconLoader;
import org.kaleta.accountant.frontend.common.NumberFilter;
import org.kaleta.accountant.frontend.dialog.Dialog;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stanislav Kaleta on 20.04.2016.
 */
class GroupDialog extends Dialog {
    private final Schema.Class.Group group;
    private final List<Schema.Class.Group.Account> accounts;

    public GroupDialog(Schema.Class.Group group, Component parent) {
        super(parent, "Group Editor");
        this.group = group;
        accounts = new ArrayList<>(group.getAccount());
        buildDialog();
        pack();
    }

    @Override
    protected void buildDialog() {
        JLabel labelId = new JLabel("Group ID:");
        JTextField tfId = new JTextField();
        ((PlainDocument) tfId.getDocument()).setDocumentFilter(new NumberFilter());
        tfId.setText(group.getId());

        JLabel labelName = new JLabel("Group Name:");
        JTextField tfName = new JTextField();
        tfName.setText(group.getName());

        JLabel labelTable = new JLabel("Accounts:");
        JTable tableAccounts = new JTable(new AbstractTableModel() {
            @Override
            public int getRowCount() {
                return accounts.size();
            }

            @Override
            public int getColumnCount() {
                return 3;
            }

            @Override
            public Object getValueAt(int r, int c) {
                switch (c) {
                    case 0:
                        return accounts.get(r).getId();
                    case 1:
                        switch (accounts.get(r).getType()) {
                            case Constants.AccountType.ASSET:return "Asset";
                            case Constants.AccountType.LIABILITY:return "Liability";
                            case Constants.AccountType.EXPENSE:return "Expense";
                            case Constants.AccountType.REVENUE:return "Revenue";
                            case Constants.AccountType.OFF_BALANCE:return "Off Balance";
                        }
                    case 2:
                        return accounts.get(r).getName();
                    default:
                        return null;
                }
            }
        });
        tableAccounts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableColumnModel columnModel = tableAccounts.getColumnModel();
        columnModel.getColumn(0).setMinWidth(15);
        columnModel.getColumn(0).setMaxWidth(15);
        columnModel.getColumn(1).setMinWidth(80);
        columnModel.getColumn(1).setMaxWidth(80);
        columnModel.getColumn(2).setMinWidth(105);
        columnModel.getColumn(2).setPreferredWidth(105);

        JButton buttonAddAcc = new JButton(IconLoader.getIcon(IconLoader.ADD, new Dimension(10, 10)));
        buttonAddAcc.addActionListener(l -> {
            Schema.Class.Group.Account newAcc = new Schema.Class.Group.Account();
            AccountDialog dialog = new AccountDialog(newAcc, this);
            dialog.setVisible(true);
            if (dialog.getResult()) {
                accounts.add(newAcc);
                tableAccounts.revalidate();
                tableAccounts.repaint();
                this.pack();
            }
        });
        JButton buttonEditAcc = new JButton(IconLoader.getIcon(IconLoader.EDIT, new Dimension(10, 10)));
        buttonEditAcc.addActionListener(l -> {
            int selectedRow = tableAccounts.getSelectedRow();
            if (selectedRow >= 0) {
                AccountDialog dialog = new AccountDialog(accounts.get(selectedRow), this);
                dialog.setVisible(true);
                if (dialog.getResult()) {
                    tableAccounts.revalidate();
                    tableAccounts.repaint();
                    this.pack();
                }
            }
        });
        JButton buttonRemoveAcc = new JButton(IconLoader.getIcon(IconLoader.DELETE, new Dimension(10, 10)));
        buttonRemoveAcc.addActionListener(l -> {
            int selectedRow = tableAccounts.getSelectedRow();
            if (selectedRow >= 0) {
                accounts.remove(selectedRow);
                tableAccounts.revalidate();
                tableAccounts.repaint();
                this.pack();
            }
        });
        JButton buttonOk = new JButton("OK");
        buttonOk.addActionListener(l -> {
            group.setId(tfId.getText());
            group.setName(tfName.getText());
            group.getAccount().clear();
            group.getAccount().addAll(accounts);
            result = true;
            this.dispose();
        });
        JButton buttonCancel = new JButton("Cancel");
        buttonCancel.addActionListener(l -> this.dispose());

        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGap(10)
                .addGroup(layout.createParallelGroup().addComponent(labelId).addComponent(tfId, 25, 25, 25))
                .addGap(5)
                .addGroup(layout.createParallelGroup().addComponent(labelName).addComponent(tfName, 25, 25, 25))
                .addGap(5)
                .addComponent(labelTable)
                .addComponent(tableAccounts)
                .addGroup(layout.createParallelGroup().addComponent(buttonRemoveAcc).addComponent(buttonEditAcc).addComponent(buttonAddAcc))
                .addGap(10)
                .addGroup(layout.createParallelGroup().addComponent(buttonCancel).addComponent(buttonOk))
                .addGap(10));
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGap(10)
                .addGroup(layout.createParallelGroup()
                        .addGroup(layout.createSequentialGroup().addComponent(labelId).addGap(5).addComponent(tfId))
                        .addGroup(layout.createSequentialGroup().addComponent(labelName).addGap(5).addComponent(tfName))
                        .addComponent(labelTable)
                        .addComponent(tableAccounts)
                        .addGroup(layout.createSequentialGroup().addComponent(buttonRemoveAcc).addGap(5).addComponent(buttonEditAcc).addGap(5).addComponent(buttonAddAcc))
                        .addGroup(layout.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE).addComponent(buttonCancel).addGap(5).addComponent(buttonOk)))
                .addGap(10));
    }
}
