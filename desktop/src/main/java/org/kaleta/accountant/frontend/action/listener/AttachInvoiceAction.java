package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.Edt;
import org.kaleta.accountant.frontend.component.SelectFileTextField;
import org.kaleta.accountant.service.Service;

import java.awt.Frame;
import java.io.File;

/**
 * Asks for the invoice of an asset and files it away under the asset.
 */
public class AttachInvoiceAction extends ActionListener {
    private final AccountsModel.Account account;

    public AttachInvoiceAction(Configurable configurable, AccountsModel.Account account) {
        super(configurable);
        this.account = account;
    }

    @Override
    protected void actionPerformed() {
        // the chooser goes to the event thread, the copying stays here on the worker: a file
        // chooser built on a worker thread deadlocks against its own directory scan
        File file = Edt.get(() -> SelectFileTextField.chooseFile((Frame) getConfiguration()));
        if (file == null) {
            return;
        }
        Service.INVOICE.attachInvoice(getConfiguration().getSelectedYear(), account, file);
        getConfiguration().update(Configuration.ACCOUNT_UPDATED);
    }
}
