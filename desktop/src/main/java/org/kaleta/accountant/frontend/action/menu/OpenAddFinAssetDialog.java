package org.kaleta.accountant.frontend.action.menu;

import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class OpenAddFinAssetDialog extends MenuAction {

    public OpenAddFinAssetDialog(Configuration config) {
        super(config, "Add Financial Asset");
    }

    @Override
    protected void actionPerformed() {
        String name = JOptionPane.showInputDialog((Component) getConfiguration(), "Set Account Name: ",
                "Creating Long-Term Financial Asset", JOptionPane.PLAIN_MESSAGE);

        String year = getConfiguration().getSelectedYear();
       List<SchemaModel.Class.Group.Account> schemaAccounts = new ArrayList<>(Service.SCHEMA.getSchemaAccountMap(Service.SCHEMA.getGroup(year, "2", "3")).values());
       List<String> options = new ArrayList<>();
       for (SchemaModel.Class.Group.Account account: schemaAccounts){
           options.add(account.getName());
        }
        int selected = JOptionPane.showOptionDialog((Component) getConfiguration(), "Set Account Group: ", "Creating Long-Term Financial Asset",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options.toArray(), options.toArray()[0]);

        if (name != null && !name.trim().isEmpty()) {

            String schemaAccountId = schemaAccounts.get(selected).getId();
            String schemaId = "23" + schemaAccountId;
            String semanticId = Service.ACCOUNT.getNextSemanticId(year, schemaId);

            Service.ACCOUNT.createAccount(year, name, schemaId, semanticId, "");
            Service.TRANSACTIONS.addTransaction(year, "0101", "0", schemaId + "." + semanticId, Constants.Account.INIT_ACC_ID, Constants.Transaction.OPEN_DESCRIPTION);

            String extendedSemanticId = schemaAccountId + "-" + semanticId;

            Service.ACCOUNT.createAccount(year, Constants.Schema.FIN_CREATION_ACCOUNT_PREFIX + name,
                    Constants.Schema.FIN_CREATION_FULL_ID, extendedSemanticId, "");
            Service.ACCOUNT.createAccount(year, Constants.Schema.FIN_EXP_REVALUATION_ACCOUNT_PREFIX + name,
                    Constants.Schema.FIN_EXP_REVALUATION_FULL_ID, extendedSemanticId, "");
            Service.ACCOUNT.createAccount(year, Constants.Schema.FIN_REV_REVALUATION_ACCOUNT_PREFIX + name,
                    Constants.Schema.FIN_REV_REVALUATION_FULL_ID, extendedSemanticId, "");

            getConfiguration().update(Configuration.ACCOUNT_UPDATED);
            getConfiguration().update(Configuration.TRANSACTION_UPDATED);
        }
    }
}
