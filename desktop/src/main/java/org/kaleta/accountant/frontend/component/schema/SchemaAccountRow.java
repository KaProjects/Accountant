package org.kaleta.accountant.frontend.component.schema;

import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.action.listener.CreateSchemaAccountAction;
import org.kaleta.accountant.frontend.action.listener.RenameSchemaAccountAction;
import org.kaleta.accountant.frontend.common.IconLoader;
import org.kaleta.accountant.frontend.component.card.CardRow;
import org.kaleta.accountant.frontend.component.card.CardStyle;

import javax.swing.*;
import java.awt.*;

/**
 * One line of a group card: either a schema account, or the free slot where one can be created.
 * The row itself is the click target - clicking anywhere on it opens the rename or create dialog -
 * so it carries no buttons. Rows are a fixed height so that the ten slots of every group line up
 * across the whole editor.
 */
class SchemaAccountRow extends CardRow {

    /** Row of an existing account. Renameable unless the app maintains it. */
    static SchemaAccountRow account(Configurable owner, int classId, int groupId,
                                    SchemaModel.Class.Group.Account account) {
        SchemaAccountRow row = new SchemaAccountRow();
        int accountId = Integer.parseInt(account.getId());
        String schemaId = "" + classId + groupId + accountId;
        boolean locked = !SchemaEditorRules.canRenameAccount(classId, groupId, accountId);

        row.add(CardStyle.id(schemaId, CardStyle.ID_FG));
        row.add(Box.createHorizontalStrut(8));

        JLabel name = new JLabel(account.getName());
        name.setFont(CardStyle.nameFont());
        // a locked account is greyed, to show that clicking it will not rename it
        name.setForeground(locked ? CardStyle.ID_FG : CardStyle.NAME_FG);
        row.add(name);

        row.add(Box.createHorizontalStrut(8));
        row.add(Box.createHorizontalGlue());
        row.add(AccountTypeChip.of(account.getType()));

        row.add(Box.createHorizontalStrut(6));
        if (!locked) {
            CardStyle.makeRowClickable(row, new RenameSchemaAccountAction(owner, classId, groupId, accountId));
        }
        return row;
    }

    /** Row of a free slot, offering to create the account that would take this id. */
    static SchemaAccountRow freeSlot(Configurable owner, int classId, int groupId, int accountId) {
        SchemaAccountRow row = new SchemaAccountRow();
        String schemaId = "" + classId + groupId + accountId;

        row.add(CardStyle.id(schemaId, CardStyle.SLOT_FG));
        row.add(Box.createHorizontalStrut(8));

        JLabel hint = new JLabel("free");
        hint.setFont(CardStyle.hintFont());
        hint.setForeground(CardStyle.SLOT_FG);
        row.add(hint);

        row.add(Box.createHorizontalGlue());
        row.add(CardStyle.icon(IconLoader.ADD, 10));
        row.add(Box.createHorizontalStrut(2));

        CardStyle.makeRowClickable(row, new CreateSchemaAccountAction(owner, classId, groupId, accountId));
        return row;
    }

}
