package org.kaleta.accountant.frontend.dialog.transaction;

import org.kaleta.accountant.backend.entity.Procedures;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.IconLoader;
import org.kaleta.accountant.frontend.dialog.Dialog;

import javax.swing.*;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stanislav Kaleta on 24.05.2016.
 */
public class CreateProcedureDialog extends Dialog {
    private List<TransactionPanel> transactionList;
    private JTextField textFieldName;
    private Configuration config;

    public CreateProcedureDialog(Configuration parent) {
        super((Component) parent, "Creating Procedure");
        transactionList = new ArrayList<>();
        config = parent;
        buildDialog();
        this.pack();
    }

    @Override
    protected void buildDialog() {
        JLabel labelName = new JLabel("Procedure Name: ");
        labelName.setFont(new Font(labelName.getFont().getName(),labelName.getFont().getStyle(), 15));
        textFieldName = new JTextField();

        JPanel transactionsPanel = new JPanel();
        transactionsPanel.setLayout(new BoxLayout(transactionsPanel, BoxLayout.Y_AXIS));
        TransactionPanel firstTransactionPanel = new TransactionPanel(config, true);
        firstTransactionPanel.toggleActive(false);
        transactionsPanel.add(firstTransactionPanel);
        transactionList.add(firstTransactionPanel);

        JButton buttonAdd = new JButton(IconLoader.getIcon(IconLoader.ADD));
        buttonAdd.addActionListener(a -> {
            TransactionPanel transactionPanel = new TransactionPanel(config, false);
            transactionPanel.toggleActive(false);
            transactionsPanel.add(transactionPanel);
            transactionList.add(transactionPanel);
            CreateProcedureDialog.this.pack();
        });

        JButton buttonCancel = new JButton("Cancel");
        buttonCancel.addActionListener(a -> this.dispose());
        JButton buttonOk = new JButton("Create");
        buttonOk.addActionListener(a -> {
            result = true;
            dispose();
        });

        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGap(10)
                .addGroup(layout.createParallelGroup()
                        .addGroup(layout.createSequentialGroup().addComponent(labelName).addComponent(textFieldName))
                        .addComponent(transactionsPanel)
                        .addComponent(buttonAdd)
                        .addGroup(layout.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE).addComponent(buttonCancel).addGap(5).addComponent(buttonOk).addGap(5)))
                .addGap(10));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGap(10)
                .addGroup(layout.createParallelGroup().addComponent(labelName,25,25,25).addComponent(textFieldName,25,25,25))
                .addGap(5)
                .addComponent(transactionsPanel)
                .addGap(5)
                .addComponent(buttonAdd)
                .addGap(5)
                .addGroup(layout.createParallelGroup().addComponent(buttonCancel).addComponent(buttonOk))
                .addGap(10));
    }

    public Procedures.Procedure getProcedure(){
        Procedures.Procedure procedure = new Procedures.Procedure();
        procedure.setName(textFieldName.getText());
        for (TransactionPanel transactionPanel : transactionList){
            procedure.getTransaction().add(transactionPanel.getTransaction());
        }
        return procedure;
    }
}
