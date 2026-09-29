package org.kaleta.accountant.frontend.dialog;

import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.Validable;

import javax.swing.*;
import java.awt.*;

/**
 * Teaches the import what an imported transaction is: the piece of its description that identifies
 * it, and the account it is booked against.
 * <p>
 * The substring is what is edited here - a statement's description carries a date, a card number, a
 * reference - and the account is the one already chosen on the row. It has to appear in the
 * description, or the mapping would never match the transaction it was made from.
 */
public class DebitMappingDialog extends Dialog {
    private final String description;
    private final String account;
    private final boolean updating;

    private JTextField textFieldSubstring;

    public DebitMappingDialog(Configuration configuration, String description, String substring, String account, boolean updating) {
        super(configuration, updating ? "Updating Mapping" : "Creating Mapping", updating ? "Update" : "Create");
        this.description = description;
        this.account = account;
        this.updating = updating;
        buildDialogContent(substring);
        pack();
        setSize(Math.max(getWidth(), 460), getHeight());
    }

    private void buildDialogContent(String substring) {
        JLabel labelDescription = new JLabel("Description:");
        JLabel valueDescription = new JLabel(description);
        valueDescription.setForeground(Color.DARK_GRAY);

        JLabel labelAccount = new JLabel("Booked against:");
        JLabel valueAccount = new JLabel(account);
        valueAccount.setFont(valueAccount.getFont().deriveFont(Font.BOLD));

        JLabel labelSubstring = new JLabel("Matches on:");
        textFieldSubstring = new SubstringField(substring);

        setContent(layout -> {
            layout.setHorizontalGroup(layout.createParallelGroup()
                    .addGroup(layout.createSequentialGroup().addComponent(labelDescription, 110, 110, 110).addComponent(valueDescription))
                    .addGroup(layout.createSequentialGroup().addComponent(labelAccount, 110, 110, 110).addComponent(valueAccount))
                    .addGroup(layout.createSequentialGroup().addComponent(labelSubstring, 110, 110, 110)
                            .addComponent(textFieldSubstring, 250, 250, Short.MAX_VALUE)));
            layout.setVerticalGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup().addComponent(labelDescription, 25, 25, 25).addComponent(valueDescription, 25, 25, 25))
                    .addGroup(layout.createParallelGroup().addComponent(labelAccount, 25, 25, 25).addComponent(valueAccount, 25, 25, 25))
                    .addGap(5)
                    .addGroup(layout.createParallelGroup().addComponent(labelSubstring, 25, 25, 25).addComponent(textFieldSubstring, 25, 25, 25)));
        });
        validateDialog();
    }

    public String getSubstring() {
        return textFieldSubstring.getText().trim();
    }

    /** True when the dialog was opened on a mapping that already exists. */
    public boolean isUpdating() {
        return updating;
    }

    /** A substring that is not in the description would never match what it was made from. */
    private class SubstringField extends JTextField implements Validable {
        SubstringField(String substring) {
            super(substring);
            getDocument().addDocumentListener(DebitMappingDialog.this);
            getDocument().putProperty("owner", this);
        }

        @Override
        public String validator() {
            String value = getText().trim();
            if (value.isEmpty()) {
                return "Nothing to match on";
            }
            return description.contains(value) ? null : "Not a part of the description";
        }
    }
}
