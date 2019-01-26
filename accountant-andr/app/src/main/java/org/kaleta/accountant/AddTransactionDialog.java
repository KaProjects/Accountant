package org.kaleta.accountant;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.kaleta.accountant.data.Transaction;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddTransactionDialog extends AlertDialog.Builder {
    private TextView textDate;
    private TextView textAmount;
    private Spinner debitSpinner;
    private Spinner creditSpinner;
    private TextView textDescription;

    public AddTransactionDialog(Context context) {
        super(context, R.style.DialogTheme);
        initComponents();

        this.setTitle(R.string.add_transaction_title);
        this.setPositiveButton(R.string.add_transaction_submit, (dialog, which) -> {
            Transaction transaction = new Transaction();
            transaction.setDate(String.valueOf(textDate.getText()));
            transaction.setAmount(String.valueOf(textAmount.getText()));
            transaction.setDebit(Service.getDebitAccounts().get(debitSpinner.getSelectedItemPosition()).getId());
            transaction.setCredit(Service.getCreditAccounts().get(creditSpinner.getSelectedItemPosition()).getId());
            transaction.setDescription(String.valueOf(textDescription.getText()));

            Service.addTransaction(transaction);
        });
        this.setNegativeButton(R.string.add_transaction_cancel, (dialog, which) -> {
        });
    }

    private void initComponents() {
        View dialogViewItems = View.inflate(getContext(), R.layout.add_transaction, null);

        textDate = dialogViewItems.findViewById(R.id.textDay);
        textAmount = dialogViewItems.findViewById(R.id.textAmount);

        Button buttonToday = dialogViewItems.findViewById(R.id.buttonToday);
        buttonToday.setOnClickListener(v -> {
            DateFormat dateFormat = new SimpleDateFormat("ddMM");
            Date date = new Date();
            textDate.setText(dateFormat.format(date));
        });

        debitSpinner = dialogViewItems.findViewById(R.id.debitSpinner);
        debitSpinner.setAdapter(new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item,
                Service.getDebitAccounts()));

        creditSpinner = dialogViewItems.findViewById(R.id.creditSpinner);
        creditSpinner.setAdapter(new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item,
                Service.getCreditAccounts()));

        textDescription = dialogViewItems.findViewById(R.id.textDescription);

        this.setView(dialogViewItems);
    }
}
