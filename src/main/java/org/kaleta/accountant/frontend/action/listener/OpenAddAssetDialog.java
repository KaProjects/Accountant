package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.constants.AccountType;
import org.kaleta.accountant.frontend.common.constants.DefaultSchemaId;
import org.kaleta.accountant.frontend.component.year.dialog.AddAssetDialog;
import org.kaleta.accountant.frontend.component.year.model.AccountModel;
import org.kaleta.accountant.frontend.component.year.model.SchemaModel;
import org.kaleta.accountant.service.Service;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Stanley on 24.2.2017.
 */
public class OpenAddAssetDialog extends ActionListener {

    public OpenAddAssetDialog(Configurable configurable) {
        super(configurable);
    }

    @Override
    protected void actionPerformed() {
        Map<Integer, SchemaModel.Clazz.Group> groupMap = new HashMap<>(getConfiguration().getModel().getSchemaModel().getClasses().get(0).getGroups());
        groupMap.remove(9);
        AddAssetDialog dialog = new AddAssetDialog((Component) getConfiguration(),new ArrayList<>(groupMap.values()));
        dialog.setVisible(true);
        if (dialog.getResult()) {
            AccountModel model = getConfiguration().getModel().getAccountModel();

            String schemaId = dialog.getSchemaId();
            List<AccountModel.Account> tempAccs = model.getAccountsBySchema(schemaId);
            int lastSemanticId = 0;
            for (AccountModel.Account tempAcc : tempAccs) {
                int thisAccId = Integer.parseInt(tempAcc.getSemanticId());
                if (thisAccId > lastSemanticId) lastSemanticId = thisAccId;
            }
            String semanticId = String.valueOf(lastSemanticId + 1);
            String assocSemanticId = schemaId.substring(2, 3) + "-" + semanticId;

            String name = dialog.getAccName();
            String date = dialog.getDate();
            String initValue = dialog.getInitValue();
            String depMetaData = dialog.getDepMetaData();


            model.getAccounts().add(new AccountModel.Account(schemaId, semanticId, AccountType.ASSET, name, ""));
            model.getTransactions().add(new AccountModel.Transaction(model.getNextTransactionId(), date, "open",
                    initValue, schemaId + "." + semanticId, DefaultSchemaId.INIT_ACC));

            model.getAccounts().add(new AccountModel.Account("09" + schemaId.substring(1, 2),
                    assocSemanticId, AccountType.LIABILITY, "A. Dep. of " + name, depMetaData));
            model.getTransactions().add(new AccountModel.Transaction(model.getNextTransactionId(), date, "open",
                    "0", DefaultSchemaId.INIT_ACC, "09" + schemaId.substring(1, 2) + "." + assocSemanticId));

            model.getAccounts().add(new AccountModel.Account("59" + schemaId.substring(1, 2),
                    assocSemanticId, AccountType.EXPENSE, "Dep. of " + name, ""));
            model.getTransactions().add(new AccountModel.Transaction(model.getNextTransactionId(), date, "open",
                    "0", "59" + schemaId.substring(1, 2) + "." + assocSemanticId, DefaultSchemaId.INIT_ACC));

            Service.YEAR.updateAccount(model);
            getConfiguration().update(Configuration.ACCOUNT_UPDATED);
        }
    }
}
