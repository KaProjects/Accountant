package org.kaleta.accountant.frontend.core;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;

import javax.swing.*;

/**
 * Created by Stanley on 31.10.2017.
 */
public class ProfitOverview extends JPanel implements Configurable {
    private Configuration configuration;

    public ProfitOverview() {
    }

    public void update(){

    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }
}
