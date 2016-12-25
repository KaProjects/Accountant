package org.kaleta.accountant.frontend.action.mouse;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.SwingWorkerHandler;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by Stanislav Kaleta on 20.08.2016.
 * Basic class for every action reacting to mouse action.
 */
abstract public class MouseAction extends MouseAdapter {
    private Configurable configurable;

    public MouseAction(Configurable configurable){
        this.configurable = configurable;
    }

    @Override
    public void mouseClicked(MouseEvent e)  {
        new SwingWorkerHandler() {
            @Override
            protected void runInBackground() {
                actionPerformed(e);
            }
        }.execute();
    }

    protected abstract void actionPerformed(MouseEvent e);

    protected Configuration getConfiguration() {
        return configurable.getConfiguration();
    }
}
