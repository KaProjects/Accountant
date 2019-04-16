package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.dialog.BuySellFinAssetDialog;
import org.kaleta.accountant.frontend.dialog.RevalueDialog;
import org.kaleta.accountant.service.Service;

import javax.swing.*;

public class OpenFinAssetDialog extends ActionListener {
    public static final int REVALUE = 0;
    public static final int BUY = 1;
    public static final int SELL = 2;

    private final AccountsModel.Account account;
    private final int actionType;

    public OpenFinAssetDialog(Configurable configurable, AccountsModel.Account account, int actionType) {
        super(configurable);
        this.account = account;
        this.actionType = actionType;
    }

    @Override
    protected void actionPerformed() {
        switch (actionType) {
            case REVALUE: {
                handleRevaluation();
                break;
            }
            case BUY: {
                handleBuying();
                break;
            }
            case SELL: {
                handleSelling();
                break;
            }
        }
    }

    private void handleRevaluation() {
        String year = getConfiguration().getSelectedYear();
        Integer currentValue = Service.TRANSACTIONS.getAccountBalance(year, account);

        RevalueDialog dialog = new RevalueDialog(getConfiguration(), account.getName(), String.valueOf(currentValue));
        dialog.setVisible(true);
        if (dialog.getResult()) {
            String date = dialog.getDate();
            Integer diff = Integer.parseInt(dialog.getNewValue()) - currentValue;
            if (diff == 0) {
                JOptionPane.showMessageDialog(dialog, "New value is the same as current value - no action required!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            } else {
                if (diff > 0) {
                    Service.TRANSACTIONS.addTransaction(year, date, String.valueOf(diff), account.getFullId(),
                            Service.ACCOUNT.getFinRevRevaluationAccountId(account.getSchemaId(), account.getSemanticId()), "Revaluation of " + account.getName());
                } else {
                    Service.TRANSACTIONS.addTransaction(year, date, String.valueOf(Math.abs(diff)),
                            Service.ACCOUNT.getFinExpRevaluationAccountId(account.getSchemaId(), account.getSemanticId()), account.getFullId(), "Revaluation of " + account.getName());
                }
                getConfiguration().update(Configuration.TRANSACTION_UPDATED);
            }
        }
    }

    private void handleBuying() {
        BuySellFinAssetDialog dialog = new BuySellFinAssetDialog(getConfiguration(), account.getName(), true);
        dialog.setVisible(true);
        if (dialog.getResult()) {
            Service.TRANSACTIONS.addTransaction(getConfiguration().getSelectedYear(), dialog.getDate(), dialog.getValue(),
                    Service.ACCOUNT.getFinCreationAccountId(account.getSchemaId(), account.getSemanticId()), dialog.getAccount(), "Creation of " + account.getName());

            getConfiguration().update(Configuration.TRANSACTION_UPDATED);
        }
    }

    private void handleSelling() {
        BuySellFinAssetDialog dialog = new BuySellFinAssetDialog(getConfiguration(), account.getName(), false);
        dialog.setVisible(true);
        if (dialog.getResult()) {
            Service.TRANSACTIONS.addTransaction(getConfiguration().getSelectedYear(), dialog.getDate(), dialog.getValue(),
                    dialog.getAccount(), account.getFullId(), "Sale of " + account.getName());

            getConfiguration().update(Configuration.TRANSACTION_UPDATED);
        }
    }
}
