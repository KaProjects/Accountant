package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.AccountPairModel;
import org.kaleta.accountant.frontend.dialog.CreateProcedureDialog;
import org.kaleta.accountant.service.Service;

import java.util.List;
import java.util.Map;

public class OpenCreateProcedureDialog extends ActionListener {

    public OpenCreateProcedureDialog(Configurable configurable) {
        super(configurable);
    }

    @Override
    protected void actionPerformed() {
        Map<String, List<AccountsModel.Account>> allAccountMap = Service.ACCOUNT.getAccountsViaSchemaMap(getConfiguration().getSelectedYear());
        List<SchemaModel.Class> classList = Service.SCHEMA.getSchemaClassList(getConfiguration().getSelectedYear());
        Map<AccountPairModel, List<String>> accountPairDescriptionMap = Service.TRANSACTIONS.getAccountPairDescriptions(getConfiguration().getSelectedYear());



        CreateProcedureDialog dialog = new CreateProcedureDialog(getConfiguration());
        dialog.setVisible(true);
        if (dialog.getResult()){




            getConfiguration().update(Configuration.PROCEDURE_UPDATED);
        }
    }
}
