package org.kaleta.accountant.frontend.action.menu;

import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.dialog.account.AccountPreviewFrame;
import org.kaleta.accountant.frontend.dialog.account.AccountPreviewModel;

import javax.swing.*;
import java.awt.Component;

/**
 * Created by Stanislav Kaleta on 03.06.2016.
 */
public class OpenAccountPreview extends MenuAction {
    public OpenAccountPreview(Configuration config) {
        super(config, "Account Preview");
    }

    @Override
    protected void actionPerformed() {
        String schemaId = JOptionPane.showInputDialog((Component) getConfiguration(),"Schema ID:");
        if (schemaId != null){
            AccountPreviewModel model = new AccountPreviewModel(schemaId, 2016); // TODO: 6/3/16 get year
            AccountPreviewFrame frame = new AccountPreviewFrame((Component) getConfiguration(), model);
            frame.setVisible(true);
        }
    }
}
