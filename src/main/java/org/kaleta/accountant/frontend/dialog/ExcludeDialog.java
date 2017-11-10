package org.kaleta.accountant.frontend.dialog;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.IconLoader;
import org.kaleta.accountant.frontend.component.DatePickerTextField;
import org.kaleta.accountant.frontend.component.HintValidatedTextField;
import org.kaleta.accountant.frontend.component.SelectAccountTextField;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class ExcludeDialog extends Dialog {
    private Map<String, List<AccountsModel.Account>> expenseAccountMap;
    private SchemaModel.Class expenseClass;
    private final Map<String, List<AccountsModel.Account>> debitAccountMap;
    private final List<SchemaModel.Class> debitClasses;
    private final Map<String, List<AccountsModel.Account>> revenueAccountMap;
    private final SchemaModel.Class revenueClass;

    private final boolean hasExpense;
    private SelectAccountTextField textFieldExpense;
    private boolean hasRevenue;
    private SelectAccountTextField textFieldDebit;
    private SelectAccountTextField textFieldRevenue;

    private DatePickerTextField textFieldDate;
    private HintValidatedTextField textFieldRevenueValue;

    public ExcludeDialog(Configuration configuration, Map<String, List<AccountsModel.Account>> expenseAccountMap, SchemaModel.Class expenseClass,
                         Map<String, List<AccountsModel.Account>> debitAccountMap, List<SchemaModel.Class> debitClasses,
                         Map<String, List<AccountsModel.Account>> revenueAccountMap, SchemaModel.Class revenueClass) {
        super(configuration, "Excluding Asset", "Confirm");
        this.expenseAccountMap = expenseAccountMap;
        this.expenseClass = expenseClass;
        this.debitAccountMap = debitAccountMap;
        this.debitClasses = debitClasses;
        this.revenueAccountMap = revenueAccountMap;
        this.revenueClass = revenueClass;
        hasExpense = true;
        buildDialogContent();
        pack();
    }

    public ExcludeDialog(Configuration configuration, Map<String, List<AccountsModel.Account>> debitAccountMap, List<SchemaModel.Class> debitClasses,
                         Map<String, List<AccountsModel.Account>> revenueAccountMap, SchemaModel.Class revenueClass) {
        super(configuration, "Excluding Asset", "Confirm");
        this.debitAccountMap = debitAccountMap;
        this.debitClasses = debitClasses;
        this.revenueAccountMap = revenueAccountMap;
        this.revenueClass = revenueClass;
        hasExpense = false;
        buildDialogContent();
        pack();
    }

    private void buildDialogContent() {

        JButton buttonOk = new JButton("Confirm");
        buttonOk.addActionListener(a -> {
            if ((!hasExpense || !textFieldExpense.getSelectedAccount().trim().isEmpty())
                    && (!hasRevenue || (!textFieldDebit.getSelectedAccount().trim().isEmpty() && !textFieldRevenue.getSelectedAccount().trim().isEmpty() && !textFieldRevenueValue.getText().trim().isEmpty()))
                    && (!textFieldDate.getText().trim().isEmpty() && textFieldDate.getText().length() == 4)){
                if (JOptionPane.showConfirmDialog(this.getParent(), "Are you sure you want to exclude this asset?",
                        "Excluding Asset", JOptionPane.YES_NO_OPTION) == 0){
                    result = true;
                    dispose();
                }
            } else {
                JOptionPane.showMessageDialog(ExcludeDialog.this, "Mandatory attribute is not set!", "Value Missing", JOptionPane.ERROR_MESSAGE);
            }
        });

        JLabel labelDate = new JLabel("Date: ");
        textFieldDate = new DatePickerTextField("", this);
        JButton buttonToday = new JButton("Today");
        buttonToday.addActionListener(a -> {
            textFieldDate.focusGained(null);
            Calendar calendar = Calendar.getInstance();
            textFieldDate.setText(String.format("%1$02d%2$02d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH)+1));
        });

        JPanel panelExpense = new JPanel();
        if (hasExpense) {
            JLabel labelExpense = new JLabel("Residual Expense:");
            labelExpense.setToolTipText("Asset is not fully depreciated, therefore its residual value must be delegated to a expense account.");
            textFieldExpense = new SelectAccountTextField(getConfiguration(), expenseAccountMap, expenseClass, "Residual Expense",this);

            GroupLayout layoutExpense = new GroupLayout(panelExpense);
            panelExpense.setLayout(layoutExpense);
            layoutExpense.setHorizontalGroup(layoutExpense.createParallelGroup().addComponent(labelExpense).addComponent(textFieldExpense));
            layoutExpense.setVerticalGroup(layoutExpense.createSequentialGroup().addComponent(labelExpense,25,25,25).addComponent(textFieldExpense,25,25,25).addGap(5));
        } else {
            textFieldExpense.setValidatorEnabled(false);
            panelExpense.setVisible(false);
        }

        hasRevenue = false;

        JPanel panelDebit = new JPanel();
        JLabel labelDebit = new JLabel("Debit:");
        textFieldDebit = new SelectAccountTextField(getConfiguration(), debitAccountMap, debitClasses, "Revenue Debit", this);
        textFieldDebit.setValidatorEnabled(hasRevenue);

        GroupLayout layoutDebit = new GroupLayout(panelDebit);
        panelDebit.setLayout(layoutDebit);
        layoutDebit.setHorizontalGroup(layoutDebit.createParallelGroup().addComponent(labelDebit).addComponent(textFieldDebit));
        layoutDebit.setVerticalGroup(layoutDebit.createSequentialGroup().addComponent(labelDebit,25,25,25).addComponent(textFieldDebit,25,25,25).addGap(5));
        panelDebit.setVisible(hasRevenue);

        JPanel panelRevenue = new JPanel();
        JLabel labelRevenue = new JLabel("Revenue:");
        textFieldRevenue  = new SelectAccountTextField(getConfiguration(), revenueAccountMap, revenueClass, "Revenue Credit", this);
        textFieldRevenue.setValidatorEnabled(hasRevenue);

        JLabel labelRevenueValue = new JLabel("Value: ");
        textFieldRevenueValue = new HintValidatedTextField("","Revenue Value", "set value", true, this);

        GroupLayout layoutRevenue = new GroupLayout(panelRevenue);
        panelRevenue.setLayout(layoutRevenue);
        layoutRevenue.setHorizontalGroup(layoutRevenue.createParallelGroup()
                .addComponent(labelRevenue)
                .addComponent(textFieldRevenue)
                .addComponent(labelRevenueValue)
                .addComponent(textFieldRevenueValue));
        layoutRevenue.setVerticalGroup(layoutRevenue.createSequentialGroup()
                .addComponent(labelRevenue,25,25,25)
                .addComponent(textFieldRevenue,25,25,25).addGap(2)
                .addComponent(labelRevenueValue,25,25,25)
                .addComponent(textFieldRevenueValue,25,25,25).addGap(5));
        panelRevenue.setVisible(hasRevenue);

        JLabel labelAddRevenue = new JLabel("Add Revenue", IconLoader.getIcon(IconLoader.ADD), SwingConstants.CENTER);
        labelAddRevenue.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                hasRevenue = !hasRevenue;
                textFieldDebit.setValidatorEnabled(hasRevenue);
                textFieldRevenue.setValidatorEnabled(hasRevenue);
                textFieldRevenueValue.setValidatorEnabled(hasRevenue);
                validateDialog();
                panelDebit.setVisible(hasRevenue);
                panelRevenue.setVisible(hasRevenue);
                labelAddRevenue.setText(hasRevenue ? "Revoke Revenue" : "Add Revenue");
                labelAddRevenue.setIcon(hasRevenue ? IconLoader.getIcon(IconLoader.DELETE) : IconLoader.getIcon(IconLoader.ADD));
                ExcludeDialog.this.pack();
            }
        });

        setContent(layout -> {
            layout.setHorizontalGroup(layout.createParallelGroup()
                            .addGroup(layout.createSequentialGroup().addComponent(labelDate).addComponent(textFieldDate,80,80,Short.MAX_VALUE).addComponent(buttonToday))
                            .addComponent(panelExpense)
                            .addComponent(labelAddRevenue)
                            .addComponent(panelDebit)
                            .addComponent(panelRevenue));
            layout.setVerticalGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup().addComponent(labelDate,25,25,25).addComponent(textFieldDate,25,25,25).addComponent(buttonToday,25,25,25))
                    .addGap(5)
                    .addComponent(panelExpense)
                    .addComponent(labelAddRevenue)
                    .addGap(5)
                    .addComponent(panelDebit)
                    .addComponent(panelRevenue));
        });
    }

    public String getExpenseAccount() {
        return textFieldExpense.getSelectedAccount();
    }

    public boolean hasRevenue() {
        return hasRevenue;
    }

    public String getDebitAccount() {
        return textFieldDebit.getSelectedAccount();
    }

    public String getRevenueAccount() {
        return textFieldRevenue.getSelectedAccount();
    }

    public String getRevenueValue(){
        return textFieldRevenueValue.getText();
    }

    public String getDate(){
        return textFieldDate.getText();
    }
}
