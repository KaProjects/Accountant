package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.dialog.AddResourcesDialog;
import org.kaleta.accountant.service.Service;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Stanislav Kaleta on 04.08.2017.
 */
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
        for (String schemaId : allAccountMap.keySet()){
            if (schemaId.startsWith("1")){
                resourceAccountMap.put(schemaId, allAccountMap.get(schemaId));
            }
            if (schemaId.startsWith("2") || schemaId.startsWith("3") || schemaId.startsWith("4") || schemaId.startsWith("6")) {
                creditAccountMap.put(schemaId, allAccountMap.get(schemaId));
            }
        }
        SchemaModel.Class resourceClass = Service.SCHEMA.getSchemaClassMap(year).get(1);
        List<SchemaModel.Class> creditClasses = new ArrayList<>();
        creditClasses.add(Service.SCHEMA.getSchemaClassMap(year).get(2));
        creditClasses.add(Service.SCHEMA.getSchemaClassMap(year).get(3));
        creditClasses.add(Service.SCHEMA.getSchemaClassMap(year).get(4));
        creditClasses.add(Service.SCHEMA.getSchemaClassMap(year).get(6));



        AddResourcesDialog dialog = new AddResourcesDialog((Frame) getConfiguration(), resourceAccountMap,
                resourceClass, creditAccountMap, creditClasses);
        dialog.setVisible(true);
        if (dialog.getResult()) {
            // TODO: 10/26/17  
//            AccountModel accountModel = getConfiguration().getModel().getAccountModel();
//            String date = dialog.getDate();
//            String creditor = dialog.getCreditor();
//            for (AddResourcesDialog.ResourcePanel resourcePanel : dialog.getResourcePanelList()){
//                String resourceFullId = resourcePanel.getResourceId();
//                String amount = resourcePanel.getAmount();
//                AccountModel.Transaction trBuy = new AccountModel.Transaction(accountModel.getNextTransactionId(), date, "buy", amount, resourceFullId, creditor);
//                accountModel.getTransactions().add(trBuy);
//                String expenseFullId = "58" + resourceFullId.substring(1,2) + "." + resourceFullId.substring(2,3) + "-" + resourceFullId.substring(4);
//                AccountModel.Transaction trConsume = new AccountModel.Transaction(accountModel.getNextTransactionId(), date, "consume", amount, expenseFullId, resourceFullId);
//                accountModel.getTransactions().add(trConsume);
//            }
        }

    }
}
