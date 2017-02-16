package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.component.year.model.SchemaModel;
import org.kaleta.accountant.service.Service;

import javax.swing.*;

/**
 * Created by Stanislav Kaleta on 14.02.2017.
 */
public class AccountActionListener extends ActionListener {
    public static final int CREATE = 0;
    public static final int EDIT = 1;
    public static final int DELETE = 2;

    private int action;
    private int cId;
    private int gId;
    private int aId;

    public AccountActionListener(Configurable configurable, int action, int cId, int gId, int aId) {
        super(configurable);
        this.action = action;
        this.cId = cId;
        this.gId = gId;
        this.aId = aId;
    }

    @Override
    protected void actionPerformed() {
        switch (action){
            case 0: {
                String name = JOptionPane.showInputDialog("Set Account Name");
                if (name != null) {
                    String[] typeTexts = new String[]{"Asset","Liability","Expense","Revenue"};
                    String[] typeIds = new String[]{"A","L","E","R"};
                    int typeIndex = JOptionPane.showOptionDialog(null, "types: ","Set Account Type",JOptionPane.DEFAULT_OPTION,
                            JOptionPane.INFORMATION_MESSAGE, null,typeTexts,"A");
                    if (typeIndex != -1){
                        createAccount(name, typeIds[typeIndex]);
                    }
                }
                break;
            }
            case 1: {
                String name = JOptionPane.showInputDialog("Set Account Name",
                        getConfiguration().getModel().getSchemaModel().getClasses().get(cId).getGroups().get(gId).getAccounts().get(aId).getName());
                if (name != null) renameAccount(name);
                break;
            }
            case 2: {
                if (getConfiguration().getModel().getAccountModel().isAccountDeletable(cId,gId,aId)){
                    deleteAccount();
                } else {
                    JOptionPane.showMessageDialog(null, "Account type cannot be deleted, because has at least one associated account with non zero turnover.",
                            "Forbidden Operation!",JOptionPane.WARNING_MESSAGE);
                }
                break;
            }
            default: throw new IllegalArgumentException("Illegal action identifier!");
        }
    }

    private void createAccount(String name, String type){
        SchemaModel model = getConfiguration().getModel().getSchemaModel();
        SchemaModel.Clazz.Group.Account newAccount = new SchemaModel.Clazz.Group.Account();
        newAccount.setId(aId);
        newAccount.setName(name);
        newAccount.setType(type);
        model.getClasses().get(cId).getGroups().get(gId).getAccounts().put(aId,newAccount);

        Service.YEAR.updateSchema(model);
        getConfiguration().update(Configuration.SCHEMA_UPDATED);
    }

    private void renameAccount(String newName){
        SchemaModel model = getConfiguration().getModel().getSchemaModel();
        model.getClasses().get(cId).getGroups().get(gId).getAccounts().get(aId).setName(newName);

        Service.YEAR.updateSchema(model);
        getConfiguration().update(Configuration.SCHEMA_UPDATED);
    }

    private void deleteAccount(){
        SchemaModel model = getConfiguration().getModel().getSchemaModel();
        model.getClasses().get(cId).getGroups().get(gId).getAccounts().remove(aId);

        Service.YEAR.updateSchema(model);
        getConfiguration().update(Configuration.SCHEMA_UPDATED);
    }
}
