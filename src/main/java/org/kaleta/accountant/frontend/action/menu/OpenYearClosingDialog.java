package org.kaleta.accountant.frontend.action.menu;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.dialog.YearClosingDialog;
import org.kaleta.accountant.service.Service;

public class OpenYearClosingDialog extends MenuAction {

    public OpenYearClosingDialog(Configuration config) {
        super(config, "Close Year");
    }

    @Override
    protected void actionPerformed() {


        YearClosingDialog dialog = new YearClosingDialog(getConfiguration());
        dialog.setVisible(true);
        if (dialog.getResult()){
            // TODO: 12/20/17 get attributes mandatory for closure from dialog 
            // TODO: 12/20/17 open some info dialog which show info about process
            //closeAccounts();
            // TODO: 12/20/17 log + dialog info about finished action
            //initYear();
            // TODO: 12/20/17 log + dialog info about finished action
            //openAccounts();
            // TODO: 12/20/17 log + dialog info about finished action
            //importProcedures();
            // TODO: 12/20/17 log + dialog info about finished action
        }
    }

    /**
     * A/L -> balance
     * E/R -> profit
     * profit -> balance
     */
    private void closeAccounts(){
        String year = getConfiguration().getSelectedYear();
        String closureDate = "3112";

        for (AccountsModel.Account account : Service.ACCOUNT.getAllAccounts(year)){
            String balance = Service.TRANSACTIONS.getAccountBalance(year, account);

            switch (Service.SCHEMA.getSchemaAccountType(year, account.getSchemaId())){
                case Constants.AccountType.ASSET:{
                    Service.TRANSACTIONS.addTransaction(year, closureDate, balance,
                            Constants.Account.CLOSING_ACC_ID, account.getFullId(), Constants.Transaction.CLOSE_DESCRIPTION);
                    break;
                }
                case Constants.AccountType.LIABILITY:{
                    Service.TRANSACTIONS.addTransaction(year, closureDate, balance,
                            account.getFullId(), Constants.Account.CLOSING_ACC_ID, Constants.Transaction.CLOSE_DESCRIPTION);
                    break;
                }
                case Constants.AccountType.EXPENSE:{
                    Service.TRANSACTIONS.addTransaction(year, closureDate, balance,
                            Constants.Account.PROFIT_ACC_ID, account.getFullId(), Constants.Transaction.CLOSE_DESCRIPTION);
                    break;
                }
                case Constants.AccountType.REVENUE:{
                    Service.TRANSACTIONS.addTransaction(year, closureDate, balance,
                            account.getFullId(), Constants.Account.PROFIT_ACC_ID, Constants.Transaction.CLOSE_DESCRIPTION);
                    break;
                }
            }
        }

        Service.TRANSACTIONS.resolveProfit(year);
    }

    /**
     * init new year's files
     * import schema
     * set new year as active
     */
    private void initYear(String newYearName){
        Service.CONFIG.initYearData(newYearName);

        // TODO: 12/20/17 import schema

        Service.CONFIG.setActiveYear(newYearName);
    }

    private void openAccounts(){
        // create non-balance accounts
        // non-zero A/L -> add + open
        // zero A/L -> to choose -> if yes -> add + open
        // E/R -> to choose -> if yes -> add (just!)

        // exceptions!!!
        //if 0** has 0 balance (09*-* should have also 0) => NOT add 0** 09*-* 50*-*
        //  else add 0** 09*-* create 50*-*
        //if 1 added -> create 51*-*
        // mandatory accounts (like 400) cant be excluded
    }

    private void importProcedures(){
        // to decide
    }
}
