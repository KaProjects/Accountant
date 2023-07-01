package org.kaleta.accountant.frontend.action.menu;

import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.component.TransactionPanel;
import org.kaleta.accountant.frontend.dialog.ImportTransactionsFromAndroidDialog;
import org.kaleta.accountant.service.Service;

public class OpenImportTransactionsFromAndroidDialog extends MenuAction {

    public OpenImportTransactionsFromAndroidDialog(Configuration config) {
        super(config, "Android Transaction(s)");
    }

    @Override
    protected void actionPerformed() {
        ImportTransactionsFromAndroidDialog dialog = new ImportTransactionsFromAndroidDialog(getConfiguration());
        dialog.setVisible(true);
        if (dialog.getResult()) {
            for (TransactionPanel panel : dialog.getTransactionPanelList()) {
                String year = getConfiguration().getSelectedYear();
                Service.TRANSACTIONS.addTransaction(year, panel.getDate(), panel.getAmount(), panel.getDebit(), panel.getCredit(), panel.getDescription());
                Service.FIREBASE.addAccountsIfMissing(year, panel.getDebit(), panel.getCredit());
            }
            getConfiguration().update(Configuration.TRANSACTION_UPDATED);

            Service.FIREBASE.clearLoadedTransactions();
        }
    }
}
