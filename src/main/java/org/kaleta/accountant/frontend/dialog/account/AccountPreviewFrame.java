package org.kaleta.accountant.frontend.dialog.account;

import org.kaleta.accountant.backend.entity.Transaction;
import org.kaleta.accountant.frontend.common.constants.AccountType;

import javax.swing.*;
import java.awt.Component;
import java.awt.Dimension;

/**
 * Created by Stanislav Kaleta on 22.08.2016.
 */
public class AccountPreviewFrame extends JFrame {
    private AccountPreviewModel model;

    public AccountPreviewFrame(Component parent, AccountPreviewModel model){
        this.model = model;
        this.setLocationRelativeTo(parent);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setTitle(model.getAccountName());

        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.X_AXIS));
        for (String semantic : model.getSemanticNames()){
            JPanel panelD = new JPanel();
            panelD.setLayout(new BoxLayout(panelD, BoxLayout.Y_AXIS));
            JLabel labelId = new JLabel(model.getSemanticId(semantic));
            labelId.setToolTipText("Account ID");
            panelD.add(labelId);
            panelD.add(new JLabel("===="));
            int turnoverD = 0;
            for (Transaction transaction : model.getSemanticTransactions(semantic, true)){
                JLabel label = new JLabel(transaction.getAmount());
                label.setToolTipText(transaction.getDate() + " - " + transaction.getDescription());
                panelD.add(label);
                turnoverD += Integer.parseInt(transaction.getAmount());
            }
            panelD.add(new JLabel("------"));
            JLabel labelTurnoverD = new JLabel(String.valueOf(turnoverD));
            labelTurnoverD.setToolTipText("Debit Turnover");
            panelD.add(labelTurnoverD);
            panelD.add(new JLabel("------"));
            panelD.add(Box.createVerticalGlue());
            panelD.add(new JLabel("===="));

            JPanel panelC = new JPanel();
            panelC.setLayout(new BoxLayout(panelC, BoxLayout.Y_AXIS));
            JLabel labelName = new JLabel(semantic);
            labelName.setToolTipText("Account Name");
            panelC.add(labelName);
            panelC.add(new JLabel("===="));
            int turnoverC = 0;
            for (Transaction transaction : model.getSemanticTransactions(semantic, false)){
                JLabel label = new JLabel(transaction.getAmount());
                label.setToolTipText(transaction.getDate() + " - " + transaction.getDescription());
                panelC.add(label);
                turnoverC += Integer.parseInt(transaction.getAmount());
            }
            panelC.add(new JLabel("------"));
            JLabel labelTurnoverC = new JLabel(String.valueOf(turnoverC));
            labelTurnoverC.setToolTipText("Credit Turnover");
            panelC.add(labelTurnoverC);
            panelC.add(new JLabel("------"));
            panelC.add(Box.createVerticalGlue());
            panelC.add(new JLabel("===="));

            JLabel labelBalance = new JLabel();
            labelBalance.setToolTipText("Closing Balance");
            if (model.getAccountType().equals(AccountType.ASSET) || model.getAccountType().equals(AccountType.EXPENSE)){
                labelBalance.setText(String.valueOf(turnoverD - turnoverC));
                panelD.add(labelBalance);
                panelC.add(new JLabel(" "));
            } else {
                labelBalance.setText(String.valueOf(turnoverC - turnoverD));
                panelC.add(labelBalance);
                panelD.add(new JLabel(" "));
            }


            this.getContentPane().add(new JSeparator(JSeparator.VERTICAL));
            this.getContentPane().add(Box.createRigidArea(new Dimension(5,5)));
            this.getContentPane().add(panelD);
            this.getContentPane().add(Box.createRigidArea(new Dimension(5,5)));
            this.getContentPane().add(panelC);
            this.getContentPane().add(Box.createRigidArea(new Dimension(5,5)));
        }


        this.pack();
    }
}
