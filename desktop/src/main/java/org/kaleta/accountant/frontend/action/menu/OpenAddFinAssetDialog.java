package org.kaleta.accountant.frontend.action.menu;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.ProceduresModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.dialog.AddFinAssetDialog;
import org.kaleta.accountant.service.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creates a long-term financial asset: the asset account itself, the three accounts it is booked
 * through, and the procedure that pays into it.
 * <p>
 * The procedure is written here rather than left to the user because everything it needs is already
 * known at this point - it always debits the asset's own creation account - except the account the
 * money comes from and the amount, which is what the dialog asks for.
 */
public class OpenAddFinAssetDialog extends MenuAction {

    public OpenAddFinAssetDialog(Configuration config) {
        super(config, "Add Financial Asset");
    }

    @Override
    protected void actionPerformed() {
        createFinancialAsset(null);
    }

    /**
     * Runs the whole flow and says whether an asset was created, so that a list of accounts which
     * opened this dialog can show what came out of it. Where it was opened from one of the 23x
     * schema accounts, that one is the type it starts on.
     *
     * @param schemaAccountId the 23x schema account to start on, or null to let the user pick
     */
    public boolean createFinancialAsset(String schemaAccountId) {
        String year = getConfiguration().getSelectedYear();

        List<SchemaModel.Class.Group.Account> schemaAccounts = new ArrayList<>(
                Service.SCHEMA.getSchemaAccountMap(Service.SCHEMA.getGroup(year, "2", "3")).values());

        AddFinAssetDialog dialog = new AddFinAssetDialog(getConfiguration(), schemaAccounts,
                paymentAccounts(year), paymentClasses(year), schemaAccountId);
        dialog.setVisible(true);
        if (!dialog.getResult()) {
            return false;
        }

        String name = dialog.getAccName();
        String schemaId = Constants.Schema.FIN_ASSET_SCHEMA_PREFIX + dialog.getSchemaAccountId();
        String semanticId = Service.ACCOUNT.getNextSemanticId(year, schemaId);

        Service.ACCOUNT.createAccount(year, name, schemaId, semanticId, "");
        Service.TRANSACTIONS.addTransaction(year, "0101", "0", schemaId + "." + semanticId,
                Constants.Account.INIT_ACC_ID, Constants.Transaction.OPEN_DESCRIPTION);

        String extendedSemanticId = dialog.getSchemaAccountId() + "-" + semanticId;

        Service.ACCOUNT.createAccount(year, Constants.Schema.FIN_CREATION_ACCOUNT_PREFIX + name,
                Constants.Schema.FIN_CREATION_FULL_ID, extendedSemanticId, "");
        Service.ACCOUNT.createAccount(year, Constants.Schema.FIN_EXP_REVALUATION_ACCOUNT_PREFIX + name,
                Constants.Schema.FIN_EXP_REVALUATION_FULL_ID, extendedSemanticId, "");
        Service.ACCOUNT.createAccount(year, Constants.Schema.FIN_REV_REVALUATION_ACCOUNT_PREFIX + name,
                Constants.Schema.FIN_REV_REVALUATION_FULL_ID, extendedSemanticId, "");

        createCreationProcedure(year, name, extendedSemanticId, dialog.getAmount(), dialog.getCreditAccount());

        getConfiguration().update(Configuration.ACCOUNT_UPDATED);
        getConfiguration().update(Configuration.TRANSACTION_UPDATED);
        getConfiguration().update(Configuration.PROCEDURE_UPDATED);
        return true;
    }

    /**
     * Paying into the asset is one transaction: out of the account the money comes from, into the
     * asset's creation account, which is where every payment into a financial asset is booked.
     */
    private void createCreationProcedure(String year, String name, String extendedSemanticId, String amount, String creditAccount) {
        ProceduresModel.Group.Procedure.Transaction transaction = new ProceduresModel.Group.Procedure.Transaction();
        transaction.setDescription(Constants.Schema.FIN_CREATION_ACCOUNT_PREFIX + name);
        transaction.setAmount(amount);
        transaction.setDebit(Constants.Schema.FIN_CREATION_FULL_ID + "." + extendedSemanticId);
        transaction.setCredit(creditAccount);

        Service.PROCEDURES.createManagedProcedure(year, Constants.Schema.FIN_CREATION_ACCOUNT_PREFIX + name,
                Constants.Procedure.FIN_CREATION_GROUP_NAME, Collections.singletonList(transaction));
    }

    /** Accounts the money can come from: anything but the asset, resource, expense and off balance classes. */
    private Map<String, List<AccountsModel.Account>> paymentAccounts(String year) {
        Map<String, List<AccountsModel.Account>> accountMap = Service.ACCOUNT.getAccountsViaSchemaMap(year);
        for (String schemaId : new HashSet<>(accountMap.keySet())) {
            if (schemaId.startsWith("0") || schemaId.startsWith("1") || schemaId.startsWith("5") || schemaId.startsWith("7")) {
                accountMap.remove(schemaId);
            }
        }
        return accountMap;
    }

    private List<SchemaModel.Class> paymentClasses(String year) {
        List<SchemaModel.Class> classes = new ArrayList<>();
        for (int classId : new int[]{2, 3, 4, 6}) {
            classes.add(Service.SCHEMA.getSchemaClassMap(year).get(classId));
        }
        return classes;
    }
}
