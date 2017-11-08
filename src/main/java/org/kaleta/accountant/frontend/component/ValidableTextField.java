package org.kaleta.accountant.frontend.component;

import org.kaleta.accountant.frontend.common.NumberFilter;
import org.kaleta.accountant.frontend.common.Validable;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class ValidableTextField extends JTextField implements FocusListener, Validable {
    protected boolean showingHint;
    private NumberFilter numberFilter;

    private String hint;
    private String label;
    private boolean isOnlyNumberInput;

    public ValidableTextField(String label, String hint, boolean isOnlyNumberInput, DocumentListener documentListener) {
        this.label = label;
        this.hint = hint;
        this.isOnlyNumberInput = isOnlyNumberInput;
        showingHint = hint != null;
        super.addFocusListener(this);


        if (isOnlyNumberInput) {
            numberFilter = new NumberFilter();
            ((PlainDocument) this.getDocument()).setDocumentFilter(numberFilter);
            this.setHorizontalAlignment(SwingConstants.RIGHT);
        }
        if (documentListener != null) {
            this.getDocument().addDocumentListener(documentListener);
            this.getDocument().putProperty("owner", this);
        }

        focusLost(null);
    }

    @Override
    public String validator(){
        return doValidate() ? null : "Value missing at '" + label + "'";
    }

    protected boolean doValidate() {
        return this.getText() != null && !this.getText().trim().isEmpty();
    }

    @Override
    public void repaint() {
        super.repaint();
        try {
            this.setBackground(doValidate() ? Color.WHITE : Color.getHSBColor(0/360f,0.25f,1));
        } catch (Exception e) {
            // ignore - repaint before full text field initialization
        }
    }


    @Override
    public void focusGained(FocusEvent e) {
        if(hint != null && showingHint) {
            showingHint = false;
            if (isOnlyNumberInput) {
                numberFilter.setSuppressed(false);
                this.setHorizontalAlignment(SwingConstants.RIGHT);
            }
            this.setForeground(Color.BLACK);
            this.setText("");
        }
    }
    @Override
    public void focusLost(FocusEvent e) {
        if(hint != null && !doValidate()) {
            showingHint = true;
            if (isOnlyNumberInput) {
                numberFilter.setSuppressed(true);
                this.setHorizontalAlignment(SwingConstants.LEFT);
            }
            this.setForeground(Color.GRAY);
            this.setText(hint);
        }
    }

    @Override
    public String getText() {
        return (showingHint) ? "" : super.getText();
    }
}
