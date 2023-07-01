package org.kaleta.accountant.frontend.action.menu;

import org.kaleta.accountant.backend.manager.PdfParserManager;
import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.PdfTransactionModel;
import org.kaleta.accountant.backend.model.ProceduresModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.ErrorHandler;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.AccountPairModel;
import org.kaleta.accountant.frontend.component.TransactionPanel;
import org.kaleta.accountant.frontend.dialog.AddTransactionDialog;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.kaleta.accountant.Initializer.DEFAULT_FILES_DIR;

public class OpenImportTransactionsFromPdfDialog extends MenuAction {

    public OpenImportTransactionsFromPdfDialog(Configuration config) {
        super(config, "PDF/CSV Transaction(s)");
    }

    @Override
    protected void actionPerformed() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setCurrentDirectory(new File(DEFAULT_FILES_DIR));
        fileChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                String extension = Arrays.stream(f.getName().split("\\.")).reduce((a, b) -> b).orElse(null);
                return  extension != null && (extension.equals("pdf") || extension.equals("csv"));
            }

            @Override
            public String getDescription() {
                return "PDF & CSV files";
            }
        });

        int result = fileChooser.showOpenDialog((Frame) getConfiguration());
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            String type = (String) JOptionPane.showInputDialog(
                    (Frame) getConfiguration(),
                    "Select Document Type",
                    "Document Type",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    PdfParserManager.getDataTypeOptions(),
                    PdfParserManager.getDataTypeOptions()[0]);

            PdfParserManager manager = new PdfParserManager(file, type);

            try {
                manager.loadContent();
            } catch (Exception e) {
                ErrorHandler.getThrowableDialog(e).setVisible(true);
                return;
            }

            List<PdfTransactionModel> transactions;
            try {
                transactions = manager.getTransactions();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(manager.getContent());
                ErrorHandler.getThrowableDialog(e).setVisible(true);
                return;
            }

            Map<String, List<AccountsModel.Account>> allAccountMap = Service.ACCOUNT.getAccountsViaSchemaMap(getConfiguration().getSelectedYear());
            List<SchemaModel.Class> classList = Service.SCHEMA.getSchemaClassList(getConfiguration().getSelectedYear());
            Map<AccountPairModel, Set<String>> accountPairDescriptionMap = Service.TRANSACTIONS.getAccountPairDescriptions(getConfiguration().getSelectedYear());

            AddTransactionDialog dialog = new AddTransactionDialog(getConfiguration(), accountPairDescriptionMap, allAccountMap, classList, new ProceduresModel.Group.Procedure());
            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if (dialog.getResult()) {
                        for (TransactionPanel panel : dialog.getTransactionPanelList()) {
                            Service.TRANSACTIONS.addTransaction(getConfiguration().getSelectedYear(),
                                    panel.getDate(), panel.getAmount(), panel.getDebit(), panel.getCredit(), panel.getDescription());
                        }
                        getConfiguration().update(Configuration.TRANSACTION_UPDATED);
                    }
                }

                @Override
                public void windowClosing(WindowEvent e) {
                }
            });
            for (PdfTransactionModel transactionModel : transactions) {
                dialog.addTransactionPanel(transactionPanel -> {
                    if (transactionModel.getDate() != null){
                        transactionPanel.setDate(transactionModel.getDate());
                    }
                    if (transactionModel.getAmount() != null){
                        transactionPanel.setAmount(transactionModel.getAmount());
                    }
                    if (transactionModel.getDescription() != null){
                        transactionPanel.setDescription(transactionModel.getDescription());
                    }
                    if (transactionModel.getDebit() != null) {
                        transactionPanel.setDebit(transactionModel.getDebit());
                    }
                    if (transactionModel.getCredit() != null) {
                        transactionPanel.setCredit(transactionModel.getCredit());
                    }
                });
            }
            dialog.setVisible(true);
        }
    }
}
