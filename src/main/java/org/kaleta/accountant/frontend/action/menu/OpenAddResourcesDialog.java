package org.kaleta.accountant.frontend.action.menu;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.AccountPairModel;
import org.kaleta.accountant.frontend.dialog.AddResourcesDialog;
import org.kaleta.accountant.service.Service;

import java.util.*;

public class OpenAddResourcesDialog extends MenuAction {

    public OpenAddResourcesDialog(Configuration config) {
        super(config, "Add Resource(s)");
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
                    || (schemaId.startsWith("5") && !schemaId.startsWith("5"+ Constants.Schema.DEPRECIATION_GROUP_ID)
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

        Map<String, List<String>> resourceDescriptionMap = new HashMap<>();
        Map<AccountPairModel, Set<String>> descMap = Service.TRANSACTIONS.getAccountPairDescriptions(getConfiguration().getSelectedYear());
        for (AccountPairModel accPair : descMap.keySet()){
            if (accPair.getDebit().startsWith("1") &&
                    (accPair.getCredit().startsWith("2") || accPair.getCredit().startsWith("3") || accPair.getCredit().startsWith("4") || accPair.getCredit().startsWith("6"))){
                resourceDescriptionMap.computeIfAbsent(accPair.getDebit(), k -> new ArrayList<>());
                for (String desc : descMap.get(accPair)){
                    if (desc.equals(Constants.Transaction.RESOURCE_ACQUIRED)) {
                        continue;
                    } else {
                        resourceDescriptionMap.get(accPair.getDebit()).add(desc.replace(Constants.Transaction.RESOURCE_ACQUIRED + " - ", ""));
                    }
                }
            }
        }

        AddResourcesDialog dialog = new AddResourcesDialog(getConfiguration(), resourceAccountMap, resourceClass,
                creditAccountMap, creditClasses, debitAccountMap, debitClasses, resourceDescriptionMap);
        dialog.setVisible(true);
        if (dialog.getResult()) {
            String date = dialog.getDate();
            String creditId = dialog.getCreditAcc();
            for (AddResourcesDialog.ResourceData resourceData : dialog.getResourceData()) {
                String acqDesc = (resourceData.getAcqDescription().trim().isEmpty()) ? "" : " - " + resourceData.getAcqDescription();
                Service.TRANSACTIONS.addTransaction(year, date, resourceData.getAmount(),
                        resourceData.getResourceId(), creditId, Constants.Transaction.RESOURCE_ACQUIRED + acqDesc);

                if (resourceData.isConsumed()) {
                    String[] resId = resourceData.getResourceId().split("\\.");
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
