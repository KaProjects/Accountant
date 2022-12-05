package org.kaleta.accountant.frontend.action.configuration;

import org.kaleta.accountant.frontend.component.ActiveYearMenu;
import org.kaleta.accountant.service.Service;

public class ActiveYearMenuYearSelected extends ConfigurationAction {
    private ActiveYearMenu menu;

    public ActiveYearMenuYearSelected(ActiveYearMenu menu) {
        super(menu);
        this.menu = menu;
    }

    @Override
    protected void actionPerformed() {
        menu.setEnabled(getConfiguration().getSelectedYear().equals(Service.CONFIG.getActiveYear()));
    }
}
