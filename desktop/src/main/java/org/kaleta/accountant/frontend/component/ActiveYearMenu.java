package org.kaleta.accountant.frontend.component;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.InitConfigurableAction;
import org.kaleta.accountant.frontend.action.configuration.ActiveYearMenuYearSelected;

import javax.swing.*;

public class ActiveYearMenu extends JMenu implements Configurable {
    private Configuration configuration;

    public ActiveYearMenu(String title) {
        super(title);
        this.getActionMap().put(Configuration.INIT_CONFIG, new InitConfigurableAction(this));
        this.getActionMap().put(Configuration.YEAR_SELECTED, new ActiveYearMenuYearSelected(this));
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
