package org.kaleta.accountant.frontend.action.menu;

import org.kaleta.accountant.backend.entity.Schema;
import org.kaleta.accountant.backend.entity.Semantic;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.dialog.semantic.SemanticDialog;
import org.kaleta.accountant.service.Service;

import java.awt.*;

/**
 * Created by Stanislav Kaleta on 23.05.2016.
 */
@Deprecated
public class OpenSemanticDialog extends MenuAction {
    public OpenSemanticDialog(Configuration config) {
        super(config, "Accounts");
    }

    @Override
    protected void actionPerformed() {
        Schema schema = Service.ACCOUNT.getSchema();
        Semantic semantic = Service.ACCOUNT.getSemanticAccounts();
        SemanticDialog dialog = new SemanticDialog((Component) getConfiguration(), schema, semantic);
        dialog.setVisible(true);
        if (dialog.getResult()) {
            Service.ACCOUNT.setSemanticAccounts(semantic);
        }
    }
}
