package org.kaleta.accountant.frontend.dialog;

import javax.swing.*;
import java.awt.*;

public abstract class Dialog extends JDialog {
    protected boolean result;

    public Dialog(Component parent, String title){
        result = false;
        this.setTitle(title);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setModal(true);
        this.setLocationRelativeTo(parent);
    }

    public boolean getResult(){
        return result;
    }

    /**
     * this method should init. dialog components
     */
    protected abstract void buildDialog();
}
