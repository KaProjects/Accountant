package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.action.menu.OpenAddFinAssetDialog;
import org.kaleta.accountant.frontend.component.accounts.AccountsEditorRules;
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
 * Some accounts are more than a name and open a dialog of their own: a credit account, which comes
 * with the procedure that repays it, a current account, with the procedure that withdraws cash from
 * it, and a long-term financial asset, with the three accounts it is booked through and the
 * procedure that pays into it. A resource needs nothing extra - naming it is enough, and its
 * consumption mirror is written along with it.
 */
public class CreateAnalyticalAccountAction extends ActionListener {
    private final Configurable configurable;
    private final String schemaId;
    private final boolean suggestGeneral;
    private Runnable afterCreate = () -> { };

    public CreateAnalyticalAccountAction(Configurable configurable, String schemaId, boolean suggestGeneral) {
        super(configurable);
        this.configurable = configurable;
        this.schemaId = schemaId;
        this.suggestGeneral = suggestGeneral;
    }

    /**
     * What to do once an account has actually been created. A list that shows accounts cannot wait
     * for this: the work runs off the event thread and behind a dialog of its own, so it is told
     * when it is done rather than guessing from the window coming back to the front.
     */
    public CreateAnalyticalAccountAction onCreated(Runnable action) {
        this.afterCreate = action;
        return this;
    }

    @Override
    protected void actionPerformed() {
        if (!AccountsEditorRules.canCreateAccount(schemaId)) {
            return; // the app fills these itself
        }
        if (schemaId.startsWith(Constants.Schema.FIN_ASSET_SCHEMA_PREFIX)) {
            // a financial asset is opened with the accounts it is booked through and its procedure,
            // so the schema account clicked here only says which type the dialog starts on
            if (new OpenAddFinAssetDialog(getConfiguration()).createFinancialAsset(schemaId.substring(2))) {
                afterCreate.run();
            }
            return;
        }
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
        // over the list the account is being opened from, not over the app behind it
        String name = NamePrompt.ask(parentComponent(), "New Account under " + schemaId,
                "Name of the new account:", suggestGeneral ? Constants.Account.GENERAL_ACCOUNT_NAME : null);
        if (name != null) {
            // the creation itself - opening transaction, consumption mirror - lives with the older
            // action, which the account picker also uses
            new AccountsEditorAccountAction(configurable, schemaId, null).subactionPerformed(name);
            afterCreate.run();
        }
    }

    private Component parentComponent() {
        return configurable instanceof Component ? (Component) configurable : (Component) getConfiguration();
    }

    private void createWithProcedure(String title, String heading, String accountLabel, String amountLabel, String otherSchemaId) {
        String year = getConfiguration().getSelectedYear();
        AddAccountWithProcedureDialog dialog = new AddAccountWithProcedureDialog(getConfiguration(), title, heading,
                accountLabel, amountLabel, accountsOf(year, otherSchemaId), schemaOf(year, otherSchemaId));
        dialog.setVisible(true);
        if (dialog.getResult()) {
            new AccountsEditorAccountAction(configurable, schemaId, null)
                    .subactionPerformed(dialog.getAccName(), dialog.getOtherAccount(), dialog.getAmount());
            afterCreate.run();
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
