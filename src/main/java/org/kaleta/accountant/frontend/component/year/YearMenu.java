package org.kaleta.accountant.frontend.component.year;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.InitConfigurableAction;
import org.kaleta.accountant.frontend.action.configuration.YearMenuYearAdded;
import org.kaleta.accountant.frontend.action.configuration.YearMenuYearSelected;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * Created by Stanislav Kaleta on 05.01.2017.
 */
public class YearMenu extends JMenu implements Configurable {
    private Configuration configuration;

    public YearMenu(){
        super("Year");
        this.setMnemonic(KeyEvent.VK_Y);
        this.getActionMap().put(Configuration.INIT_CONFIG, new InitConfigurableAction(this));
        this.getActionMap().put(Configuration.YEAR_ADDED, new YearMenuYearAdded(this));
        this.getActionMap().put(Configuration.YEAR_SELECTED, new YearMenuYearSelected(this));
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
