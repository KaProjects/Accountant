package org.kaleta.accountant.frontend.action.menu;

import org.kaleta.accountant.backend.entity.Transaction;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.dep.dialog.transaction.AddTransactionDialog;

/**
 * Created by Stanislav Kaleta on 24.05.2016.
 */
@Deprecated
public class OpenAddTransactionDialog extends MenuAction {
    public OpenAddTransactionDialog(Configuration config) {
        super(config, "Add Transaction");
    }

    @Override
    protected void actionPerformed() {
        AddTransactionDialog dialog = new AddTransactionDialog(getConfiguration());
        dialog.setVisible(true);
        if (dialog.getResult()) {
            for (Transaction transaction : dialog.getCreatedTransactions()){
                //todo Service.JOURNAL.addTransaction(transaction, getConfiguration().getActiveYear());
            }
            getConfiguration().update(Configuration.TRANSACTION_UPDATED);
        }
    }
}
