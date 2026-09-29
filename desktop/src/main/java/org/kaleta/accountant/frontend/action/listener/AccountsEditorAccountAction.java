package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.ProceduresModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.component.accounts.AccountsEditorRules;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.util.Collections;

public class AccountsEditorAccountAction extends ActionListener {
    private String schemaId;
    private JTextField tfName;

    public AccountsEditorAccountAction(Configurable configurable, String schemaId, JTextField tfName) {
        super(configurable);
        if (!AccountsEditorRules.canCreateAccount(schemaId)) {
            throw new IllegalArgumentException("Adding accounts restricted for '" + schemaId + "'");
        }
        this.schemaId = schemaId;
        this.tfName = tfName;
    }

    @Override
    protected void actionPerformed() {
        subactionPerformed(tfName.getText());

    }

    public AccountsModel.Account subactionPerformed(String name) {
        return subactionPerformed(name, "", "");
    }

    /**
     * The same, with what the account's own procedure needs: the other account of its single
     * transaction and the amount usually moved. Both may be left empty - the procedure is still
     * written, with the account it follows already filled in, and the rest is added later by
     * editing the procedure.
     */
    public AccountsModel.Account subactionPerformed(String name, String otherAccount, String amount) {
        String year = getConfiguration().getSelectedYear();
        String semanticId = Service.ACCOUNT.getNextSemanticId(getConfiguration().getSelectedYear(), schemaId);

        AccountsModel.Account createdAccount = Service.ACCOUNT.createAccount(year, name, schemaId, semanticId, "");

        //Calendar calendar = Calendar.getInstance();
        //String date = String.format("%1$02d%2$02d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1);
        String date = "0101";

        String createdAccType = Service.SCHEMA.getSchemaAccountType(year, createdAccount.getSchemaId());
        switch (createdAccType) {
            case Constants.AccountType.ASSET: {
                Service.TRANSACTIONS.addTransaction(year, date, "0", createdAccount.getFullId(), Constants.Account.INIT_ACC_ID, Constants.Transaction.OPEN_DESCRIPTION);
                break;
            }
            case Constants.AccountType.LIABILITY: {
                Service.TRANSACTIONS.addTransaction(year, date, "0", Constants.Account.INIT_ACC_ID, createdAccount.getFullId(), Constants.Transaction.OPEN_DESCRIPTION);
                break;
            }
            // accounts of other types aren't openable, thus no open transaction
        }

        if (schemaId.startsWith(Constants.Schema.CREDIT_ACCOUNT_SCHEMA_PREFIX)) {
            // repaying a debt: out of the account it is paid from, into the debt itself
            createProcedure(year, Constants.Procedure.REPAYMENT_GROUP_NAME, Constants.Procedure.REPAYMENT_PROCEDURE_PREFIX + name,
                    createdAccount.getFullId(), otherAccount, amount);
        }
        if (schemaId.equals(Constants.Schema.CURRENT_ACCOUNT_SCHEMA_ID)) {
            // withdrawing cash: out of the bank account, into the cash it becomes
            createProcedure(year, Constants.Procedure.WITHDRAWAL_GROUP_NAME, Constants.Procedure.WITHDRAWAL_PROCEDURE_PREFIX + name,
                    otherAccount, createdAccount.getFullId(), amount);
        }

        if (schemaId.startsWith("1")) {
            String consumptionAccId = Service.ACCOUNT.getConsumptionAccountId(schemaId, semanticId);
            String conAccName = (name.equals(Constants.Account.GENERAL_ACCOUNT_NAME))
                    ? "General " + Constants.Schema.CONSUMPTION_ACCOUNT_PREFIX + Service.SCHEMA.getAccountName(year, "1", createdAccount.getGroupId(), createdAccount.getSchemaAccountId())
                    : Constants.Schema.CONSUMPTION_ACCOUNT_PREFIX + name;
            Service.ACCOUNT.createAccount(year, conAccName, consumptionAccId.split("\\.")[0], consumptionAccId.split("\\.")[1], "");
        }

        getConfiguration().update(Configuration.ACCOUNT_UPDATED);
        getConfiguration().update(Configuration.TRANSACTION_UPDATED);
        getConfiguration().update(Configuration.PROCEDURE_UPDATED);
        return createdAccount;
    }

    /** Both procedures are a single transaction; only which side the new account sits on differs. */
    private void createProcedure(String year, String groupName, String name, String debit, String credit, String amount) {
        ProceduresModel.Group.Procedure.Transaction transaction = new ProceduresModel.Group.Procedure.Transaction();
        transaction.setDescription(name);
        transaction.setAmount(amount == null ? "" : amount);
        transaction.setDebit(debit == null ? "" : debit);
        transaction.setCredit(credit == null ? "" : credit);

        Service.PROCEDURES.createManagedProcedure(year, name, groupName, Collections.singletonList(transaction));
    }
}
