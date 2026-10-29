package org.kaleta.accountant.frontend.component;

import javax.swing.*;
import java.awt.*;

/**
 * A button with a small icon before its text, which still looks like every other button.
 * <p>
 * Giving a {@link JButton} an {@link Icon} is not an option: the macOS look and feel then swaps the
 * native rounded push button for a smaller, flatter style with a smaller font, and the button no
 * longer matches the ones standing next to it. An image inside HTML text keeps the button's look
 * but sits on the text baseline, which leaves it visibly high, and the alignment attributes Swing
 * understands only move it from one wrong place to another.
 * <p>
 * So the look and feel is shown a plain text button - {@code getIcon()} stays empty - room for the
 * icon is reserved in the left margin, and the icon is painted there, centred on the height of the
 * button rather than on the text.
 */
public class LeadingIconButton extends JButton {
    private final Icon icon;
    private final int gap;

    public LeadingIconButton(String text, Icon icon, int gap) {
        super(text);
        this.icon = icon;
        this.gap = gap;
        Insets margin = getMargin();
        if (margin != null) {
            setMargin(new Insets(margin.top, margin.left + icon.getIconWidth() + gap,
                    margin.bottom, margin.right));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // where the look and feel centred the text, worked out the same way it does
        Insets insets = getInsets();
        int viewWidth = getWidth() - insets.left - insets.right;
        int textWidth = getFontMetrics(getFont()).stringWidth(getText());
        int textX = insets.left + (viewWidth - textWidth) / 2;

        icon.paintIcon(this, g, textX - gap - icon.getIconWidth(),
                (getHeight() - icon.getIconHeight()) / 2);
    }
}
