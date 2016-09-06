package org.kaleta.accountant.frontend.dialog.transaction;

import org.kaleta.accountant.backend.entity.Procedures;
import org.kaleta.accountant.backend.entity.Transaction;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.dialog.Dialog;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Stanislav Kaleta on 30.05.2016.
 */
public class ProceduresDialog extends Dialog {
    private Procedures procedures;
    private List<TransactionPanel> transactionList;
    private Configuration config;

    public ProceduresDialog(Configuration parent) {
        super((Component) parent, "Procedures");
        procedures = Service.ACCOUNT.getProcedures();
        transactionList = new ArrayList<>();
        config = parent;
        buildDialog();
        this.pack();
    }

    @Override
    protected void buildDialog() {
        JComboBox<String> comboBoxType = new JComboBox<>();
        for (Procedures.Procedure procedure : procedures.getProcedure()){
            ((DefaultComboBoxModel<String>) comboBoxType.getModel()).addElement(procedure.getName());
        }
        comboBoxType.setSelectedIndex(-1);

        JPanel transactionsPanel = new JPanel();
        transactionsPanel.setLayout(new BoxLayout(transactionsPanel, BoxLayout.Y_AXIS));

        comboBoxType.addActionListener(actionEvent -> {
            transactionList.clear();
            transactionsPanel.removeAll();
            transactionsPanel.repaint();
            Procedures.Procedure procedure = procedures.getProcedure().get(comboBoxType.getSelectedIndex());
            boolean first = true;
            for (Transaction transaction : procedure.getTransaction()){
                TransactionPanel transactionPanel = new TransactionPanel(config, first);
                first = false;
                transactionPanel.setTransaction(transaction);
                transactionList.add(transactionPanel);
                transactionsPanel.add(transactionPanel);
            }
            transactionsPanel.revalidate();
            transactionsPanel.repaint();
            ProceduresDialog.this.pack();
        });

        JButton buttonCancel = new JButton("Cancel");
        buttonCancel.addActionListener(a -> {
            for (TransactionPanel transactionPanel : transactionList) {
                transactionPanel.toggleActive(false);
            }
            dispose();
        });
        JButton buttonOk = new JButton("Add");
        buttonOk.addActionListener(a -> {
            for (TransactionPanel transactionPanel : transactionList) {
                transactionPanel.toggleActive(false);
            }
            result = true;
            dispose();
        });

        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGap(10)
                .addGroup(layout.createParallelGroup()
                        .addComponent(comboBoxType)
                        .addComponent(transactionsPanel)
                        .addGroup(layout.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE).addComponent(buttonCancel).addGap(5).addComponent(buttonOk).addGap(5)))
                .addGap(10));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGap(10)
                .addComponent(comboBoxType)
                .addGap(5)
                .addComponent(transactionsPanel)
                .addGap(5)
                .addGroup(layout.createParallelGroup().addComponent(buttonCancel).addComponent(buttonOk))
                .addGap(10));
    }

    public List<Transaction> getCreatedTransactions(){
        return transactionList.stream().map(TransactionPanel::getTransaction).collect(Collectors.toList());
    }
}
