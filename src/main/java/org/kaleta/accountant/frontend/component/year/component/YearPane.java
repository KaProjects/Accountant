package org.kaleta.accountant.frontend.component.year.component;

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
