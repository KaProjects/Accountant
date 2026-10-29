package org.kaleta.accountant.frontend.component.card;

import javax.swing.*;
import java.awt.*;

/**
 * One line of a card's body. Rows are a fixed height so that they line up across every card in the
 * editor, and they are laid out left to right with the row itself as the click target.
 */
public class CardRow extends JPanel {

    protected CardRow() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setOpaque(false);
        setAlignmentX(LEFT_ALIGNMENT);
        setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(super.getPreferredSize().width, CardStyle.ROW_HEIGHT);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(0, CardStyle.ROW_HEIGHT);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, CardStyle.ROW_HEIGHT);
    }
}
