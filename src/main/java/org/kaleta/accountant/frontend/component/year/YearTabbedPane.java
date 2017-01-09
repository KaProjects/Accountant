package org.kaleta.accountant.frontend.component.year;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.InitConfigurableAction;
import org.kaleta.accountant.frontend.action.configuration.ConfigurationAction;
import org.kaleta.accountant.service.Service;

import javax.swing.*;

/**
 * Created by Stanislav Kaleta on 05.01.2017.
 */
public class YearTabbedPane extends JTabbedPane implements Configurable{
    private Configuration configuration;

    public YearTabbedPane(){




        this.getActionMap().put(Configuration.INIT_CONFIG, new InitConfigurableAction(this));
        this.getActionMap().put(Configuration.YEAR_SELECTED, new ConfigurationAction(this) {
            @Override
            protected void actionPerformed() {
                YearTabbedPane.this.update(Service.YEAR.getYearModel(getConfiguration().getSelectedYear()));
            }
        });
    }

    public void update(YearModel model){
        this.removeAll();

        SchemaEditor schemaEditor = new SchemaEditor(model.getSchema());
        if (model.isActive()){
            JTabbedPane schemaPane = new JTabbedPane();
            this.addTab("Schema", schemaPane);
            schemaPane.addTab("Overview", new JScrollPane(schemaEditor.getOverview()));
            schemaPane.addTab("Editor", new JScrollPane(schemaEditor.getEditor()));
        } else {
            this.addTab("Schema", /*new JScrollPane(*/schemaEditor.getOverview()/*)*/);
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
