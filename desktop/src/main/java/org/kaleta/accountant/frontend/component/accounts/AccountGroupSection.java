package org.kaleta.accountant.frontend.component.accounts;

import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.common.IconLoader;
import org.kaleta.accountant.frontend.component.card.CardStyle;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * One schema group: a bar that opens it, and underneath a card per schema account laid out side by
 * side, each listing the accounts opened under it.
 * <p>
 * Everything a group holds is visible as soon as the group is open. The old editor hid the accounts
 * behind a second collapsed level per schema account, so seeing what was in a group took a click
 * per schema account, and comparing two of them was impossible.
 * <p>
 * The cards are placed in a grid whose column count is worked out from the width available, rather
 * than in a layout that wraps by itself. A wrapping layout only knows how tall it is once it knows
 * how wide it is, and inside a vertical stack the height is asked for first - so the group would be
 * measured for one row and then paint two, over whatever came next. Every card here is the same
 * size, so the number of rows is arithmetic and the height is known before the layout runs.
 */
class AccountGroupSection extends JPanel {
    private static final int GAP = 12;
    private static final int SCROLLBAR_ALLOWANCE = 20;

    private final Configurable owner;
    private final SchemaModel.Class.Group group;
    private final String classId;

    private final JPanel cards;
    private final JLabel toggle;
    private boolean expanded;
    private int laidOutColumns = -1;

    AccountGroupSection(Configurable owner, String classId, SchemaModel.Class.Group group) {
        this.owner = owner;
        this.classId = classId;
        this.group = group;

        setLayout(new BorderLayout());
        setOpaque(false);
        setAlignmentX(LEFT_ALIGNMENT);

        toggle = new JLabel(IconLoader.getIcon(IconLoader.TOGGLE_EXPAND, new Dimension(12, 12)));

        JPanel bar = bar();
        add(bar, BorderLayout.NORTH);

        cards = new JPanel(new GridLayout(0, 1, GAP, GAP));
        cards.setOpaque(false);
        cards.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
        cards.setVisible(false);
        add(cards, BorderLayout.CENTER);

        CardStyle.makeClickable(bar, e -> setExpanded(!expanded),
                () -> bar.setBackground(CardStyle.HEADER_BG_HOVER),
                () -> bar.setBackground(CardStyle.HEADER_BG));
    }

    private JPanel bar() {
        JPanel bar = new JPanel();
        bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
        bar.setBackground(CardStyle.HEADER_BG);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, CardStyle.CARD_BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        bar.add(toggle);
        bar.add(Box.createHorizontalStrut(8));
        bar.add(CardStyle.id(classId + group.getId(), CardStyle.ID_FG));
        bar.add(Box.createHorizontalStrut(8));

        JLabel name = new JLabel(group.getName());
        name.setFont(CardStyle.titleFont());
        name.setForeground(CardStyle.NAME_FG);
        bar.add(name);

        bar.add(Box.createHorizontalStrut(10));
        JLabel count = new JLabel(visibleAccounts().size() + " schema accounts");
        count.setFont(CardStyle.hintFont());
        count.setForeground(CardStyle.MUTED_FG);
        bar.add(count);

        bar.add(Box.createHorizontalGlue());
        return bar;
    }

    /** The app maintains some schema accounts itself; those are not the user's to edit here. */
    private List<SchemaModel.Class.Group.Account> visibleAccounts() {
        List<SchemaModel.Class.Group.Account> visible = new ArrayList<>();
        for (SchemaModel.Class.Group.Account account : group.getAccount()) {
            if (!AccountsEditorRules.isAppMaintained(classId + group.getId() + account.getId())) {
                visible.add(account);
            }
        }
        return visible;
    }

    void setExpanded(boolean expanded) {
        this.expanded = expanded;
        toggle.setIcon(IconLoader.getIcon(expanded ? IconLoader.TOGGLE_HIDE : IconLoader.TOGGLE_EXPAND,
                new Dimension(12, 12)));
        cards.setVisible(expanded);
        if (expanded) {
            refresh();
        } else {
            cards.removeAll();
            laidOutColumns = -1;
        }
        revalidate();
        repaint();
    }

    /** Rebuilds the cards, so an account opened elsewhere shows up here too. */
    void refresh() {
        if (!expanded) {
            return;
        }
        List<JComponent> built = new ArrayList<>();
        for (SchemaModel.Class.Group.Account account : visibleAccounts()) {
            built.add(new SchemaAccountCard(owner, classId + group.getId() + account.getId(), account.getName()));
        }
        CardStyle.applyUniformSize(built);

        int columns = columnsFor(built);
        laidOutColumns = columns;
        cards.removeAll();
        cards.setLayout(new GridLayout(0, columns, GAP, GAP));
        for (JComponent card : built) {
            cards.add(card);
        }
        cards.revalidate();
        cards.repaint();
    }

    /** How many cards of this width fit across the space the section has. */
    private int columnsFor(List<JComponent> built) {
        if (built.isEmpty()) {
            return 1;
        }
        return columnsFor(built.get(0).getPreferredSize().width, built.size());
    }

    private int columnsFor(int cardWidth, int cardCount) {
        // minus room for a vertical scrollbar, so that a scrollbar appearing cannot take the width
        // back below the threshold and start the count oscillating
        int fits = (availableWidth() - GAP - SCROLLBAR_ALLOWANCE) / (cardWidth + GAP);
        return Math.max(1, Math.min(fits, cardCount));
    }

    /**
     * Re-lays the cards out when the window has changed enough to fit another one per row. Called
     * on a resize rather than during layout, so a rebuild can never feed back into the layout that
     * caused it.
     */
    void reflowIfNeeded() {
        if (!expanded || cards.getComponentCount() == 0) {
            return;
        }
        int columns = columnsFor(cards.getComponent(0).getPreferredSize().width, cards.getComponentCount());
        if (columns != laidOutColumns) {
            laidOutColumns = columns;
            cards.setLayout(new GridLayout(0, columns, GAP, GAP));
            cards.revalidate();
            revalidate();
        }
    }

    /** The first build happens before the section has been given a size, so ask upwards for one. */
    private int availableWidth() {
        for (Component component = this; component != null; component = component.getParent()) {
            if (component.getWidth() > 0) {
                return component.getWidth();
            }
        }
        return 0;
    }


    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }
}
