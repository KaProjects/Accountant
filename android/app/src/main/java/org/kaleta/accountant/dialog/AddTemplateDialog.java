package org.kaleta.accountant.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.kaleta.accountant.R;
import org.kaleta.accountant.Service;
import org.kaleta.accountant.data.Account;
import org.kaleta.accountant.data.Template;
import org.kaleta.accountant.validation.Validable;
import org.kaleta.accountant.validation.ValidatorOnItemSelectedListener;
import org.kaleta.accountant.validation.ValidatorTextWatcher;

import java.util.ArrayList;
import java.util.List;

public class AddTemplateDialog extends AlertDialog.Builder implements Validable {
    private TextView textName;
    private TextView textAmount;
    private Spinner debitSpinner;
    private Spinner creditSpinner;
    private TextView textDescription;

    private Button confirmButton;

    public AddTemplateDialog(Context context) {
        super(context, R.style.DialogTheme);
        initComponents();

        this.setTitle(R.string.add_template_title);
        this.setPositiveButton(R.string.dialog_submit, (dialog, which) -> {
            Template template = new Template(textName.getText().toString());
            template.setAmount(String.valueOf(textAmount.getText()));
            template.setDebit(Service.getDebitAccounts().get(debitSpinner.getSelectedItemPosition() - 1).getId());
            template.setCredit(Service.getCreditAccounts().get(creditSpinner.getSelectedItemPosition() - 1).getId());
            template.setDescription(String.valueOf(textDescription.getText()));

            Service.addTemplate(template);
        });

        this.setNegativeButton(R.string.dialog_cancel, (dialog, which) -> {
        });
    }

    private void initComponents() {
        View dialogViewItems = View.inflate(getContext(), R.layout.add_template, null);

        textName = dialogViewItems.findViewById(R.id.textName);
        textName.addTextChangedListener(new ValidatorTextWatcher(this));

        textAmount = dialogViewItems.findViewById(R.id.textAmount);
        textAmount.addTextChangedListener(new ValidatorTextWatcher(this));

        List<Account> debitList = new ArrayList<>();
        debitList.add(new Account("-1", "<select debit>"));
        debitList.addAll(Service.getDebitAccounts());

        debitSpinner = dialogViewItems.findViewById(R.id.debitSpinner);
        debitSpinner.setAdapter(new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, debitList));
        debitSpinner.setOnItemSelectedListener(new ValidatorOnItemSelectedListener(this));

        List<Account> creditList = new ArrayList<>();
        creditList.add(new Account("-1", "<select credit>"));
        creditList.addAll(Service.getCreditAccounts());

        creditSpinner = dialogViewItems.findViewById(R.id.creditSpinner);
        creditSpinner.setAdapter(new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, creditList));
        creditSpinner.setOnItemSelectedListener(new ValidatorOnItemSelectedListener(this));

        textDescription = dialogViewItems.findViewById(R.id.textDescription);

        this.setView(dialogViewItems);
    }

    public void createValidator(Button button) {
        confirmButton = button;
        validate();
    }

    @Override
    public void validate() {
        if (confirmButton != null){
            confirmButton.setEnabled(!textName.getText().toString().isEmpty()
                    && !textAmount.getText().toString().isEmpty()
                    && debitSpinner.getSelectedItemPosition() > 0
                    && creditSpinner.getSelectedItemPosition() > 0);
        }
    }
}