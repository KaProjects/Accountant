package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configurable;

import java.awt.*;

/**
 * Opens a new account under a schema account, asking for its name first.
 * <p>
 * The first account of a schema account is almost always the general one, so that name is offered
 * as the default; every later one starts empty.
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
        String name = NamePrompt.ask((Component) getConfiguration(), "New Account under " + schemaId,
                "Name of the new account:", suggestGeneral ? Constants.Account.GENERAL_ACCOUNT_NAME : null);
        if (name != null) {
            // the creation itself - opening transaction, consumption mirror - lives with the older
            // action, which the account picker also uses
            new AccountsEditorAccountAction(configurable, schemaId, null).subactionPerformed(name);
        }
    }
}
