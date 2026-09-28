package org.kaleta.accountant.frontend.component.procedure;

import org.kaleta.accountant.backend.model.ProceduresModel;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.action.listener.RenameProcedureGroupAction;
import org.kaleta.accountant.frontend.component.card.CardStyle;

import javax.swing.*;
import java.awt.*;

/**
 * One procedure group, drawn as a card: the group name in the header, one row per procedure
 * underneath, and a row at the end for adding another one to this group.
 */
public class ProcedureGroupCard extends CardStyle.Card {

    public ProcedureGroupCard(Configurable owner, ProceduresModel.Group group) {
        super(CardStyle.HEADER_BG);
        String groupName = group.getName();

        JPanel header = header(groupName);
        add(header, BorderLayout.NORTH);
        CardStyle.makeHeaderClickable(header, this, new RenameProcedureGroupAction(owner, groupName));

        add(procedures(owner, group, groupName), BorderLayout.CENTER);
    }

    private JPanel header(String groupName) {
        JPanel header = CardStyle.header();
        JLabel name = new JLabel(groupName);
        name.setFont(CardStyle.titleFont());
        name.setForeground(CardStyle.NAME_FG);
        header.add(name);
        header.add(Box.createHorizontalGlue());
        return header;
    }

    private JPanel procedures(Configurable owner, ProceduresModel.Group group, String groupName) {
        JPanel procedures = new JPanel();
        procedures.setLayout(new BoxLayout(procedures, BoxLayout.Y_AXIS));
        procedures.setOpaque(false);
        procedures.setBorder(BorderFactory.createEmptyBorder(5, 4, 7, 4));

        for (ProceduresModel.Group.Procedure procedure : group.getProcedure()) {
            procedures.add(ProcedureRow.procedure(owner, procedure, groupName));
        }
        procedures.add(ProcedureRow.newProcedure(owner, groupName));
        procedures.add(Box.createVerticalGlue());
        return procedures;
    }
}
