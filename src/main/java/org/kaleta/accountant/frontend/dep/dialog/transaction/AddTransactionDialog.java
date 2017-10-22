package org.kaleta.accountant.frontend.dep.dialog.transaction;

import org.kaleta.accountant.backend.entity.Transaction;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.IconLoader;
import org.kaleta.accountant.frontend.dialog.Dialog;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Created by Stanislav Kaleta on 24.05.2016.
 */
public class AddTransactionDialog extends Dialog {
    private TransactionManagementPanel transactionManagementPanel;

    public AddTransactionDialog(Configuration parent) {
        super((Component) parent, "Adding Transaction(s)");
        transactionManagementPanel = new TransactionManagementPanel(parent);
        buildDialog();
        transactionManagementPanel.addNewTransaction(true);
        this.pack();
    }

    @Override
    protected void buildDialog() {
        JButton buttonCancel = new JButton("Cancel");
        buttonCancel.addActionListener(a -> dispose());

        JButton buttonOk = new JButton("Add");
        buttonOk.addActionListener(a -> {
            result = true;
            dispose();
        });
        transactionManagementPanel.setOkButton(buttonOk);

        JButton buttonAddTr = new JButton(IconLoader.getIcon(IconLoader.ADD, "Add New Transaction", new Dimension(20,20)));
        buttonAddTr.addActionListener(l -> {
            transactionManagementPanel.addNewTransaction(true);
            buttonOk.setEnabled(true);
            this.pack();
        });

        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGap(10)
                .addGroup(layout.createParallelGroup()
                        .addComponent(transactionManagementPanel)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(20).addComponent(buttonAddTr,50,50,50)
                                .addGap(5, 5, Short.MAX_VALUE)
                                .addComponent(buttonCancel).addGap(5).addComponent(buttonOk).addGap(5)))
                .addGap(10));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGap(10)
                .addComponent(transactionManagementPanel)
                .addGap(5)
                .addGroup(layout.createParallelGroup().addComponent(buttonAddTr,25,25,25).addComponent(buttonCancel,25,25,25).addComponent(buttonOk,25,25,25))
                .addGap(10));
    }

    public List<Transaction> getCreatedTransactions(){
        return transactionManagementPanel.getTransactions();
    }
}
