package org.kaleta.accountant.frontend.action.menu;

import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.SwingWorkerHandler;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Basic class for every menu action.
 */
abstract public class MenuAction extends AbstractAction {
    private Configuration config;

    public MenuAction(Configuration config, String name, Icon icon) {
        super(name, icon);
        this.config = config;
    }

    public MenuAction(Configuration config, String name) {
        super(name);
        this.config = config;
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

    protected Configuration getConfiguration(){
        return config;
    }
}
