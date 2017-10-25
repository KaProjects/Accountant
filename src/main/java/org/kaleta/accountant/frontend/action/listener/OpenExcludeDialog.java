package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.dialog.ExcludeDialog;
import org.kaleta.accountant.service.Service;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenExcludeDialog extends ActionListener {
    private AccountsModel.Account account;

    public OpenExcludeDialog(Configurable configurable, AccountsModel.Account account) {
        super(configurable);
        this.account = account;
    }

    @Override
    protected void actionPerformed() {
        Map<String, List<AccountsModel.Account>> allAccountMap = Service.ACCOUNT.getAccountsViaSchemaMap(getConfiguration().getSelectedYear());
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
        SchemaModel.Class expenseClass = Service.SCHEMA.getSchemaClassMap(getConfiguration().getSelectedYear()).get(5);
        List<SchemaModel.Class> debitClasses = new ArrayList<>();
        debitClasses.add(Service.SCHEMA.getSchemaClassMap(getConfiguration().getSelectedYear()).get(2));
        debitClasses.add(Service.SCHEMA.getSchemaClassMap(getConfiguration().getSelectedYear()).get(3));
        SchemaModel.Class revenueClass = Service.SCHEMA.getSchemaClassMap(getConfiguration().getSelectedYear()).get(6);


        // TODO: 10/25/17 get accDepAcc
        // TODO: 10/25/17 determine whether E or not


        ExcludeDialog dialog = (account.getSchemaId().startsWith("000"))
        ? new ExcludeDialog((Frame) getConfiguration()/*, expenseAccountMap, expenseClass*/,debitAccountMap,debitClasses,revenueAccountMap,revenueClass)
        : new ExcludeDialog((Frame) getConfiguration(), expenseAccountMap, expenseClass,debitAccountMap,debitClasses,revenueAccountMap,revenueClass);
        dialog.setVisible(true);
        if (dialog.getResult()) {

            // TODO: 10/25/17 odpisane:  accDep/asset
            // TODO: 10/25/17 neodpisane:  skoda,dar,zustatkova cena(prodej)/accDep + accDep/asset
            // TODO: 10/25/17 vynos: peniaze, pohld.,.../vynos z prodeje

            //getConfiguration().update(Configuration.TRANSACTION_UPDATED);
        }
    }
}
