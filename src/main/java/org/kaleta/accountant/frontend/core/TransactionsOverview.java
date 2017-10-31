package org.kaleta.accountant.frontend.core;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.dep.JournalTable;

import javax.swing.*;
import java.awt.*;

public class TransactionsOverview extends JPanel implements Configurable {
    private Configuration configuration;

    public TransactionsOverview(){
        this.setLayout(new GridLayout(1,1));
        this.add(new JournalTable());
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
