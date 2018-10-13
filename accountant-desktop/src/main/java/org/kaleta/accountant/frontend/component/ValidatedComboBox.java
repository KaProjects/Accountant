package org.kaleta.accountant.frontend.component;

import org.kaleta.accountant.frontend.common.Validable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ValidatedComboBox<E>  extends JComboBox<E> implements Validable {
    private final String label;

    private boolean validatorEnabled;

    public ValidatedComboBox(String label, ActionListener actionListener){
        this.label = label;
        validatorEnabled = true;

        if (actionListener != null){
            this.addActionListener(actionListener);
        }
    }

    public void setValidatorEnabled(boolean enabled){
        this.validatorEnabled = enabled;
    }

    @Override
    public void repaint() {
        super.repaint();
        try {
            Color buttonColor = this.getComponent(0).getBackground();
            this.setBackground(validator() == null ? Color.WHITE : Color.getHSBColor(0/360f,0.25f,1));
            this.getComponent(0).setBackground(buttonColor);
        } catch (Exception e) {
            // ignore - repaint before full text field initialization
        }
    }

    @Override
    public String validator() {
        if (!validatorEnabled) return null;
        return (this.getSelectedIndex() == -1) ? "No item selected at '" + label + "'" : null;
    }
}
