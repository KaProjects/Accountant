package org.kaleta.accountant.frontend.dialog;

import org.kaleta.accountant.frontend.Configuration;

public class YearClosingDialog extends Dialog {

    public YearClosingDialog(Configuration configuration) {
        super(configuration, "Closing year", "Continue");
    }

    private void closeAccounts(){
        //E/R -> profit
        // A/L -> balance
        // profit -> balance
    }

    private void initYear(){
        // init files
        // import schema
        // set as active
    }

    private void openAccounts(){
        // non-zero A/L -> add + open
        // zero A/L -> to choose -> if yes -> add + open
        // E/R -> to choose -> if yes -> add (just!)

        //if 0** has 0 balance (09*-* should have also 0) =>
    }
}
