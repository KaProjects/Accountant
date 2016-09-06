package org.kaleta.accountant.frontend.action.menu;

import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.dialog.transaction.AddTransactionDialog;
import org.kaleta.accountant.service.Service;

/**
 * Created by Stanislav Kaleta on 24.05.2016.
 */
public class OpenAddTransactionDialog extends MenuAction {
    public OpenAddTransactionDialog(Configuration config) {
        super(config, "Add Transaction");
    }

    @Override
    protected void actionPerformed() {
        AddTransactionDialog dialog = new AddTransactionDialog(getConfiguration());
        dialog.setVisible(true);
        if (dialog.getResult()) {
            Service.JOURNAL.addTransaction(dialog.getTransaction(), getConfiguration().getActiveYear());
            getConfiguration().update(Configuration.TRANSACTION_ACTION);
        }
    }
}
