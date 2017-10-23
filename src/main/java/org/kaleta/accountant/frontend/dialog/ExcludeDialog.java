package org.kaleta.accountant.frontend.dialog;

import javax.swing.*;
import java.awt.*;

public class ExcludeDialog extends Dialog {

    public ExcludeDialog(Frame parent) {
        super(parent, "Excluding Asset");
        buildDialog();
        pack();
    }

    @Override
    protected void buildDialog() {
        JButton buttonCancel = new JButton("Cancel");
        buttonCancel.addActionListener(a -> dispose());

        JButton buttonOk = new JButton("Confirm");
        buttonOk.addActionListener(a -> {
            if (true){
                result = true;
                dispose();
            }
        });
    }
}
