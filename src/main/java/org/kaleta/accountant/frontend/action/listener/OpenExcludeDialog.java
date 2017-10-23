package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.dialog.ExcludeDialog;

import java.awt.*;

public class OpenExcludeDialog extends ActionListener {
    private AccountsModel.Account account;

    public OpenExcludeDialog(Configurable configurable, AccountsModel.Account account) {
        super(configurable);
        this.account = account;
    }

    @Override
    protected void actionPerformed() {
        ExcludeDialog dialog = new ExcludeDialog((Frame) getConfiguration());
        dialog.setVisible(true);
        if (dialog.getResult()) {


            //getConfiguration().update(Configuration.TRANSACTION_UPDATED);
        }
    }
}
