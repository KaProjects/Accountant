package org.kaleta.accountant.frontend.component;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.AccountPairModel;
import org.kaleta.accountant.frontend.common.IconLoader;
import org.kaleta.accountant.frontend.common.SwingWorkerHandler;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TransactionPanel extends JPanel implements DocumentListener {
    private final Object lock = new Object();
    private final Map<AccountPairModel, Set<String>> accountPairDescriptionMap;

    private final JButton buttonDelete;
    private final DatePickerTextField tfDate;
    private final JComboBox<String> cbDescription;
    private final HintValidatedTextField tfAmount;
    private final SelectAccountTextField tfDebit;
    private final SelectAccountTextField tfCredit;

    private boolean isSuppressedUpdate = false;

    public TransactionPanel(Configuration configuration, Map<AccountPairModel, Set<String>> accountPairDescriptionMap,
                            Map<String, List<AccountsModel.Account>> accountMap, List<SchemaModel.Class> classList,
                            DocumentListener documentListener, boolean withDate) {
        this.accountPairDescriptionMap = accountPairDescriptionMap;

        tfDate = new DatePickerTextField("",documentListener);
        if (!withDate){
            tfDate.setVisible(false);
            tfDate.setValidatorEnabled(false);
        }
        cbDescription = new JComboBox<>();
        cbDescription.setEditable(true);

        tfAmount = new HintValidatedTextField("","Transaction Amount", "set amount", true, documentListener);
        tfDebit = new SelectAccountTextField(configuration, accountMap, classList, "Debit",documentListener);
        tfDebit.getDocument().addDocumentListener(this);
        tfCredit = new SelectAccountTextField(configuration, accountMap, classList,"Credit", documentListener);
        tfCredit.getDocument().addDocumentListener(this);

        buttonDelete = new JButton(IconLoader.getIcon(IconLoader.DELETE, new Dimension(10, 10)));
        buttonDelete.setEnabled(false);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addComponent(buttonDelete,10,10,10)
                .addComponent(tfDate,50,50,50)
                .addGap(5)
                .addComponent(tfAmount,75,75,75)
                .addGap(5)
                .addComponent(tfDebit)
                .addGap(5)
                .addComponent(tfCredit)
                .addGap(5)
                .addComponent(cbDescription,200,200,Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(buttonDelete,25,25,25)
                .addComponent(tfDate,25,25,25)
                .addComponent(tfAmount,25,25,25)
                .addComponent(tfDebit,25,25,25)
                .addComponent(tfCredit,25,25,25)
                .addComponent(cbDescription,25,25,25));
    }

    public void disableValidators(){
        tfAmount.setValidatorEnabled(false);
        tfDebit.setValidatorEnabled(false);
        tfCredit.setValidatorEnabled(false);
        tfDate.setValidatorEnabled(false);
    }

    public void addDeleteAction(ActionListener action){
        buttonDelete.addActionListener(action);
        buttonDelete.setEnabled(true);
    }

    public String getDate(){
        return tfDate.getText();
    }

    public String getAmount(){
        return tfAmount.getText();
    }

    public String getDebit(){
        return tfDebit.getSelectedAccount();
    }

    public String getCredit(){
        return tfCredit.getSelectedAccount();
    }

    public String getDescription(){
        return ((JTextField)cbDescription.getEditor().getEditorComponent()).getText();
    }

    public void setDate(String date){
        tfDate.focusGained(null);
        tfDate.setText(date);
    }

    public void setAmount(String amount){
        tfAmount.focusGained(null);
        tfAmount.setText(amount);
    }

    public void setDebit(String debit){
        tfDebit.setSelectedAccount(debit);
    }

    public void setCredit(String credit){
        tfCredit.setSelectedAccount(credit);
    }

    public void setDescription(String description){
        ((JTextField)cbDescription.getEditor().getEditorComponent()).setText(description);
    }

    public void setDebitCreditDescription(String debit, String credit, String description) {
        isSuppressedUpdate = true;
        setDebit(debit);
        setCredit(credit);
        updateDescriptions();
        setDescription(description);
        isSuppressedUpdate = false;
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        new SwingWorkerHandler() {
            @Override
            protected void runInBackground() {
                if (!isSuppressedUpdate) updateDescriptions();
            }
        }.execute();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        new SwingWorkerHandler() {
            @Override
            protected void runInBackground() {
                if (!isSuppressedUpdate) updateDescriptions();
            }
        }.execute();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        new SwingWorkerHandler() {
            @Override
            protected void runInBackground() {
                if (!isSuppressedUpdate) updateDescriptions();
            }
        }.execute();
    }

    private void updateDescriptions() {
        synchronized(lock) {
            String debit = tfDebit.getSelectedAccount();
            String credit = tfCredit.getSelectedAccount();
            if (!debit.trim().isEmpty() && !credit.trim().isEmpty()) {
                Set<String> descList = accountPairDescriptionMap.get(new AccountPairModel(debit, credit));
                if (descList != null) {
                    String cbValue = ((JTextField)cbDescription.getEditor().getEditorComponent()).getText();
                    DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) cbDescription.getModel();
                    model.removeAllElements();
                    descList.forEach(model::addElement);
                    ((JTextField)cbDescription.getEditor().getEditorComponent()).setText(cbValue);
                }
            }
        }
    }
}
