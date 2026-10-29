package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.service.Service;

import java.awt.*;

/**
 * Creates an empty procedure group, which is all the "new group" card promises: the group is named
 * here and filled from its own card afterwards, rather than by composing a procedure first.
 */
public class CreateProcedureGroupAction extends ActionListener {

    public CreateProcedureGroupAction(Configurable configurable) {
        super(configurable);
    }

    @Override
    protected void actionPerformed() {
        String name = NamePrompt.ask((Component) getConfiguration(), "New Procedure Group",
                "Name of the group:", "");
        if (name == null) {
            return;
        }
        Service.PROCEDURES.createProcedureGroup(getConfiguration().getSelectedYear(), name);
        getConfiguration().update(Configuration.PROCEDURE_UPDATED);
    }
}
