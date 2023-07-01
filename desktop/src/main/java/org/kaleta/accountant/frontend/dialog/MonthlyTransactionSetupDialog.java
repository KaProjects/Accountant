package org.kaleta.accountant.frontend.dialog;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.component.DayOfMonthPickerTextField;
import org.kaleta.accountant.frontend.component.HintValidatedTextField;
import org.kaleta.accountant.frontend.component.SelectAccountTextField;

import javax.swing.*;
import java.util.List;
import java.util.Map;

public class MonthlyTransactionSetupDialog extends Dialog {

    private DayOfMonthPickerTextField tfDayOfMonth;
    private HintValidatedTextField tfYearlyAmount;
    private SelectAccountTextField tfDebit;
    private SelectAccountTextField tfCredit;
    private JComboBox<String> cbDescription;

    private final Map<String, List<AccountsModel.Account>> accountMap;
    private final List<SchemaModel.Class> classList;

    public MonthlyTransactionSetupDialog(Configuration configuration, Map<String, List<AccountsModel.Account>> accountMap, List<SchemaModel.Class> classList) {
        super(configuration, "Monthly Transaction Setup", "Create");
        this.accountMap = accountMap;
        this.classList = classList;
        buildDialogContent();
        pack();
    }

    private void buildDialogContent() {
        tfDayOfMonth = new DayOfMonthPickerTextField("", this);

        tfYearlyAmount = new HintValidatedTextField("","Yearly Transaction Amount Sum", "set yearly amount", true, this);

        JLabel labelDebit = new JLabel("Debit:");
        tfDebit = new SelectAccountTextField(getConfiguration(), accountMap, classList, "Debit", this);
        tfDebit.getDocument().addDocumentListener(this);

        JLabel labelCredit = new JLabel("Credit:");
        tfCredit = new SelectAccountTextField(getConfiguration(), accountMap, classList,"Credit", this);
        tfCredit.getDocument().addDocumentListener(this);

        JLabel labelDesc = new JLabel("Description:");
        cbDescription = new JComboBox<>();
        cbDescription.setEditable(true);

        setContent(layout -> {
            layout.setHorizontalGroup(layout.createParallelGroup()
                    .addComponent(tfDayOfMonth)
                    .addComponent(tfYearlyAmount)
                    .addComponent(labelDebit)
                    .addComponent(tfDebit)
                    .addComponent(labelCredit)
                    .addComponent(tfCredit)
                    .addComponent(labelDesc)
                    .addComponent(cbDescription));
            layout.setVerticalGroup(layout.createSequentialGroup()
                    .addComponent(tfDayOfMonth)
                    .addComponent(tfYearlyAmount)
                    .addComponent(labelDebit)
                    .addComponent(tfDebit)
                    .addComponent(labelCredit)
                    .addComponent(tfCredit)
                    .addComponent(labelDesc)
                    .addComponent(cbDescription));
        });
    }

    public String getDayOfMonth(){
        return tfDayOfMonth.getText();
    }
    public String getYearlyAmount(){
        return tfYearlyAmount.getText();
    }
    public String getDebit(){
        return tfDebit.getSelectedAccount();
    }
    public String getCredit(){
        return tfCredit.getSelectedAccount();
    }
    public String getDescription(){
        return ((JTextField)cbDescription.getEditor().getEditorComponent()).getText();
    }
}
