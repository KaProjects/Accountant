package org.kaleta.accountant.frontend.dialog.schema;

import org.kaleta.accountant.backend.entity.Schema;
import org.kaleta.accountant.frontend.common.NumberFilter;
import org.kaleta.accountant.frontend.common.constants.AccountType;
import org.kaleta.accountant.frontend.dialog.Dialog;

import javax.swing.*;
import javax.swing.text.PlainDocument;
import java.awt.Component;

/**
 * Created by Stanislav Kaleta on 20.04.2016.
 */
class AccountDialog extends Dialog {
    private final Schema.Class.Group.Account account;

    public AccountDialog(Schema.Class.Group.Account account, Component parent) {
        super(parent, "Account Editor");
        this.account = account;
        buildDialog();
        pack();
    }

    @Override
    protected void buildDialog() {
        JLabel labelId = new JLabel("Account ID:");
        JTextField tfId = new JTextField();
        ((PlainDocument) tfId.getDocument()).setDocumentFilter(new NumberFilter());
        tfId.setText(account.getId());

        JLabel labelName = new JLabel("Account Name:");
        JTextField tfName = new JTextField();
        tfName.setText(account.getName());

        JLabel labelType = new JLabel("Account Type:");
        JComboBox<String> cbType = new JComboBox<String>(new String[]{"Asset", "Liability", "Expense", "Revenue", "Off Balance"});
        switch (account.getType()) {
            case AccountType.ASSET:cbType.setSelectedIndex(0);break;
            case AccountType.LIABILITY:cbType.setSelectedIndex(1);break;
            case AccountType.EXPENSE:cbType.setSelectedIndex(2);break;
            case AccountType.REVENUE:cbType.setSelectedIndex(3);break;
            case AccountType.OFF_BALANCE:cbType.setSelectedIndex(4);break;
        }

        JButton buttonOk = new JButton("OK");
        buttonOk.addActionListener(l -> {
            account.setId(tfId.getText());
            account.setName(tfName.getText());
            switch (cbType.getSelectedIndex()) {
                case 0:account.setType(AccountType.ASSET);break;
                case 1:account.setType(AccountType.LIABILITY);break;
                case 2:account.setType(AccountType.EXPENSE);break;
                case 3:account.setType(AccountType.REVENUE);break;
                case 4:account.setType(AccountType.OFF_BALANCE);break;
            }
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
                .addGroup(layout.createParallelGroup().addComponent(labelType).addComponent(cbType, 25, 25, 25))
                .addGap(5)
                .addGroup(layout.createParallelGroup().addComponent(labelName).addComponent(tfName, 25, 25, 25))
                .addGap(10)
                .addGroup(layout.createParallelGroup().addComponent(buttonCancel).addComponent(buttonOk))
                .addGap(10));
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGap(10)
                .addGroup(layout.createParallelGroup()
                        .addGroup(layout.createSequentialGroup().addComponent(labelId).addGap(5).addComponent(tfId))
                        .addGroup(layout.createSequentialGroup().addComponent(labelType).addGap(5).addComponent(cbType))
                        .addGroup(layout.createSequentialGroup().addComponent(labelName).addGap(5).addComponent(tfName))
                        .addGroup(layout.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE).addComponent(buttonCancel).addGap(5).addComponent(buttonOk)))
                .addGap(10));
    }
}
