package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.year.model.SchemaModel;
import org.kaleta.accountant.service.Service;

import javax.swing.*;

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
        switch (action) {
            case 0: {
                String name = JOptionPane.showInputDialog("Set Group Name");
                if (name != null && !name.trim().isEmpty()) {
                    Service.SCHEMA.createGroup(getConfiguration().getSelectedYear(), String.valueOf(cId), String.valueOf(gId), name);
                    getConfiguration().update(Configuration.SCHEMA_UPDATED);
                }
                break;
            }
            case 1: {
                String name = JOptionPane.showInputDialog("Set Group Name",
                        Service.SCHEMA.getSchemaName(getConfiguration().getSelectedYear(), cId, gId));
                if (name != null && !name.trim().isEmpty()) renameGroup(name);
                break;
            }
            case 2: {
                if (Service.SCHEMA.isGroupDeletable(getConfiguration().getSelectedYear(), cId, gId)) {
                    deleteGroup();
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Group cannot be deleted, because has at least one associated account is opened!",
                            "Forbidden Operation!",
                            JOptionPane.WARNING_MESSAGE);
                }
                break;
            }
            default:
                throw new IllegalArgumentException("Illegal action identifier!");
        }
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
