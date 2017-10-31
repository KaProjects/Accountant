package org.kaleta.accountant.frontend.component;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.dialog.SelectAccountDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SelectAccountTextField extends JTextField {
    private String selectedAccount;

    public SelectAccountTextField(Configuration config, Map<String, List<AccountsModel.Account>> accountMap, List<SchemaModel.Class> classes){
        init(config, accountMap, classes);
    }

    public SelectAccountTextField(Configuration config, Map<String, List<AccountsModel.Account>> accountMap, SchemaModel.Class clazz){
        List<SchemaModel.Class> classes = new ArrayList<>();
        classes.add(clazz);
        init(config, accountMap, classes);
    }

    private void init(Configuration config, Map<String, List<AccountsModel.Account>> accountMap, List<SchemaModel.Class> classes){
        selectedAccount = "";
        this.setText(" - - Click to Select - - ");
        this.setForeground(Color.GRAY);
        this.setEditable(false);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                SelectAccountDialog selectExpenseAccountDialog = new SelectAccountDialog(config, accountMap, classes);
                selectExpenseAccountDialog.setVisible(true);
                if (selectExpenseAccountDialog.getResult()) {
                    selectedAccount = selectExpenseAccountDialog.getSelectedAccountId();
                    SelectAccountTextField.this.setText(selectExpenseAccountDialog.getSelectedAccountName());
                    SelectAccountTextField.this.setForeground(new JTextField().getForeground());
                }
            }
        });
    }

    public String getSelectedAccount(){
        return selectedAccount;
    }

}
