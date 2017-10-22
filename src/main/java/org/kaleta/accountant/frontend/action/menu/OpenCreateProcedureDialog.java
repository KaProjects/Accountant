package org.kaleta.accountant.frontend.action.menu;

import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.dep.dialog.transaction.CreateProcedureDialog;
import org.kaleta.accountant.service.Service;

/**
 * Created by Stanislav Kaleta on 24.05.2016.
 */
@Deprecated
public class OpenCreateProcedureDialog extends MenuAction {
    public OpenCreateProcedureDialog(Configuration config) {
        super(config, "Create Procedure");
    }

    @Override
    protected void actionPerformed() {
        CreateProcedureDialog dialog = new CreateProcedureDialog(getConfiguration());
        dialog.setVisible(true);
        if (dialog.getResult()) {
            Service.DEPACCOUNT.createProcedure(dialog.getProcedure());
        }
    }
}
