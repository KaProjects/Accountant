package org.kaleta.accountant.frontend.dialog;

import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.component.DatePickerTextField;
import org.kaleta.accountant.frontend.component.HintValidatedTextField;
import org.kaleta.accountant.frontend.component.SelectAccountTextField;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.*;
import java.util.Calendar;

public class BuySellFinAssetDialog extends Dialog {
    private DatePickerTextField textFieldDate;
    private HintValidatedTextField textFieldValue;
    private SelectAccountTextField textFieldAccPair;

    public BuySellFinAssetDialog(Configuration configuration, String accName, boolean isBuying) {
        super(configuration, (isBuying ? "Buying" : "Selling") + " Financial Asset", isBuying ? "Buy" : "Sell");

        buildDialogContent(accName, isBuying);
        pack();
    }

    private void buildDialogContent(String accName, boolean isBuying) {
        JLabel labelName = new JLabel(accName);
        labelName.setFont(new Font(labelName.getName(), Font.BOLD, 15));

        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);

        JLabel labelDate = new JLabel("Date: ");
        textFieldDate = new DatePickerTextField("", this);
        JButton buttonToday = new JButton("Today");
        buttonToday.addActionListener(a -> {
            textFieldDate.focusGained(null);
            Calendar calendar = Calendar.getInstance();
            textFieldDate.setText(String.format("%1$02d%2$02d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1));
        });

        JLabel labelValue = new JLabel("Value: ");
        textFieldValue = new HintValidatedTextField("", "Value", "set value", true, this);

        JLabel labelAccPair = new JLabel(isBuying ? "Credit: " : "Debit: ");
        textFieldAccPair = new SelectAccountTextField(getConfiguration(), Service.ACCOUNT.getAccountsViaSchemaMap(getConfiguration().getSelectedYear()),
                Service.SCHEMA.getSchemaClassMap(getConfiguration().getSelectedYear()).get(2), "Credit", this);

        setContent(layout -> {
            layout.setHorizontalGroup(layout.createParallelGroup()
                    .addComponent(labelName)
                    .addComponent(separator)
                    .addGroup(layout.createSequentialGroup().addComponent(labelDate).addComponent(textFieldDate, 80, 80, Short.MAX_VALUE).addComponent(buttonToday))
                    .addGroup(layout.createSequentialGroup()
                            .addComponent(labelValue)
                            .addComponent(textFieldValue, 80, 80, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                            .addComponent(labelAccPair)
                            .addComponent(textFieldAccPair, 80, 80, Short.MAX_VALUE)));
            layout.setVerticalGroup(layout.createSequentialGroup()
                    .addComponent(labelName, 25, 25, 25)
                    .addComponent(separator, 8, 8, 8)
                    .addGroup(layout.createParallelGroup().addComponent(labelDate, 25, 25, 25).addComponent(textFieldDate, 25, 25, 25).addComponent(buttonToday, 25, 25, 25))
                    .addGap(8)
                    .addGroup(layout.createParallelGroup()
                            .addComponent(labelAccPair, 25, 25, 25)
                            .addComponent(textFieldAccPair, 25, 25, 25))
                    .addGroup(layout.createParallelGroup()
                            .addComponent(labelValue, 25, 25, 25)
                            .addComponent(textFieldValue, 25, 25, 25))
                    .addGap(10));
        });
    }

    public String getDate(){
        return textFieldDate.getText();
    }

    public String getValue(){
        return textFieldValue.getText();
    }

    public String getAccount() {
        return textFieldAccPair.getSelectedAccount();
    }

}
