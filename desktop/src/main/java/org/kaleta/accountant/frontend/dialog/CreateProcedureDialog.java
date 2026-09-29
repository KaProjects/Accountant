package org.kaleta.accountant.frontend.dialog;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.ProceduresModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.AccountPairModel;
import org.kaleta.accountant.frontend.component.ArrowKeyNavigation;
import org.kaleta.accountant.frontend.component.HintValidatedTextField;
import org.kaleta.accountant.frontend.component.TransactionPanel;
import org.kaleta.accountant.frontend.component.procedure.ProcedureRules;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CreateProcedureDialog extends Dialog {
    private final Map<AccountPairModel, Set<String>> accountPairDescriptionMap;
    private final Map<String, List<AccountsModel.Account>> accountMap;
    private final List<SchemaModel.Class> classList;

    private JPanel panelTransactions;
    private HintValidatedTextField tfName;

    private JComboBox<String> cbGroup;
    private final List<TransactionPanel> transactionPanelList;
    private boolean deleteRequested;

    public CreateProcedureDialog(Configuration configuration, Map<AccountPairModel, Set<String>> accountPairDescriptionMap,
                                 Map<String, List<AccountsModel.Account>> accountMap, List<SchemaModel.Class> classList,
                                 ProceduresModel.Group.Procedure procedure, String procedureGroupName, List<String> procedureGroupNameList) {
        super(configuration,
                (procedure == null) ? "Creating Procedure" : "Editing Procedure",
                (procedure == null) ? "Create" : "Update");
        this.accountPairDescriptionMap = accountPairDescriptionMap;
        this.accountMap = accountMap;
        this.classList = classList;
        transactionPanelList = new ArrayList<>();
        buildDialogContent();

        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) cbGroup.getModel();
        // a derived group is the app's to fill: nothing is composed into it by hand, so it is not
        // offered here - except as the group of a derived procedure being edited, which stays put
        procedureGroupNameList.stream()
                .filter(group -> !ProcedureRules.isAppMaintained(group) || group.equals(procedureGroupName))
                .forEach(model::addElement);

        // filled in for both cases: when creating from inside a group's card it is that group, and
        // when editing it is the procedure's own group - which may be changed, moving the procedure
        if (procedureGroupName != null) {
            ((JTextField) cbGroup.getEditor().getEditorComponent()).setText(procedureGroupName);
        }

        if (procedure == null) {
            addTransactionPanel();
        } else {
            if (ProcedureRules.isAppMaintained(procedureGroupName)) {
                // what the app writes, it names and keeps: only the transactions are the user's to
                // change here - the amount usually put in is exactly what this dialog is opened for
                tfName.setEnabled(false);
                cbGroup.setEnabled(false);
            } else {
                addDeleteButton();
            }
            tfName.focusGained(null);
            tfName.setText(procedure.getName());

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
        this.setSize(new Dimension(this.getWidth(), this.getHeight() + 100));
    }

    /**
     * Deleting is offered where the procedure is already open and its transactions are in view, so
     * that it is deleted knowing what it books - and it is closed out of the way immediately after,
     * since there is nothing left to edit.
     */
    private void addDeleteButton() {
        JButton buttonDelete = new JButton("Delete");
        buttonDelete.setToolTipText("Delete this procedure");
        buttonDelete.addActionListener(a -> {
            deleteRequested = true;
            dispose();
        });
        setButtons(panel -> panel.add(buttonDelete));
    }

    /** Whether the dialog was closed by the delete button rather than by confirming the edit. */
    public boolean isDeleteRequested() {
        return deleteRequested;
    }

    private void buildDialogContent() {
        JLabel labelName = new JLabel("Procedure Name:");
        tfName = new HintValidatedTextField("", "Procedure Name", "set procedure name", false, this);

        JLabel labelGroup = new JLabel("Procedure Group:");
        cbGroup = new JComboBox<>();
        cbGroup.setEditable(true);

        panelTransactions = new JPanel();
        panelTransactions.setLayout(new BoxLayout(panelTransactions, BoxLayout.Y_AXIS));
        JScrollPane trPane = new JScrollPane(panelTransactions);

        JButton buttonAddTr = new JButton("Add Transaction");
        buttonAddTr.addActionListener(e -> addTransactionPanel());

        setContent(layout -> {
            layout.setHorizontalGroup(layout.createParallelGroup()
                    .addGroup(layout.createSequentialGroup()
                            .addComponent(labelName)
                            .addGap(5)
                            .addComponent(tfName))
                    .addGroup(layout.createSequentialGroup()
                            .addComponent(labelGroup)
                            .addGap(5)
                            .addComponent(cbGroup))
                    .addComponent(trPane));
            layout.setVerticalGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup()
                            .addComponent(labelName, 25, 25, 25)
                            .addComponent(tfName, 25, 25, 25))
                    .addGroup(layout.createParallelGroup()
                            .addComponent(labelGroup, 25, 25, 25)
                            .addComponent(cbGroup, 25, 25, 25))
                    .addGap(5)
                    .addComponent(trPane));
        });
        setButtons(jPanel -> jPanel.add(buttonAddTr));
    }

    private void addTransactionPanel(){
        TransactionPanel transactionPanel = new TransactionPanel(getConfiguration(), accountPairDescriptionMap, accountMap, classList, this, false);
        ArrowKeyNavigation.install(transactionPanel, () -> transactionPanelList);
        transactionPanel.addDeleteAction(e1 -> {
            transactionPanel.disableValidators();
            CreateProcedureDialog.this.validateDialog();
            transactionPanelList.remove(transactionPanel);
            panelTransactions.removeAll();
            if (transactionPanelList.isEmpty()){
                CreateProcedureDialog.this.setDialogValid("No Transaction");
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

    public String getProcedureName(){
        return tfName.getText();
    }

    public String getGroupName() {
        return ((JTextField)cbGroup.getEditor().getEditorComponent()).getText();
    }

    public List<ProceduresModel.Group.Procedure.Transaction> getTransactions(){
        List<ProceduresModel.Group.Procedure.Transaction> transactionList = new ArrayList<>();
        for (TransactionPanel panel : transactionPanelList){
            ProceduresModel.Group.Procedure.Transaction transaction = new ProceduresModel.Group.Procedure.Transaction();
            transaction.setAmount(panel.getAmount());
            transaction.setDebit(panel.getDebit());
            transaction.setCredit(panel.getCredit());
            transaction.setDescription(panel.getDescription());
            transactionList.add(transaction);
        }
        return transactionList;
    }
}
