package org.kaleta.accountant.frontend.action;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created by Stanislav Kaleta on 03.08.2016.
 *
 * Action which inits configuration in every component which needs it.
 */
public class InitConfigurableAction extends AbstractAction {
    private Configurable configurable;

    public InitConfigurableAction(Configurable configurable){
        this.configurable = configurable;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        Configuration configuration = (Configuration) actionEvent.getSource();
        configurable.setConfiguration(configuration);
    }
}
