package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.dialog.AddResourcesDialog;
import org.kaleta.accountant.service.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenAddResourcesDialog extends ActionListener {

    public OpenAddResourcesDialog(Configurable configurable) {
        super(configurable);
    }

    @Override
    protected void actionPerformed() {
        String year = getConfiguration().getSelectedYear();
        Map<String, List<AccountsModel.Account>> allAccountMap = Service.ACCOUNT.getAccountsViaSchemaMap(year);
        Map<String, List<AccountsModel.Account>> resourceAccountMap = new HashMap<>();
        Map<String, List<AccountsModel.Account>> creditAccountMap = new HashMap<>();
        Map<String, List<AccountsModel.Account>> debitAccountMap = new HashMap<>();
        for (String schemaId : allAccountMap.keySet()){
            if (schemaId.startsWith("1")){
                resourceAccountMap.put(schemaId, allAccountMap.get(schemaId));
            }
            if (schemaId.startsWith("2") || schemaId.startsWith("3") || schemaId.startsWith("4") || schemaId.startsWith("6")) {
                creditAccountMap.put(schemaId, allAccountMap.get(schemaId));
            }
            if (schemaId.startsWith("2") || schemaId.startsWith("3")
                    || (schemaId.startsWith("5") && !schemaId.startsWith("5"+Constants.Schema.DEPRECIATION_GROUP_ID)
                        && !schemaId.startsWith("5"+Constants.Schema.CONSUMPTION_GROUP_ID))) {
                debitAccountMap.put(schemaId, allAccountMap.get(schemaId));
            }
        }
        SchemaModel.Class resourceClass = Service.SCHEMA.getSchemaClassMap(year).get(1);
        List<SchemaModel.Class> creditClasses = new ArrayList<>();
        creditClasses.add(Service.SCHEMA.getSchemaClassMap(year).get(2));
        creditClasses.add(Service.SCHEMA.getSchemaClassMap(year).get(3));
        creditClasses.add(Service.SCHEMA.getSchemaClassMap(year).get(4));
        creditClasses.add(Service.SCHEMA.getSchemaClassMap(year).get(6));
        List<SchemaModel.Class> debitClasses = new ArrayList<>();
        debitClasses.add(Service.SCHEMA.getSchemaClassMap(year).get(2));
        debitClasses.add(Service.SCHEMA.getSchemaClassMap(year).get(3));
        debitClasses.add(Service.SCHEMA.getSchemaClassMap(year).get(5));

        AddResourcesDialog dialog = new AddResourcesDialog(getConfiguration(),
                resourceAccountMap, resourceClass, creditAccountMap, creditClasses, debitAccountMap, debitClasses);
        dialog.setVisible(true);
        if (dialog.getResult()) {
            String date = dialog.getDate();
            String creditId = dialog.getCreditAcc();
            for (AddResourcesDialog.ResourceData resourceData : dialog.getResourceData()) {
                Service.TRANSACTIONS.addTransaction(year, date, resourceData.getAmount(),
                        resourceData.getResourceId(), creditId, Constants.Transaction.RESOURCE_ACQUIRED);

                String[] resId = resourceData.getResourceId().split("\\.");
                if (resourceData.isConsumed()) {
                    Service.TRANSACTIONS.addTransaction(year, date, resourceData.getAmount(),
                            Service.ACCOUNT.getConsumptionAccountId(resId[0], resId[1]),
                            resourceData.getResourceId(), Constants.Transaction.RESOURCE_CONSUMED);
                } else {
                    Service.TRANSACTIONS.addTransaction(year, date, resourceData.getAmount(),
                            resourceData.getDebitId(), resourceData.getResourceId(), resourceData.getDebitInfo());
                }
            }
            getConfiguration().update(Configuration.TRANSACTION_UPDATED);
        }

    }
}
