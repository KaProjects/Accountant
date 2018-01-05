package org.kaleta.accountant.frontend.dialog;

import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.component.HintValidatedTextField;

public class YearClosingDialog extends Dialog {
    private HintValidatedTextField tfNewYearName;

    // TODO post 1.0 : manage proceed button
    // TODO post 1.0 : to decide how select acc. to add + open & import porcedures

    public YearClosingDialog(Configuration configuration) {
        super(configuration, "Closing year", "Proceed");


        buildDialogContent();
        pack();
    }

    private void buildDialogContent() {

        validateDialog();
    }




}
