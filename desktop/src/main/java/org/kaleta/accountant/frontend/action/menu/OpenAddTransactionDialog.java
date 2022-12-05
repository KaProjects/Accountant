package org.kaleta.accountant.frontend.action.menu;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.AccountPairModel;
import org.kaleta.accountant.frontend.component.TransactionPanel;
import org.kaleta.accountant.frontend.dialog.AddTransactionDialog;
import org.kaleta.accountant.service.Service;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OpenAddTransactionDialog extends MenuAction {

    public OpenAddTransactionDialog(Configuration config) {
        super(config, "Add Transaction(s)");
    }

    @Override
    protected void actionPerformed() {
        Map<String, List<AccountsModel.Account>> allAccountMap = Service.ACCOUNT.getAccountsViaSchemaMap(getConfiguration().getSelectedYear());
        List<SchemaModel.Class> classList = Service.SCHEMA.getSchemaClassList(getConfiguration().getSelectedYear());
        Map<AccountPairModel, Set<String>> accountPairDescriptionMap = Service.TRANSACTIONS.getAccountPairDescriptions(getConfiguration().getSelectedYear());

        AddTransactionDialog dialog = new AddTransactionDialog(getConfiguration(), accountPairDescriptionMap, allAccountMap, classList, null);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (dialog.getResult()){
                    for (TransactionPanel panel : dialog.getTransactionPanelList()){
                        Service.TRANSACTIONS.addTransaction(getConfiguration().getSelectedYear(),
                                panel.getDate(), panel.getAmount(), panel.getDebit(), panel.getCredit(), panel.getDescription());
                    }
                    getConfiguration().update(Configuration.TRANSACTION_UPDATED);
                }
            }
            @Override
            public void windowClosing(WindowEvent e) {
            }
        });

        dialog.setVisible(true);
    }
}
