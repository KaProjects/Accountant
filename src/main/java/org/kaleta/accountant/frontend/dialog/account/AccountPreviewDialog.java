package org.kaleta.accountant.frontend.dialog.account;

import org.kaleta.accountant.backend.constants.AccountType;
import org.kaleta.accountant.backend.entity.Transaction;
import org.kaleta.accountant.frontend.dialog.Dialog;

import javax.swing.*;
import java.awt.Component;

/**
 * Created by Stanislav Kaleta on 30.05.2016.
 */
public class AccountPreviewDialog extends Dialog {
    // TODO: 5/30/16 nacitat vsetky transakcie kde vystupuje dany acc. schemovy(nie semanticky - cez checkboxy kt. ukazat)
    // TODO: 5/30/16 PS, ma dati, dat + description, OBRAT, KS?
    private AccountModel model;

    public AccountPreviewDialog(Component parent, AccountModel model) {
        super(parent, "Account Preview - " + model.getName());
        this.model = model;
        buildDialog();
        pack();
    }

    @Override
    protected void buildDialog() {
        JPanel panelDebit = new JPanel();
        panelDebit.setLayout(new BoxLayout(panelDebit, BoxLayout.Y_AXIS));

        JSeparator sideSeparator = new JSeparator(JSeparator.VERTICAL);

        JPanel panelCredit = new JPanel();
        panelCredit.setLayout(new BoxLayout(panelCredit, BoxLayout.Y_AXIS));

        if (model.getType().equals(AccountType.ASSET)) {
            panelDebit.add(new AccountValueLabel(model.getInitialState(),"Initial State"));
            panelCredit.add(new AccountValueLabel());
        }
        if (model.getType().equals(AccountType.LIABILITY)) {
            panelDebit.add(new AccountValueLabel());
            panelCredit.add(new AccountValueLabel(model.getInitialState(),"Initial State"));
        }
        panelDebit.add(new JSeparator(JSeparator.HORIZONTAL));
        panelCredit.add(new JSeparator(JSeparator.HORIZONTAL));
        for (Transaction transaction : model.getTrDebit()) {
            panelDebit.add(new AccountValueLabel(transaction));
        }
        for (Transaction transaction : model.getTrCredit()) {
            panelCredit.add(new AccountValueLabel(transaction));
        }
        int debitTurnover = 0;
        for (Transaction transaction : model.getTrDebit()){
            debitTurnover += Integer.parseInt(transaction.getAmount());
        }
        panelDebit.add(new JSeparator(JSeparator.HORIZONTAL));
        panelDebit.add(new AccountValueLabel(debitTurnover, "Turnover"));
        int creditTurnover = 0;
        for (Transaction transaction : model.getTrCredit()){
            creditTurnover += Integer.parseInt(transaction.getAmount());
        }
        panelCredit.add(new JSeparator(JSeparator.HORIZONTAL));
        panelCredit.add(new AccountValueLabel(creditTurnover, "Turnover"));

        JButton buttonClose = new JButton("Close");
        buttonClose.addActionListener(l -> this.dispose());

        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGap(10)
                .addGroup(layout.createParallelGroup()
                        .addComponent(panelDebit)
                        .addComponent(sideSeparator)
                        .addComponent(panelCredit))
                .addGap(5)
                .addComponent(buttonClose)
                .addGap(10));
        layout.setHorizontalGroup(layout.createSequentialGroup()
            .addGap(10)
                .addGroup(layout.createParallelGroup()
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panelDebit)
                            .addGap(5)
                        .addComponent(sideSeparator,2,2,2)
                            .addGap(5)
                        .addComponent(panelCredit))
                    .addComponent(buttonClose))
            .addGap(10));
    }
}
