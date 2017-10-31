package org.kaleta.accountant.frontend.dialog;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.NumberFilter;
import org.kaleta.accountant.frontend.component.SelectAccountTextField;

import javax.swing.*;
import javax.swing.text.PlainDocument;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class AddAssetDialog extends Dialog {
    private final List<SchemaModel.Class.Group> groups;
    private final List<SchemaModel.Class.Group.Account> accounts;

    private JTextField textFieldName;
    private JTextField textFieldDate;
    private JTextField textFieldAmount;
    private JTextField textFieldMonthlyDep;
    private JComboBox<SchemaModel.Class.Group> comboBoxGroup;
    private JComboBox<SchemaModel.Class.Group.Account> comboBoxAcc;
    private final Map<String, List<AccountsModel.Account>> creditAccountMap;
    private final List<SchemaModel.Class> classList;

    private SelectAccountTextField textFieldCreditAcc;

    public AddAssetDialog(Configuration configuration, List<SchemaModel.Class.Group> groups, Map<String, List<AccountsModel.Account>> creditAccountMap, List<SchemaModel.Class> classList) {
        super(configuration, "Adding Asset");
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
                    || textFieldCreditAcc.getSelectedAccount().trim().isEmpty()){
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
        textFieldDate.setHorizontalAlignment(SwingConstants.RIGHT);
        JButton buttonToday = new JButton("Today");
        buttonToday.addActionListener(a -> {
            Calendar calendar = Calendar.getInstance();
            textFieldDate.setText(String.format("%1$02d%2$02d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH)+1));
        });
        JLabel labelAmount = new JLabel("Amount:");
        textFieldAmount = new JTextField();
        textFieldAmount.setHorizontalAlignment(SwingConstants.RIGHT);
        ((PlainDocument) textFieldAmount.getDocument()).setDocumentFilter(new NumberFilter());
        JLabel labelMonthlyDep = new JLabel("Preferred Monthly Deprecation:");
        textFieldMonthlyDep = new JTextField();
        textFieldMonthlyDep.setHorizontalAlignment(SwingConstants.RIGHT);
        ((PlainDocument) textFieldMonthlyDep.getDocument()).setDocumentFilter(new NumberFilter());
        JLabel labelGroup = new JLabel("Group:");
        comboBoxGroup = new JComboBox<>();
        groups.forEach(group -> comboBoxGroup.addItem(group));
        comboBoxGroup.setSelectedIndex(-1);
        comboBoxGroup.addActionListener(a -> {
            comboBoxAcc.removeAllItems();
            accounts.clear();
            if (comboBoxGroup.getSelectedItem() != null) {
                accounts.addAll(((SchemaModel.Class.Group)comboBoxGroup.getSelectedItem()).getAccount());
                accounts.forEach(account -> comboBoxAcc.addItem(account));
            }
            comboBoxAcc.setSelectedIndex(-1);
            AddAssetDialog.this.pack();
        });
        JLabel labelAcc = new JLabel("Account:");
        comboBoxAcc = new JComboBox<>();

        JLabel labelCreditAcc = new JLabel("Purchased by:");
        textFieldCreditAcc = new SelectAccountTextField(getConfiguration(), creditAccountMap, classList);

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
                        .addComponent(labelCreditAcc)
                        .addComponent(textFieldCreditAcc)
                        .addGroup(layout.createSequentialGroup().addComponent(labelDate).addGap(5,5,Short.MAX_VALUE).addComponent(buttonToday))
                        .addComponent(textFieldDate)
                        .addComponent(labelMonthlyDep)
                        .addComponent(textFieldMonthlyDep)
                        .addComponent(separator2)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(5, 5, Short.MAX_VALUE)
                                .addComponent(buttonCancel).addGap(5).addComponent(buttonOk)))
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
                .addComponent(labelCreditAcc,25,25,25)
                .addComponent(textFieldCreditAcc,25,25,25)
                .addGap(5)
                .addGroup(layout.createParallelGroup().addComponent(labelDate,25,25,25).addComponent(buttonToday,25,25,25))
                .addComponent(textFieldDate,25,25,25)
                .addGap(5)
                .addComponent(labelMonthlyDep,25,25,25)
                .addComponent(textFieldMonthlyDep,25,25,25)
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
        return textFieldCreditAcc.getSelectedAccount();
    }
}
