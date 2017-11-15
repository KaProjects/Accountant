package org.kaleta.accountant.frontend.action.menu;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.dialog.AddAssetDialog;
import org.kaleta.accountant.service.Service;

import java.util.*;

public class OpenAddAssetDialog extends MenuAction {

    public OpenAddAssetDialog(Configuration config) {
        super(config, "Add Asset");
    }

    @Override
    protected void actionPerformed() {
        String year = getConfiguration().getSelectedYear();
        Map<Integer, SchemaModel.Class.Group> groupMap = Service.SCHEMA.getSchemaGroupMap(Service.SCHEMA.getSchemaClassMap(year).get(0));
        groupMap.remove(9);
        Map<String, List<AccountsModel.Account>> creditAccountMap = Service.ACCOUNT.getAccountsViaSchemaMap(year);
        Set<String> keySet = new HashSet<>(creditAccountMap.keySet());
        for (String schemaId : keySet){
            if (schemaId.startsWith("0") || schemaId.startsWith("1") || schemaId.startsWith("5") || schemaId.startsWith("7")){
                creditAccountMap.remove(schemaId);
            }
        }
        List<SchemaModel.Class> creditClasses = new ArrayList<>();
        creditClasses.add(Service.SCHEMA.getSchemaClassMap(year).get(2));
        creditClasses.add(Service.SCHEMA.getSchemaClassMap(year).get(3));
        creditClasses.add(Service.SCHEMA.getSchemaClassMap(year).get(4));
        creditClasses.add(Service.SCHEMA.getSchemaClassMap(year).get(6));
        AddAssetDialog dialog = new AddAssetDialog(getConfiguration(),
                new ArrayList<>(groupMap.values()), creditAccountMap,
                creditClasses);
        dialog.setVisible(true);
        if (dialog.getResult()) {
            String semanticId = String.valueOf(Service.ACCOUNT.getAccountsBySchemaId(getConfiguration().getSelectedYear(),dialog.getSchemaId()).size());

            Service.ACCOUNT.createAccount(year, dialog.getAccName(), dialog.getSchemaId(), semanticId, "");

            String accId = dialog.getSchemaId() + "." + semanticId;
            Service.TRANSACTIONS.addTransaction(year, dialog.getDate(), "0", accId, Constants.Account.INIT_ACC_ID, Constants.Transaction.OPEN_DESCRIPTION);
            Service.TRANSACTIONS.addTransaction(year, dialog.getDate(), dialog.getInitValue(), accId, dialog.getCreditAccount(), Constants.Transaction.PURCHASE_DESCRIPTION);

            String accDepId = Service.ACCOUNT.getAccumulatedDepAccountId(dialog.getSchemaId(), semanticId);
            Service.ACCOUNT.createAccount(year, Constants.Schema.ACCUMULATED_DEP_ACCOUNT_PREFIX + dialog.getAccName(),
                    accDepId.split("\\.")[0], accDepId.split("\\.")[1], dialog.getDepMetaData());

            Service.TRANSACTIONS.addTransaction(year, dialog.getDate(), "0", Constants.Account.INIT_ACC_ID, accDepId, Constants.Transaction.OPEN_DESCRIPTION);

            String depId = Service.ACCOUNT.getDepreciationAccountId(dialog.getSchemaId(), semanticId);
            Service.ACCOUNT.createAccount(year, Constants.Schema.DEPRECIATION_ACCOUNT_PREFIX + dialog.getAccName(),
                    depId.split("\\.")[0], depId.split("\\.")[1], "");

            getConfiguration().update(Configuration.ACCOUNT_UPDATED);
            getConfiguration().update(Configuration.TRANSACTION_UPDATED);
        }
    }
}
