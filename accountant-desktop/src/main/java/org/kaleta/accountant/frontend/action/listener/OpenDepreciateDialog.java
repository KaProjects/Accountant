package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.TransactionsModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.dialog.DepreciateDialog;
import org.kaleta.accountant.service.Service;

import java.util.ArrayList;
import java.util.List;

public class OpenDepreciateDialog extends ActionListener {
    private final List<AccountsModel.Account> accounts;

    public OpenDepreciateDialog(Configurable configurable, AccountsModel.Account account) {
        super(configurable);
        accounts = new ArrayList<>();
        accounts.add(account);
    }

    public OpenDepreciateDialog(Configurable configurable, List<AccountsModel.Account> accounts) {
        super(configurable);
        this.accounts = accounts;
    }

    @Override
    protected void actionPerformed() {
        List<DepreciateDialog.Config> configs = new ArrayList<>();
        String year = getConfiguration().getSelectedYear();
        for (AccountsModel.Account account : accounts){
            AccountsModel.Account accDepAccount = Service.ACCOUNT.getAccumulatedDepAccount(year, account);

            String monthlyDepHint = accDepAccount.getMetadata().split(",")[0];
            int assetValue = Service.TRANSACTIONS.getAccountBalance(year, account);
            int accDepSum = Service.TRANSACTIONS.getAccountBalance(year, accDepAccount);
            monthlyDepHint = ((assetValue-accDepSum) < Integer.parseInt(monthlyDepHint)) ? String.valueOf(assetValue-accDepSum) : monthlyDepHint;
            if (monthlyDepHint.equals("0")) monthlyDepHint = "x";

            String lastDepDate = Service.TRANSACTIONS.getLastDepreciationDate(year,account);
            if (lastDepDate == null){
                for (TransactionsModel.Transaction tr : Service.TRANSACTIONS.getTransactions(year, account.getFullId(), null)){
                    if (tr.getDescription().contains(Constants.Transaction.PURCHASE_DESCRIPTION)){
                        lastDepDate = tr.getDate();
                    }
                }
                if (lastDepDate == null) {
                    lastDepDate = String.format("%02d", Integer.parseInt(accDepAccount.getMetadata().split(",")[1])) + "00";
                }
            }
            String dateHint = (lastDepDate.substring(2,4).equals("12"))
                    ? "x"
                    : lastDepDate.substring(0,2) + String.format("%02d", (1 + Integer.parseInt(lastDepDate.substring(2,4))));

            boolean enabled = (!dateHint.equals("x") && !monthlyDepHint.equals("x"));

            configs.add(new DepreciateDialog.Config(account, accDepAccount, dateHint, monthlyDepHint, enabled));
        }
        DepreciateDialog dialog = new DepreciateDialog(getConfiguration(), configs);
        dialog.setVisible(true);
        if (dialog.getResult()){
            for (DepreciateDialog.Config config : dialog.getConfigs()){
                if (config.isEnabled()){
                    Service.TRANSACTIONS.addTransaction(year, config.getDateHint(), config.getValueHint(),
                            Service.ACCOUNT.getDepreciationAccount(year, config.getAccount()).getFullId(),
                            config.getDepAccount().getFullId(), Constants.Transaction.MONTHLY_DEP_DESCRIPTION);

                }
            }
            getConfiguration().update(Configuration.TRANSACTION_UPDATED);
        }
    }
}
