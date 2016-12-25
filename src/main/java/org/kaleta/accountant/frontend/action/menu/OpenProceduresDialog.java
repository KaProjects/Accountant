package org.kaleta.accountant.frontend.action.menu;

import org.kaleta.accountant.backend.entity.Transaction;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.dialog.transaction.ProceduresDialog;
import org.kaleta.accountant.service.Service;

/**
 * Created by Stanislav Kaleta on 30.05.2016.
 */
public class OpenProceduresDialog extends MenuAction {
    public OpenProceduresDialog(Configuration config) {
        super(config, "Procedures");
    }

    @Override
    protected void actionPerformed() {
        ProceduresDialog dialog = new ProceduresDialog(getConfiguration());
        dialog.setVisible(true);
        if (dialog.getResult()) {
            for (Transaction transaction : dialog.getCreatedTransactions()){
                Service.JOURNAL.addTransaction(transaction, getConfiguration().getActiveYear());
            }
            getConfiguration().update(Configuration.TRANSACTION_ACTION);
        }
    }
}
