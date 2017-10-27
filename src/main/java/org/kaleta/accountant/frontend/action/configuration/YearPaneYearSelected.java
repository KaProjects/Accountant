package org.kaleta.accountant.frontend.action.configuration;

import org.kaleta.accountant.frontend.component.*;
import org.kaleta.accountant.frontend.year.component.ResourcesEditor;
import org.kaleta.accountant.service.Service;

import javax.swing.*;

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
        AccountsOverview accountsOverview = new AccountsOverview();
        accountsOverview.setConfiguration(getConfiguration());
        accountsOverview.update();

        if (pane.getConfiguration().getSelectedYear().equals(Service.CONFIG.getActiveYear())){
            JTabbedPane schemaPane = new JTabbedPane();
            pane.addTab("Schema", schemaPane);
            schemaPane.addTab("Overview", new JScrollPane(schemaOverview));
            SchemaEditor schemaEditor = new SchemaEditor(getConfiguration());
            schemaEditor.update();
            schemaPane.addTab("Editor", new JScrollPane(schemaEditor));

            JTabbedPane accountsPane = new JTabbedPane();
            pane.addTab("Accounts", accountsPane);
            accountsPane.addTab("Overview", new JScrollPane(accountsOverview));
            AssetsEditor assetsEditor = new AssetsEditor(getConfiguration());
            assetsEditor.update();
            accountsPane.add("Assets", assetsEditor);
            ResourcesEditor resourcesEditor = new ResourcesEditor(getConfiguration());
            accountsPane.add("Resources", resourcesEditor);
        } else {
            pane.addTab("Schema", new JScrollPane(schemaOverview));
            pane.addTab("Accounts", new JScrollPane(accountsOverview));
        }
        pane.revalidate();
        pane.repaint();
    }
}
