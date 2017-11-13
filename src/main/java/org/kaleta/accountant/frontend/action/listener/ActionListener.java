package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.SwingWorkerHandler;

import java.awt.event.ActionEvent;

/**
 * Basic class for every action.
 */
abstract class ActionListener implements java.awt.event.ActionListener {
    private final Configurable configurable;

    ActionListener(Configurable configurable){
        this.configurable = configurable;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        new SwingWorkerHandler() {
            @Override
            protected void runInBackground() {
                actionPerformed();
            }
        }.execute();
    }

    protected abstract void actionPerformed();

    Configuration getConfiguration() {
        return configurable.getConfiguration();
    }
}
