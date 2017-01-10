package org.kaleta.accountant.frontend.action.configuration;

import org.kaleta.accountant.frontend.component.year.YearPane;
import org.kaleta.accountant.service.Service;

/**
 * Created by Stanislav Kaleta on 10.01.2017.
 */
public class YearPaneYearSelected extends ConfigurationAction {
    private YearPane pane;

    public YearPaneYearSelected(YearPane pane) {
        super(pane);
        this.pane = pane;
    }

    @Override
    protected void actionPerformed() {
        pane.update(Service.YEAR.getYearModel(getConfiguration().getSelectedYear()));
    }
}
