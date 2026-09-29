package org.kaleta.accountant.frontend.action.menu;

import org.kaleta.accountant.backend.manager.PdfParserManager;
import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.PdfTransactionModel;
import org.kaleta.accountant.backend.model.ProceduresModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.Initializer;
import org.kaleta.accountant.common.ErrorHandler;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.AccountPairModel;
import org.kaleta.accountant.frontend.common.Edt;
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
        // the dialogs go to the event thread, the parsing stays here on the worker: building a file
        // chooser from a worker thread deadlocks against its own background directory scan
        File file = Edt.get(this::chooseFile);
        if (file == null) {
            return;
        }
        String type = Edt.get(this::askDocumentType);
        if (type == null) {
            return;
        }

        PdfParserManager manager = new PdfParserManager(file, type);
        try {
            manager.loadContent();
        } catch (Exception e) {
            Edt.run(() -> ErrorHandler.getThrowableDialog(e).setVisible(true));
            return;
        }

        List<PdfTransactionModel> transactions;
        try {
            transactions = manager.getTransactions();
        } catch (Exception e) {
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e) + "\ncontent was:\n" + manager.getContent());
            Edt.run(() -> ErrorHandler.getThrowableDialog(e).setVisible(true));
            return;
        }

        String year = getConfiguration().getSelectedYear();
        Map<String, List<AccountsModel.Account>> allAccountMap = Service.ACCOUNT.getAccountsViaSchemaMap(year);
        List<SchemaModel.Class> classList = Service.SCHEMA.getSchemaClassList(year);
        Map<AccountPairModel, Set<String>> accountPairDescriptionMap = Service.TRANSACTIONS.getAccountPairDescriptions(year);

        Edt.run(() -> showTransactions(transactions, allAccountMap, classList, accountPairDescriptionMap));
    }

    /** Asks for the file to import. Returns null when the user cancels. */
    private File chooseFile() {
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
        return result == JFileChooser.APPROVE_OPTION ? fileChooser.getSelectedFile() : null;
    }

    /** Asks which kind of statement it is. Returns null when the user cancels. */
    private String askDocumentType() {
        return (String) JOptionPane.showInputDialog(
                (Frame) getConfiguration(),
                "Select Document Type",
                "Document Type",
                JOptionPane.PLAIN_MESSAGE,
                null,
                PdfParserManager.getDataTypeOptions(),
                PdfParserManager.getDataTypeOptions()[0]);
    }

    /** Shows the parsed transactions for confirmation, and books the ones that are confirmed. */
    private void showTransactions(List<PdfTransactionModel> transactions,
                                  Map<String, List<AccountsModel.Account>> allAccountMap,
                                  List<SchemaModel.Class> classList,
                                  Map<AccountPairModel, Set<String>> accountPairDescriptionMap) {
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
                dialog.addImportedTransactionPanel(transactionPanel -> {
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
