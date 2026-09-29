package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.backend.model.ProceduresModel;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.*;

/**
 * Opens what clicking a procedure group's header does: renaming the group, or deleting it.
 * <p>
 * Giving the group the name of another group merges the two. Deleting takes the procedures in it
 * with it, so a group that still holds any is confirmed first - the card shows the group's name,
 * not its contents, and the count is what makes the consequence visible.
 */
public class RenameProcedureGroupAction extends ActionListener {
    private final String groupName;

    public RenameProcedureGroupAction(Configurable configurable, String groupName) {
        super(configurable);
        this.groupName = groupName;
    }

    @Override
    protected void actionPerformed() {
        Component parent = (Component) getConfiguration();
        JTextField field = new JTextField(groupName, 20);
        Object[] options = {"Rename", "Delete Group", "Cancel"};
        int choice = JOptionPane.showOptionDialog(parent, new Object[]{"Name of the group:", field},
                "Procedure Group", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (choice == 0) {
            rename(field.getText().trim());
        } else if (choice == 1) {
            delete(parent);
        }
    }

    private void rename(String newName) {
        if (newName.isEmpty() || newName.equals(groupName)) {
            return;
        }
        Service.PROCEDURES.renameProcedureGroup(getConfiguration().getSelectedYear(), groupName, newName);
        getConfiguration().update(Configuration.PROCEDURE_UPDATED);
    }

    private void delete(Component parent) {
        int count = procedureCount();
        if (count > 0) {
            String message = "Delete the group '" + groupName + "' and the " + count
                    + (count == 1 ? " procedure" : " procedures") + " in it?";
            int confirmed = JOptionPane.showConfirmDialog(parent, message, "Delete Procedure Group",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirmed != JOptionPane.YES_OPTION) {
                return;
            }
        }
        Service.PROCEDURES.deleteProcedureGroup(getConfiguration().getSelectedYear(), groupName);
        getConfiguration().update(Configuration.PROCEDURE_UPDATED);
    }

    private int procedureCount() {
        for (ProceduresModel.Group group : Service.PROCEDURES.getProcedureGroupList(getConfiguration().getSelectedYear())) {
            if (group.getName().equals(groupName)) {
                return group.getProcedure().size();
            }
        }
        return 0;
    }
}
