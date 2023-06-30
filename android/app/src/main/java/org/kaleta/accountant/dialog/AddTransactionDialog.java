package org.kaleta.accountant.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.kaleta.accountant.R;
import org.kaleta.accountant.Service;
import org.kaleta.accountant.data.Account;
import org.kaleta.accountant.data.Template;
import org.kaleta.accountant.data.Transaction;
import org.kaleta.accountant.validation.Validable;
import org.kaleta.accountant.validation.ValidatorOnItemSelectedListener;
import org.kaleta.accountant.validation.ValidatorTextWatcher;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AddTransactionDialog extends AlertDialog.Builder implements Validable {
    private TextView textDate;
    private TextView textAmount;
    private Spinner debitSpinner;
    private Spinner creditSpinner;
    private TextView textDescription;
    private Spinner templatesSpinner;

    private Button confirmButton;

    public AddTransactionDialog(Context context) {
        super(context, R.style.DialogTheme);
        initComponents();

        this.setTitle(R.string.add_transaction_title);
        this.setPositiveButton(R.string.dialog_submit, (dialog, which) -> {
            Transaction transaction = new Transaction();
            transaction.setDate(String.valueOf(textDate.getText()));
            transaction.setAmount(String.valueOf(textAmount.getText()));
            if (debitSpinner.getSelectedItemPosition() > 0) {
                transaction.setDebit(Service.getDebitAccounts().get(debitSpinner.getSelectedItemPosition() - 1).getId());
            } else {
                transaction.setDebit("");
            }
            if (creditSpinner.getSelectedItemPosition() > 0) {
                transaction.setCredit(Service.getCreditAccounts().get(creditSpinner.getSelectedItemPosition() - 1).getId());
            } else {
                transaction.setCredit("");
            }
            transaction.setDescription(String.valueOf(textDescription.getText()));

            Service.addTransaction(transaction);
        });

        this.setNegativeButton(R.string.dialog_cancel, (dialog, which) -> {
        });
    }

    private void initComponents() {
        View dialogViewItems = View.inflate(getContext(), R.layout.add_transaction, null);

        textDate = dialogViewItems.findViewById(R.id.textDay);
        textDate.addTextChangedListener(new ValidatorTextWatcher(this));

        Button buttonToday = dialogViewItems.findViewById(R.id.buttonToday);
        buttonToday.setOnClickListener(v -> {
            DateFormat dateFormat = new SimpleDateFormat("ddMM");
            Date date = new Date();
            textDate.setText(dateFormat.format(date));
        });

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
        textDescription.addTextChangedListener(new ValidatorTextWatcher(this));

        List<Template> templateList = new ArrayList<>();
        templateList.add(new Template("<no template>"));
        templateList.addAll(Service.getTemplates());

        templatesSpinner = dialogViewItems.findViewById(R.id.templatesSpinner);
        templatesSpinner.setAdapter(new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, templateList));
        templatesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i > 0){
                    Template template = (Template) adapterView.getItemAtPosition(i);

                    debitSpinner.setSelection(Service.getDebitAccounts().indexOf(Service.getDebitAccount(template.getDebit())) + 1);
                    creditSpinner.setSelection(Service.getCreditAccounts().indexOf(Service.getCreditAccount(template.getCredit())) + 1);
                    textAmount.setText(template.getAmount());
                    textDescription.setText(template.getDescription());
                } else {
                    debitSpinner.setSelection(0);
                    creditSpinner.setSelection(0);
                    textAmount.setText("");
                    textDescription.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        this.setView(dialogViewItems);
    }

    public void createValidator(Button button) {
        confirmButton = button;
        validate();
    }

    @Override
    public void validate() {
        if (confirmButton != null){
            confirmButton.setEnabled(!textDate.getText().toString().isEmpty()
                    && !textAmount.getText().toString().isEmpty()
                    && ((debitSpinner.getSelectedItemPosition() > 0 && creditSpinner.getSelectedItemPosition() > 0) || !textDescription.getText().toString().isEmpty())
            );
        }
    }
}
