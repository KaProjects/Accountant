package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.year.dialog.DepreciateDialog;
import org.kaleta.accountant.frontend.year.model.AccountModel;
import org.kaleta.accountant.service.Service;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stanislav Kaleta on 11.04.2017.
 */
public class OpenDepreciateDialog extends ActionListener {
    private List<AccountModel.Account> accounts;

    public OpenDepreciateDialog(Configurable configurable, AccountModel.Account account) {
        super(configurable);
        accounts = new ArrayList<>();
        accounts.add(account);
    }

    public OpenDepreciateDialog(Configurable configurable, List<AccountModel.Account> accounts) {
        super(configurable);
        this.accounts = accounts;
    }

    @Override
    protected void actionPerformed() {
        List<DepreciateDialog.Config> configs = new ArrayList<>();
        for (AccountModel.Account account : accounts){
            AccountModel model = getConfiguration().getModel().getAccountModel();
            String accumDepAccSchemaId = "09" + account.getSchemaId().substring(1,2);
            String accumDepAccSemanticId = account.getSchemaId().substring(2, 3) + "-" + account.getSemanticId();
            AccountModel.Account accumDepAcc = model.getAccount(accumDepAccSchemaId, accumDepAccSemanticId);

            String montlyDepHint = (accumDepAcc.getMetadata().split(",").length == 2) ? accumDepAcc.getMetadata().split(",")[0] : "0";

            int assetValue = Integer.parseInt(model.getAccBalance(account));
            int depSum = Integer.parseInt(model.getAccBalance(accumDepAcc));
            montlyDepHint = ((assetValue-depSum) < Integer.parseInt(montlyDepHint)) ? String.valueOf(assetValue-depSum) : montlyDepHint;

            String dayHint = (accumDepAcc.getMetadata().split(",").length == 2) ? accumDepAcc.getMetadata().split(",")[1] : "1";

            String yearHint = String.valueOf(getConfiguration().getModel().getYearId());

            String monthHint = "0";
            for (AccountModel.Transaction tr : getConfiguration().getModel().getAccountModel().getTransactions(
                    "59"+account.getSchemaId().substring(1,2)+"."+accumDepAccSemanticId, accumDepAccSchemaId+"."+accumDepAccSemanticId)){
                    Integer month = Integer.parseInt(tr.getDate().substring(2,4));
                    monthHint = (month >= Integer.parseInt(monthHint)) ? String.valueOf(++month) : monthHint;
            }
            if (monthHint.equals("0")){
                monthHint = String.valueOf(1 + Integer.parseInt(getConfiguration().getModel().getAccountModel().getTransactions(account.getFullId(), Constants.Account.INIT_ACC_ID).get(0).getDate().substring(2,4)));
            }
            String dateHint;
            if (monthHint.equals("13") || montlyDepHint.equals("0")){
                dateHint = "x";
                montlyDepHint = "x";
            } else {
                dateHint  = String.format("%02d", Integer.parseInt(dayHint))+String.format("%02d", Integer.parseInt(monthHint))+yearHint;
            }
            configs.add(new DepreciateDialog.Config(account, accumDepAcc, dateHint, montlyDepHint, (assetValue-depSum), true));
        }
        DepreciateDialog dialog = new DepreciateDialog((Component) getConfiguration(), configs);
        dialog.setVisible(true);
        if (dialog.getResult()){
            AccountModel model = getConfiguration().getModel().getAccountModel();
            for (DepreciateDialog.Config config : dialog.getConfigs()){
                if (config.isEnabled()){
                    String expenseAccId = "59" + config.getDepAccount().getSchemaId().substring(2,3)+"."+config.getDepAccount().getSemanticId();
                    model.getTransactions().add(new AccountModel.Transaction(
                            model.getNextTransactionId(),
                            config.getDateHint(),
                            "monthly depreciation",
                            config.getValueHint(),
                            expenseAccId,
                            config.getDepAccount().getFullId()));
                }
            }
            Service.YEAR.updateAccount(model);
            getConfiguration().update(Configuration.ACCOUNT_UPDATED);
        }
    }
}
