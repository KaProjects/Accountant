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

public class CreateProcedureDialog extends Dialog {

    private HintValidatedTextField tfName;
    private List<TransactionPanel> transactionPanelList;

    public CreateProcedureDialog(Configuration configuration, Map<AccountPairModel, List<String>> accountPairDescriptionMap,
                                 Map<String, List<AccountsModel.Account>> accountMap, List<SchemaModel.Class> classList) {
        super(configuration, "Creating Procedure", "Create");
        buildDialogContent(accountPairDescriptionMap, accountMap, classList);
        pack();
        this.setSize(new Dimension(this.getWidth(), this.getHeight() + 100));
    }

    private void buildDialogContent(Map<AccountPairModel, List<String>> accountPairDescriptionMap, Map<String, List<AccountsModel.Account>> accountMap, List<SchemaModel.Class> classList) {
        transactionPanelList = new ArrayList<>();

        JLabel labelName = new JLabel("Procedure Name:");
        tfName = new HintValidatedTextField("", "Procedure Name", "set procedure name", false, this);

        JPanel trPanel = new JPanel();
        trPanel.setLayout(new BoxLayout(trPanel, BoxLayout.Y_AXIS));
        JScrollPane trPane = new JScrollPane(trPanel);

        TransactionPanel firstTrPanel = new TransactionPanel(getConfiguration(), accountPairDescriptionMap, accountMap, classList, this, false);
        trPanel.add(firstTrPanel);
        transactionPanelList.add(firstTrPanel);

        JButton buttonAddTr = new JButton("Add Transaction");
        buttonAddTr.addActionListener(e -> {
            TransactionPanel panel = new TransactionPanel(getConfiguration(), accountPairDescriptionMap, accountMap, classList, this, false);
            panel.addDeleteAction(e1 -> {
                panel.disableValidators();
                CreateProcedureDialog.this.validateDialog();
                transactionPanelList.remove(panel);
                trPanel.removeAll();
                for (TransactionPanel transactionPanel : transactionPanelList){
                    trPanel.add(transactionPanel);
                }
                trPanel.repaint();
                trPanel.revalidate();
            });
            transactionPanelList.add(panel);
            trPanel.add(panel);
            trPanel.repaint();
            trPanel.revalidate();
        });

        setContent(layout -> {
            layout.setHorizontalGroup(layout.createParallelGroup()
                    .addGroup(layout.createSequentialGroup()
                            .addComponent(labelName)
                            .addGap(5)
                            .addComponent(tfName))
                    .addComponent(trPane));
            layout.setVerticalGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup()
                            .addComponent(labelName,25,25,25)
                            .addComponent(tfName,25,25,25))
                    .addGap(5)
                    .addComponent(trPane));
        });
        setButtons(jPanel -> {
            jPanel.add(buttonAddTr);
        });
    }

    public String getProcedureName(){
        return tfName.getText();
    }

    public List<ProceduresModel.Procedure.Transaction> getTransactions(){
        List<ProceduresModel.Procedure.Transaction> transactionList = new ArrayList<>();
        for (TransactionPanel panel : transactionPanelList){
            ProceduresModel.Procedure.Transaction transaction = new ProceduresModel.Procedure.Transaction();
            transaction.setAmount(panel.getAmount());
            transaction.setDebit(panel.getDebit());
            transaction.setCredit(panel.getCredit());
            transaction.setDescription(panel.getDescription());
            transactionList.add(transaction);
        }
        return transactionList;
    }
}
