package org.kaleta.accountant.frontend.component;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
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

    public SelectAccountTextField(Frame parent, Map<String, List<AccountsModel.Account>> accountMap, List<SchemaModel.Class> classes){
        init(parent, accountMap, classes);
    }

    public SelectAccountTextField(Frame parent, Map<String, List<AccountsModel.Account>> accountMap, SchemaModel.Class clazz){
        List<SchemaModel.Class> classes = new ArrayList<>();
        classes.add(clazz);
        init(parent, accountMap, classes);
    }

    private void init(Frame parent, Map<String, List<AccountsModel.Account>> accountMap, List<SchemaModel.Class> classes){
        selectedAccount = "";
        this.setText(" - - Click to Select - - ");
        this.setForeground(Color.GRAY);
        this.setEditable(false);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                SelectAccountDialog selectExpenseAccountDialog = new SelectAccountDialog(parent, accountMap, classes);
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
