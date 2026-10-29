package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.service.Service;

/**
 * Renames a schema group, and with it the mirror accounts named after it.
 */
public class RenameSchemaGroupAction extends SchemaEditorAction {

    public RenameSchemaGroupAction(Configurable configurable, int classId, int groupId) {
        super(configurable, classId, groupId);
    }

    @Override
    protected void actionPerformed() {
        String currentName = Service.SCHEMA.getGroupName(selectedYear(), classId, groupId);
        String newName = promptForName("Rename Group " + classId + groupId, "Name of group " + classId + groupId + ":", currentName);
        if (newName != null && !newName.equals(currentName)) {
            Service.SCHEMA.renameGroup(selectedYear(), classId, groupId, newName);
            schemaUpdated();
        }
    }
}
