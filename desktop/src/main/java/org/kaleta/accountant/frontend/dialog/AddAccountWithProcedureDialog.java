package org.kaleta.accountant.frontend.dialog;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.component.HintValidatedTextField;
import org.kaleta.accountant.frontend.component.SelectAccountTextField;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Creates an account that comes with a procedure of its own: a credit account and the repayment
 * that pays it down, a bank account and the withdrawal that takes cash out of it.
 * <p>
 * Both procedures are one transaction whose other side never varies, so the only things asked for
 * beyond the name are that other account and the amount usually moved. Neither is required: the
 * procedure is written regardless, with the account it follows already in place, and what is left
 * out is filled in later by editing it. The picker is narrowed to the accounts that make sense
 * there - a loan is repaid from a current account, cash is withdrawn into a cash account.
 */
public class AddAccountWithProcedureDialog extends Dialog {
    private final String procedureHeading;
    private final String accountLabel;
    private final String amountLabel;
    private final Map<String, List<AccountsModel.Account>> accountMap;
    private final List<SchemaModel.Class> classList;

    private JTextField textFieldName;
    private SelectAccountTextField textFieldAccount;
    private HintValidatedTextField textFieldAmount;

    public AddAccountWithProcedureDialog(Configuration configuration, String title, String procedureHeading,
                                         String accountLabel, String amountLabel,
                                         Map<String, List<AccountsModel.Account>> accountMap,
                                         List<SchemaModel.Class> classList) {
        super(configuration, title, "Create");
        this.procedureHeading = procedureHeading;
        this.accountLabel = accountLabel;
        this.amountLabel = amountLabel;
        this.accountMap = accountMap;
        this.classList = classList;
        buildDialogContent();
        pack();
    }

    private void buildDialogContent() {
        JLabel labelName = new JLabel("Name:");
        textFieldName = new HintValidatedTextField("", "Name", "set name", false, this);

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        JLabel labelProcedure = new JLabel(procedureHeading);
        labelProcedure.setFont(labelProcedure.getFont().deriveFont(Font.BOLD));

        // the procedure is written either way: what is not known now is filled in by editing it
        JLabel labelAccount = new JLabel(accountLabel);
        textFieldAccount = new SelectAccountTextField(getConfiguration(), accountMap, classList, accountLabel, this);
        textFieldAccount.setValidatorEnabled(false);

        JLabel labelAmount = new JLabel(amountLabel);
        textFieldAmount = new HintValidatedTextField("", amountLabel, "optional", true, this);
        textFieldAmount.setValidatorEnabled(false);

        setContent(layout -> {
            layout.setHorizontalGroup(layout.createParallelGroup()
                    .addGroup(layout.createSequentialGroup().addComponent(labelName, 110, 110, 110).addComponent(textFieldName, 250, 250, Short.MAX_VALUE))
                    .addComponent(separator)
                    .addComponent(labelProcedure)
                    .addComponent(labelAccount)
                    .addComponent(textFieldAccount, 250, 250, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup().addComponent(labelAmount, 110, 110, 110).addComponent(textFieldAmount, 250, 250, Short.MAX_VALUE)));
            layout.setVerticalGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup().addComponent(labelName, 25, 25, 25).addComponent(textFieldName, 25, 25, 25))
                    .addGap(8)
                    .addComponent(separator, 5, 5, 5)
                    .addComponent(labelProcedure, 25, 25, 25)
                    .addComponent(labelAccount, 25, 25, 25)
                    .addComponent(textFieldAccount, 25, 25, 25)
                    .addGap(3)
                    .addGroup(layout.createParallelGroup().addComponent(labelAmount, 25, 25, 25).addComponent(textFieldAmount, 25, 25, 25)));
        });
    }

    public String getAccName() {
        return textFieldName.getText().trim();
    }

    /** The other side of the procedure's transaction: what repays the debt, or what the cash goes into. */
    public String getOtherAccount() {
        return textFieldAccount.getSelectedAccount();
    }

    public String getAmount() {
        return textFieldAmount.getText();
    }
}
