package org.kaleta.accountant.frontend.dialog;

import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.component.HintValidatedTextField;

public class YearClosingDialog extends Dialog {
    private HintValidatedTextField tfNewYearName;

    // TODO: 12/20/17 manage proceed button
    // TODO: 12/20/17 to decide how select acc. to add + open & import porcedures

    public YearClosingDialog(Configuration configuration) {
        super(configuration, "Closing year", "Proceed");


        buildDialogContent();
        pack();
    }

    private void buildDialogContent() {

        validateDialog();
    }




}
