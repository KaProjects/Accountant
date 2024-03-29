package org.kaleta.accountant.frontend.dialog;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.ProceduresModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.AccountPairModel;
import org.kaleta.accountant.frontend.common.Validable;
import org.kaleta.accountant.frontend.component.DatePickerTextField;
import org.kaleta.accountant.frontend.component.TransactionPanel;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;

public class AddTransactionDialog extends Dialog {
    private final Map<AccountPairModel, Set<String>> accountPairDescriptionMap;
    private final Map<String, List<AccountsModel.Account>> accountMap;
    private final List<SchemaModel.Class> classList;

    private final List<TransactionPanel> transactionPanelList;
    private JPanel panelTransactions;

    public AddTransactionDialog(Configuration configuration, Map<AccountPairModel, Set<String>> accountPairDescriptionMap,
                                Map<String, List<AccountsModel.Account>> accountMap, List<SchemaModel.Class> classList,
                                ProceduresModel.Group.Procedure procedure) {
        super(configuration, "Adding Transaction(s)", "Add");
        setModal(false);
        this.accountPairDescriptionMap = accountPairDescriptionMap;
        this.accountMap = accountMap;
        this.classList = classList;
        transactionPanelList = new ArrayList<>();
        buildDialogContent();
        if (procedure == null) {
            addTransactionPanel();
        } else {
            for (ProceduresModel.Group.Procedure.Transaction preparedTr : procedure.getTransaction()){
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
        this.setSize(new Dimension(this.getWidth() + 500, this.getHeight() + 500));
    }

    private void buildDialogContent() {
        panelTransactions = new JPanel();
        panelTransactions.setLayout(new BoxLayout(panelTransactions, BoxLayout.Y_AXIS));
        JScrollPane trPane = new JScrollPane(panelTransactions);

        JButton buttonAddTr = new JButton("Add Transaction");
        buttonAddTr.addActionListener(e -> addTransactionPanel());

        JButton buttonAddProcedure = new JButton("Add Procedure");
        buttonAddProcedure.addActionListener(e -> addProcedurePanel());

        JButton buttonAddResource = new JButton("Add Resource");
        buttonAddResource.addActionListener(e -> addResourcePanel());

        JButton buttonShowAccounts = new JButton("Show Accounts");
        buttonShowAccounts.addActionListener(e -> showAccounts());

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
        setButtons(jPanel -> {
            jPanel.add(buttonAddTr);
            jPanel.add(buttonAddProcedure);
            jPanel.add(buttonAddResource);
            jPanel.add(buttonShowAccounts);
        });
    }

    private void showAccounts() {
        SelectAccountDialog selectExpenseAccountDialog = new SelectAccountDialog(getConfiguration(), accountMap, classList, false, false);
        selectExpenseAccountDialog.setVisible(true);
    }

    public List<TransactionPanel> getTransactionPanelList() {
        return transactionPanelList;
    }

    private void addTransactionPanel(){
        addTransactionPanel(null);
    }

    public void addTransactionPanel(Consumer<TransactionPanel> transactionPanelConsumer){
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

        if (transactionPanelConsumer != null) {
            transactionPanelConsumer.accept(transactionPanel);
        }

        panelTransactions.add(transactionPanel);
        transactionPanelList.add(transactionPanel);
        panelTransactions.repaint();
        panelTransactions.revalidate();
    }

    private void addProcedurePanel() {
        Dialog dialog = new Dialog(getConfiguration(), "Selecting Procedure...", "Select") {};

        List<ProceduresModel.Group> procedureGroupList = Service.PROCEDURES.getProcedureGroupList(getConfiguration().getSelectedYear());

        List<JList<String>> uiLists = new ArrayList<>();

        JTabbedPane pane = new JTabbedPane();
        for(ProceduresModel.Group group : procedureGroupList)
        {
            JList<String> list = new ValidatedProcedureList(group.getProcedure());
            list.addListSelectionListener(dialog);
            list.setSelectedIndex(-1);
            list.setCellRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    Component c = super.getListCellRendererComponent(list, "   " + value + "   ", index, isSelected, cellHasFocus);
                    c.setBackground(new JPanel().getBackground());
                    c.setFont(new Font(c.getFont().getName(), Font.BOLD, 15));
                    return c;
                }
            });

            uiLists.add(list);
            pane.addTab(group.getName(), new JScrollPane(list));
        }

        dialog.setContent( layout -> {
            layout.setHorizontalGroup(layout.createParallelGroup().addComponent(pane));
            layout.setVerticalGroup(layout.createSequentialGroup().addGap(5).addComponent(pane).addGap(10));
        });

        dialog.setDialogValid("No Item Selected");
        dialog.pack();
        dialog.setSize(dialog.getWidth()*2,dialog.getHeight());
        dialog.setVisible(true);
        if (dialog.getResult()) {
            ProceduresModel.Group group = procedureGroupList.get(pane.getSelectedIndex());
            ProceduresModel.Group.Procedure procedure = group.getProcedure().get(uiLists.get(pane.getSelectedIndex()).getSelectedIndex());
            for (ProceduresModel.Group.Procedure.Transaction transaction : procedure.getTransaction()) {
                addTransactionPanel(transactionPanel -> {
                    transactionPanel.setAmount(transaction.getAmount());
                    transactionPanel.setDebitCreditDescription(transaction.getDebit(), transaction.getCredit(), transaction.getDescription());
                });
            }
        }
    }

    private void addResourcePanel() {
        String year = getConfiguration().getSelectedYear();
        Map<String, List<AccountsModel.Account>> allAccountMap = Service.ACCOUNT.getAccountsViaSchemaMap(year);
        Map<String, List<AccountsModel.Account>> resourceAccountMap = new HashMap<>();
        for (String schemaId : allAccountMap.keySet()){
            if (schemaId.startsWith("1")){
                resourceAccountMap.put(schemaId, allAccountMap.get(schemaId));
            }
        }
        List<SchemaModel.Class> resourceClasses = new ArrayList<>();
        resourceClasses.add(Service.SCHEMA.getSchemaClassMap(year).get(1));

        SelectAccountDialog dialog = new SelectAccountDialog(getConfiguration(), resourceAccountMap, resourceClasses, true);
        dialog.setVisible(true);
        if (dialog.getResult()) {
            String amount = JOptionPane.showInputDialog(this, "Set Amount");
            if (amount != null) {
                String selectedAccId = dialog.getSelectedAccountId();

                addTransactionPanel(transactionPanel -> {
                    transactionPanel.setDebit(selectedAccId);
                    transactionPanel.setAmount(amount);
                    transactionPanel.setDescription(Constants.Transaction.RESOURCE_ACQUIRED);
                });
                addTransactionPanel(transactionPanel -> {
                    transactionPanel.setAmount(amount);
                    transactionPanel.setDebitCreditDescription(Service.ACCOUNT.getConsumptionAccountId(selectedAccId.split("\\.")[0],selectedAccId.split("\\.")[1]), selectedAccId, Constants.Transaction.RESOURCE_CONSUMED);
                });
            }
        }
    }

    private class ValidatedProcedureList extends JList<String> implements Validable {

        ValidatedProcedureList(List<ProceduresModel.Group.Procedure> procedureList) {
            super(new ListModel<String>() {
                @Override
                public int getSize() {
                    return procedureList.size();
                }

                @Override
                public String getElementAt(int index) {
                    return procedureList.get(index).getName();
                }

                @Override
                public void addListDataListener(ListDataListener l) {}

                @Override
                public void removeListDataListener(ListDataListener l) {}


            });
        }

        @Override
        public String validator() {
            return this.isSelectionEmpty() ? "No Item Selected" : null;
        }
    }

}
