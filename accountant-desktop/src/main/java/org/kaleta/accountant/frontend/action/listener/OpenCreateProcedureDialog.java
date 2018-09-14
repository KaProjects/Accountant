package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.frontend.Configurable;

public class OpenCreateProcedureDialog extends org.kaleta.accountant.frontend.action.menu.OpenCreateProcedureDialog {

    public OpenCreateProcedureDialog(Configurable configurable) {
        super(configurable.getConfiguration());
    }
}
