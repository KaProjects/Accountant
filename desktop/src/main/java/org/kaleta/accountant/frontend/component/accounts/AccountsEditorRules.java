package org.kaleta.accountant.frontend.component.accounts;

import org.kaleta.accountant.common.Constants;

/**
 * Says which schema accounts the user may open accounts under.
 * <p>
 * Most of the chart is the user's own, but some schema accounts are filled by the app itself and
 * are not places to add anything by hand: the mirrors of the asset and resource classes, the off
 * balance machinery, and the financial asset accounts that buying or revaluing an asset writes
 * into. The editors leave those alone, and the create action refuses them, so the rule lives here
 * rather than in either of them.
 */
public final class AccountsEditorRules {

    private AccountsEditorRules() {
        // static members only
    }

    /**
     * True for a schema account whose accounts the app creates itself, named after the asset they
     * belong to: the financial asset creation and revaluation accounts.
     */
    public static boolean isAppMaintained(String schemaId) {
        return schemaId.equals(Constants.Schema.FIN_CREATION_FULL_ID)
                || schemaId.equals(Constants.Schema.FIN_EXP_REVALUATION_FULL_ID)
                || schemaId.equals(Constants.Schema.FIN_REV_REVALUATION_FULL_ID);
    }

    /** True if the user may open a new account under this schema account. */
    public static boolean canCreateAccount(String schemaId) {
        if (isAppMaintained(schemaId)) {
            return false;
        }
        // assets and off balance are opened by their own flows, the rest mirror another class
        return !schemaId.startsWith("0")
                && !schemaId.startsWith("7")
                && !schemaId.startsWith("5" + Constants.Schema.DEPRECIATION_GROUP_ID)
                && !schemaId.startsWith("5" + Constants.Schema.CONSUMPTION_GROUP_ID);
    }
}
