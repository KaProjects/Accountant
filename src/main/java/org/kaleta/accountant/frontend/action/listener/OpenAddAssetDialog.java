package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.frontend.Configurable;

public class OpenAddAssetDialog extends org.kaleta.accountant.frontend.action.menu.OpenAddAssetDialog {

    public OpenAddAssetDialog(Configurable configurable) {
        super(configurable.getConfiguration());
    }
}
