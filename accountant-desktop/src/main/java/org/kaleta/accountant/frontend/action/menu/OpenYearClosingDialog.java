package org.kaleta.accountant.frontend.action.menu;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.TransactionsModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.service.Service;

public class OpenYearClosingDialog extends MenuAction {

    private String newYear;
    private String lastYear;

    public OpenYearClosingDialog(Configuration config) {
        super(config, "Close Year");

        // TODO: 1/2/18 to finish year closure procedure
        this.enabled = false;
    }

    @Override
    protected void actionPerformed() {
        lastYear = Service.CONFIG.getActiveYear();
        newYear = "2018";

        closeAccounts();
        initYear();
        openAccounts();
        importProcedures();
        Service.CONFIG.setActiveYear(newYear);
        getConfiguration().selectYear(newYear);

//        YearClosingDialog dialog = new YearClosingDialog(getConfiguration());
//        dialog.setVisible(true);
//        if (dialog.getResult()){
//            // TODO post 1.0 : get attributes mandatory for closure from dialog
//            // TODO post 1.0 : open some info dialog which show info about process
//            try {
//                closeAccounts();
//                String msg = "";
//                Initializer.LOG.info(msg);
//                //  add info about finished action
//                //initYear();
//                msg = "";
//                Initializer.LOG.info(msg);
//                //  add info about finished action
//                //openAccounts();
//                msg = "";
//                Initializer.LOG.info(msg);
//                // add info about finished action
//                //importProcedures();
//                msg = "";
//                Initializer.LOG.info(msg);
//                //  add info about finished action
//            } catch (Exception e){
//                String errmsg = "";
//                Initializer.LOG.severe(errmsg);
//                //  add info about failed action
//            }
//        }
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
            String type = Service.SCHEMA.getSchemaAccountType(year, account.getSchemaId());
            if (type.equals(Constants.AccountType.OFF_BALANCE)) continue;

            String balance = String.valueOf(Service.TRANSACTIONS.getAccountBalance(year, account));

            switch (type){
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
        Service.TRANSACTIONS.resolveProfit(year, closureDate, Service.ACCOUNT.getAccount(year, Constants.Account.PROFIT_ACC_ID));
        getConfiguration().update(Configuration.TRANSACTION_UPDATED);
    }

    /**
     * init new year's files
     * import schema
     * set new year as active
     */
    private void initYear(){
        Service.CONFIG.initYearData(newYear);
        getConfiguration().update(Configuration.YEAR_ADDED);

        Service.SCHEMA.importSchema(lastYear, newYear);
    }

    /**
     * closing/profit acc last year -> init acc. new year
     */
    private void openAccounts(){
        // TODO post 1.0 : which E/R acc. to import ?
        // add E accounts
        for (TransactionsModel.Transaction tr : Service.TRANSACTIONS.getTransactions(lastYear, Constants.Account.PROFIT_ACC_ID, null)) {
            if (tr.getCredit().startsWith("5" + Constants.Schema.DEPRECIATION_GROUP_ID)) continue;
            if (tr.getCredit().startsWith("5" + Constants.Schema.CONSUMPTION_GROUP_ID)) continue;
            if (tr.getCredit().equals(Constants.Account.CLOSING_ACC_ID)) continue;
            AccountsModel.Account acc = Service.ACCOUNT.getAccount(lastYear, tr.getCredit());
            Service.ACCOUNT.createAccount(newYear, acc.getName(), acc.getSchemaId(), acc.getSemanticId(), acc.getMetadata());
        }
        // add R accounts
        for (TransactionsModel.Transaction tr : Service.TRANSACTIONS.getTransactions(lastYear, null, Constants.Account.PROFIT_ACC_ID)) {
            AccountsModel.Account acc = Service.ACCOUNT.getAccount(lastYear, tr.getDebit());
            Service.ACCOUNT.createAccount(newYear, acc.getName(), acc.getSchemaId(), acc.getSemanticId(), acc.getMetadata());
        }

        // TODO post 1.0 : zero A/L -> to choose -> if yes -> add + open (mandatory accounts (like 400) cant be excluded)
        // add + open A accounts
        String openDate = "0101";
        for (TransactionsModel.Transaction tr : Service.TRANSACTIONS.getTransactions(lastYear, Constants.Account.CLOSING_ACC_ID, null)) {
            if (tr.getCredit().startsWith("0") || tr.getCredit().startsWith("1")) continue;
            AccountsModel.Account acc = Service.ACCOUNT.getAccount(lastYear, tr.getCredit());
            Service.ACCOUNT.createAccount(newYear, acc.getName(), acc.getSchemaId(), acc.getSemanticId(), acc.getMetadata());
            Service.TRANSACTIONS.addTransaction(newYear, openDate, tr.getAmount(), acc.getFullId(), Constants.Account.INIT_ACC_ID, Constants.Transaction.OPEN_DESCRIPTION);
        }
        // add + open L accounts
        for (TransactionsModel.Transaction tr : Service.TRANSACTIONS.getTransactions(lastYear, null, Constants.Account.CLOSING_ACC_ID)) {
            if (tr.getDebit().startsWith("0")) continue;
            if (tr.getDebit().startsWith(Constants.Account.PERSONAL_CAPITAL_ACC_ID)) continue;
            if (tr.getDebit().equals(Constants.Account.PROFIT_ACC_ID)) continue;
            AccountsModel.Account acc = Service.ACCOUNT.getAccount(lastYear, tr.getDebit());
            Service.ACCOUNT.createAccount(newYear, acc.getName(), acc.getSchemaId(), acc.getSemanticId(), acc.getMetadata());
            Service.TRANSACTIONS.addTransaction(newYear, openDate, tr.getAmount(), Constants.Account.INIT_ACC_ID, acc.getFullId(), Constants.Transaction.OPEN_DESCRIPTION);
        }

        // add + open class 0, 1 accounts
        for (TransactionsModel.Transaction tr : Service.TRANSACTIONS.getTransactions(lastYear, Constants.Account.CLOSING_ACC_ID, null)) {
            // 0 rule: if 0** has 0 balance (09*-* should have also 0) => NOT add 0** 09*-* 50*-*
            if (tr.getCredit().startsWith("0") && !tr.getAmount().equals("0")) {
                AccountsModel.Account acc = Service.ACCOUNT.getAccount(lastYear, tr.getCredit());
                AccountsModel.Account accDepAcc = Service.ACCOUNT.getAccumulatedDepAccount(lastYear, acc);
                TransactionsModel.Transaction accDepTr = Service.TRANSACTIONS.getTransactions(lastYear, accDepAcc.getFullId(), Constants.Account.CLOSING_ACC_ID).get(0);
                AccountsModel.Account depAcc = Service.ACCOUNT.getDepreciationAccount(lastYear, acc);

                Service.ACCOUNT.createAccount(newYear, acc.getName(), acc.getSchemaId(), acc.getSemanticId(), acc.getMetadata());
                Service.TRANSACTIONS.addTransaction(newYear, openDate, tr.getAmount(), acc.getFullId(), Constants.Account.INIT_ACC_ID, Constants.Transaction.OPEN_DESCRIPTION);

                Service.ACCOUNT.createAccount(newYear, accDepAcc.getName(), accDepAcc.getSchemaId(), accDepAcc.getSemanticId(), accDepAcc.getMetadata());
                Service.TRANSACTIONS.addTransaction(newYear, openDate, accDepTr.getAmount(), Constants.Account.INIT_ACC_ID, accDepAcc.getFullId(), Constants.Transaction.OPEN_DESCRIPTION);

                Service.ACCOUNT.createAccount(newYear, depAcc.getName(), depAcc.getSchemaId(), depAcc.getSemanticId(), depAcc.getMetadata());
            }
            // 1 rule: if 1 added -> create 51*-*
            if (tr.getCredit().startsWith("1")){
                AccountsModel.Account acc = Service.ACCOUNT.getAccount(lastYear, tr.getCredit());
                AccountsModel.Account consAcc = Service.ACCOUNT.getConsumptionAccount(lastYear, acc);

                Service.ACCOUNT.createAccount(newYear, acc.getName(), acc.getSchemaId(), acc.getSemanticId(), acc.getMetadata());
                Service.TRANSACTIONS.addTransaction(newYear, openDate, tr.getAmount(), acc.getFullId(), Constants.Account.INIT_ACC_ID, Constants.Transaction.OPEN_DESCRIPTION);

                Service.ACCOUNT.createAccount(newYear, consAcc.getName(), consAcc.getSchemaId(), consAcc.getSemanticId(), consAcc.getMetadata());
            }
        }

        // set personal capital
        String lastYearPcValue = Service.TRANSACTIONS.getTransactions(lastYear, Constants.Account.PERSONAL_CAPITAL_ACC_ID, Constants.Account.CLOSING_ACC_ID).get(0).getAmount();
        String lastYearProfit = Service.TRANSACTIONS.getTransactions(lastYear, Constants.Account.PROFIT_ACC_ID, Constants.Account.CLOSING_ACC_ID).get(0).getAmount();
        String newYearPcValue = String.valueOf((Integer.parseInt(lastYearPcValue) + Integer.parseInt(lastYearProfit)));
        Service.TRANSACTIONS.addTransaction(newYear, openDate, newYearPcValue, Constants.Account.INIT_ACC_ID, Constants.Account.PERSONAL_CAPITAL_ACC_ID, Constants.Transaction.OPEN_DESCRIPTION);
    }

    private void importProcedures(){
        // TODO post 1.0 : import procedures - somehow
    }
}
