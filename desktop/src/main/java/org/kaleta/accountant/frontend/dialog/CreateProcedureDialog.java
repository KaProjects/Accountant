package org.kaleta.accountant.frontend.dialog;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.ProceduresModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.AccountPairModel;
import org.kaleta.accountant.frontend.component.HintValidatedTextField;
import org.kaleta.accountant.frontend.component.TransactionPanel;

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
        procedureGroupNameList.forEach(model::addElement);

        if (procedure == null) {
            addTransactionPanel();
        } else {
            tfName.focusGained(null);
            tfName.setText(procedure.getName());

            ((JTextField)cbGroup.getEditor().getEditorComponent()).setText(procedureGroupName);
            cbGroup.setEnabled(false);

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
