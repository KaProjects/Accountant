package org.kaleta.accountant.frontend.dialog;

import org.kaleta.accountant.frontend.Configuration;

public class CreateProcedureDialog extends Dialog {

    public CreateProcedureDialog(Configuration configuration) {
        super(configuration, "Creating Procedure", "Create");



        buildDialogContent();
        pack();
    }


    private void buildDialogContent() {
        //new TransactionPanel(getConfiguration(), accountPairDescriptionMap, allAccountMap, classList);
    }
}
