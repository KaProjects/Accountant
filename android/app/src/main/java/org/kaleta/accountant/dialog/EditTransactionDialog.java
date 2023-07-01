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
import org.kaleta.accountant.data.Transaction;
import org.kaleta.accountant.validation.Validable;
import org.kaleta.accountant.validation.ValidatorOnItemSelectedListener;
import org.kaleta.accountant.validation.ValidatorTextWatcher;

import java.util.ArrayList;
import java.util.List;

public class EditTransactionDialog extends AlertDialog.Builder implements Validable {
    private TextView textDate;
    private TextView textAmount;
    private Spinner debitSpinner;
    private Spinner creditSpinner;
    private TextView textDescription;

    private Button confirmButton;

    private Transaction transaction;

    public EditTransactionDialog(Context context, Transaction transaction) {
        super(context, R.style.DialogTheme);
        this.transaction = transaction;
        initComponents();

        this.setTitle(R.string.edit_transaction_title);
        this.setPositiveButton(R.string.dialog_submit_edit, (dialog, which) -> {
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

            Service.updateTransaction(transaction);
        });

        this.setNegativeButton(R.string.dialog_cancel, (dialog, which) -> {
        });
    }

    private void initComponents() {
        View dialogViewItems = View.inflate(getContext(), R.layout.edit_transaction, null);

        textDate = dialogViewItems.findViewById(R.id.textDay);
        textDate.addTextChangedListener(new ValidatorTextWatcher(this));
        textDate.setText(transaction.getDate());

        List<Account> debitList = new ArrayList<>();
        debitList.add(new Account("-1", "<select debit>"));
        debitList.addAll(Service.getDebitAccounts());

        debitSpinner = dialogViewItems.findViewById(R.id.debitSpinner);
        debitSpinner.setAdapter(new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, debitList));
        debitSpinner.setOnItemSelectedListener(new ValidatorOnItemSelectedListener(this));
        debitSpinner.setSelection(debitList.indexOf(Service.getDebitAccount(transaction.getDebit())));

        List<Account> creditList = new ArrayList<>();
        creditList.add(new Account("-1", "<select credit>"));
        creditList.addAll(Service.getCreditAccounts());

        creditSpinner = dialogViewItems.findViewById(R.id.creditSpinner);
        creditSpinner.setAdapter(new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, creditList));
        creditSpinner.setOnItemSelectedListener(new ValidatorOnItemSelectedListener(this));
        creditSpinner.setSelection(creditList.indexOf(Service.getCreditAccount(transaction.getCredit())));

        textAmount = dialogViewItems.findViewById(R.id.textAmount);
        textAmount.addTextChangedListener(new ValidatorTextWatcher(this));
        textAmount.setText(transaction.getAmount());

        textDescription = dialogViewItems.findViewById(R.id.textDescription);
        textDescription.addTextChangedListener(new ValidatorTextWatcher(this));
        textDescription.setText(transaction.getDescription());

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
