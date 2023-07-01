package org.kaleta.accountant.frontend.action.menu;

import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.service.Service;

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
    }
}
