package org.kaleta.accountant.frontend.dialog;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.FirebaseTransactionModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.ErrorHandler;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.AccountPairModel;
import org.kaleta.accountant.frontend.component.TransactionPanel;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImportTransactionsFromAndroidDialog extends Dialog {

    private final List<TransactionPanel> transactionPanelList;
    private JPanel panelTransactions;

    public ImportTransactionsFromAndroidDialog(Configuration configuration) {
        super(configuration, "Importing Transaction(s)", "Import");

        transactionPanelList = new ArrayList<>();
        buildDialogContent();
        pack();
        this.setSize(new Dimension(this.getWidth() + 500, this.getHeight() + 500));
    }

    private void buildDialogContent() {
        panelTransactions = new JPanel();
        panelTransactions.setLayout(new BoxLayout(panelTransactions, BoxLayout.Y_AXIS));
        JScrollPane trPane = new JScrollPane(panelTransactions);

        JButton buttonLoad = new JButton("Load Transactions");

        buttonLoad.addActionListener(l -> {
            try {
                panelTransactions.removeAll();
                transactionPanelList.clear();

                Map<String, List<AccountsModel.Account>> allAccountMap = Service.ACCOUNT.getAccountsViaSchemaMap(getConfiguration().getSelectedYear());
                List<SchemaModel.Class> classList = Service.SCHEMA.getSchemaClassList(getConfiguration().getSelectedYear());
                Map<AccountPairModel, Set<String>> accountPairDescriptionMap = Service.TRANSACTIONS.getAccountPairDescriptions(getConfiguration().getSelectedYear());

                for (FirebaseTransactionModel transactionModel : Service.FIREBASE.loadTransactions()) {
                    TransactionPanel transactionPanel = new TransactionPanel(getConfiguration(), accountPairDescriptionMap, allAccountMap, classList, this, true);
                    transactionPanel.setDate(transactionModel.getDate());
                    transactionPanel.setAmount(transactionModel.getAmount());
                    if (!transactionModel.getDebit().isEmpty()) {
                        transactionPanel.setDebit(transactionModel.getDebit());
                    }
                    if (!transactionModel.getCredit().isEmpty()) {
                        transactionPanel.setCredit(transactionModel.getCredit());
                    }
                    transactionPanel.setDescription(transactionModel.getDescription());

                    panelTransactions.add(transactionPanel);
                    transactionPanelList.add(transactionPanel);
                }
                panelTransactions.repaint();
                panelTransactions.revalidate();
                buttonLoad.setEnabled(false);
            } catch (Throwable e) {
                e.printStackTrace();
                ErrorHandler.getThrowableDialog(e).setVisible(true);
            }
        });

        setContent(layout -> {
            layout.setHorizontalGroup(layout.createParallelGroup()
                    .addComponent(trPane));
            layout.setVerticalGroup(layout.createSequentialGroup()
                    .addComponent(trPane));
        });

        setButtons(jPanel -> {
            jPanel.add(buttonLoad);
        });
    }

    public List<TransactionPanel> getTransactionPanelList() {
        return transactionPanelList;
    }
}
