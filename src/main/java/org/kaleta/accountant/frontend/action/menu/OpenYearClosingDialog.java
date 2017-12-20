package org.kaleta.accountant.frontend.action.menu;

import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.dialog.YearClosingDialog;

public class OpenYearClosingDialog extends MenuAction {

    public OpenYearClosingDialog(Configuration config) {
        super(config, "Close Year");
    }

    @Override
    protected void actionPerformed() {


        YearClosingDialog dialog = new YearClosingDialog(getConfiguration());
        dialog.setVisible(true);
        if (dialog.getResult()){



        }
    }
}
