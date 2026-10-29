package org.kaleta.accountant.frontend.action.menu;

import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.dialog.YearClosingDialog;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Closes the year that is open and starts the next one.
 * <p>
 * The dialog works out what would happen and asks what the new year should start with; this action
 * only performs what was agreed there, and says what it did. Everything it writes - the closing
 * entries, the new year's accounts and their opening balances, the procedures that had to be
 * corrected - is written by the closing service in that order, once.
 */
public class OpenYearClosingDialog extends MenuAction {

    public OpenYearClosingDialog(Configuration config) {
        super(config, "Close Year");
    }

    @Override
    protected void actionPerformed() {
        String year = Service.CONFIG.getActiveYear();
        if (!year.equals(getConfiguration().getSelectedYear())) {
            JOptionPane.showMessageDialog((Frame) getConfiguration(),
                    "Only the year that is open can be closed, and that is " + year + ".",
                    "Closing a year", JOptionPane.WARNING_MESSAGE);
            return;
        }

        YearClosingDialog dialog = new YearClosingDialog(getConfiguration(), year);
        dialog.setVisible(true);
        if (!dialog.getResult()) {
            return;
        }

        List<String> done = Service.CLOSING.close(dialog.getPlan(), dialog.getNewYear(), dialog.getCarried());

        getConfiguration().update(Configuration.YEAR_ADDED);
        getConfiguration().update(Configuration.ACCOUNT_UPDATED);
        getConfiguration().update(Configuration.TRANSACTION_UPDATED);
        getConfiguration().update(Configuration.PROCEDURE_UPDATED);
        getConfiguration().selectYear(dialog.getNewYear());

        JOptionPane.showMessageDialog((Frame) getConfiguration(),
                String.join("\n", done), "Year " + year + " closed", JOptionPane.INFORMATION_MESSAGE);
    }
}
