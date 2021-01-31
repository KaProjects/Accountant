package org.kaleta.accountant.frontend.component;

import org.kaleta.accountant.frontend.common.Validable;

import javax.swing.*;
import javax.swing.event.DocumentListener;

public class DatePickerTextField extends HintValidatedTextField implements Validable {

    public DatePickerTextField(String date, DocumentListener documentListener) {
        super(date, "Date Picker", "set date", true, documentListener);
        this.setHorizontalAlignment(SwingConstants.RIGHT);
    }

    @Override
    boolean doValidate() {
        return this.getText() != null && !this.getText().trim().isEmpty() && this.getText().length() == 4;
    }

    @Override
    public String validator() {
        if (!validatorEnabled) return null;
        return doValidate() ? null : "Date NOT set";
    }
}
