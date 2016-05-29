package org.kaleta.accountant.frontend.action.menu;

import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.dialog.procedure.CreateProcedureDialog;
import org.kaleta.accountant.service.Service;

import java.awt.Component;

/**
 * Created by Stanislav Kaleta on 24.05.2016.
 */
public class OpenCreateProcedureDialog extends MenuAction {
    public OpenCreateProcedureDialog(Configuration config) {
        super(config, "Create Procedure");
    }

    @Override
    protected void actionPerformed() {
        CreateProcedureDialog dialog = new CreateProcedureDialog((Component) getConfiguration());
        dialog.setVisible(true);
        if (dialog.getResult()) {
            Service.ACCOUNT.createProcedure(dialog.getProcedure());
        }
    }
}
