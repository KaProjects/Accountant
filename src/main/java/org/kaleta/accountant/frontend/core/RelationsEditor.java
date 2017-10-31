package org.kaleta.accountant.frontend.core;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.year.component.AccountsEditor;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class RelationsEditor extends JPanel implements Configurable {
    private Configuration configuration;

    private AccountsEditor accountsEditor;

    public RelationsEditor(Configuration configuration) {
        setConfiguration(configuration);

        JButton buttonAddItems = new JButton("Add Items");


        accountsEditor = new AccountsEditor(configuration);
        accountsEditor.resetEditor(Service.SCHEMA.getSchemaClassMap(configuration.getSelectedYear()).get(3));
        JScrollPane paneGroups = new JScrollPane(accountsEditor);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                        .addComponent(buttonAddItems))
                .addComponent(paneGroups));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                        .addComponent(buttonAddItems, 25, 25, 25)).addGap(4)
                .addComponent(paneGroups));

        this.getActionMap().put(Configuration.SCHEMA_UPDATED, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accountsEditor.resetEditor(Service.SCHEMA.getSchemaClassMap(configuration.getSelectedYear()).get(3));
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
