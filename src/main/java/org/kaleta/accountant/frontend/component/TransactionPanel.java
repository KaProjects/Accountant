package org.kaleta.accountant.frontend.component;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.AccountPairModel;
import org.kaleta.accountant.frontend.common.SwingWorkerHandler;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.List;
import java.util.Map;

/**
 * TODO tfAmount only numbers
 */
public class TransactionPanel extends JPanel implements DocumentListener {
    private final Object lock = new Object();
    private Map<AccountPairModel, List<String>> accountPairDescriptionMap;

    private JTextField tfDate;
    private JComboBox<String> cbDescription;
    private JTextField tfAmount;
    private SelectAccountTextField tfDebit;
    private SelectAccountTextField tfCredit;

    public TransactionPanel(Configuration configuration, Map<AccountPairModel, List<String>> accountPairDescriptionMap,
                            Map<String, List<AccountsModel.Account>> accountMap, List<SchemaModel.Class> classList) {
        this.accountPairDescriptionMap = accountPairDescriptionMap;

        tfDate = new JTextField();
        cbDescription = new JComboBox<>();
        cbDescription.setEditable(true);

        tfAmount = new JTextField();
        tfDebit = new SelectAccountTextField(configuration, accountMap, classList);
        tfDebit.getDocument().addDocumentListener(this);
        tfCredit = new SelectAccountTextField(configuration, accountMap, classList);
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
            String debit = tfDebit.getSelectedAccount();
            String credit = tfCredit.getSelectedAccount();
            if (!debit.trim().isEmpty() && !credit.trim().isEmpty()) {
                List<String> descList = accountPairDescriptionMap.get(new AccountPairModel(debit, credit));
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
