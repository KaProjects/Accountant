package org.kaleta.accountant.frontend.dialog.transaction;

import org.kaleta.accountant.backend.entity.Journal;
import org.kaleta.accountant.frontend.dialog.Dialog;

import javax.swing.*;
import java.awt.Component;

/**
 * Created by Stanislav Kaleta on 24.05.2016.
 */
public class AddTransactionDialog extends Dialog {
    private JTextField tfDate;
    private JTextField tfDescription;
    private JTextField tfAmount;
    private JTextField tfDebit;
    private  JTextField tfCredit;

    public AddTransactionDialog(Component parent) {
        super(parent, "Adding Transaction");
        buildDialog();
        this.pack();
    }

    @Override
    protected void buildDialog() {
        JLabel labelDate = new JLabel("Date:");
        tfDate = new JTextField();

        JLabel labelDescription = new JLabel("Description:");
        tfDescription = new JTextField();

        JLabel labelAmount = new JLabel("Amount:");
        tfAmount = new JTextField();

        JLabel labelDebit = new JLabel("Debit:");
        tfDebit = new JTextField();

        JLabel labelCredit = new JLabel("Credit:");
        tfCredit = new JTextField();

        JButton buttonCancel = new JButton("Cancel");
        buttonCancel.addActionListener(a -> this.dispose());
        JButton buttonOk = new JButton("Add");
        buttonOk.addActionListener(a -> {
            result = true;
            dispose();
        });

        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                        .addGap(5).addGroup(layout.createParallelGroup().addComponent(labelDate).addComponent(tfDate,50,50,50))
                        .addGap(5).addGroup(layout.createParallelGroup().addComponent(labelDescription).addComponent(tfDescription,200,200,Short.MAX_VALUE))
                        .addGap(5).addGroup(layout.createParallelGroup().addComponent(labelAmount).addComponent(tfAmount,75,75,75))
                        .addGap(5).addGroup(layout.createParallelGroup().addComponent(labelDebit).addComponent(tfDebit,50,50,50))
                        .addGap(5).addGroup(layout.createParallelGroup().addComponent(labelCredit).addComponent(tfCredit,50,50,50))
                        .addGap(5))
                .addGroup(layout.createSequentialGroup()
                        .addGap(5, 5, Short.MAX_VALUE).addComponent(buttonCancel).addGap(5).addComponent(buttonOk).addGap(5)));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGap(5)
                .addGroup(layout.createParallelGroup()
                        .addGroup(layout.createSequentialGroup().addComponent(labelDate).addComponent(tfDate,25,25,25))
                        .addGroup(layout.createSequentialGroup().addComponent(labelDescription).addComponent(tfDescription,25,25,25))
                        .addGroup(layout.createSequentialGroup().addComponent(labelAmount).addComponent(tfAmount,25,25,25))
                        .addGroup(layout.createSequentialGroup().addComponent(labelDebit).addComponent(tfDebit,25,25,25))
                        .addGroup(layout.createSequentialGroup().addComponent(labelCredit).addComponent(tfCredit,25,25,25)))
                .addGap(5)
                .addGroup(layout.createParallelGroup()
                        .addComponent(buttonCancel).addComponent(buttonOk))
                .addGap(5));
    }

    public Journal.Transaction getTransaction(){
        Journal.Transaction transaction = new Journal.Transaction();
        transaction.setDate(tfDate.getText());
        transaction.setDescription(tfDescription.getText());
        transaction.setAmount(tfAmount.getText());
        transaction.setDebit(tfDebit.getText());
        transaction.setCredit(tfCredit.getText());
        return transaction;
    }
}
