package org.kaleta.accountant.frontend.dialog.procedure;

import org.kaleta.accountant.backend.entity.Transaction;

import javax.swing.*;

/**
 * Created by Stanislav Kaleta on 24.05.2016.
 */
public class TransactionPanel extends JPanel {
    private JTextField tfDate;
    private JTextField tfDescription;
    private JTextField tfAmount;
    private JTextField tfDebit;
    private  JTextField tfCredit;
    private boolean labeled;

    public TransactionPanel(boolean labeled){
        JLabel labelDate = new JLabel("Date:");
        labelDate.setVisible(labeled);
        tfDate = new JTextField();

        JLabel labelDescription = new JLabel("Description:");
        labelDescription.setVisible(labeled);
        tfDescription = new JTextField();

        JLabel labelAmount = new JLabel("Amount:");
        labelAmount.setVisible(labeled);
        tfAmount = new JTextField();

        JLabel labelDebit = new JLabel("Debit:");
        labelDebit.setVisible(labeled);
        tfDebit = new JTextField();

        JLabel labelCredit = new JLabel("Credit:");
        labelCredit.setVisible(labeled);
        tfCredit = new JTextField();

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGap(5).addGroup(layout.createParallelGroup().addComponent(labelDate).addComponent(tfDate,50,50,50))
                .addGap(5).addGroup(layout.createParallelGroup().addComponent(labelDescription).addComponent(tfDescription,200,200,Short.MAX_VALUE))
                .addGap(5).addGroup(layout.createParallelGroup().addComponent(labelAmount).addComponent(tfAmount,75,75,75))
                .addGap(5).addGroup(layout.createParallelGroup().addComponent(labelDebit).addComponent(tfDebit,50,50,50))
                .addGap(5).addGroup(layout.createParallelGroup().addComponent(labelCredit).addComponent(tfCredit,50,50,50))
                .addGap(5));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGap(5)
                .addGroup(layout.createParallelGroup()
                        .addGroup(layout.createSequentialGroup().addComponent(labelDate).addComponent(tfDate,25,25,25))
                        .addGroup(layout.createSequentialGroup().addComponent(labelDescription).addComponent(tfDescription,25,25,25))
                        .addGroup(layout.createSequentialGroup().addComponent(labelAmount).addComponent(tfAmount,25,25,25))
                        .addGroup(layout.createSequentialGroup().addComponent(labelDebit).addComponent(tfDebit,25,25,25))
                        .addGroup(layout.createSequentialGroup().addComponent(labelCredit).addComponent(tfCredit,25,25,25)))
                .addGap(5));
    }

    public Transaction getTransaction() {
        Transaction transaction = new Transaction();
        transaction.setDate(tfDate.getText());
        transaction.setDescription(tfDescription.getText());
        transaction.setAmount(tfAmount.getText());
        transaction.setDebit(tfDebit.getText());
        transaction.setCredit(tfCredit.getText());
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        tfDate.setText(transaction.getDate());
        tfDescription.setText(transaction.getDescription());
        tfAmount.setText(transaction.getAmount());
        tfDebit.setText(transaction.getDebit());
        tfCredit.setText(transaction.getCredit());

    }
}
