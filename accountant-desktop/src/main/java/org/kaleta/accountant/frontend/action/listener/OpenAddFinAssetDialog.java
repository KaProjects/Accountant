package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.frontend.Configurable;

public class OpenAddFinAssetDialog extends org.kaleta.accountant.frontend.action.menu.OpenAddFinAssetDialog{

    public OpenAddFinAssetDialog(Configurable configurable) {
        super(configurable.getConfiguration());
    }
}
