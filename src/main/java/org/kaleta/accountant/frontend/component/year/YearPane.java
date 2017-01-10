package org.kaleta.accountant.frontend.component.year;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.InitConfigurableAction;
import org.kaleta.accountant.frontend.action.configuration.YearPaneYearSelected;

import javax.swing.*;

/**
 * Created by Stanislav Kaleta on 05.01.2017.
 */
public class YearPane extends JTabbedPane implements Configurable{
    private Configuration configuration;

    public YearPane(){

        this.getActionMap().put(Configuration.INIT_CONFIG, new InitConfigurableAction(this));
        this.getActionMap().put(Configuration.YEAR_SELECTED, new YearPaneYearSelected(this));
//        this.getActionMap().put(Configuration.SCHEMA_UPDATED, new ConfigurationAction(this) {
//            @Override
//            protected void actionPerformed() {
//                // update model's schema
//                // update components which use schema (schemaEditor.updateOverview,schemaEditor.updateEditor(configuration),...)
//            }
//        });
    }

    public void update(YearModel model){
        this.removeAll();

        SchemaEditor schemaEditor = new SchemaEditor(model);
        if (model.isActive()){
            JTabbedPane schemaPane = new JTabbedPane();
            this.addTab("Schema", schemaPane);
            schemaPane.addTab("Overview", new JScrollPane(schemaEditor.getOverview()));
            schemaEditor.updateOverview();
            schemaPane.addTab("Editor", new JScrollPane(schemaEditor.getEditor()));
            schemaEditor.updateEditor(this);
        } else {
            this.addTab("Schema", new JScrollPane(schemaEditor.getOverview()));
            schemaEditor.updateOverview();
        }



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
