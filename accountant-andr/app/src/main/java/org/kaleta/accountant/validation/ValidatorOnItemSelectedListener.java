package org.kaleta.accountant.validation;

import android.view.View;
import android.widget.AdapterView;

public class ValidatorOnItemSelectedListener implements AdapterView.OnItemSelectedListener{

    private Validable validable;

    public ValidatorOnItemSelectedListener(Validable validable){
        this.validable = validable;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        validable.validate();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
