package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.service.Service;

import java.awt.*;

/**
 * Renames an account. The id keeps its meaning, only the label changes.
 * <p>
 * Several accounts exist only to carry another account's numbers: the consumption account of a
 * resource, the accumulated depreciation and depreciation accounts of an asset, the creation and
 * revaluation accounts of a financial asset. Each is created as a fixed prefix plus its owner's
 * name, so they are renamed along with the owner - leaving them behind would give the same thing
 * several different names.
 */
public class RenameAccountAction extends ActionListener {
    private final AccountsModel.Account account;

    public RenameAccountAction(Configurable configurable, AccountsModel.Account account) {
        super(configurable);
        this.account = account;
    }

    @Override
    protected void actionPerformed() {
        String year = getConfiguration().getSelectedYear();
        String currentName = account.getName();
        String newName = NamePrompt.ask((Component) getConfiguration(), "Rename " + account.getFullId(),
                "Name of account " + account.getFullId() + ":", currentName);
        if (newName == null || newName.equals(currentName)) {
            return;
        }

        Service.ACCOUNT.renameAccountWithRelated(year, account, newName);

        getConfiguration().update(Configuration.ACCOUNT_UPDATED);
    }

}
