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
        List<Integer> yearIds = Service.YEAR.getYearIds();
        for (Integer yearId : yearIds){
            JMenuItem menuItem = new MenuItemWrapper(new MenuAction(getConfiguration(), String.valueOf(yearId)) {
                @Override
                protected void actionPerformed() {
                    getConfiguration().selectYear(yearId);
                }
            });
            if (yearIds.indexOf(yearId) == yearIds.size() - 1){
                menuItem.setForeground(Color.GREEN.darker());
            }
            menu.add(menuItem);
        }
        getConfiguration().selectYear(yearIds.get(yearIds.size() - 1));
    }
}
