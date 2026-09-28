package org.kaleta.accountant.frontend.dialog;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.component.HintValidatedTextField;
import org.kaleta.accountant.frontend.component.SelectAccountTextField;
import org.kaleta.accountant.frontend.component.ValidatedComboBox;

import javax.swing.*;
import java.util.List;
import java.util.Map;

/**
 * Creates a long-term financial asset.
 * <p>
 * Besides the asset itself the dialog collects what its creation procedure needs: the account the
 * money comes from, and the amount usually put in. The procedure is written as the asset is
 * created, so that paying into it afterwards is a matter of running it rather than composing the
 * same transaction by hand every time.
 */
public class AddFinAssetDialog extends Dialog {
    private final List<SchemaModel.Class.Group.Account> schemaAccounts;
    private final Map<String, List<AccountsModel.Account>> creditAccountMap;
    private final List<SchemaModel.Class> creditClasses;

    private JTextField textFieldName;
    private JComboBox<SchemaModel.Class.Group.Account> comboBoxAcc;
    private JTextField textFieldAmount;
    private SelectAccountTextField textFieldCreditAcc;

    public AddFinAssetDialog(Configuration configuration, List<SchemaModel.Class.Group.Account> schemaAccounts,
                             Map<String, List<AccountsModel.Account>> creditAccountMap, List<SchemaModel.Class> creditClasses) {
        super(configuration, "Creating Long-Term Financial Asset", "Create");
        this.schemaAccounts = schemaAccounts;
        this.creditAccountMap = creditAccountMap;
        this.creditClasses = creditClasses;
        buildDialogContent();
        pack();
    }

    private void buildDialogContent() {
        JLabel labelName = new JLabel("Name:");
        textFieldName = new HintValidatedTextField("", "Name", "set name", false, this);

        JLabel labelAcc = new JLabel("Type:");
        comboBoxAcc = new ValidatedComboBox<>("Type", this);
        schemaAccounts.forEach(account -> comboBoxAcc.addItem(account));
        comboBoxAcc.setSelectedIndex(-1);

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        JLabel labelProcedure = new JLabel("Creation procedure");
        labelProcedure.setFont(labelProcedure.getFont().deriveFont(java.awt.Font.BOLD));

        JLabel labelCreditAcc = new JLabel("Paid from:");
        textFieldCreditAcc = new SelectAccountTextField(getConfiguration(), creditAccountMap, creditClasses, "Paid from", this);

        JLabel labelAmount = new JLabel("Usual Amount:");
        textFieldAmount = new HintValidatedTextField("", "Usual Amount", "set usual amount", true, this);

        setContent(layout -> {
            layout.setHorizontalGroup(layout.createParallelGroup()
                    .addGroup(layout.createSequentialGroup().addComponent(labelName, 110, 110, 110).addComponent(textFieldName, 250, 250, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup().addComponent(labelAcc, 110, 110, 110).addComponent(comboBoxAcc, 250, 250, Short.MAX_VALUE))
                    .addComponent(separator)
                    .addComponent(labelProcedure)
                    .addComponent(labelCreditAcc)
                    .addComponent(textFieldCreditAcc, 250, 250, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup().addComponent(labelAmount, 110, 110, 110).addComponent(textFieldAmount, 250, 250, Short.MAX_VALUE)));
            layout.setVerticalGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup().addComponent(labelName, 25, 25, 25).addComponent(textFieldName, 25, 25, 25))
                    .addGap(3)
                    .addGroup(layout.createParallelGroup().addComponent(labelAcc, 25, 25, 25).addComponent(comboBoxAcc, 25, 25, 25))
                    .addGap(8)
                    .addComponent(separator, 5, 5, 5)
                    .addComponent(labelProcedure, 25, 25, 25)
                    .addComponent(labelCreditAcc, 25, 25, 25)
                    .addComponent(textFieldCreditAcc, 25, 25, 25)
                    .addGap(3)
                    .addGroup(layout.createParallelGroup().addComponent(labelAmount, 25, 25, 25).addComponent(textFieldAmount, 25, 25, 25)));
        });
    }

    public String getAccName() {
        return textFieldName.getText().trim();
    }

    /** Id of the schema account within group 23, which is what makes the asset's schema id. */
    public String getSchemaAccountId() {
        return schemaAccounts.get(comboBoxAcc.getSelectedIndex()).getId();
    }

    public String getAmount() {
        return textFieldAmount.getText();
    }

    public String getCreditAccount() {
        return textFieldCreditAcc.getSelectedAccount();
    }
}
