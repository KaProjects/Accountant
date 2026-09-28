package org.kaleta.accountant.frontend.action.listener;

import javax.swing.*;
import java.awt.*;

/**
 * The one-line name dialog the card editors use for creating and renaming.
 */
final class NamePrompt {

    private NamePrompt() {
        // static members only
    }

    /**
     * Asks for a name, pre-filled with initialValue when there is one. Returns null when the user
     * cancels or leaves the field blank.
     */
    static String ask(Component parent, String title, String message, String initialValue) {
        Object input = JOptionPane.showInputDialog(parent, message, title,
                JOptionPane.PLAIN_MESSAGE, null, null, initialValue);
        if (input == null) {
            return null;
        }
        String name = input.toString().trim();
        return name.isEmpty() ? null : name;
    }
}
