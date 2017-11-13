package org.kaleta.accountant.frontend.action.mouse;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.SwingWorkerHandler;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Basic class for every action reacting to mouse action.
 */
abstract class MouseAction extends MouseAdapter {
    private final Configurable configurable;

    MouseAction(Configurable configurable){
        this.configurable = configurable;
    }

    @Override
    public void mouseReleased(MouseEvent e)  {
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
