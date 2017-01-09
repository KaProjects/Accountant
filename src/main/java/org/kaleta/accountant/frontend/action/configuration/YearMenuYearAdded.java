package org.kaleta.accountant.frontend.action.configuration;

import org.kaleta.accountant.frontend.action.menu.MenuAction;
import org.kaleta.accountant.frontend.common.MenuItemWrapper;
import org.kaleta.accountant.frontend.component.year.YearMenu;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.Color;
import java.util.List;

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
        List<String> years = Service.YEAR.getYearNames();
        for (String year : years){
            JMenuItem menuItem = new MenuItemWrapper(new MenuAction(menu.getConfiguration(), year) {
                @Override
                protected void actionPerformed() {
                    getConfiguration().setSelectedYear(years.indexOf(year));
                }
            });
            if (years.indexOf(year) == years.size() - 1){
                menuItem.setForeground(Color.GREEN.darker());
            }
            menu.add(menuItem);
        }
        getConfiguration().setSelectedYear(years.size() - 1);
    }
}
