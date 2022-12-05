package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.frontend.Configurable;

public class OpenAddTransactionDialog extends org.kaleta.accountant.frontend.action.menu.OpenAddTransactionDialog {

    public OpenAddTransactionDialog(Configurable configurable) {
        super(configurable.getConfiguration());
    }
}
