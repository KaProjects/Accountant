package org.kaleta.accountant.frontend.dialog.transaction;

import org.kaleta.accountant.backend.entity.Transaction;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.dialog.Dialog;

import javax.swing.*;
import java.awt.Component;

/**
 * Created by Stanislav Kaleta on 24.05.2016.
 */
public class AddTransactionDialog extends Dialog {
    private TransactionPanel transactionPanel;
    private Configuration config;

    public AddTransactionDialog(Configuration parent) {
        super((Component) parent, "Adding Transaction");
        config = parent;
        buildDialog();
        this.pack();
    }

    @Override
    protected void buildDialog() {
        transactionPanel = new TransactionPanel(config, true);

        JButton buttonCancel = new JButton("Cancel");
        buttonCancel.addActionListener(a -> {
            transactionPanel.toggleActive(false);
            dispose();
        });
        JButton buttonOk = new JButton("Add");
        buttonOk.addActionListener(a -> {
            transactionPanel.toggleActive(false);
            result = true;
            dispose();
        });

        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(transactionPanel)
                .addGroup(layout.createSequentialGroup()
                        .addGap(5, 5, Short.MAX_VALUE).addComponent(buttonCancel).addGap(5).addComponent(buttonOk).addGap(5)));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGap(5)
                .addComponent(transactionPanel)
                .addGap(5)
                .addGroup(layout.createParallelGroup()
                        .addComponent(buttonCancel).addComponent(buttonOk))
                .addGap(5));
    }

    public Transaction getTransaction(){
        return transactionPanel.getTransaction();
    }
}
