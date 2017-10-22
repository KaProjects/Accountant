package org.kaleta.accountant.frontend.dialog;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.common.NumberFilter;

import javax.swing.*;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class AddAssetDialog extends Dialog {
    private List<SchemaModel.Class.Group> groups;
    private List<SchemaModel.Class.Group.Account> accounts;

    private JTextField textFieldName;
    private JTextField textFieldDate;
    private JTextField textFieldAmount;
    private JTextField textFieldMonthlyDep;
    private JComboBox<SchemaModel.Class.Group> comboBoxGroup;
    private JComboBox<SchemaModel.Class.Group.Account> comboBoxAcc;
    private Map<String, List<AccountsModel.Account>> creditAccountMap;
    private List<SchemaModel.Class> classList;

    private String creditAcc;

    public AddAssetDialog(Component parent, List<SchemaModel.Class.Group> groups, Map<String, List<AccountsModel.Account>> creditAccountMap, List<SchemaModel.Class> classList) {
        super(parent, "Adding Asset");
        this.groups = groups;
        this.creditAccountMap = creditAccountMap;
        this.classList = classList;
        accounts = new ArrayList<>();
        buildDialog();
        pack();
    }

    @Override
    protected void buildDialog() {
        JButton buttonCancel = new JButton("Cancel");
        buttonCancel.addActionListener(a -> dispose());

        JButton buttonOk = new JButton("Add");
        buttonOk.addActionListener(a -> {
            if (comboBoxGroup.getSelectedIndex() == -1 || comboBoxAcc.getSelectedIndex() == -1
                    || textFieldName.getText() == null || textFieldName.getText().trim().isEmpty()
                    || textFieldDate.getText() == null || textFieldDate.getText().trim().isEmpty()
                    || textFieldAmount.getText() == null || textFieldAmount.getText().trim().isEmpty()
                    || textFieldMonthlyDep.getText() == null || textFieldMonthlyDep.getText().trim().isEmpty()
                    || creditAcc.trim().isEmpty()){
                JOptionPane.showMessageDialog(AddAssetDialog.this, "Mandatory attribute is not set!", "Value Missing", JOptionPane.ERROR_MESSAGE);
                return;
            }
            result = true;
            dispose();
        });

        JLabel labelName = new JLabel("Name:");
        textFieldName = new JTextField();
        JLabel labelDate = new JLabel("Date of Purchase:");
        textFieldDate = new JTextField();
        JButton buttonToday = new JButton("Today");
        buttonToday.addActionListener(a -> {
            Calendar calendar = Calendar.getInstance();
            textFieldDate.setText(String.format("%1$02d%2$02d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH)+1));
        });
        JLabel labelAmount = new JLabel("Amount:");
        textFieldAmount = new JTextField();
        ((PlainDocument) textFieldAmount.getDocument()).setDocumentFilter(new NumberFilter());
        JLabel labelMonthlyDep = new JLabel("Preferred Monthly Deprecation:");
        textFieldMonthlyDep = new JTextField();
        ((PlainDocument) textFieldMonthlyDep.getDocument()).setDocumentFilter(new NumberFilter());
        JLabel labelGroup = new JLabel("Group:");
        comboBoxGroup = new JComboBox<>();
        groups.forEach(group -> comboBoxGroup.addItem(group));
        comboBoxGroup.setSelectedIndex(-1);
        comboBoxGroup.addActionListener(a -> {
            comboBoxAcc.removeAllItems();
            accounts.clear();
            accounts.addAll(((SchemaModel.Class.Group)comboBoxGroup.getSelectedItem()).getAccount());
            accounts.forEach(account -> comboBoxAcc.addItem(account));
            comboBoxAcc.setSelectedIndex(-1);
        });
        JLabel labelAcc = new JLabel("Type:");
        comboBoxAcc = new JComboBox<>();

        JLabel labelCreditAcc = new JLabel("Purchased by:");
        JTextField textFieldCreditAcc = new JTextField();
        textFieldCreditAcc.setEditable(false);
        creditAcc = "";
        JButton buttonSelectCreditAcc = new JButton("Select");
        buttonSelectCreditAcc.addActionListener(e -> {
            SelectAccountDialog selectCreditAccountDialog = new SelectAccountDialog(AddAssetDialog.this, creditAccountMap, classList);
            selectCreditAccountDialog.setVisible(true);
            if (selectCreditAccountDialog.getResult()){
                creditAcc = selectCreditAccountDialog.getSelectedAccountId();
                textFieldCreditAcc.setText(selectCreditAccountDialog.getSelectedAccountName());
            }
        });

        JSeparator separator1 = new JSeparator(SwingConstants.HORIZONTAL);
        JSeparator separator2 = new JSeparator(SwingConstants.HORIZONTAL);

        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGap(10)
                .addGroup(layout.createParallelGroup()
                        .addGroup(layout.createSequentialGroup().addComponent(labelName,60,60,60).addComponent(textFieldName))
                        .addGroup(layout.createSequentialGroup().addComponent(labelGroup,60,60,60).addComponent(comboBoxGroup))
                        .addGroup(layout.createSequentialGroup().addComponent(labelAcc,60,60,60).addComponent(comboBoxAcc))
                        .addGroup(layout.createSequentialGroup().addComponent(labelAmount,60,60,60).addComponent(textFieldAmount))
                        .addComponent(separator1)
                        .addGroup(layout.createSequentialGroup().addComponent(labelCreditAcc).addGap(0,0,Short.MAX_VALUE).addComponent(buttonSelectCreditAcc))
                        .addGroup(layout.createSequentialGroup().addComponent(textFieldCreditAcc,150,150,Short.MAX_VALUE))
                        .addGroup(layout.createSequentialGroup().addComponent(labelDate).addGap(5).addComponent(textFieldDate,80,80,Short.MAX_VALUE).addComponent(buttonToday))
                        .addGroup(layout.createSequentialGroup().addComponent(labelMonthlyDep).addGap(5).addComponent(textFieldMonthlyDep))
                        .addComponent(separator2)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(5, 5, Short.MAX_VALUE)
                                .addComponent(buttonCancel).addGap(5).addComponent(buttonOk).addGap(5)))
                .addGap(10));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGap(10)
                .addGroup(layout.createParallelGroup().addComponent(labelName,25,25,25).addComponent(textFieldName,25,25,25))
                .addGap(3)
                .addGroup(layout.createParallelGroup().addComponent(labelGroup,25,25,25).addComponent(comboBoxGroup,25,25,25))
                .addGap(3)
                .addGroup(layout.createParallelGroup().addComponent(labelAcc,25,25,25).addComponent(comboBoxAcc,25,25,25))
                .addGap(3)
                .addGroup(layout.createParallelGroup().addComponent(labelAmount,25,25,25).addComponent(textFieldAmount,25,25,25))
                .addGap(5)
                .addComponent(separator1,5,5,5)
                .addGroup(layout.createParallelGroup().addComponent(labelCreditAcc,25,25,25).addComponent(buttonSelectCreditAcc,25,25,25))
                .addGroup(layout.createParallelGroup().addComponent(textFieldCreditAcc,25,25,25))
                .addGap(5)
                .addGroup(layout.createParallelGroup().addComponent(labelDate,25,25,25).addComponent(textFieldDate,25,25,25).addComponent(buttonToday,25,25,25))
                .addGap(5)
                .addGroup(layout.createParallelGroup().addComponent(labelMonthlyDep,25,25,25).addComponent(textFieldMonthlyDep,25,25,25))
                .addGap(5)
                .addComponent(separator2,5,5,5)
                .addGroup(layout.createParallelGroup().addComponent(buttonCancel,25,25,25).addComponent(buttonOk,25,25,25))
                .addGap(10));
    }

    public String getAccName(){
        return textFieldName.getText();
    }

    public String getDate(){
        return textFieldDate.getText();
    }

    public String getSchemaId(){
        return "0" + groups.get(comboBoxGroup.getSelectedIndex()).getId() + accounts.get(comboBoxAcc.getSelectedIndex()).getId();
    }

    public String getInitValue(){
        return textFieldAmount.getText();
    }

    public String getDepMetaData(){
        return textFieldMonthlyDep.getText()+","+textFieldDate.getText().substring(0,2);
    }

    public String getCreditAccount(){
        return creditAcc;
    }
}
