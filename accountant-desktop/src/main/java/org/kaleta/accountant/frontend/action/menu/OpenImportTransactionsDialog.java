package org.kaleta.accountant.frontend.action.menu;

import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.component.TransactionPanel;
import org.kaleta.accountant.frontend.dialog.ImportTransactionsDialog;
import org.kaleta.accountant.service.Service;

public class OpenImportTransactionsDialog extends MenuAction {

    public OpenImportTransactionsDialog(Configuration config) {
        super(config, "Import Transaction(s)");
    }

    @Override
    protected void actionPerformed() {
        ImportTransactionsDialog dialog = new ImportTransactionsDialog(getConfiguration(), Service.FIREBASE.loadTransactions());
        dialog.setVisible(true);
        if (dialog.getResult()) {
            for (TransactionPanel panel : dialog.getTransactionPanelList()) {
                Service.TRANSACTIONS.addTransaction(getConfiguration().getSelectedYear(),
                        panel.getDate(), panel.getAmount(), panel.getDebit(), panel.getCredit(), panel.getDescription());
            }
            getConfiguration().update(Configuration.TRANSACTION_UPDATED);

            Service.FIREBASE.clearLoadedTransactions();
        }
    }
}
