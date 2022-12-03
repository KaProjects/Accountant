package org.kaleta.accountant.validation;

import android.text.Editable;
import android.text.TextWatcher;

public class ValidatorTextWatcher implements TextWatcher {

    private Validable validable;

    public ValidatorTextWatcher(Validable validable){
        this.validable = validable;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        validable.validate();
    }
}
