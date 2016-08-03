package org.kaleta.accountant.frontend.component;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.InitConfigurableAction;
import org.kaleta.accountant.frontend.action.configuration.JournalPanelTransactionsUpdated;

import javax.swing.*;

/**
 * Created by Stanislav Kaleta on 03.08.2016.
 */
public class JournalPanel extends JPanel implements Configurable {
    private Configuration configuration;

    public JournalPanel() {
        initComponents();
    }

    private void initComponents(){
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.getActionMap().put(Configuration.INIT_CONFIG, new InitConfigurableAction(this));
        this.getActionMap().put(Configuration.TRANSACTION_ACTION, new JournalPanelTransactionsUpdated(this));
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
