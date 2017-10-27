package org.kaleta.accountant.frontend.dialog;

import javax.swing.*;
import java.awt.*;

public abstract class Dialog extends JDialog {
    protected boolean result;

    public Dialog(Frame parent, String title){
        super(parent);
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

    /**
     * this method should init. dialog components
     */
    protected abstract void buildDialog();
}
