package org.kaleta.accountant.frontend.component.schema;

import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.component.card.CardStyle;

import javax.swing.*;
import java.awt.*;

/**
 * The little A / L / E / R / X badge on a schema account row, coloured by the side of the books
 * the account belongs to.
 */
public final class AccountTypeChip {

    private AccountTypeChip() {
        // static members only
    }

    /** A, L, E, R or X in the colour the rest of the app uses for that side of the books. */
    static JLabel of(String type) {
        JLabel chip = new JLabel(type, SwingConstants.CENTER);
        chip.setFont(CardStyle.baseFont().deriveFont(Font.BOLD, CardStyle.baseFont().getSize2D() - 2f));
        chip.setForeground(typeForeground(type));
        chip.setBackground(typeBackground(type));
        chip.setOpaque(true);
        chip.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
        return chip;
    }

    /** The colour of a side of the books, for anything that shows an account type without a chip. */
    public static Color colourOf(String type) {
        return typeForeground(type);
    }

    private static Color typeForeground(String type) {
        switch (type) {
            case Constants.AccountType.ASSET: return new Color(0x1B, 0x5E, 0xC4);
            case Constants.AccountType.LIABILITY: return new Color(0xA4, 0x5B, 0x07);
            case Constants.AccountType.EXPENSE: return new Color(0xB3, 0x32, 0x28);
            case Constants.AccountType.REVENUE: return new Color(0x1B, 0x6E, 0x3E);
            default: return new Color(0x5A, 0x64, 0x72);
        }
    }

    private static Color typeBackground(String type) {
        switch (type) {
            case Constants.AccountType.ASSET: return new Color(0xE4, 0xEE, 0xFF);
            case Constants.AccountType.LIABILITY: return new Color(0xFD, 0xF0, 0xDD);
            case Constants.AccountType.EXPENSE: return new Color(0xFD, 0xE9, 0xE7);
            case Constants.AccountType.REVENUE: return new Color(0xE4, 0xF4, 0xEA);
            default: return new Color(0xEC, 0xEF, 0xF2);
        }
    }

    private static String typeDescription(String type) {
        switch (type) {
            case Constants.AccountType.ASSET: return "asset";
            case Constants.AccountType.LIABILITY: return "liability";
            case Constants.AccountType.EXPENSE: return "expense";
            case Constants.AccountType.REVENUE: return "revenue";
            default: return "off balance";
        }
    }

}
