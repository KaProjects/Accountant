package org.kaleta.accountant.frontend.component.procedure;

import org.kaleta.accountant.backend.model.ProceduresModel;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.action.listener.OpenCreateProcedureDialog;
import org.kaleta.accountant.frontend.action.listener.OpenEditProcedureDialog;
import org.kaleta.accountant.frontend.common.IconLoader;
import org.kaleta.accountant.frontend.component.card.CardRow;
import org.kaleta.accountant.frontend.component.card.CardStyle;

import javax.swing.*;
import java.awt.*;

/**
 * One line of a procedure group card: a procedure, or the invitation to add one. Clicking a row
 * edits that procedure, exactly as a schema row edits its account.
 */
class ProcedureRow extends CardRow {

    private ProcedureRow() {
        super();
    }

    static ProcedureRow procedure(Configurable owner, ProceduresModel.Group.Procedure procedure, String groupName) {
        ProcedureRow row = new ProcedureRow();

        row.add(CardStyle.id(procedure.getId(), CardStyle.ID_FG));
        row.add(Box.createHorizontalStrut(8));

        JLabel name = new JLabel(procedure.getName());
        name.setFont(CardStyle.nameFont());
        name.setForeground(CardStyle.NAME_FG);
        row.add(name);

        row.add(Box.createHorizontalStrut(8));
        row.add(Box.createHorizontalGlue());
        row.add(transactionCount(procedure.getTransaction().size()));
        row.add(Box.createHorizontalStrut(6));

        CardStyle.makeRowClickable(row, new OpenEditProcedureDialog(owner.getConfiguration(), procedure, groupName));
        return row;
    }

    /** Row that adds another procedure to this group. */
    static ProcedureRow newProcedure(Configurable owner, String groupName) {
        ProcedureRow row = new ProcedureRow();

        row.add(CardStyle.id(" ", CardStyle.SLOT_FG));
        row.add(Box.createHorizontalStrut(8));

        JLabel hint = new JLabel("new procedure");
        hint.setFont(CardStyle.hintFont());
        hint.setForeground(CardStyle.SLOT_FG);
        row.add(hint);

        row.add(Box.createHorizontalGlue());
        row.add(CardStyle.icon(IconLoader.ADD, 10));
        row.add(Box.createHorizontalStrut(2));

        CardStyle.makeRowClickable(row, new OpenCreateProcedureDialog(owner, groupName));
        return row;
    }

    /** How many transactions the procedure books, in the place the schema editor puts its type. */
    private static JLabel transactionCount(int count) {
        JLabel chip = new JLabel(String.valueOf(count), SwingConstants.CENTER);
        chip.setFont(CardStyle.baseFont().deriveFont(Font.BOLD, CardStyle.baseFont().getSize2D() - 2f));
        chip.setForeground(CardStyle.ID_FG);
        chip.setBackground(CardStyle.HEADER_BG);
        chip.setOpaque(true);
        chip.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
        return chip;
    }
}
