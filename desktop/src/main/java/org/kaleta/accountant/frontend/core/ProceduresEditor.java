package org.kaleta.accountant.frontend.core;

import org.kaleta.accountant.backend.model.ProceduresModel;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.configuration.ConfigurationAction;
import org.kaleta.accountant.frontend.action.listener.OpenCreateProcedureDialog;
import org.kaleta.accountant.frontend.component.card.CardCanvas;
import org.kaleta.accountant.frontend.component.card.CardStyle;
import org.kaleta.accountant.frontend.component.card.PlaceholderCard;
import org.kaleta.accountant.frontend.component.procedure.ProcedureGroupCard;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Editor of the procedures: one card per procedure group, one row per procedure.
 * <p>
 * A procedure is a template of transactions that are always booked together - a salary, a standing
 * order. This editor only maintains those templates: clicking a row edits the procedure it names.
 * <p>
 * Procedures live in a single file shared by every year, so what is shown here does not depend on
 * the selected year; it is read afresh on every update rather than cached, so a procedure created
 * or moved elsewhere shows up immediately.
 */
public class ProceduresEditor extends JPanel implements Configurable {
    private Configuration configuration;
    private final CardCanvas canvas;

    public ProceduresEditor(Configuration configuration) {
        setConfiguration(configuration);
        setLayout(new BorderLayout());
        setBackground(CardStyle.CANVAS);

        canvas = new CardCanvas();
        JScrollPane scrollPane = new JScrollPane(canvas);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(CardStyle.ROW_HEIGHT);
        // the cards wrap into as many rows as the window is wide, so it never scrolls sideways
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);

        this.getActionMap().put(Configuration.PROCEDURE_UPDATED, new ConfigurationAction(this) {
            @Override
            protected void actionPerformed() {
                ProceduresEditor.this.update();
            }
        });

        update();
    }

    public void update() {
        List<JComponent> cards = new ArrayList<>();
        for (ProceduresModel.Group group : Service.PROCEDURES.getProcedureGroupList(getConfiguration().getSelectedYear())) {
            cards.add(new ProcedureGroupCard(this, group));
        }
        cards.add(new PlaceholderCard("+  new group", new OpenCreateProcedureDialog(this)));
        canvas.showCards(cards);
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }
}
