package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.dialog.ExcludeDialog;
import org.kaleta.accountant.service.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenExcludeDialog extends ActionListener {
    private final AccountsModel.Account account;

    public OpenExcludeDialog(Configurable configurable, AccountsModel.Account account) {
        super(configurable);
        this.account = account;
    }

    @Override
    protected void actionPerformed() {
        String year = getConfiguration().getSelectedYear();
        Map<String, List<AccountsModel.Account>> allAccountMap = Service.ACCOUNT.getAccountsViaSchemaMap(year);
        Map<String, List<AccountsModel.Account>> expenseAccountMap = new HashMap<>();
        Map<String, List<AccountsModel.Account>> debitAccountMap = new HashMap<>();
        Map<String, List<AccountsModel.Account>> revenueAccountMap = new HashMap<>();
        for (String schemaId : allAccountMap.keySet()){
            if (schemaId.startsWith("5")
                    && !schemaId.startsWith("5"+Constants.Schema.DEPRECIATION_GROUP_ID)
                    && !schemaId.startsWith("5"+Constants.Schema.CONSUMPTION_GROUP_ID)){
                expenseAccountMap.put(schemaId, allAccountMap.get(schemaId));
            }
            if (schemaId.startsWith("2") || schemaId.startsWith("3")) {
                debitAccountMap.put(schemaId, allAccountMap.get(schemaId));
            }
            if (schemaId.startsWith("6")) {
                revenueAccountMap.put(schemaId, allAccountMap.get(schemaId));
            }
        }
        SchemaModel.Class expenseClass = Service.SCHEMA.getSchemaClassMap(year).get(5);
        List<SchemaModel.Class> debitClasses = new ArrayList<>();
        debitClasses.add(Service.SCHEMA.getSchemaClassMap(year).get(2));
        debitClasses.add(Service.SCHEMA.getSchemaClassMap(year).get(3));
        SchemaModel.Class revenueClass = Service.SCHEMA.getSchemaClassMap(year).get(6);

        AccountsModel.Account accDepAccount = Service.ACCOUNT.getAccumulatedDepAccount(year,account);

        Integer assetValue = Service.TRANSACTIONS.getAccountBalance(year, account);
        Integer residualExpense = assetValue - Service.TRANSACTIONS.getAccountBalance(year, accDepAccount);

        ExcludeDialog dialog = (residualExpense == 0)
        ? new ExcludeDialog(getConfiguration(),debitAccountMap,debitClasses,revenueAccountMap,revenueClass)
        : new ExcludeDialog(getConfiguration(), expenseAccountMap, expenseClass,debitAccountMap,debitClasses,revenueAccountMap,revenueClass);
        dialog.setVisible(true);
        if (dialog.getResult()) {
            if (residualExpense != 0){
                Service.TRANSACTIONS.addTransaction(year,dialog.getDate(), String.valueOf(residualExpense),
                        dialog.getExpenseAccount(), accDepAccount.getFullId(),"residual expense");
            }

            Service.TRANSACTIONS.addTransaction(year,dialog.getDate(), String.valueOf(assetValue), accDepAccount.getFullId(), account.getFullId(),"excluded");

            if (dialog.hasRevenue()){
                Service.TRANSACTIONS.addTransaction(year, dialog.getDate(), dialog.getRevenueValue(),
                        dialog.getDebitAccount(), dialog.getRevenueAccount(), "revenue from excluding asset");
            }
            getConfiguration().update(Configuration.TRANSACTION_UPDATED);
        }
    }
}
