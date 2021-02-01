package org.kaleta.accountant.frontend.dialog;

import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.component.DatePickerTextField;
import org.kaleta.accountant.frontend.component.HintValidatedTextField;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.Calendar;

public class RevalueDialog extends Dialog {
    private DatePickerTextField textFieldDate;
    private HintValidatedTextField textFieldNewValue;

    public RevalueDialog(Configuration configuration, String accName, String currentValue) {
        super(configuration, "Revaluing Asset", "Revalue");

        buildDialogContent(accName, currentValue);
        pack();
    }

    private void buildDialogContent(String accName, String currentValue) {
        JLabel labelName = new JLabel(accName);
        labelName.setFont(new Font(labelName.getName(), Font.BOLD, 15));
        JLabel labelCurrentValue = new JLabel("Current Value : " + currentValue);
        labelCurrentValue.setFont(labelName.getFont());

        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);

        JLabel labelDate = new JLabel("Date: ");
        textFieldDate = new DatePickerTextField("", this);
        JButton buttonToday = new JButton("Today");
        buttonToday.addActionListener(a -> {
            textFieldDate.focusGained(null);
            Calendar calendar = Calendar.getInstance();
            textFieldDate.setText(String.format("%1$02d%2$02d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1));
        });

        JLabel labelNewValue = new JLabel("New Value: ");
        textFieldNewValue = new HintValidatedTextField("", "New Value", "set value", true, this);

        JLabel labelDiff = new JLabel("Diff: ");
        JLabel labelDiffValue = new JLabel("", SwingConstants.RIGHT);

        textFieldNewValue.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateLabel();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateLabel();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateLabel();
            }

            private void updateLabel() {
                if (textFieldNewValue.getText() != null && !textFieldNewValue.getText().trim().isEmpty()) {
                    Integer diff = Integer.parseInt(textFieldNewValue.getText()) - Integer.parseInt(currentValue);
                    if (diff == 0) {
                        labelDiffValue.setText(String.valueOf(diff));
                        labelDiffValue.setForeground(labelName.getForeground());
                    } else {
                        if (diff > 0) {
                            labelDiffValue.setText("+" + String.valueOf(diff));
                            labelDiffValue.setForeground(Constants.Color.INCOME_GREEN);
                        } else {
                            labelDiffValue.setText(String.valueOf(diff));
                            labelDiffValue.setForeground(Constants.Color.EXPENSE_RED);
                        }
                    }
                } else {
                    labelDiffValue.setText("");
                }
            }
        });

        setContent(layout -> {
            layout.setHorizontalGroup(layout.createParallelGroup()
                    .addComponent(labelName)
                    .addComponent(labelCurrentValue)
                    .addComponent(separator)
                    .addGroup(layout.createSequentialGroup().addComponent(labelDate).addComponent(textFieldDate, 80, 80, Short.MAX_VALUE).addComponent(buttonToday))
                    .addGroup(layout.createSequentialGroup()
                            .addComponent(labelNewValue)
                            .addComponent(textFieldNewValue, 80, 80, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                            .addComponent(labelDiff, 75, 75, Short.MAX_VALUE)
                            .addComponent(labelDiffValue, 75, 75, 75)));
            layout.setVerticalGroup(layout.createSequentialGroup()
                    .addComponent(labelName, 25, 25, 25)
                    .addComponent(labelCurrentValue, 25, 25, 25)
                    .addComponent(separator, 8, 8, 8)
                    .addGroup(layout.createParallelGroup().addComponent(labelDate, 25, 25, 25).addComponent(textFieldDate, 25, 25, 25).addComponent(buttonToday, 25, 25, 25))
                    .addGap(8)
                    .addGroup(layout.createParallelGroup()
                            .addComponent(labelNewValue, 25, 25, 25)
                            .addComponent(textFieldNewValue, 25, 25, 25))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addComponent(labelDiff, 25, 25, 25)
                            .addComponent(labelDiffValue, 25, 25, 25))
                    .addGap(10));
        });
    }

    public String getDate(){
        return textFieldDate.getText();
    }

    public String getNewValue(){
        return textFieldNewValue.getText();
    }
}
