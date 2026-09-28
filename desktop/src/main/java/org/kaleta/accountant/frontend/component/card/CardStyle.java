package org.kaleta.accountant.frontend.component.card;

import org.kaleta.accountant.frontend.common.IconLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Colours, fonts and the small widgets the card editors are built from, kept in one place so that
 * the schema editor and the procedures editor cannot drift apart.
 *
 * The vocabulary is: a canvas of cards, each with a tinted header and a body of fixed height rows.
 * A row carries no buttons - the row itself is the click target - and a header behaves the same
 * way, so a card reads as content rather than as a form.
 */
public final class CardStyle {

    public static final Color CANVAS = new Color(0xF4, 0xF6, 0xF8);
    public static final Color CARD_BG = new Color(0xFF, 0xFF, 0xFF);
    public static final Color CARD_BORDER = new Color(0xD5, 0xDB, 0xE2);
    public static final Color HEADER_BG = new Color(0xEB, 0xEF, 0xF5);
    public static final Color HEADER_BG_HOVER = new Color(0xDD, 0xE6, 0xF7);
    public static final Color HEADER_BG_LOCKED = new Color(0xF1, 0xF2, 0xF4);
    public static final Color SLOT_BG = new Color(0xFA, 0xFB, 0xFC);
    public static final Color ROW_HOVER = new Color(0xE7, 0xEF, 0xFD);
    public static final Color NAME_FG = new Color(0x22, 0x28, 0x31);
    public static final Color ID_FG = new Color(0x7B, 0x86, 0x97);
    public static final Color MUTED_FG = new Color(0xA6, 0xAE, 0xBA);
    public static final Color SLOT_FG = new Color(0xBC, 0xC4, 0xCF);

    public static final int CARD_ARC = 10;
    public static final int HEADER_HEIGHT = 28;
    public static final int ROW_HEIGHT = 24;
    public static final int MIN_CARD_WIDTH = 210;
    public static final int MAX_CARD_WIDTH = 400;
    private static final int BODY_INSETS = 12;

    private CardStyle() {
        // static members only
    }

    /**
     * Gives every card in a class the same footprint: as wide and as tall as the biggest one, so
     * that the wrapping layout falls into a clean grid whatever the window width is.
     */
    public static void applyUniformSize(List<? extends JComponent> cards) {
        int width = MIN_CARD_WIDTH;
        int height = HEADER_HEIGHT + 3 * ROW_HEIGHT + BODY_INSETS;
        for (JComponent card : cards) {
            width = Math.max(width, card.getPreferredSize().width);
            height = Math.max(height, card.getPreferredSize().height);
        }
        Dimension size = new Dimension(Math.min(width, MAX_CARD_WIDTH), height);
        for (JComponent card : cards) {
            card.setPreferredSize(size);
            card.setMinimumSize(size);
            card.setMaximumSize(size);
        }
    }

    public static Font baseFont() {
        return new JLabel().getFont();
    }

    public static Font nameFont() {
        return baseFont().deriveFont(Font.PLAIN, baseFont().getSize2D());
    }

    public static Font titleFont() {
        return baseFont().deriveFont(Font.BOLD, baseFont().getSize2D());
    }

    public static Font idFont() {
        return new Font(Font.MONOSPACED, Font.PLAIN, baseFont().getSize() - 1);
    }

    public static Font hintFont() {
        return baseFont().deriveFont(Font.ITALIC, baseFont().getSize2D() - 2f);
    }

    /**
     * A card's title band. Its height is fixed, but its width is whatever its contents need, so a
     * card ends up wide enough for its own title rather than only for the rows beneath it.
     */
    public static JPanel header() {
        JPanel header = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, HEADER_HEIGHT);
            }

            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, HEADER_HEIGHT);
            }
        };
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
        return header;
    }

    /** Monospaced id, so the digits of every row line up under each other. */
    public static JLabel id(String id, Color color) {
        JLabel label = new JLabel(id);
        label.setFont(idFont());
        label.setForeground(color);
        return label;
    }

    /** Plain icon, no button chrome: a marker on a row that is itself the click target. */
    public static JLabel icon(String iconPath, int size) {
        JLabel label = new JLabel(IconLoader.getIcon(iconPath, new Dimension(size, size)));
        label.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        return label;
    }

    /**
     * Turns a whole account row into the click target: it lights up under the pointer and opens its
     * dialog wherever it is clicked, which is why the rows carry no buttons of their own.
     */
    public static void makeRowClickable(JPanel row, java.awt.event.ActionListener action) {
        makeClickable(row, action, () -> {
            row.setBackground(ROW_HOVER);
            row.setOpaque(true);
            row.repaint();
        }, () -> {
            row.setOpaque(false);
            row.getParent().repaint();
        });
    }

    /** The same for a card header, which tints the card's whole title band instead. */
    public static void makeHeaderClickable(JPanel header, Card card, java.awt.event.ActionListener action) {
        Color resting = card.getHeaderBackground();
        makeClickable(header, action, () -> card.setHeaderBackground(HEADER_BG_HOVER), () -> card.setHeaderBackground(resting));
    }

    /**
     * Makes a whole panel behave like a button: highlighted under the pointer, acting on a click
     * anywhere inside it, wherever in its children that click actually landed.
     */
    public static void makeClickable(JPanel area, java.awt.event.ActionListener action, Runnable highlight, Runnable unhighlight) {
        area.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        MouseAdapter adapter = new MouseAdapter() {
            private boolean pressedHere;

            @Override
            public void mouseEntered(MouseEvent e) {
                highlight.run();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // moving onto a label inside the area is not leaving it
                if (!isOverTheArea(e)) {
                    unhighlight.run();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                pressedHere = SwingUtilities.isLeftMouseButton(e) && isOverTheArea(e);
            }

            /**
             * Deliberately not mouseClicked: AWT swallows that one as soon as the pointer moves a
             * single pixel between press and release, which made the rows open their dialog only
             * some of the time. Press and release over the area is a click, drift included, while
             * releasing somewhere else still cancels it.
             */
            @Override
            public void mouseReleased(MouseEvent e) {
                boolean click = pressedHere && SwingUtilities.isLeftMouseButton(e) && isOverTheArea(e);
                pressedHere = false;
                if (click && e.getClickCount() <= 1) { // the second half of a double click opens nothing
                    action.actionPerformed(new ActionEvent(area, ActionEvent.ACTION_PERFORMED, null));
                }
            }

            private boolean isOverTheArea(MouseEvent e) {
                return area.contains(SwingUtilities.convertPoint((Component) e.getSource(), e.getPoint(), area));
            }
        };
        attach(area, adapter);
    }

    private static void attach(Component component, MouseAdapter adapter) {
        // a button inside a clickable area keeps its own click: a row whose main action is "use
        // this" can still carry an edit button that means something else
        if (component instanceof AbstractButton) {
            return;
        }
        component.addMouseListener(adapter);
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                attach(child, adapter);
            }
        }
    }

    /**
     * Icon button without the chrome, for the rare row that has a second action beside the one the
     * row itself performs.
     */
    public static JButton flatButton(String iconPath, int iconSize, String tooltip) {
        JButton button = new JButton(IconLoader.getIcon(iconPath, new Dimension(iconSize, iconSize)));
        button.setToolTipText(tooltip);
        button.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(iconSize + 10, ROW_HEIGHT - 4));
        return button;
    }

    /**
     * Rounded, softly outlined panel with a tinted band along its top edge. The band is painted
     * rather than filled by a child so that it follows the rounded corners.
     */
    public static class Card extends JPanel {
        private Color headerBackground;

        public Card(Color headerBackground) {
            this.headerBackground = headerBackground;
            setLayout(new BorderLayout());
            setOpaque(false);
        }

        public Color getHeaderBackground() {
            return headerBackground;
        }

        public void setHeaderBackground(Color headerBackground) {
            this.headerBackground = headerBackground;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth() - 1;
            int h = getHeight() - 1;
            g2.setColor(CARD_BG);
            g2.fillRoundRect(0, 0, w, h, CARD_ARC, CARD_ARC);
            g2.clip(new RoundRectangle2D.Float(0, 0, w, h, CARD_ARC, CARD_ARC));
            g2.setColor(headerBackground);
            g2.fillRect(0, 0, w + 1, HEADER_HEIGHT);
            g2.setColor(CARD_BORDER);
            g2.drawLine(0, HEADER_HEIGHT, w + 1, HEADER_HEIGHT);
            g2.setClip(null);
            g2.setColor(CARD_BORDER);
            g2.drawRoundRect(0, 0, w, h, CARD_ARC, CARD_ARC);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
