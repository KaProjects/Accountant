package org.kaleta.accountant.frontend.action.configuration;

import org.kaleta.accountant.frontend.component.YearMenu;
import org.kaleta.accountant.service.Service;

import java.awt.*;

public class YearMenuYearSelected extends ConfigurationAction {
    private YearMenu menu;

    public YearMenuYearSelected(YearMenu menu) {
        super(menu);
        this.menu = menu;
    }

    @Override
    protected void actionPerformed() {
        menu.setText("Year: " + getConfiguration().getSelectedYear());
        if (getConfiguration().getSelectedYear().equals(Service.CONFIG.getActiveYear())){
            menu.setForeground(Color.GREEN.darker());
        } else {
            menu.setForeground(Color.BLACK);
        }
    }
}
