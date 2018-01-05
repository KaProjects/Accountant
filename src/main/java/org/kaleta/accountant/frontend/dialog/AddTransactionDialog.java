package org.kaleta.accountant.frontend.dialog;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.ProceduresModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.AccountPairModel;
import org.kaleta.accountant.frontend.component.DatePickerTextField;
import org.kaleta.accountant.frontend.component.TransactionPanel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.*;
import java.util.List;

public class AddTransactionDialog extends Dialog {
    private final Map<AccountPairModel, Set<String>> accountPairDescriptionMap;
    private final Map<String, List<AccountsModel.Account>> accountMap;
    private final List<SchemaModel.Class> classList;

    private final List<TransactionPanel> transactionPanelList;
    private JPanel panelTransactions;

    public AddTransactionDialog(Configuration configuration, Map<AccountPairModel, Set<String>> accountPairDescriptionMap,
                                Map<String, List<AccountsModel.Account>> accountMap, List<SchemaModel.Class> classList,
                                ProceduresModel.Procedure procedure) {
        super(configuration, "Adding Transaction(s)", "Add");
        this.accountPairDescriptionMap = accountPairDescriptionMap;
        this.accountMap = accountMap;
        this.classList = classList;
        transactionPanelList = new ArrayList<>();
        buildDialogContent();
        if (procedure == null) {
            addTransactionPanel();
        } else {
            for (ProceduresModel.Procedure.Transaction preparedTr : procedure.getTransaction()){
                addTransactionPanel();
                TransactionPanel panel = transactionPanelList.get(transactionPanelList.size() - 1);
                panel.setDescription(preparedTr.getDescription());
                panel.setAmount(preparedTr.getAmount());
                panel.setDebit(preparedTr.getDebit());
                panel.setCredit(preparedTr.getCredit());
            }
            validateDialog();
        }
        pack();
        this.setSize(new Dimension(this.getWidth(), this.getHeight() + 100));
    }

    private void buildDialogContent() {
        panelTransactions = new JPanel();
        panelTransactions.setLayout(new BoxLayout(panelTransactions, BoxLayout.Y_AXIS));
        JScrollPane trPane = new JScrollPane(panelTransactions);

        JButton buttonAddTr = new JButton("Add Transaction");
        buttonAddTr.addActionListener(e -> addTransactionPanel());

        JButton buttonSetDate = new JButton("Set Date");
        JButton buttonConfirmSetDate = new JButton("Confirm");
        JButton buttonCancelSetDate = new JButton("Cancel");
        DatePickerTextField tfSetDate = new DatePickerTextField("", null);
        tfSetDate.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                buttonConfirmSetDate.setEnabled(tfSetDate.validator() == null);
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                buttonConfirmSetDate.setEnabled(tfSetDate.validator() == null);
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                buttonConfirmSetDate.setEnabled(tfSetDate.validator() == null);
            }
        });
        JButton buttonToday = new JButton("Set Today");
        buttonToday.addActionListener(a -> {
            tfSetDate.focusGained(null);
            Calendar calendar = Calendar.getInstance();
            tfSetDate.setText(String.format("%1$02d%2$02d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1));
        });
        JPanel panelSetDate = new JPanel();
        panelSetDate.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        panelSetDate.setLayout(new BoxLayout(panelSetDate, BoxLayout.X_AXIS));
        panelSetDate.add(tfSetDate);
        panelSetDate.add(buttonToday);
        panelSetDate.add(buttonCancelSetDate);
        panelSetDate.add(buttonConfirmSetDate);
        panelSetDate.setVisible(false);

        buttonSetDate.addActionListener(actionEvent -> {
            panelSetDate.setVisible(true);
            buttonSetDate.setEnabled(false);
        });
        buttonConfirmSetDate.addActionListener(actionEvent -> {
            panelSetDate.setVisible(false);
            buttonSetDate.setEnabled(true);
            for (TransactionPanel panel : transactionPanelList){
                panel.setDate(tfSetDate.getText());
            }
        });
        buttonCancelSetDate.addActionListener(actionEvent -> {
            panelSetDate.setVisible(false);
            buttonSetDate.setEnabled(true);
        });

        setContent(layout -> {
            layout.setHorizontalGroup(layout.createParallelGroup()
                    .addGroup(layout.createSequentialGroup()
                            .addComponent(buttonSetDate)
                            .addComponent(panelSetDate))
                    .addComponent(trPane));
            layout.setVerticalGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup()
                            .addComponent(buttonSetDate, 25, 25, 25)
                            .addComponent(panelSetDate, 25, 25, 25))
                    .addGap(5)
                    .addComponent(trPane));
        });
        setButtons(jPanel -> jPanel.add(buttonAddTr));
    }

    private void addTransactionPanel(){
        TransactionPanel transactionPanel = new TransactionPanel(getConfiguration(), accountPairDescriptionMap, accountMap, classList, this, true);
        transactionPanel.addDeleteAction(e1 -> {
            transactionPanel.disableValidators();
            AddTransactionDialog.this.validateDialog();
            transactionPanelList.remove(transactionPanel);
            panelTransactions.removeAll();
            if (transactionPanelList.isEmpty()){
                AddTransactionDialog.this.setDialogValid("No Transaction");
            } else {
                for (TransactionPanel trPanel : transactionPanelList) {
                    panelTransactions.add(trPanel);
                }
            }
            panelTransactions.repaint();
            panelTransactions.revalidate();
        });
        panelTransactions.add(transactionPanel);
        transactionPanelList.add(transactionPanel);
        panelTransactions.repaint();
        panelTransactions.revalidate();
    }

    public List<TransactionPanel> getTransactionPanelList() {
        return transactionPanelList;
    }
}
