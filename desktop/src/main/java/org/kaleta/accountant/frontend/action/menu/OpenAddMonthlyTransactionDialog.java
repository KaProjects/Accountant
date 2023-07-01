package org.kaleta.accountant.frontend.action.menu;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.ProceduresModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.AccountPairModel;
import org.kaleta.accountant.frontend.component.TransactionPanel;
import org.kaleta.accountant.frontend.dialog.AddTransactionDialog;
import org.kaleta.accountant.frontend.dialog.MonthlyTransactionSetupDialog;
import org.kaleta.accountant.service.Service;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OpenAddMonthlyTransactionDialog extends MenuAction {

    public OpenAddMonthlyTransactionDialog(Configuration config) {
        super(config, "Add Monthly Transaction");
    }

    @Override
    protected void actionPerformed() {
        Map<String, List<AccountsModel.Account>> allAccountMap = Service.ACCOUNT.getAccountsViaSchemaMap(getConfiguration().getSelectedYear());
        List<SchemaModel.Class> classList = Service.SCHEMA.getSchemaClassList(getConfiguration().getSelectedYear());

        MonthlyTransactionSetupDialog setupDialog = new MonthlyTransactionSetupDialog(getConfiguration(), allAccountMap, classList);

        setupDialog.setVisible(true);
        if (setupDialog.getResult()) {
            Map<AccountPairModel, Set<String>> accountPairDescriptionMap = Service.TRANSACTIONS.getAccountPairDescriptions(getConfiguration().getSelectedYear());

            AddTransactionDialog dialog = new AddTransactionDialog(getConfiguration(), accountPairDescriptionMap, allAccountMap, classList, new ProceduresModel.Group.Procedure());

            int yearlyAmount = Integer.parseInt(setupDialog.getYearlyAmount());
            int monthlyAmount = yearlyAmount / 12;
            int residual = yearlyAmount % 12;
            for(int i = 1; i <= 12; i++) {
                int amount = monthlyAmount + ((i == 1) ? residual : 0);
                String date = setupDialog.getDayOfMonth() + String.format("%02d", i);

                dialog.addTransactionPanel(transactionPanel -> {
                    transactionPanel.setDebit(setupDialog.getDebit());
                    transactionPanel.setCredit(setupDialog.getCredit());
                    transactionPanel.setDescription(setupDialog.getDescription());
                    transactionPanel.setAmount(String.valueOf(amount));
                    transactionPanel.setDate(date);
                });
            }

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
}
