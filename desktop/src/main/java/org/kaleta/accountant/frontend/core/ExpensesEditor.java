package org.kaleta.accountant.frontend.core;

import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.component.AccountsEditor;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class ExpensesEditor extends JPanel implements Configurable {
    private Configuration configuration;

    private final AccountsEditor accountsEditor;

    public ExpensesEditor(Configuration configuration) {
        setConfiguration(configuration);

        accountsEditor = new AccountsEditor(configuration);
        accountsEditor.resetEditor(getEditorSchemaClass());
        JScrollPane paneGroups = new JScrollPane(accountsEditor);

        this.setLayout(new GridLayout(1,1));
        this.add(paneGroups);

        this.getActionMap().put(Configuration.SCHEMA_UPDATED, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accountsEditor.resetEditor(getEditorSchemaClass());
            }
        });
        this.getActionMap().put(Configuration.ACCOUNT_UPDATED, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accountsEditor.updateEditor();
            }
        });
    }

    private SchemaModel.Class getEditorSchemaClass(){
        SchemaModel.Class clazz = Service.SCHEMA.getSchemaClassMap(configuration.getSelectedYear()).get(5);
        List<SchemaModel.Class.Group> tempGroups = new ArrayList<>();
        tempGroups.addAll(clazz.getGroup());
        for (SchemaModel.Class.Group group : tempGroups){
            if (group.getId().equals(Constants.Schema.CONSUMPTION_GROUP_ID) || group.getId().equals(Constants.Schema.DEPRECIATION_GROUP_ID)) {
                clazz.getGroup().remove(group);
            }
        }
        return clazz;
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
