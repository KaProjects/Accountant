package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.*;

public class SchemaEditorAccountAction extends ActionListener {
    public static final int CREATE = 0;
    public static final int EDIT = 1;
    public static final int DELETE = 2;

    private int action;
    private String classId;
    private String groupId;
    private String accountId;

    public SchemaEditorAccountAction(Configurable configurable, int action, int classId, int groupId, int accountId) {
        super(configurable);
        this.action = action;
        this.classId = String.valueOf(classId);
        this.groupId = String.valueOf(groupId);
        this.accountId = String.valueOf(accountId);
    }

    @Override
    protected void actionPerformed() {
        switch (action) {
            case 0: {
                String name = JOptionPane.showInputDialog((Component) getConfiguration(), "Set Account Name");
                if (name != null && !name.trim().isEmpty()) {
                    switch (classId) {
                        case "0":
                        case "1": {
                            Service.SCHEMA.createAccount(getConfiguration().getSelectedYear(), classId, groupId, accountId, name, Constants.AccountType.ASSET);
                            getConfiguration().update(Configuration.SCHEMA_UPDATED);
                            break;
                        }
                        case "2":
                        case "3": {
                            String[] typeTexts = new String[]{"Asset", "Liability"};
                            String[] typeIds = new String[]{Constants.AccountType.ASSET, Constants.AccountType.LIABILITY};
                            int typeIndex = JOptionPane.showOptionDialog((Component) getConfiguration(), "types: ", "Set Account Type", JOptionPane.DEFAULT_OPTION,
                                    JOptionPane.INFORMATION_MESSAGE, null, typeTexts, "A");
                            if (typeIndex != -1) {
                                Service.SCHEMA.createAccount(getConfiguration().getSelectedYear(), classId, groupId, accountId, name, typeIds[typeIndex]);
                                getConfiguration().update(Configuration.SCHEMA_UPDATED);
                            }
                            break;
                        }
                        case "4": {
                            Service.SCHEMA.createAccount(getConfiguration().getSelectedYear(), classId, groupId, accountId, name, Constants.AccountType.LIABILITY);
                            getConfiguration().update(Configuration.SCHEMA_UPDATED);
                            break;
                        }
                        case "5": {
                            Service.SCHEMA.createAccount(getConfiguration().getSelectedYear(), classId, groupId, accountId, name, Constants.AccountType.EXPENSE);
                            getConfiguration().update(Configuration.SCHEMA_UPDATED);
                            break;
                        }
                        case "6": {
                            Service.SCHEMA.createAccount(getConfiguration().getSelectedYear(), classId, groupId, accountId, name, Constants.AccountType.REVENUE);
                            getConfiguration().update(Configuration.SCHEMA_UPDATED);
                            break;
                        }
                        default:
                            throw new IllegalArgumentException("Illegal class id!");
                    }
                }
                break;
            }
            case 1: {
                String newName = JOptionPane.showInputDialog((Component) getConfiguration(),"Set Account Name",
                        Service.SCHEMA.getAccountName(getConfiguration().getSelectedYear(), classId, groupId, accountId));
                if (newName != null && !newName.trim().isEmpty()) {
                    Service.SCHEMA.renameAccount(getConfiguration().getSelectedYear(), classId, groupId, accountId, newName);
                    getConfiguration().update(Configuration.SCHEMA_UPDATED);
                }
                break;
            }
            case 2: {
                if (Service.SCHEMA.isAccountDeletable(getConfiguration().getSelectedYear(), classId, groupId, accountId)){
                    if (JOptionPane.showConfirmDialog((Component) getConfiguration(),
                            "Are you sure you want to delete '"
                                    + Service.SCHEMA.getAccountName(getConfiguration().getSelectedYear(), classId, groupId, accountId)
                                    + "'?", "Deleting Account", JOptionPane.YES_NO_OPTION) == 0) {
                        Service.SCHEMA.deleteAccount(getConfiguration().getSelectedYear(), classId, groupId, accountId);
                        getConfiguration().update(Configuration.SCHEMA_UPDATED);
                    }
                } else {
                    JOptionPane.showMessageDialog((Component) getConfiguration(), "Schema Account cannot be deleted, because at least one associated account is opened!",
                            "Forbidden Operation!",JOptionPane.WARNING_MESSAGE);
                }
                break;
            }
            default:
                throw new IllegalArgumentException("Illegal action identifier! value=" + action);
        }
    }
}
