package org.kaleta.accountant.frontend.component.schema;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.action.listener.CreateSchemaGroupAction;
import org.kaleta.accountant.frontend.component.card.PlaceholderCard;

import javax.swing.*;
import java.util.List;

/**
 * Offers the group ids still free in a class. Clicking it lists them, so a new group can be given
 * the slot it belongs in rather than the next one that happens to be free.
 */
public class NewSchemaGroupCard extends PlaceholderCard {

    public NewSchemaGroupCard(Configurable owner, int classId, List<Integer> freeGroupIds) {
        super("+  new group", null);
        JPopupMenu slots = new JPopupMenu();
        for (int groupId : freeGroupIds) {
            JMenuItem item = new JMenuItem("group " + classId + groupId);
            item.addActionListener(new CreateSchemaGroupAction(owner, classId, groupId));
            slots.add(item);
        }
        onClick(e -> slots.show(this, getLabel().getX(), getLabel().getY() + getLabel().getHeight()));
    }
}
