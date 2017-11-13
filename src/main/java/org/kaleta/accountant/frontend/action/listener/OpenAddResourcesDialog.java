package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.frontend.Configurable;

public class OpenAddResourcesDialog extends org.kaleta.accountant.frontend.action.menu.OpenAddResourcesDialog {

    public OpenAddResourcesDialog(Configurable configurable) {
        super(configurable.getConfiguration());
    }
}
