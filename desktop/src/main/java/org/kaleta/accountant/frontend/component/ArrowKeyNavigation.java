package org.kaleta.accountant.frontend.component;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Moves the focus around a grid of transaction rows with the arrow keys: up and down to the same
 * field of the row above or below, left and right to the field beside it.
 * <p>
 * Left and right only move once the caret has nowhere left to go, so typing in a field still works
 * the way typing does; a field that is hidden, such as the date of a procedure's row, is stepped
 * over rather than focused. Where there is nothing to move to, the key does what it always did.
 */
public final class ArrowKeyNavigation {

    private ArrowKeyNavigation() {
        // static members only
    }

    /** @param rows all the rows currently shown, read afresh on every key, since they come and go */
    public static void install(TransactionPanel panel, Supplier<List<TransactionPanel>> rows) {
        List<JComponent> fields = panel.navigableFields();
        for (int i = 0; i < fields.size(); i++) {
            JComponent field = fields.get(i);
            int column = i;
            bind(field, KeyEvent.VK_LEFT, () -> beside(rows, panel, column, -1), caretAtStart(field));
            bind(field, KeyEvent.VK_RIGHT, () -> beside(rows, panel, column, 1), caretAtEnd(field));
            bind(field, KeyEvent.VK_UP, () -> above(rows, panel, column, -1), leavesTheRow(field));
            bind(field, KeyEvent.VK_DOWN, () -> above(rows, panel, column, 1), leavesTheRow(field));
        }
    }

    private static void bind(JComponent field, int keyCode, Supplier<JComponent> target, BooleanSupplier when) {
        KeyStroke stroke = KeyStroke.getKeyStroke(keyCode, 0);
        Object originalKey = field.getInputMap(JComponent.WHEN_FOCUSED).get(stroke);
        Action original = originalKey == null ? null : field.getActionMap().get(originalKey);

        String name = "arrow-navigation-" + keyCode;
        field.getInputMap(JComponent.WHEN_FOCUSED).put(stroke, name);
        field.getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (when.getAsBoolean()) {
                    JComponent moveTo = target.get();
                    if (moveTo != null) {
                        moveTo.requestFocusInWindow();
                        return;
                    }
                }
                if (original != null) {
                    original.actionPerformed(e);
                }
            }
        });
    }

    /** The next field of this row in that direction, skipping the ones that are not shown. */
    static JComponent beside(Supplier<List<TransactionPanel>> rows, TransactionPanel panel, int column, int step) {
        List<TransactionPanel> shown = rows.get();
        if (!shown.contains(panel)) {
            return null;
        }
        List<JComponent> fields = panel.navigableFields();
        for (int i = column + step; i >= 0 && i < fields.size(); i += step) {
            if (fields.get(i).isVisible()) {
                return fields.get(i);
            }
        }
        return null;
    }

    /** The same field of the row above or below, or the nearest shown one in it. */
    static JComponent above(Supplier<List<TransactionPanel>> rows, TransactionPanel panel, int column, int step) {
        List<TransactionPanel> shown = rows.get();
        int row = shown.indexOf(panel) + step;
        if (row < 0 || row >= shown.size()) {
            return null;
        }
        List<JComponent> fields = shown.get(row).navigableFields();
        if (column < fields.size() && fields.get(column).isVisible()) {
            return fields.get(column);
        }
        for (JComponent field : fields) {
            if (field.isVisible()) {
                return field;
            }
        }
        return null;
    }

    private static BooleanSupplier caretAtStart(JComponent field) {
        return () -> !(field instanceof JTextComponent)
                || ((JTextComponent) field).getCaretPosition() == 0;
    }

    private static BooleanSupplier caretAtEnd(JComponent field) {
        return () -> !(field instanceof JTextComponent)
                || ((JTextComponent) field).getCaretPosition() == ((JTextComponent) field).getDocument().getLength();
    }

    /** Vertical arrows always move, except while a combo box is using them for its own list. */
    private static BooleanSupplier leavesTheRow(JComponent field) {
        JComboBox<?> combo = comboOwning(field);
        return combo == null ? () -> true : () -> !combo.isPopupVisible();
    }

    private static JComboBox<?> comboOwning(JComponent field) {
        return field.getParent() instanceof JComboBox ? (JComboBox<?>) field.getParent() : null;
    }
}
