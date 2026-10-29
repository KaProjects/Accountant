package org.kaleta.accountant.frontend.component.card;

import javax.swing.*;
import java.awt.*;

/**
 * Outlined placeholder that sits at the end of a canvas and offers to add another card. It is drawn
 * as an outline rather than a card so that it reads as an invitation, not as content.
 */
public class PlaceholderCard extends JPanel {
    private final JLabel label;
    private java.awt.event.ActionListener action;

    public PlaceholderCard(String text, java.awt.event.ActionListener action) {
        this.action = action;
        setLayout(new GridBagLayout());
        setOpaque(false);

        label = new JLabel(text);
        label.setFont(CardStyle.baseFont());
        label.setForeground(CardStyle.MUTED_FG);
        add(label);

        CardStyle.makeClickable(this, e -> {
                    if (this.action != null) {
                        this.action.actionPerformed(e);
                    }
                },
                () -> label.setForeground(CardStyle.ID_FG),
                () -> label.setForeground(CardStyle.MUTED_FG));
    }

    /** Sets what clicking does, for a subclass that can only build its action after construction. */
    protected void onClick(java.awt.event.ActionListener action) {
        this.action = action;
    }

    /** The caption, for anything that needs to anchor a popup to it. */
    protected JLabel getLabel() {
        return label;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth() - 1;
        int h = getHeight() - 1;
        g2.setColor(CardStyle.SLOT_BG);
        g2.fillRoundRect(0, 0, w, h, CardStyle.CARD_ARC, CardStyle.CARD_ARC);
        g2.setColor(CardStyle.SLOT_FG);
        g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{4f, 4f}, 0f));
        g2.drawRoundRect(0, 0, w, h, CardStyle.CARD_ARC, CardStyle.CARD_ARC);
        g2.dispose();
        super.paintComponent(g);
    }
}
