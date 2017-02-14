package org.kaleta.accountant.frontend.action.configuration;

import org.kaleta.accountant.frontend.component.year.YearMenu;
import org.kaleta.accountant.service.Service;

import java.awt.Color;
import java.util.List;

/**
 * Created by Stanislav Kaleta on 10.01.2017.
 */
public class YearMenuYearSelected extends ConfigurationAction {
    private YearMenu menu;

    public YearMenuYearSelected(YearMenu menu) {
        super(menu);
        this.menu = menu;
    }

    @Override
    protected void actionPerformed() {
        List<Integer> yearIds = Service.YEAR.getYearIds();
        menu.setText("Year: " + getConfiguration().getModel().getYearId());
        if (yearIds.get(yearIds.size() - 1) == getConfiguration().getModel().getYearId()){
            menu.setForeground(Color.GREEN.darker());
        } else {
            menu.setForeground(Color.BLACK);
        }
    }
}
