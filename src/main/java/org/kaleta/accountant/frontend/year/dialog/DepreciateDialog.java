package org.kaleta.accountant.frontend.year.dialog;

import org.kaleta.accountant.frontend.common.NumberFilter;
import org.kaleta.accountant.frontend.dialog.Dialog;
import org.kaleta.accountant.frontend.year.model.AccountModel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.util.List;

/**
 * Created by Stanislav Kaleta on 11.04.2017.
 * TODO add possibility to preview earlier depreciations
 */
public class DepreciateDialog extends Dialog {
    private List<Config> configs;

    public DepreciateDialog(Component parent, List<Config> configs) {
        super(parent, "Depreciate Asset(s) Dialog");
        this.configs = configs;
        buildDialog();
        pack();
    }

    @Override
    protected void buildDialog() {
        JPanel panelItems = new JPanel();
        panelItems.setLayout(new BoxLayout(panelItems, BoxLayout.Y_AXIS));
        JScrollPane pane = new JScrollPane(panelItems);
        for (Config config : configs) {
            JCheckBox checkBoxEnabled = new JCheckBox("", config.isEnabled());
            checkBoxEnabled.addItemListener(e -> config.setEnabled(checkBoxEnabled.isSelected()));
            checkBoxEnabled.setToolTipText("Exclude/Include from Depreciation Process");

            JLabel labelName = new JLabel(config.getAccount().getName());

            JLabel labelValue = new JLabel("Value: ");
            JTextField textFieldValue = new JTextField((config.getValueHint().contains("x")) ? "" : config.getValueHint());
            ((PlainDocument) textFieldValue.getDocument()).setDocumentFilter(new NumberFilter());
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
            textFieldValue.setMinimumSize(new Dimension(50,25));

            JLabel labelDate = new JLabel("Date:");
            JTextField textFieldDate = new JTextField((config.getDateHint().contains("x")) ? "" : config.getDateHint());
            ((PlainDocument) textFieldDate.getDocument()).setDocumentFilter(new NumberFilter());
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
            textFieldDate.setMinimumSize(new Dimension(50,25));

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
                            .addComponent(labelValue,25,25,25).addComponent(textFieldValue,25,25,25)).addGap(25));

            panelItems.add(panelItem);
        }

        JButton buttonCancel = new JButton("Cancel");
        buttonCancel.addActionListener(a -> dispose());

        JButton buttonOk = new JButton("Confirm");
        buttonOk.addActionListener(a -> {
            if (validator()){
                result = true;
                dispose();
            }
        });

        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup().addGap(5)
                .addGroup(layout.createParallelGroup()
                        .addComponent(pane)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(5, 5, Short.MAX_VALUE)
                                .addComponent(buttonCancel).addGap(5).addComponent(buttonOk).addGap(5))).addGap(10));
        layout.setVerticalGroup(layout.createSequentialGroup().addGap(10)
                .addComponent(pane).addGap(5)
                .addGroup(layout.createParallelGroup().addComponent(buttonCancel).addComponent(buttonOk)).addGap(10));
    }

    private boolean validator(){
        for (Config config : configs){
            if (config.isEnabled()){
                if (config.getDateHint().isEmpty() || config.getDateHint().trim().isEmpty() || config.getDateHint().length() != 8 || config.getDateHint().contains("x")){
                    JOptionPane.showMessageDialog(this, "Inserted Date for '" + config.getAccount().getName() + "' is invalid!", "Not Valid", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if (config.getValueHint().isEmpty() || config.getDateHint().trim().isEmpty() || config.getDateHint().equals("0") || config.getValueHint().contains("x")
                        || (config.getMaxDepValue() < Integer.parseInt(config.getValueHint()))){
                    JOptionPane.showMessageDialog(this, "Inserted Value for '" + config.getAccount().getName() + "' is invalid!", "Not Valid", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }
        return true;
    }

    public List<Config> getConfigs() {
        return configs;
    }

    public static class Config{
        private AccountModel.Account account;
        private AccountModel.Account depAccount;
        private String dateHint;
        private String valueHint;
        private Integer maxDepValue;
        private Boolean enabled;

        public Config(AccountModel.Account account, AccountModel.Account depAccount, String dateHint, String valueHint, Integer maxDepValue, Boolean enabled) {
            this.account = account;
            this.depAccount = depAccount;
            this.dateHint = dateHint;
            this.valueHint = valueHint;
            this.maxDepValue = maxDepValue;
            this.enabled = enabled;
        }

        public AccountModel.Account getAccount() {
            return account;
        }

        public void setAccount(AccountModel.Account account) {
            this.account = account;
        }

        public AccountModel.Account getDepAccount() {
            return depAccount;
        }

        public void setDepAccount(AccountModel.Account depAccount) {
            this.depAccount = depAccount;
        }

        public String getDateHint() {
            return dateHint;
        }

        public void setDateHint(String dateHint) {
            this.dateHint = dateHint;
        }

        public String getValueHint() {
            return valueHint;
        }

        public void setValueHint(String valueHint) {
            this.valueHint = valueHint;
        }

        public Boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public Integer getMaxDepValue() {
            return maxDepValue;
        }

        public void setMaxDepValue(Integer maxDepValue) {
            this.maxDepValue = maxDepValue;
        }
    }
}
