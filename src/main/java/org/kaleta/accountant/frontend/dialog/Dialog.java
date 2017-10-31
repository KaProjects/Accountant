package org.kaleta.accountant.frontend.dialog;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;

import javax.swing.*;
import java.awt.*;

public abstract class Dialog extends JDialog implements Configurable{
    private Configuration configuration;
    protected boolean result;

    Dialog(Configuration configuration, String title){
        super((Frame) configuration);
        setConfiguration(configuration);
        result = false;
        this.setTitle(title);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setModal(true);
    }

    protected Dialog(Frame frame, String title){
        super(frame);
        result = false;
        this.setTitle(title);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setModal(true);
    }

    @Override
    public void setVisible(boolean b) {
        this.setLocation(getParent().getLocationOnScreen().x + getParent().getSize().width/2 - getSize().width/2,
                getParent().getLocationOnScreen().y + getParent().getSize().height/2 - getSize().height/2);
        super.setVisible(b);
    }

    public boolean getResult(){
        return result;
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * this method should init. dialog components
     */
    protected abstract void buildDialog();
}
