package org.kaleta.accountant.frontend.component;

import org.kaleta.accountant.frontend.common.Validable;

import javax.swing.*;
import javax.swing.event.DocumentListener;

/**
 * TODO post 1.0 : impl. & use + date validator
 */
public class DatePickerTextField extends ValidableTextField implements Validable {

    public DatePickerTextField(DocumentListener documentListener) {
        super("Date Picker", "set date", true, documentListener);
        this.setHorizontalAlignment(SwingConstants.RIGHT);
    }

    @Override
    protected boolean doValidate()  {
        return this.getText() != null && !this.getText().trim().isEmpty() && this.getText().length() == 4;
    }

    @Override
    public String validator() {
        return doValidate() ? null : "Date NOT set";
    }
}
