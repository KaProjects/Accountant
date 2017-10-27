package org.kaleta.accountant.frontend.dep.dialog.transaction;

import org.kaleta.accountant.backend.entity.Procedures;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.IconLoader;
import org.kaleta.accountant.frontend.dialog.Dialog;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Stanislav Kaleta on 24.05.2016.
 */
public class CreateProcedureDialog extends Dialog {
    private TransactionManagementPanel transactionManagementPanel;
    private JTextField textFieldName;

    public CreateProcedureDialog(Configuration parent) {
        super((Frame) parent, "Creating Procedure");
        transactionManagementPanel = new TransactionManagementPanel(parent);
        buildDialog();
        transactionManagementPanel.addNewTransaction(false);
        this.pack();
    }

    @Override
    protected void buildDialog() {
        JButton buttonOk = new JButton("Create"){
            @Override
            public void setText(String text) {
                super.setText("Create");
            }
        };
        buttonOk.addActionListener(a -> {
            result = true;
            dispose();
        });
        transactionManagementPanel.setOkButton(buttonOk);

        JLabel labelName = new JLabel("Procedure Name: ");
        labelName.setFont(new Font(labelName.getFont().getName(),labelName.getFont().getStyle(), 15));
        textFieldName = new JTextField();

        JButton buttonAddTr = new JButton(IconLoader.getIcon(IconLoader.ADD, "Add New Transaction", new Dimension(20,20)));
        buttonAddTr.addActionListener(l -> {
            transactionManagementPanel.addNewTransaction(false);
            buttonOk.setEnabled(true);
            buttonOk.setText("Create");
            this.pack();
        });

        JButton buttonCancel = new JButton("Cancel");
        buttonCancel.addActionListener(a -> this.dispose());


        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGap(10)
                .addGroup(layout.createParallelGroup()
                        .addGroup(layout.createSequentialGroup().addComponent(labelName).addComponent(textFieldName))
                        .addComponent(transactionManagementPanel)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(20).addComponent(buttonAddTr,50,50,50)
                                .addGap(5, 5, Short.MAX_VALUE)
                                .addComponent(buttonCancel).addGap(5).addComponent(buttonOk).addGap(5)))
                .addGap(10));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGap(10)
                .addGroup(layout.createParallelGroup().addComponent(labelName,25,25,25).addComponent(textFieldName,25,25,25))
                .addGap(5)
                .addComponent(transactionManagementPanel)
                .addGap(5)
                .addGroup(layout.createParallelGroup().addComponent(buttonAddTr,25,25,25).addComponent(buttonCancel,25,25,25).addComponent(buttonOk,25,25,25))
                .addGap(10));
    }

    public Procedures.Procedure getProcedure(){
        Procedures.Procedure procedure = new Procedures.Procedure();
        procedure.setName(textFieldName.getText());
        procedure.getTransaction().addAll(transactionManagementPanel.getTransactions());
        return procedure;
    }
}
