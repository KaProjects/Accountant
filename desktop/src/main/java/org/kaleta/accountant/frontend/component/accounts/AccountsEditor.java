package org.kaleta.accountant.frontend.component.accounts;

import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.component.card.CardStyle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Editor of the accounts of one schema class: a bar per group, and inside an opened group a card
 * per schema account holding the accounts opened under it.
 * <p>
 * Clicking anywhere on a group's bar opens it - the whole bar, not a toggle icon at its edge - and
 * everything the group holds is then visible at once.
 */
public class AccountsEditor extends JPanel implements Configurable, Scrollable {
    private Configuration configuration;

    private final List<AccountGroupSection> sections = new ArrayList<>();

    public AccountsEditor(Configuration configuration) {
        setConfiguration(configuration);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(CardStyle.CANVAS);
        // stated rather than inherited from the groups: a box layout that holds this editor next to
        // something centred would align the two instead of stretching them, and the editor would
        // stop short of the right edge
        setAlignmentX(LEFT_ALIGNMENT);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                for (AccountGroupSection section : sections) {
                    section.reflowIfNeeded();
                }
            }
        });
    }

    /** Rebuilds the editor for this class, for instance after the schema changed. */
    public void resetEditor(SchemaModel.Class clazz) {
        removeAll();
        sections.clear();
        for (SchemaModel.Class.Group group : clazz.getGroup()) {
            AccountGroupSection section = new AccountGroupSection(this, clazz.getId(), group);
            sections.add(section);
            add(section);
        }
        revalidate();
        repaint();
    }

    /** Refreshes what the opened groups show, after an account was created. */
    public void updateEditor() {
        for (AccountGroupSection section : sections) {
            section.refresh();
        }
        revalidate();
        repaint();
    }

    /**
     * The editor takes the width of its scroll pane rather than asking for the width its widest
     * group would like. Without this the groups would lay their cards out in one endless row and
     * the pane would scroll sideways, because the width a wrapping layout asks for is the width it
     * needs when nothing wraps.
     */
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return CardStyle.ROW_HEIGHT;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return visibleRect.height;
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
