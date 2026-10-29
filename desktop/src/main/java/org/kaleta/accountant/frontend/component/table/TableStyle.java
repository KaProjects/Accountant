package org.kaleta.accountant.frontend.component.table;

import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.component.card.CardStyle;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * How the accounting and analysis tables look: the one place their colours, type and spacing are
 * decided, so that a balance, a profit statement and a depreciation table read as the same table.
 * <p>
 * The tables are dense - a year is thirteen columns of numbers - so the design gets out of the way:
 * no box around every cell, one hairline between them, weight and a quiet tint to tell a total from
 * a group from an account, and colour kept for what it means.
 */
public final class TableStyle {

    /** The levels a row can be, from the widest total down to a single account. */
    public static final int TOTAL = 0;
    public static final int CLASS = 1;
    public static final int GROUP = 2;
    public static final int ACCOUNT = 3;
    public static final int HEADING = 4;

    public static final Color GRID = new Color(0xDD, 0xE2, 0xE9);
    public static final Color HEADING_BG = new Color(0xF4, 0xF6, 0xF8);
    public static final Color TOTAL_BG = new Color(0xD7, 0xDF, 0xEA);
    public static final Color CLASS_BG = new Color(0xE5, 0xEB, 0xF3);
    public static final Color GROUP_BG = new Color(0xF2, 0xF5, 0xF9);
    public static final Color ACCOUNT_BG = Color.WHITE;
    public static final Color TEXT = new Color(0x22, 0x28, 0x31);
    public static final Color MUTED = new Color(0x7B, 0x86, 0x97);

    /** The tints that carry a meaning: what came in, what went out, and what moved. */
    public static final Color REVENUE_BG = new Color(0xE6, 0xF4, 0xE9);
    public static final Color EXPENSE_BG = new Color(0xFB, 0xEA, 0xEA);
    public static final Color CASH_FLOW_BG = new Color(0xF1, 0xEA, 0xF6);
    public static final Color REVENUE_FG = Constants.Color.INCOME_GREEN.darker();
    public static final Color EXPENSE_FG = Constants.Color.EXPENSE_RED.darker();
    public static final Color CASH_FLOW_FG = Constants.Color.CASH_FLOW_PURPLE.darker();

    private TableStyle() {
        // static members only
    }

    public static int rowHeight(int level) {
        switch (level) {
            case TOTAL: return 32;
            case CLASS: return 28;
            case GROUP: return 25;
            case HEADING: return 22;
            default: return 23;
        }
    }

    public static Font font(int level) {
        float size = CardStyle.baseFont().getSize2D();
        switch (level) {
            case TOTAL: return CardStyle.baseFont().deriveFont(Font.BOLD, size + 3f);
            case CLASS: return CardStyle.baseFont().deriveFont(Font.BOLD, size + 1f);
            case GROUP: return CardStyle.baseFont().deriveFont(Font.BOLD, size);
            case HEADING: return CardStyle.baseFont().deriveFont(Font.PLAIN, size - 1f);
            default: return CardStyle.baseFont().deriveFont(Font.PLAIN, size);
        }
    }

    public static Color background(int level) {
        switch (level) {
            case TOTAL: return TOTAL_BG;
            case CLASS: return CLASS_BG;
            case GROUP: return GROUP_BG;
            case HEADING: return HEADING_BG;
            default: return ACCOUNT_BG;
        }
    }

    /** A value: right aligned under its heading, with the hairline that separates the columns. */
    public static JLabel value(String text, int level) {
        JLabel cell = new JLabel(text, SwingConstants.RIGHT);
        cell.setFont(font(level));
        cell.setForeground(TEXT);
        cell.setBorder(BorderFactory.createCompoundBorder(cellBorder(), BorderFactory.createEmptyBorder(0, 6, 0, 8)));
        return cell;
    }

    /** A column's name, which is not a number and is centred over the ones below it. */
    public static JLabel heading(String text) {
        JLabel cell = new JLabel(text, SwingConstants.CENTER);
        cell.setFont(font(HEADING));
        cell.setForeground(MUTED);
        cell.setBorder(BorderFactory.createCompoundBorder(cellBorder(), BorderFactory.createEmptyBorder(0, 4, 0, 4)));
        return cell;
    }

    /** The name of what a row is about, which reads from the left and is the widest thing on it. */
    public static JLabel name(String text, int level) {
        JLabel cell = new JLabel(text);
        cell.setFont(font(level));
        cell.setForeground(level == HEADING ? MUTED : TEXT);
        return cell;
    }

    /** One hairline below and one to the right: a grid that is read rather than looked at. */
    public static Border cellBorder() {
        return BorderFactory.createMatteBorder(0, 0, 1, 1, GRID);
    }

    public static Border rowBorder() {
        return BorderFactory.createMatteBorder(0, 0, 1, 0, GRID);
    }
}
