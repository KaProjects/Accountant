package org.kaleta.accountant.frontend.core;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.AccountPairModel;
import org.kaleta.accountant.frontend.component.TransactionPanel;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.util.List;
import java.util.Map;

public class TransactionsEditor extends JPanel implements Configurable {
    private Configuration configuration;

    public TransactionsEditor(){
        JButton buttonAddTr = new JButton();
        buttonAddTr.addActionListener(actionEvent -> {

        });

        JButton createProcedure = new JButton();
        buttonAddTr.addActionListener(actionEvent -> {

        });

        // TODO: 10/31/17 procedures list (design & impl backend, service, frontend)




    }

    public void update(){
        Map<String, List<AccountsModel.Account>> allAccountMap = Service.ACCOUNT.getAccountsViaSchemaMap(getConfiguration().getSelectedYear());
        List<SchemaModel.Class> classList = Service.SCHEMA.getSchemaClassList(getConfiguration().getSelectedYear());
        Map<AccountPairModel, List<String>> accountPairDescriptionMap = Service.TRANSACTIONS.getAccountPairDescriptions(getConfiguration().getSelectedYear());

        this.add(new TransactionPanel(getConfiguration(), accountPairDescriptionMap, allAccountMap, classList));
    }





    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    private class ProcedurePanel {


    }
}
