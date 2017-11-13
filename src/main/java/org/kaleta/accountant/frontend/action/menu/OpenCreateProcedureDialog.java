package org.kaleta.accountant.frontend.action.menu;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.AccountPairModel;
import org.kaleta.accountant.frontend.dialog.CreateProcedureDialog;
import org.kaleta.accountant.service.Service;

import java.util.List;
import java.util.Map;

public class OpenCreateProcedureDialog extends MenuAction {

    public OpenCreateProcedureDialog(Configuration config) {
        super(config, "Create Procedure");
    }

    @Override
    protected void actionPerformed() {
        Map<String, List<AccountsModel.Account>> allAccountMap = Service.ACCOUNT.getAccountsViaSchemaMap(getConfiguration().getSelectedYear());
        List<SchemaModel.Class> classList = Service.SCHEMA.getSchemaClassList(getConfiguration().getSelectedYear());
        Map<AccountPairModel, List<String>> accountPairDescriptionMap = Service.TRANSACTIONS.getAccountPairDescriptions(getConfiguration().getSelectedYear());

        CreateProcedureDialog dialog = new CreateProcedureDialog(getConfiguration(), accountPairDescriptionMap, allAccountMap, classList);
        dialog.setVisible(true);
        if (dialog.getResult()){
            Service.PROCEDURES.createProcedure(getConfiguration().getSelectedYear(), dialog.getProcedureName(), dialog.getTransactions());
            getConfiguration().update(Configuration.PROCEDURE_UPDATED);
        }
    }
}
