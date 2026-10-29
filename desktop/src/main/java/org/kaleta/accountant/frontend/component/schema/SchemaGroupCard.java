package org.kaleta.accountant.frontend.component.schema;

import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.component.card.CardStyle;
import org.kaleta.accountant.frontend.action.listener.RenameSchemaGroupAction;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * One schema group, drawn as a card: the group's two digit id and name in the header, its accounts
 * underneath, one row per account id. Groups the user owns also show their free slots, so a card is
 * a complete picture of what the group holds and what it could still hold.
 */
public class SchemaGroupCard extends CardStyle.Card {

    public SchemaGroupCard(Configurable owner, int classId, SchemaModel.Class.Group group) {
        super(headerBackground(classId, group));
        int groupId = Integer.parseInt(group.getId());
        SchemaEditorRules.GroupMode mode = SchemaEditorRules.modeOf(classId, groupId);

        JPanel header = header(classId, groupId, group.getName(), mode);
        add(header, BorderLayout.NORTH);
        if (SchemaEditorRules.canRenameGroup(classId, groupId)) {
            CardStyle.makeHeaderClickable(header, this, new RenameSchemaGroupAction(owner, classId, groupId));
        }
        add(accounts(owner, classId, groupId, group), BorderLayout.CENTER);
    }

    private static Color headerBackground(int classId, SchemaModel.Class.Group group) {
        return SchemaEditorRules.modeOf(classId, Integer.parseInt(group.getId())) == SchemaEditorRules.GroupMode.EDITABLE
                ? CardStyle.HEADER_BG
                : CardStyle.HEADER_BG_LOCKED;
    }

    private JPanel header(int classId, int groupId, String groupName, SchemaEditorRules.GroupMode mode) {
        JPanel header = CardStyle.header();
        header.add(CardStyle.id("" + classId + groupId, CardStyle.ID_FG));
        header.add(Box.createHorizontalStrut(8));

        JLabel name = new JLabel(groupName);
        name.setFont(CardStyle.titleFont());
        name.setForeground(CardStyle.NAME_FG);
        header.add(name);

        header.add(Box.createHorizontalGlue());

        String hintText = SchemaEditorRules.modeHint(mode);
        if (hintText != null) {
            header.add(Box.createHorizontalStrut(8));
            JLabel hint = new JLabel(hintText);
            hint.setFont(CardStyle.hintFont());
            hint.setForeground(CardStyle.MUTED_FG);
            header.add(hint);
        }
        header.add(Box.createHorizontalStrut(8));
        return header;
    }

    private JPanel accounts(Configurable owner, int classId, int groupId, SchemaModel.Class.Group group) {
        JPanel accounts = new JPanel();
        accounts.setLayout(new BoxLayout(accounts, BoxLayout.Y_AXIS));
        accounts.setOpaque(false);
        accounts.setBorder(BorderFactory.createEmptyBorder(5, 4, 7, 4));

        Map<Integer, SchemaModel.Class.Group.Account> accountMap = Service.SCHEMA.getSchemaAccountMap(group);
        for (int accountId = 0; accountId <= SchemaEditorRules.LAST_ID; accountId++) {
            SchemaModel.Class.Group.Account account = accountMap.get(accountId);
            if (account != null) {
                accounts.add(SchemaAccountRow.account(owner, classId, groupId, account));
            } else if (SchemaEditorRules.canCreateAccount(classId, groupId)) {
                accounts.add(SchemaAccountRow.freeSlot(owner, classId, groupId, accountId));
            }
        }
        accounts.add(Box.createVerticalGlue());
        return accounts;
    }
}
