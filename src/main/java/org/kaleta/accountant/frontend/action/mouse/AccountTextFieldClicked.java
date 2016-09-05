package org.kaleta.accountant.frontend.action.mouse;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.dialog.transaction.SelectAccountDialog;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.event.MouseEvent;

/**
 * Created by Stanislav Kaleta on 05.09.2016.
 */
public class AccountTextFieldClicked extends MouseAction {
    private boolean isDebit;

    public AccountTextFieldClicked(Configurable parent, boolean isDebit) {
        super(parent);
        this.isDebit = isDebit;
    }

    @Override
    protected void actionPerformed(MouseEvent e) {
        JTextField source = (JTextField) e.getSource();
        SelectAccountDialog dialog = new SelectAccountDialog(source, isDebit);
        dialog.preselectPath(source.getText());
        dialog.setVisible(true);
        if (dialog.getResult()){
            String accId = dialog.getSelectedAcc();
            source.setText(dialog.getSelectedAcc());
            if (accId.length() > 2){
                String text = (accId.contains("-"))
                        ? Service.ACCOUNT.getAccountFullName(accId.split("-")[0], accId.split("-")[1])
                        : accId + " " + Service.ACCOUNT.getAccountName(accId);
                source.setToolTipText(text);
            } else {
                if (accId.length() == 2) source.setToolTipText(Service.ACCOUNT.getGroupName(accId));
                if (accId.length() == 1) source.setToolTipText(Service.ACCOUNT.getClassName(accId));
            }

        }
    }
}
