package org.kaleta.accountant.frontend.component;

import org.kaleta.accountant.frontend.common.Validable;

import javax.swing.*;
import javax.swing.event.DocumentListener;

public class DayOfMonthPickerTextField extends HintValidatedTextField implements Validable {

    public DayOfMonthPickerTextField(String day, DocumentListener documentListener) {
        super(day, "Day Of Month Picker", "set day of a month", true, documentListener);
        this.setHorizontalAlignment(SwingConstants.RIGHT);
    }

    @Override
    boolean doValidate() {
        return this.getText() != null && !this.getText().trim().isEmpty() && this.getText().length() == 2;
    }

    @Override
    public String validator() {
        if (!validatorEnabled) return null;
        return doValidate() ? null : "Day NOT set";
    }
}
