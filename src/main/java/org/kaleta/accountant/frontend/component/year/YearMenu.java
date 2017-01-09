package org.kaleta.accountant.frontend.component.year;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.InitConfigurableAction;
import org.kaleta.accountant.frontend.action.configuration.ConfigurationAction;
import org.kaleta.accountant.frontend.action.configuration.YearMenuYearAdded;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.List;

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
        this.getActionMap().put(Configuration.YEAR_SELECTED, new ConfigurationAction(this) {
            @Override
            protected void actionPerformed() {
                List<String> years = Service.YEAR.getYearNames();
                YearMenu.this.setText("Year: " + years.get(getConfiguration().getSelectedYear()));
                if (years.size() - 1 == getConfiguration().getSelectedYear()){
                    YearMenu.this.setForeground(Color.GREEN.darker());
                } else {
                    YearMenu.this.setForeground(Color.BLACK);
                }
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
