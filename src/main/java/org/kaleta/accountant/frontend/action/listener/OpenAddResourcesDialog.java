package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.component.year.dialog.AddResourcesDialog;
import org.kaleta.accountant.frontend.component.year.model.AccountModel;
import org.kaleta.accountant.frontend.component.year.model.SchemaModel;
import org.kaleta.accountant.frontend.component.year.model.YearModel;

import java.awt.Component;
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
        YearModel model = getConfiguration().getModel();
        List<SchemaModel.Clazz.Group> resourcesGroupList = new ArrayList<>(model.getSchemaModel().getClasses().get(1).getGroups().values());
        Map<String, List<AccountModel.Account>> resourcesSemanticMap = new HashMap<>();
        for (SchemaModel.Clazz.Group group : resourcesGroupList) {
            for (SchemaModel.Clazz.Group.Account account : group.getAccounts().values()){
                String schemaId = "1" + String.valueOf(group.getId()) + String.valueOf(account.getId());
                resourcesSemanticMap.put(schemaId, model.getAccountModel().getAccountsBySchema(schemaId));
            }
        }
        List<SchemaModel.Clazz> payableClassesList = new ArrayList<>();
        payableClassesList.add(model.getSchemaModel().getClasses().get(2));
        payableClassesList.add(model.getSchemaModel().getClasses().get(3));
        Map<String, List<AccountModel.Account>> payableSemanticMap = new HashMap<>();
        for (SchemaModel.Clazz clazz : payableClassesList){
            for (SchemaModel.Clazz.Group group : clazz.getGroups().values()) {
                for (SchemaModel.Clazz.Group.Account account : group.getAccounts().values()){
                    String schemaId = String.valueOf(clazz.getId())+ String.valueOf(group.getId()) + String.valueOf(account.getId());
                    payableSemanticMap.put(schemaId, model.getAccountModel().getAccountsBySchema(schemaId));
                }
            }
        }
        AddResourcesDialog dialog = new AddResourcesDialog((Component) getConfiguration(), resourcesGroupList, resourcesSemanticMap, payableClassesList, payableSemanticMap);
        dialog.setVisible(true);
        if (dialog.getResult()) {
            AccountModel accountModel = getConfiguration().getModel().getAccountModel();
            String date = dialog.getDate();
            String creditor = dialog.getCreditor();
            for (AddResourcesDialog.ResourcePanel resourcePanel : dialog.getResourcePanelList()){
                String resourceFullId = resourcePanel.getResourceId();
                String amount = resourcePanel.getAmount();
                AccountModel.Transaction trBuy = new AccountModel.Transaction(accountModel.getNextTransactionId(), date, "buy", amount, resourceFullId, creditor);
                accountModel.getTransactions().add(trBuy);
                String expenseFullId = "58" + resourceFullId.substring(1,2) + "." + resourceFullId.substring(2,3) + "-" + resourceFullId.substring(4);
                AccountModel.Transaction trConsume = new AccountModel.Transaction(accountModel.getNextTransactionId(), date, "consume", amount, expenseFullId, resourceFullId);
                accountModel.getTransactions().add(trConsume);
            }
        }

    }
}
