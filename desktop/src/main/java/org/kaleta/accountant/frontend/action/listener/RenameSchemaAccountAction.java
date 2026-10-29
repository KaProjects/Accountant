package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.service.Service;

/**
 * Renames a schema account. The id keeps its meaning, only the label changes.
 */
public class RenameSchemaAccountAction extends SchemaEditorAction {
    private final String accountId;

    public RenameSchemaAccountAction(Configurable configurable, int classId, int groupId, int accountId) {
        super(configurable, classId, groupId);
        this.accountId = String.valueOf(accountId);
    }

    @Override
    protected void actionPerformed() {
        String schemaId = classId + groupId + accountId;
        String currentName = Service.SCHEMA.getAccountName(selectedYear(), classId, groupId, accountId);
        String newName = promptForName("Rename Account " + schemaId, "Name of account " + schemaId + ":", currentName);
        if (newName != null && !newName.equals(currentName)) {
            Service.SCHEMA.renameAccount(selectedYear(), classId, groupId, accountId, newName);
            schemaUpdated();
        }
    }
}
