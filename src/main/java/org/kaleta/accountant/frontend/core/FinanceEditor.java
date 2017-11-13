package org.kaleta.accountant.frontend.core;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.component.AccountsEditor;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class FinanceEditor extends JPanel implements Configurable {
    private Configuration configuration;

    private final AccountsEditor accountsEditor;

    public FinanceEditor(Configuration configuration) {
        setConfiguration(configuration);

        accountsEditor = new AccountsEditor(configuration);
        accountsEditor.resetEditor(Service.SCHEMA.getSchemaClassMap(configuration.getSelectedYear()).get(2));
        JScrollPane paneGroups = new JScrollPane(accountsEditor);

        this.setLayout(new GridLayout(1,1));
        this.add(paneGroups);

        this.getActionMap().put(Configuration.SCHEMA_UPDATED, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accountsEditor.resetEditor(Service.SCHEMA.getSchemaClassMap(configuration.getSelectedYear()).get(2));
            }
        });
        this.getActionMap().put(Configuration.ACCOUNT_UPDATED, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accountsEditor.updateEditor();
            }
        });
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }
}
