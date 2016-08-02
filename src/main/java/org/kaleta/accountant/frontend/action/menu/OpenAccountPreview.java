package org.kaleta.accountant.frontend.action.menu;

import org.kaleta.accountant.backend.entity.Transaction;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.dialog.account.AccountModel;
import org.kaleta.accountant.frontend.dialog.account.AccountPreviewDialog;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stanislav Kaleta on 03.06.2016.
 */
public class OpenAccountPreview extends MenuAction {
    public OpenAccountPreview(Configuration config) {
        super(config, "Account Preview");
    }

    @Override
    protected void actionPerformed() {
        String schemaId = JOptionPane.showInputDialog((Component) getConfiguration(),"Schema ID:");
        if (schemaId == null){
            return;
        }
        String accType = Service.ACCOUNT.getAccountType(schemaId);
        String accName = Service.ACCOUNT.getAccountName(schemaId);
        List<Transaction> transactionList = Service.JOURNAL.listAccountTransactions(schemaId, 2016);
        // TODO: 6/3/16 get year
        List<Transaction> trDebit = new ArrayList<>();
        List<Transaction> trCredit = new ArrayList<>();
        for (Transaction transaction : transactionList){
            if (transaction.getDebit().startsWith(schemaId)){
                trDebit.add(transaction);
            }
            if (transaction.getCredit().startsWith(schemaId)){
                trCredit.add(transaction);
            }
        }
        AccountModel model = new AccountModel(accType, accName, trDebit, trCredit, 0);
        // TODO: 6/3/16 get INITIAL STATE
        AccountPreviewDialog dialog = new AccountPreviewDialog((Component) getConfiguration(), model);
        dialog.setVisible(true);
    }
}
