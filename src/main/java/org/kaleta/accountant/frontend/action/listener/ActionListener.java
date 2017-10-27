package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.SwingWorkerHandler;

import java.awt.event.ActionEvent;

/**
 * Created by Stanislav Kaleta on 10.01.2017.
 */
public abstract class ActionListener implements java.awt.event.ActionListener {
    private Configurable configurable;

    public ActionListener(Configurable configurable){
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

    protected Configuration getConfiguration() {
        return configurable.getConfiguration();
    }
}
