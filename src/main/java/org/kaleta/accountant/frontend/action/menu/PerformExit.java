package org.kaleta.accountant.frontend.action.menu;

import org.kaleta.accountant.frontend.Configuration;

/**
 * Created by Stanislav Kaleta on 19.04.2016.
 */
public class PerformExit extends MenuAction{

    public PerformExit(Configuration configuration) {
        super(configuration, "Exit");
    }

    @Override
    protected void actionPerformed() {
        System.exit(0);
    }
}
