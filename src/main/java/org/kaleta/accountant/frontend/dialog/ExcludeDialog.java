package org.kaleta.accountant.frontend.dialog;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.common.IconLoader;
import org.kaleta.accountant.frontend.common.NumberFilter;

import javax.swing.*;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class ExcludeDialog extends Dialog {
    private Map<String, List<AccountsModel.Account>> expenseAccountMap;
    private SchemaModel.Class expenseClass;
    private Map<String, List<AccountsModel.Account>> debitAccountMap;
    private List<SchemaModel.Class> debitClasses;
    private Map<String, List<AccountsModel.Account>> revenueAccountMap;
    private SchemaModel.Class revenueClass;

    private boolean hasExpense;
    private String expenseAccount;
    private boolean hasRevenue;
    private String debitAccount;
    private String revenueAccount;

    private JTextField textFieldDate;
    private JTextField textFieldRevenueValue;

    public ExcludeDialog(Frame parent, Map<String, List<AccountsModel.Account>> expenseAccountMap, SchemaModel.Class expenseClass,
                         Map<String, List<AccountsModel.Account>> debitAccountMap, List<SchemaModel.Class> debitClasses,
                         Map<String, List<AccountsModel.Account>> revenueAccountMap, SchemaModel.Class revenueClass) {
        super(parent, "Excluding Asset");
        this.expenseAccountMap = expenseAccountMap;
        this.expenseClass = expenseClass;
        this.debitAccountMap = debitAccountMap;
        this.debitClasses = debitClasses;
        this.revenueAccountMap = revenueAccountMap;
        this.revenueClass = revenueClass;
        hasExpense = true;
        buildDialog();
        pack();
    }

    public ExcludeDialog(Frame parent, Map<String, List<AccountsModel.Account>> debitAccountMap, List<SchemaModel.Class> debitClasses,
                         Map<String, List<AccountsModel.Account>> revenueAccountMap, SchemaModel.Class revenueClass) {
        super(parent, "Excluding Asset");
        this.debitAccountMap = debitAccountMap;
        this.debitClasses = debitClasses;
        this.revenueAccountMap = revenueAccountMap;
        this.revenueClass = revenueClass;
        hasExpense = false;
        buildDialog();
        pack();
    }

    @Override
    protected void buildDialog() {
        JButton buttonCancel = new JButton("Cancel");
        buttonCancel.addActionListener(a -> dispose());

        JButton buttonOk = new JButton("Confirm");
        buttonOk.addActionListener(a -> {
            if ((!hasExpense || !expenseAccount.trim().isEmpty())
                    && (hasRevenue && !debitAccount.trim().isEmpty()
                        && !revenueAccount.trim().isEmpty() && !textFieldRevenueValue.getText().trim().isEmpty() || !hasRevenue)
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
        textFieldDate = new JTextField();
        ((PlainDocument) textFieldDate.getDocument()).setDocumentFilter(new NumberFilter());
        JButton buttonToday = new JButton("Today");
        buttonToday.addActionListener(a -> {
            Calendar calendar = Calendar.getInstance();
            textFieldDate.setText(String.format("%1$02d%2$02d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH)+1));
        });

        JPanel panelExpense = new JPanel();
        if (hasExpense) {
            JLabel labelExpense = new JLabel("Residual Expense:");
            labelExpense.setToolTipText("Asset is not fully depreciated, therefore its residual value must be delegated to a expense account.");
            JTextField textFieldExpense = new JTextField();
            textFieldExpense.setEditable(false);
            expenseAccount = "";
            JButton buttonSelectExpenseAcc = new JButton("Select");
            buttonSelectExpenseAcc.addActionListener(e -> {
                SelectAccountDialog selectExpenseAccountDialog = new SelectAccountDialog((Frame) ExcludeDialog.this.getParent(), expenseAccountMap, expenseClass);
                selectExpenseAccountDialog.setVisible(true);
                if (selectExpenseAccountDialog.getResult()) {
                    expenseAccount = selectExpenseAccountDialog.getSelectedAccountId();
                    textFieldExpense.setText(selectExpenseAccountDialog.getSelectedAccountName());
                }
            });
            GroupLayout layoutExpense = new GroupLayout(panelExpense);
            panelExpense.setLayout(layoutExpense);
            layoutExpense.setHorizontalGroup(layoutExpense.createParallelGroup()
                    .addGroup(layoutExpense.createSequentialGroup()
                            .addComponent(labelExpense).addGap(10, 10, Short.MAX_VALUE).addComponent(buttonSelectExpenseAcc))
                    .addComponent(textFieldExpense));
            layoutExpense.setVerticalGroup(layoutExpense.createSequentialGroup()
                    .addGroup(layoutExpense.createParallelGroup()
                            .addComponent(labelExpense,25,25,25).addComponent(buttonSelectExpenseAcc,25,25,25))
                    .addComponent(textFieldExpense,25,25,25).addGap(5));
        } else {
            panelExpense.setVisible(false);
        }

        hasRevenue = false;

        JPanel panelDebit = new JPanel();
        JLabel labelDebit = new JLabel("Debit:");
        JTextField textFieldDebit = new JTextField();
        textFieldDebit.setEditable(false);
        debitAccount = "";
        JButton buttonSelectDebitAcc = new JButton("Select");
        buttonSelectDebitAcc.addActionListener(e -> {
            SelectAccountDialog selectDebitAccountDialog = new SelectAccountDialog((Frame) ExcludeDialog.this.getParent(), debitAccountMap, debitClasses);
            selectDebitAccountDialog.setVisible(true);
            if (selectDebitAccountDialog.getResult()) {
                debitAccount = selectDebitAccountDialog.getSelectedAccountId();
                textFieldDebit.setText(selectDebitAccountDialog.getSelectedAccountName());
            }
        });
        GroupLayout layoutDebit = new GroupLayout(panelDebit);
        panelDebit.setLayout(layoutDebit);
        layoutDebit.setHorizontalGroup(layoutDebit.createParallelGroup()
                .addGroup(layoutDebit.createSequentialGroup()
                        .addComponent(labelDebit).addGap(10, 10, Short.MAX_VALUE).addComponent(buttonSelectDebitAcc))
                .addComponent(textFieldDebit));
        layoutDebit.setVerticalGroup(layoutDebit.createSequentialGroup()
                .addGroup(layoutDebit.createParallelGroup()
                        .addComponent(labelDebit,25,25,25).addComponent(buttonSelectDebitAcc,25,25,25))
                .addComponent(textFieldDebit,25,25,25).addGap(5));
        panelDebit.setVisible(hasRevenue);


        JPanel panelRevenue = new JPanel();
        JLabel labelRevenue = new JLabel("Revenue:");
        JTextField textFieldRevenue = new JTextField();
        textFieldRevenue.setEditable(false);
        revenueAccount = "";
        JButton buttonSelectRevenueAcc = new JButton("Select");
        buttonSelectRevenueAcc.addActionListener(e -> {
            SelectAccountDialog selectRevenueAccountDialog = new SelectAccountDialog((Frame) ExcludeDialog.this.getParent(), revenueAccountMap, revenueClass);
            selectRevenueAccountDialog.setVisible(true);
            if (selectRevenueAccountDialog.getResult()) {
                revenueAccount = selectRevenueAccountDialog.getSelectedAccountId();
                textFieldRevenue.setText(selectRevenueAccountDialog.getSelectedAccountName());
            }
        });
        JLabel labelRevenueValue = new JLabel("Value: ");
        textFieldRevenueValue = new JTextField();
        ((PlainDocument) textFieldRevenueValue.getDocument()).setDocumentFilter(new NumberFilter());

        GroupLayout layoutRevenue = new GroupLayout(panelRevenue);
        panelRevenue.setLayout(layoutRevenue);
        layoutRevenue.setHorizontalGroup(layoutRevenue.createParallelGroup()
                .addGroup(layoutRevenue.createSequentialGroup()
                        .addComponent(labelRevenue).addGap(10, 10, Short.MAX_VALUE).addComponent(buttonSelectRevenueAcc))
                .addComponent(textFieldRevenue)
                .addGroup(layoutRevenue.createSequentialGroup()
                        .addComponent(labelRevenueValue).addComponent(textFieldRevenueValue)));
        layoutRevenue.setVerticalGroup(layoutRevenue.createSequentialGroup()
                .addGroup(layoutRevenue.createParallelGroup()
                        .addComponent(labelRevenue,25,25,25).addComponent(buttonSelectRevenueAcc,25,25,25))
                .addComponent(textFieldRevenue,25,25,25).addGap(2)
                .addGroup(layoutRevenue.createParallelGroup().addComponent(labelRevenueValue,25,25,25).addComponent(textFieldRevenueValue,25,25,25)).addGap(5));
        panelRevenue.setVisible(hasRevenue);

        JLabel labelAddRevenue = new JLabel("Add Revenue", IconLoader.getIcon(IconLoader.ADD), SwingConstants.CENTER);
        labelAddRevenue.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                hasRevenue = !hasRevenue;
                panelDebit.setVisible(hasRevenue);
                panelRevenue.setVisible(hasRevenue);
                labelAddRevenue.setText(hasRevenue ? "Revoke Revenue" : "Add Revenue");
                labelAddRevenue.setIcon(hasRevenue ? IconLoader.getIcon(IconLoader.DELETE) : IconLoader.getIcon(IconLoader.ADD));
                ExcludeDialog.this.pack();
            }
        });

        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup().addGap(10)
            .addGroup(layout.createParallelGroup()
                    .addGroup(layout.createSequentialGroup().addComponent(labelDate).addComponent(textFieldDate,80,80,Short.MAX_VALUE).addComponent(buttonToday))
                    .addComponent(panelExpense)
                    .addComponent(labelAddRevenue)
                    .addComponent(panelDebit)
                    .addComponent(panelRevenue)
                    .addGroup(layout.createSequentialGroup().addGap(5, 5, Short.MAX_VALUE).addComponent(buttonCancel).addGap(5).addComponent(buttonOk))).addGap(10));
        layout.setVerticalGroup(layout.createSequentialGroup().addGap(10)
                .addGroup(layout.createParallelGroup().addComponent(labelDate,25,25,25).addComponent(textFieldDate,25,25,25).addComponent(buttonToday,25,25,25))
                .addGap(5)
                .addComponent(panelExpense)
                .addComponent(labelAddRevenue)
                .addGap(5)
                .addComponent(panelDebit)
                .addComponent(panelRevenue)
                .addGroup(layout.createParallelGroup().addComponent(buttonCancel).addComponent(buttonOk)).addGap(10));
    }

    public String getExpenseAccount() {
        return expenseAccount;
    }

    public boolean hasRevenue() {
        return hasRevenue;
    }

    public String getDebitAccount() {
        return debitAccount;
    }

    public String getRevenueAccount() {
        return revenueAccount;
    }

    public String getRevenueValue(){
        return textFieldRevenueValue.getText();
    }

    public String getDate(){
        return textFieldDate.getText();
    }
}
