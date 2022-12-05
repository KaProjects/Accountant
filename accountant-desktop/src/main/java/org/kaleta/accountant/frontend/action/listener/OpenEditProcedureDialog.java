package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.ProceduresModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.menu.MenuAction;
import org.kaleta.accountant.frontend.common.AccountPairModel;
import org.kaleta.accountant.frontend.dialog.CreateProcedureDialog;
import org.kaleta.accountant.service.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class OpenEditProcedureDialog extends MenuAction {
    private ProceduresModel.Group.Procedure procedure;
    private String procedureGroup;

    public OpenEditProcedureDialog(Configuration config, ProceduresModel.Group.Procedure procedure, String procedureGroup) {
        super(config, "Edit Procedure");
        this.procedure = procedure;
        this.procedureGroup = procedureGroup;
    }

    @Override
    protected void actionPerformed() {
        Map<String, List<AccountsModel.Account>> allAccountMap = Service.ACCOUNT.getAccountsViaSchemaMap(getConfiguration().getSelectedYear());
        List<SchemaModel.Class> classList = Service.SCHEMA.getSchemaClassList(getConfiguration().getSelectedYear());
        Map<AccountPairModel, Set<String>> accountPairDescriptionMap = Service.TRANSACTIONS.getAccountPairDescriptions(getConfiguration().getSelectedYear());
        List<String> procedureGroupNameList = Service.PROCEDURES.getProcedureGroupNameList(getConfiguration().getSelectedYear());

        CreateProcedureDialog dialog = new CreateProcedureDialog(getConfiguration(), accountPairDescriptionMap, allAccountMap, classList, procedure, procedureGroup, procedureGroupNameList);
        dialog.setVisible(true);
        if (dialog.getResult()){
            Service.PROCEDURES.updateProcedure(getConfiguration().getSelectedYear(), procedure.getId(), dialog.getProcedureName(), dialog.getGroupName(), dialog.getTransactions());
            getConfiguration().update(Configuration.PROCEDURE_UPDATED);
        }
    }
}
