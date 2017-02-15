package org.kaleta.accountant.frontend.action.configuration;

import org.kaleta.accountant.frontend.component.year.component.AccountsEditor;
import org.kaleta.accountant.frontend.component.year.component.SchemaEditor;
import org.kaleta.accountant.frontend.component.year.component.SchemaOverview;
import org.kaleta.accountant.frontend.component.year.component.YearPane;

import javax.swing.*;

/**
 * Created by Stanislav Kaleta on 10.01.2017.
 */
public class YearPaneYearSelected extends ConfigurationAction {
    private YearPane pane;

    public YearPaneYearSelected(YearPane pane) {
        super(pane);
        this.pane = pane;
    }

    @Override
    protected void actionPerformed() {
        pane.removeAll();
        SchemaOverview schemaOverview = new SchemaOverview();
        schemaOverview.setConfiguration(getConfiguration());
        schemaOverview.update();

        if (pane.getConfiguration().getModel().isActive()){
            JTabbedPane schemaPane = new JTabbedPane();
            pane.addTab("Schema", schemaPane);
            schemaPane.addTab("Overview", new JScrollPane(schemaOverview));
            SchemaEditor schemaEditor = new SchemaEditor(getConfiguration());
            schemaEditor.update();
            schemaPane.addTab("Editor", new JScrollPane(schemaEditor));

            AccountsEditor accountsEditor = new AccountsEditor();
            accountsEditor.setConfiguration(getConfiguration());
            accountsEditor.update();
            pane.add("Accounts", accountsEditor);
        } else {
            pane.addTab("Schema", new JScrollPane(schemaOverview));
        }
        pane.revalidate();
        pane.repaint();
    }
}
