package org.kaleta.accountant.frontend.action.menu;

import org.kaleta.accountant.frontend.Configuration;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Stanislav Kaleta on 03.06.2016.
 */
@Deprecated
public class OpenAccountPreview extends MenuAction {
    public OpenAccountPreview(Configuration config) {
        super(config, "Account Preview");
    }

    @Override
    protected void actionPerformed() {
        String schemaId = JOptionPane.showInputDialog((Component) getConfiguration(),"Schema ID:");
        if (schemaId != null){
            //todo AccountPreviewModel model = new AccountPreviewModel(schemaId, getConfiguration().getActiveYear());
            //todo AccountPreviewFrame frame = new AccountPreviewFrame((Component) getConfiguration(), model);
            //todo frame.setVisible(true);
        }
    }
}
