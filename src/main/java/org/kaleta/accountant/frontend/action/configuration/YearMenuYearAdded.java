package org.kaleta.accountant.frontend.action.configuration;

import org.kaleta.accountant.frontend.action.menu.MenuAction;
import org.kaleta.accountant.frontend.common.MenuItemWrapper;
import org.kaleta.accountant.frontend.component.year.YearMenu;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Stanislav Kaleta on 05.01.2017.
 */
public class YearMenuYearAdded extends ConfigurationAction {
    private YearMenu menu;

    public YearMenuYearAdded(YearMenu menu) {
        super(menu);
        this.menu = menu;
    }

    @Override
    protected void actionPerformed() {
        menu.removeAll();
        for (String year : Service.CONFIG.getYears()){
            JMenuItem menuItem = new MenuItemWrapper(new MenuAction(getConfiguration(), year) {
                @Override
                protected void actionPerformed() {
                    getConfiguration().selectYear(year);
                }
            });
            if (year.equals(Service.CONFIG.getActiveYear())){
                menuItem.setForeground(Color.GREEN.darker());
            }
            menu.add(menuItem);
        }
    }
}
