package org.kaleta.accountant.frontend.component;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.Validable;
import org.kaleta.accountant.frontend.dialog.SelectAccountDialog;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SelectAccountTextField extends JTextField implements Validable{
    private String selectedAccount;

    public SelectAccountTextField(Configuration config, Map<String, List<AccountsModel.Account>> accountMap, List<SchemaModel.Class> classes, DocumentListener documentListener){
        init(config, accountMap, classes, documentListener);
    }

    public SelectAccountTextField(Configuration config, Map<String, List<AccountsModel.Account>> accountMap, SchemaModel.Class clazz, DocumentListener documentListener){
        List<SchemaModel.Class> classes = new ArrayList<>();
        classes.add(clazz);
        init(config, accountMap, classes, documentListener);
    }

    private void init(Configuration config, Map<String, List<AccountsModel.Account>> accountMap, List<SchemaModel.Class> classes, DocumentListener documentListener){
        this.getDocument().addDocumentListener(documentListener);
        this.getDocument().putProperty("owner", this);
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

    @Override
    public String validator() {
        return selectedAccount.isEmpty() ? "No Account Selected" : null;
    }
}
