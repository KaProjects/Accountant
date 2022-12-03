package org.kaleta.accountant.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import org.kaleta.accountant.R;
import org.kaleta.accountant.Service;
import org.kaleta.accountant.data.Account;
import org.kaleta.accountant.validation.Validable;
import org.kaleta.accountant.validation.ValidatorTextWatcher;

public class AddAccountDialog extends AlertDialog.Builder implements Validable {
    private TextView textId;
    private TextView textName;
    private Switch switchType;

    private Button confirmButton;


    public AddAccountDialog(Context context) {
        super(context, R.style.DialogTheme);
        initComponents();

        this.setTitle(R.string.add_account_title);
        this.setPositiveButton(R.string.dialog_submit, (dialog, which) -> {
            Account account = new Account();
            account.setId(String.valueOf(textId.getText()));
            account.setName(String.valueOf(textName.getText()));

            Service.addAccount(account, switchType.isChecked());
        });
        this.setNegativeButton(R.string.dialog_cancel, (dialog, which) -> {
        });
    }

    private void initComponents() {
        View dialogViewItems = View.inflate(getContext(), R.layout.add_account, null);

        textId = dialogViewItems.findViewById(R.id.textId);
        textId.addTextChangedListener(new ValidatorTextWatcher(this));

        textName = dialogViewItems.findViewById(R.id.textName);
        textName.addTextChangedListener(new ValidatorTextWatcher(this));

        switchType = dialogViewItems.findViewById(R.id.type);

        this.setView(dialogViewItems);
    }

    public void createValidator(Button button) {
        confirmButton = button;
        validate();
    }

    @Override
    public void validate() {
        if (confirmButton != null){
            confirmButton.setEnabled(!textId.getText().toString().isEmpty()
                    && textId.getText().toString().contains(".")
                    && !textName.getText().toString().isEmpty());
        }
    }
}