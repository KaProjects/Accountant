package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.*;

/**
 * Creates a schema account in a free slot of a group. The class decides which side of the books
 * the account belongs to; only finance and relations can hold both, and there the user picks.
 */
public class CreateSchemaAccountAction extends SchemaEditorAction {
    private final String accountId;

    public CreateSchemaAccountAction(Configurable configurable, int classId, int groupId, int accountId) {
        super(configurable, classId, groupId);
        this.accountId = String.valueOf(accountId);
    }

    @Override
    protected void actionPerformed() {
        String schemaId = classId + groupId + accountId;
        String name = promptForName("New Account " + schemaId, "Name of the new account " + schemaId + ":", null);
        if (name == null) {
            return;
        }
        String type = accountTypeOfClass();
        if (type == null) {
            return;
        }
        Service.SCHEMA.createAccount(selectedYear(), classId, groupId, accountId, name, type);
        schemaUpdated();
    }

    /**
     * Account type implied by the class, asking the user where the class allows both. Returns null
     * when the user cancels that question.
     */
    private String accountTypeOfClass() {
        switch (classId) {
            case "0":
            case "1":
                return Constants.AccountType.ASSET;
            case "2":
            case "3":
                return askForAssetOrLiability();
            case "4":
                return Constants.AccountType.LIABILITY;
            case "5":
                return Constants.AccountType.EXPENSE;
            case "6":
                return Constants.AccountType.REVENUE;
            case "7":
                return Constants.AccountType.OFF_BALANCE;
            default:
                throw new IllegalArgumentException("Illegal class id! value=" + classId);
        }
    }

    private String askForAssetOrLiability() {
        String[] options = new String[]{"Asset", "Liability"};
        int choice = JOptionPane.showOptionDialog((Component) getConfiguration(), "Which side of the books does it belong to?",
                "Account Type", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        switch (choice) {
            case 0: return Constants.AccountType.ASSET;
            case 1: return Constants.AccountType.LIABILITY;
            default: return null;
        }
    }
}
