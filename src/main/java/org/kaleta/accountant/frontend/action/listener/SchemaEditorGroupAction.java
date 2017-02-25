package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.constants.AccountType;
import org.kaleta.accountant.frontend.component.year.model.SchemaModel;
import org.kaleta.accountant.service.Service;

import javax.swing.*;

/**
 * Created by Stanislav Kaleta on 20.01.2017.
 */
public class SchemaEditorGroupAction extends ActionListener {
    public static final int CREATE = 0;
    public static final int EDIT = 1;
    public static final int DELETE = 2;

    private int action;
    private int cId;
    private int gId;

    public SchemaEditorGroupAction(Configurable configurable, int action, int cId, int gId) {
        super(configurable);
        this.action = action;
        this.cId = cId;
        this.gId = gId;
    }

    @Override
    protected void actionPerformed() {
        switch (action){
            case 0: {
                String name = JOptionPane.showInputDialog("Set Group Name");
                if (name != null) createGroup(name);
                break;
            }
            case 1: {
                String name = JOptionPane.showInputDialog("Set Group Name",
                        getConfiguration().getModel().getSchemaModel().getClasses().get(cId).getGroups().get(gId).getName());
                if (name != null) renameGroup(name);
                break;
            }
            case 2: {
                if (getConfiguration().getModel().getAccountModel().isGroupDeletable(cId,gId)){
                    deleteGroup();
                } else {
                    JOptionPane.showMessageDialog(null, "Group cannot be deleted, because has at least one associated account with non zero turnover.",
                            "Forbidden Operation!",JOptionPane.WARNING_MESSAGE);
                }
                break;
            }
            default: throw new IllegalArgumentException("Illegal action identifier!");
        }
    }

    private void createGroup(String name){
        SchemaModel model = getConfiguration().getModel().getSchemaModel();
        SchemaModel.Clazz.Group newGroup = new SchemaModel.Clazz.Group();
        newGroup.setId(gId);
        newGroup.setName(name);
        model.getClasses().get(cId).getGroups().put(gId,newGroup);

        switch (cId){
            case 0: {
                SchemaModel.Clazz.Group.Account accumulatedDepreciationAccount = new SchemaModel.Clazz.Group.Account();
                accumulatedDepreciationAccount.setId(gId);
                accumulatedDepreciationAccount.setName("A. D. of " + name);
                accumulatedDepreciationAccount.setType(AccountType.LIABILITY);
                model.getClasses().get(cId).getGroups().get(9).getAccounts().put(gId,accumulatedDepreciationAccount);

                SchemaModel.Clazz.Group.Account depreciationAccount = new SchemaModel.Clazz.Group.Account();
                depreciationAccount.setId(gId);
                depreciationAccount.setName("D. of " + name);
                depreciationAccount.setType(AccountType.EXPENSE);
                model.getClasses().get(5).getGroups().get(9).getAccounts().put(gId,depreciationAccount);
                break;
            }
            case 1: {
                SchemaModel.Clazz.Group.Account consumptionAccount = new SchemaModel.Clazz.Group.Account();
                consumptionAccount.setId(gId);
                consumptionAccount.setName("C. of " + name);
                consumptionAccount.setType(AccountType.EXPENSE);
                model.getClasses().get(5).getGroups().get(8).getAccounts().put(gId,consumptionAccount);
                break;
            }
            // todo impl if needed
        }

        Service.YEAR.updateSchema(model);
        getConfiguration().update(Configuration.SCHEMA_UPDATED);
    }

    private void renameGroup(String newName){
        SchemaModel model = getConfiguration().getModel().getSchemaModel();
        model.getClasses().get(cId).getGroups().get(gId).setName(newName);

        switch (cId){
            case 0: {
                model.getClasses().get(cId).getGroups().get(9).getAccounts().get(gId).setName("A. D. of " + newName);
                model.getClasses().get(5).getGroups().get(9).getAccounts().get(gId).setName("D. of " + newName);
                break;
            }
            case 1: {
                model.getClasses().get(5).getGroups().get(8).getAccounts().get(gId).setName("C. of " + newName);
                break;
            }
            // todo impl if needed
        }

        Service.YEAR.updateSchema(model);
        getConfiguration().update(Configuration.SCHEMA_UPDATED);
    }

    private void deleteGroup(){
        SchemaModel model = getConfiguration().getModel().getSchemaModel();
        model.getClasses().get(cId).getGroups().remove(gId);

        switch (cId){
            case 0: {
                model.getClasses().get(cId).getGroups().get(9).getAccounts().remove(gId);
                model.getClasses().get(5).getGroups().get(9).getAccounts().remove(gId);
                break;
            }
            case 1: {
                model.getClasses().get(5).getGroups().get(8).getAccounts().remove(gId);
                break;
            }
            // todo impl if needed
        }

        Service.YEAR.updateSchema(model);
        getConfiguration().update(Configuration.SCHEMA_UPDATED);
    }
}
