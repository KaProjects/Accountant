package org.kaleta.accountant.frontend.dialog;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.component.DatePickerTextField;
import org.kaleta.accountant.frontend.component.HintValidatedTextField;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;

public class DepreciateDialog extends Dialog {
    private final List<Config> configs;

    public DepreciateDialog(Configuration configuration, List<Config> configs) {
        super(configuration, "Depreciating Asset(s) Dialog", "Confirm");
        this.configs = configs;
        buildDialogContent();
        pack();
    }

    private void buildDialogContent() {
        JPanel panelItems = new JPanel();
        panelItems.setLayout(new BoxLayout(panelItems, BoxLayout.Y_AXIS));
        JScrollPane pane = new JScrollPane(panelItems);
        for (Config config : configs) {
            if (!config.isEnabled()) continue;

            JLabel labelName = new JLabel(config.getAccount().getName());

            JLabel labelValue = new JLabel("Value: ");
            String valueHint = ((config.getValueHint().contains("x")) ? "" : config.getValueHint());
            HintValidatedTextField textFieldValue = new HintValidatedTextField(valueHint,"Depreciation Value", "set value", true, this);
            textFieldValue.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent documentEvent) {
                    config.setValueHint(textFieldValue.getText());
                }

                @Override
                public void removeUpdate(DocumentEvent documentEvent) {
                    config.setValueHint(textFieldValue.getText());
                }

                @Override
                public void changedUpdate(DocumentEvent documentEvent) {
                    config.setValueHint(textFieldValue.getText());
                }
            });
            textFieldValue.setMinimumSize(new Dimension(70,25));

            JLabel labelDate = new JLabel("Date:");
            String dateHint = (config.getDateHint().contains("x")) ? "" : config.getDateHint();
            DatePickerTextField textFieldDate = new DatePickerTextField(dateHint, this);
            textFieldDate.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent documentEvent) {
                    config.setDateHint(textFieldDate.getText());
                }

                @Override
                public void removeUpdate(DocumentEvent documentEvent) {
                    config.setDateHint(textFieldDate.getText());
                }

                @Override
                public void changedUpdate(DocumentEvent documentEvent) {
                    config.setDateHint(textFieldDate.getText());
                }
            });
            textFieldDate.setMinimumSize(new Dimension(75,25));

            JCheckBox checkBoxEnabled = new JCheckBox("", config.isEnabled());
            checkBoxEnabled.addItemListener(e -> {
                config.setEnabled(checkBoxEnabled.isSelected());
                textFieldValue.setValidatorEnabled(checkBoxEnabled.isSelected());
                textFieldDate.setValidatorEnabled(checkBoxEnabled.isSelected());
                DepreciateDialog.this.validateDialog();
            });
            checkBoxEnabled.setToolTipText("Exclude/Include from Depreciation Process");

            JPanel panelItem = new JPanel();
            panelItem.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            GroupLayout layoutItem = new GroupLayout(panelItem);
            panelItem.setLayout(layoutItem);
            layoutItem.setHorizontalGroup(layoutItem.createSequentialGroup().addGap(5).addGroup(layoutItem.createParallelGroup()
                    .addGroup(layoutItem.createSequentialGroup().addComponent(checkBoxEnabled).addGap(5).addComponent(labelName))
                    .addGroup(layoutItem.createSequentialGroup().addGap(5)
                            .addComponent(labelDate).addGap(5).addComponent(textFieldDate).addGap(10)
                            .addComponent(labelValue).addGap(5).addComponent(textFieldValue))).addGap(25));
            layoutItem.setVerticalGroup(layoutItem.createSequentialGroup().addGap(5)
                    .addGroup(layoutItem.createParallelGroup().addComponent(checkBoxEnabled).addComponent(labelName)).addGap(4)
                    .addGroup(layoutItem.createParallelGroup()
                            .addComponent(labelDate,25,25,25).addComponent(textFieldDate,25,25,25)
                            .addComponent(labelValue,25,25,25).addComponent(textFieldValue,25,25,25)).addGap(10));

            panelItems.add(panelItem);
        }

        setContent(layout -> {
            layout.setHorizontalGroup(layout.createParallelGroup().addComponent(pane));
            layout.setVerticalGroup(layout.createSequentialGroup().addComponent(pane));
        });

        validateDialog();
    }

    public List<Config> getConfigs() {
        return configs;
    }

    public static class Config{
        private AccountsModel.Account account;
        private AccountsModel.Account depAccount;
        private String dateHint;
        private String valueHint;
        private Integer maxDepValue;
        private Boolean enabled;

        public Config(AccountsModel.Account account, AccountsModel.Account depAccount, String dateHint, String valueHint, Integer maxDepValue, Boolean enabled) {
            this.account = account;
            this.depAccount = depAccount;
            this.dateHint = dateHint;
            this.valueHint = valueHint;
            this.maxDepValue = maxDepValue;
            this.enabled = enabled;
        }

        public AccountsModel.Account getAccount() {
            return account;
        }

        public AccountsModel.Account getDepAccount() {
            return depAccount;
        }

        public String getDateHint() {
            return dateHint;
        }

        void setDateHint(String dateHint) {
            this.dateHint = dateHint;
        }

        public String getValueHint() {
            return valueHint;
        }

        void setValueHint(String valueHint) {
            this.valueHint = valueHint;
        }

        public Boolean isEnabled() {
            return enabled;
        }

        void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        Integer getMaxDepValue() {
            return maxDepValue;
        }
    }
}
