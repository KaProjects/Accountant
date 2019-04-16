package org.kaleta.accountant.frontend.action.menu;

import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.*;
import java.util.Calendar;

public class OpenAddFinAssetDialog extends MenuAction {

    public OpenAddFinAssetDialog(Configuration config) {
        super(config, "Add Financial Asset");
    }

    @Override
    protected void actionPerformed() {
        String name = JOptionPane.showInputDialog((Component) getConfiguration(), "Set Account Name: ",
                "Creating Long-Term Financial Asset", JOptionPane.PLAIN_MESSAGE);

        if (name != null && !name.trim().isEmpty()) {
            String year = getConfiguration().getSelectedYear();
            String schemaId = "230";
            String semanticId = Service.ACCOUNT.getNextSemanticId(year, schemaId);

            Service.ACCOUNT.createAccount(year, name, schemaId, semanticId, "");
            Service.TRANSACTIONS.addTransaction(year, "0101", "0", schemaId + "." + semanticId, Constants.Account.INIT_ACC_ID, Constants.Transaction.OPEN_DESCRIPTION);

            Service.ACCOUNT.createAccount(year, Constants.Schema.FIN_CREATION_ACCOUNT_PREFIX + name,
                    Constants.Schema.FIN_CREATION_FULL_ID, semanticId, "");
            Service.ACCOUNT.createAccount(year, Constants.Schema.FIN_EXP_REVALUATION_ACCOUNT_PREFIX + name,
                    Constants.Schema.FIN_EXP_REVALUATION_FULL_ID, semanticId, "");
            Service.ACCOUNT.createAccount(year, Constants.Schema.FIN_REV_REVALUATION_ACCOUNT_PREFIX + name,
                    Constants.Schema.FIN_REV_REVALUATION_FULL_ID, semanticId, "");

            getConfiguration().update(Configuration.ACCOUNT_UPDATED);
            getConfiguration().update(Configuration.TRANSACTION_UPDATED);
        }
    }
}
