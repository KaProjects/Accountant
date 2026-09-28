package org.kaleta.accountant.frontend.component.card;

import org.kaleta.accountant.frontend.common.WrapLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Canvas the cards are laid out on. It tracks the width of its scroll pane so the cards wrap into as
 * many rows as the window allows instead of running off to the right.
 */
public class CardCanvas extends JPanel implements Scrollable {
    private int laidOutWidth = -1;

    public CardCanvas() {
        setLayout(new WrapLayout(FlowLayout.LEFT, 12, 12));
        setBackground(CardStyle.CANVAS);
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    }

    /** Replaces the cards, sized so that they line up as a grid. */
    public void showCards(List<JComponent> cards) {
        removeAll();
        CardStyle.applyUniformSize(cards);
        for (JComponent card : cards) {
            add(card);
        }
        laidOutWidth = -1;
        revalidate();
        repaint();
    }

    /**
     * How tall the panel needs to be only becomes known once it has been given a width and the
     * cards have wrapped, so after a width change it asks the scroll pane to measure it again.
     */
    @Override
    public void doLayout() {
        super.doLayout();
        if (laidOutWidth != getWidth()) {
            laidOutWidth = getWidth();
            revalidate();
        }
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
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
