package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.util.Calendar;

public class AccountsEditorAccountAction extends ActionListener{
    private String schemaId;
    private JTextField tfName;

    public AccountsEditorAccountAction(Configurable configurable, String schemaId, JTextField tfName) {
        super(configurable);
        if (schemaId.startsWith("7") || schemaId.startsWith("0")
                || schemaId.startsWith("0"+Constants.Schema.ACCUMULATED_DEP_GROUP_ID)
                || schemaId.startsWith("5"+Constants.Schema.DEPRECIATION_GROUP_ID)
                || schemaId.startsWith("5"+Constants.Schema.CONSUMPTION_GROUP_ID)){
            throw new IllegalArgumentException("Adding accounts restricted for '"+schemaId+"'");
        }
        this.schemaId = schemaId;
        this.tfName = tfName;
    }

    @Override
    protected void actionPerformed() {
        subactionPerformed(tfName.getText());

    }

    public AccountsModel.Account subactionPerformed(String name){
        String semanticId = String.valueOf(Service.ACCOUNT.getAccountsBySchemaId(getConfiguration().getSelectedYear(),schemaId).size());

        AccountsModel.Account createdAccount =
                Service.ACCOUNT.createAccount(getConfiguration().getSelectedYear(), name, schemaId, semanticId, "");

        Calendar calendar = Calendar.getInstance();
        String date = String.format("%1$02d%2$02d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1);
        Service.TRANSACTIONS.addTransaction(getConfiguration().getSelectedYear(),date, "0",
                createdAccount.getFullId(), Constants.Account.INIT_ACC_ID, Constants.Transaction.OPEN_DESCRIPTION);

        if (schemaId.startsWith("1")){
            String consumptionAccId = Service.ACCOUNT.getConsumptionAccountId(schemaId, semanticId);
            Service.ACCOUNT.createAccount(getConfiguration().getSelectedYear(), Constants.Schema.CONSUMPTION_ACCOUNT_PREFIX + name,
                    consumptionAccId.split("\\.")[0], consumptionAccId.split("\\.")[1], "");
        }

        getConfiguration().update(Configuration.ACCOUNT_UPDATED);
        getConfiguration().update(Configuration.TRANSACTION_UPDATED);
        return createdAccount;
    }
}
