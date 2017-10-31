package org.kaleta.accountant.frontend.dep.dialog.transaction;

import org.kaleta.accountant.backend.entity.Transaction;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.SwingWorkerHandler;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Created by Stanislav Kaleta on 24.05.2016.
 */
@Deprecated
public class TransactionPanel extends JPanel implements Configurable, DocumentListener {
    private Configuration configuration;

    private JTextField tfDate;
    private JComboBox<String> cbDescription;
    private JTextField tfAmount;
    private JTextField tfDebit;
    private JTextField tfCredit;

    private boolean isActive;
    private final Object lock = new Object();

    public TransactionPanel(Configuration configuration){
        setConfiguration(configuration);
        isActive = true;
        tfDate = new JTextField();
        cbDescription = new JComboBox<>();
        cbDescription.setEditable(true);
        tfAmount = new JTextField();
        tfDebit = new JTextField();
        //tfDebit.addMouseListener(new AccountTextFieldClicked(this, true));
        tfDebit.getDocument().addDocumentListener(this);
        tfCredit = new JTextField();
        //tfCredit.addMouseListener(new AccountTextFieldClicked(this, false));
        tfCredit.getDocument().addDocumentListener(this);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGap(5).addGroup(layout.createParallelGroup().addComponent(tfDate,50,50,50))
                .addGap(5).addGroup(layout.createParallelGroup().addComponent(cbDescription,200,200,Short.MAX_VALUE))
                .addGap(5).addGroup(layout.createParallelGroup().addComponent(tfAmount,75,75,75))
                .addGap(5).addGroup(layout.createParallelGroup().addComponent(tfDebit,50,50,50))
                .addGap(5).addGroup(layout.createParallelGroup().addComponent(tfCredit,50,50,50))
                .addGap(5));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGap(5)
                .addGroup(layout.createParallelGroup()
                        .addGroup(layout.createSequentialGroup().addComponent(tfDate,25,25,25))
                        .addGroup(layout.createSequentialGroup().addComponent(cbDescription,25,25,25))
                        .addGroup(layout.createSequentialGroup().addComponent(tfAmount,25,25,25))
                        .addGroup(layout.createSequentialGroup().addComponent(tfDebit,25,25,25))
                        .addGroup(layout.createSequentialGroup().addComponent(tfCredit,25,25,25)))
                .addGap(5));

        new Thread(() -> {
            while(isActive){
                JTextField cbDescriptionEditor = (JTextField) cbDescription.getEditor().getEditorComponent();
                boolean filled = tfDate.getText() != null && !tfDate.getText().trim().isEmpty() && tfDate.getText().length() == 4
                        && cbDescriptionEditor.getText() != null && !cbDescriptionEditor.getText().trim().isEmpty()
                        && tfAmount.getText() != null && !tfAmount.getText().trim().isEmpty()
                        && tfCredit.getText() != null && !tfCredit.getText().trim().isEmpty() && tfCredit.getText().length() > 2
                        && tfDebit.getText() != null && !tfDebit.getText().trim().isEmpty() && tfDebit.getText().length() > 2;
                TransactionPanel.this.setBackground((filled) ? Constants.Color.INCOME_GREEN : Constants.Color.EXPENSE_RED);
            }
            TransactionPanel.this.setBackground(new JPanel().getBackground());
        }).start();
    }

    public Transaction getTransaction() {
        toggleActive(false);
        Transaction transaction = new Transaction();
        transaction.setDate(tfDate.getText());
        transaction.setDescription(((JTextField) cbDescription.getEditor().getEditorComponent()).getText());
        transaction.setAmount(tfAmount.getText());
        transaction.setDebit(tfDebit.getText());
        transaction.setCredit(tfCredit.getText());
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        tfDate.setText(transaction.getDate());
        ((JTextField) cbDescription.getEditor().getEditorComponent()).setText(transaction.getDescription());
        tfAmount.setText(transaction.getAmount());
        tfDebit.setText(transaction.getDebit());
        tfCredit.setText(transaction.getCredit());
    }

    public void toggleActive(boolean flag){
        this.isActive = flag;
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        new SwingWorkerHandler() {
            @Override
            protected void runInBackground() {
                updateDescriptions();
            }
        }.execute();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        new SwingWorkerHandler() {
            @Override
            protected void runInBackground() {
                updateDescriptions();
            }
        }.execute();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        new SwingWorkerHandler() {
            @Override
            protected void runInBackground() {
                updateDescriptions();
            }
        }.execute();
    }

    private void updateDescriptions() {
        synchronized(lock) {
            // Set<String> descList = Service.JOURNAL.listTransactionDescriptions(tfDebit.getText(), tfCredit.getText(), getConfiguration().getActiveYear());
            String cbValue = ((JTextField)cbDescription.getEditor().getEditorComponent()).getText();
            DefaultComboBoxModel model = (DefaultComboBoxModel) cbDescription.getModel();
            model.removeAllElements();
            // descList.forEach(model::addElement);
            ((JTextField)cbDescription.getEditor().getEditorComponent()).setText(cbValue);
        }
    }
}
