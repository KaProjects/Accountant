package org.kaleta.accountant.frontend.action.menu;

import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.service.Service;

/**
 * Drops every cached model and rebuilds the views from what is on disk.
 * <p>
 * The services keep the parsed XML in memory, so a file changed underneath the running app - by a
 * script, or by another copy of the data - is invisible until the caches go. Dropping them is only
 * half of it: the views hold what they were last given, so each kind of model is announced as
 * updated afterwards, which is what makes them read the files again.
 */
public class InvalidateModels extends MenuAction {

    public InvalidateModels(Configuration config) {
        super(config, "Invalidate Models");
    }

    @Override
    protected void actionPerformed() {
        Service.CONFIG.invalidateModel();
        Service.SCHEMA.invalidateModel();
        Service.ACCOUNT.invalidateModel();
        Service.TRANSACTIONS.invalidateModel();
        Service.PROCEDURES.invalidateModel();

        getConfiguration().update(Configuration.SCHEMA_UPDATED);
        getConfiguration().update(Configuration.ACCOUNT_UPDATED);
        getConfiguration().update(Configuration.TRANSACTION_UPDATED);
        getConfiguration().update(Configuration.PROCEDURE_UPDATED);
    }
}
