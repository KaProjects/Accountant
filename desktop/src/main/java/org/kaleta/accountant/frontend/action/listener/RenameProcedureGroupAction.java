package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.service.Service;

import java.awt.*;

/**
 * Renames a procedure group. Giving it the name of another group merges the two.
 */
public class RenameProcedureGroupAction extends ActionListener {
    private final String groupName;

    public RenameProcedureGroupAction(Configurable configurable, String groupName) {
        super(configurable);
        this.groupName = groupName;
    }

    @Override
    protected void actionPerformed() {
        String newName = NamePrompt.ask((Component) getConfiguration(), "Rename Procedure Group",
                "Name of the group:", groupName);
        if (newName != null && !newName.equals(groupName)) {
            Service.PROCEDURES.renameProcedureGroup(getConfiguration().getSelectedYear(), groupName, newName);
            getConfiguration().update(Configuration.PROCEDURE_UPDATED);
        }
    }
}
