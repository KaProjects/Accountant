package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.dialog.AddAccountWithProcedureDialog;
import org.kaleta.accountant.service.Service;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Opens a new account under a schema account, asking for its name first.
 * <p>
 * The first account of a schema account is almost always the general one, so that name is offered
 * as the default; every later one starts empty.
 * <p>
 * Two kinds of account are asked for more than a name, because the app writes a procedure for them
 * as they are created: a credit account, which is repaid from a current account, and a current
 * account, which is withdrawn from into cash.
 */
public class CreateAnalyticalAccountAction extends ActionListener {
    private final Configurable configurable;
    private final String schemaId;
    private final boolean suggestGeneral;

    public CreateAnalyticalAccountAction(Configurable configurable, String schemaId, boolean suggestGeneral) {
        super(configurable);
        this.configurable = configurable;
        this.schemaId = schemaId;
        this.suggestGeneral = suggestGeneral;
    }

    @Override
    protected void actionPerformed() {
        if (schemaId.startsWith(Constants.Schema.CREDIT_ACCOUNT_SCHEMA_PREFIX)) {
            createWithProcedure("Creating Credit Account", "Repayment procedure", "Repaid from:",
                    "Usual Instalment:", Constants.Schema.CURRENT_ACCOUNT_SCHEMA_ID);
            return;
        }
        if (schemaId.equals(Constants.Schema.CURRENT_ACCOUNT_SCHEMA_ID)) {
            createWithProcedure("Creating Current Account", "Withdrawal procedure", "Withdrawn into:",
                    "Usual Amount:", Constants.Schema.CASH_ACCOUNT_SCHEMA_ID);
            return;
        }
        String name = NamePrompt.ask((Component) getConfiguration(), "New Account under " + schemaId,
                "Name of the new account:", suggestGeneral ? Constants.Account.GENERAL_ACCOUNT_NAME : null);
        if (name != null) {
            // the creation itself - opening transaction, consumption mirror - lives with the older
            // action, which the account picker also uses
            new AccountsEditorAccountAction(configurable, schemaId, null).subactionPerformed(name);
        }
    }

    private void createWithProcedure(String title, String heading, String accountLabel, String amountLabel, String otherSchemaId) {
        String year = getConfiguration().getSelectedYear();
        AddAccountWithProcedureDialog dialog = new AddAccountWithProcedureDialog(getConfiguration(), title, heading,
                accountLabel, amountLabel, accountsOf(year, otherSchemaId), schemaOf(year, otherSchemaId));
        dialog.setVisible(true);
        if (dialog.getResult()) {
            new AccountsEditorAccountAction(configurable, schemaId, null)
                    .subactionPerformed(dialog.getAccName(), dialog.getOtherAccount(), dialog.getAmount());
        }
    }

    /** The accounts of one schema account, which is all the procedure's other side may be. */
    private Map<String, List<AccountsModel.Account>> accountsOf(String year, String otherSchemaId) {
        Map<String, List<AccountsModel.Account>> accountMap = Service.ACCOUNT.getAccountsViaSchemaMap(year);
        accountMap.keySet().removeIf(id -> !id.equals(otherSchemaId));
        return accountMap;
    }

    /** The picker's tree, trimmed to that one schema account so nothing else can be chosen. */
    private List<SchemaModel.Class> schemaOf(String year, String otherSchemaId) {
        SchemaModel.Class clazz = Service.SCHEMA.getSchemaClassMap(year).get(Integer.parseInt(otherSchemaId.substring(0, 1)));
        clazz.getGroup().removeIf(group -> !group.getId().equals(otherSchemaId.substring(1, 2)));
        clazz.getGroup().forEach(group -> group.getAccount().removeIf(account -> !account.getId().equals(otherSchemaId.substring(2, 3))));
        return Collections.singletonList(clazz);
    }
}
