package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.service.Service;

/**
 * Creates a schema group in a free slot. For asset and resource classes the service also creates
 * the group's mirror accounts (accumulated depreciation, depreciation, consumption).
 */
public class CreateSchemaGroupAction extends SchemaEditorAction {

    public CreateSchemaGroupAction(Configurable configurable, int classId, int groupId) {
        super(configurable, classId, groupId);
    }

    @Override
    protected void actionPerformed() {
        String name = promptForName("New Group " + classId + groupId, "Name of the new group " + classId + groupId + ":", null);
        if (name != null) {
            Service.SCHEMA.createGroup(selectedYear(), classId, groupId, name);
            schemaUpdated();
        }
    }
}
