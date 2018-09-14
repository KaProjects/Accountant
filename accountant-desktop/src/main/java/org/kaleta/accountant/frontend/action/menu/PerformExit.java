package org.kaleta.accountant.frontend.action.menu;

import org.kaleta.accountant.frontend.Configuration;

public class PerformExit extends MenuAction{

    public PerformExit(Configuration configuration) {
        super(configuration, "Exit");
    }

    @Override
    protected void actionPerformed() {
        System.exit(0);
    }
}
