package org.kaleta.accountant.frontend.action.configuration;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.SwingWorkerHandler;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Basic class for every action which impacts on configuration.
 */
public abstract class ConfigurationAction extends AbstractAction {
    private final Configurable configurable;

    public ConfigurationAction(Configurable configurable){
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