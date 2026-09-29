package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;

import java.awt.*;

/**
 * Shared plumbing for the schema editor's actions: the group being edited, the name prompt and the
 * refresh that follows a change.
 * <p>
 * There is deliberately no delete action. The schema is shared by every year and its three digit
 * ids are referenced by every account and transaction ever recorded, so a schema group or account
 * is only ever created or renamed.
 */
abstract class SchemaEditorAction extends ActionListener {
    final String classId;
    final String groupId;

    SchemaEditorAction(Configurable configurable, int classId, int groupId) {
        super(configurable);
        this.classId = String.valueOf(classId);
        this.groupId = String.valueOf(groupId);
    }

    String promptForName(String title, String message, String initialValue) {
        return NamePrompt.ask((Component) getConfiguration(), title, message, initialValue);
    }

    String selectedYear() {
        return getConfiguration().getSelectedYear();
    }

    void schemaUpdated() {
        getConfiguration().update(Configuration.SCHEMA_UPDATED);
    }
}
