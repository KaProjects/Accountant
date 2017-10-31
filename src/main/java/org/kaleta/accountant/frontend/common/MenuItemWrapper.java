package org.kaleta.accountant.frontend.common;

import javax.swing.*;

public class MenuItemWrapper extends JMenuItem {

    public MenuItemWrapper(Action action, KeyStroke keyStroke, String tipText) {
        super(action);
        this.setAccelerator(keyStroke);
        this.setToolTipText(tipText);
    }

    public MenuItemWrapper(Action action, KeyStroke keyStroke) {
        super(action);
        this.setAccelerator(keyStroke);
    }

    public MenuItemWrapper(Action action, String tipText) {
        super(action);
        this.setToolTipText(tipText);
    }

    public MenuItemWrapper(Action action) {
        super(action);
    }
}
