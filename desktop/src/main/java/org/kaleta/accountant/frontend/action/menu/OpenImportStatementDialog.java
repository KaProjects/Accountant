package org.kaleta.accountant.frontend.action.menu;

import org.kaleta.accountant.backend.manager.StatementParserManager;
import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.ConfigModel;
import org.kaleta.accountant.backend.model.StatementTransactionModel;
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

public class OpenImportStatementDialog extends MenuAction {

    public OpenImportStatementDialog(Configuration config) {
        super(config, "CSV Transaction(s)");
    }

    @Override
    protected void actionPerformed() {
        // the dialogs go to the event thread, the parsing stays here on the worker: building a file
        // chooser from a worker thread deadlocks against its own background directory scan
        File file = Edt.get(this::chooseFile);
        if (file == null) {
            return;
        }
        ConfigModel.Imports.Source source = Edt.get(this::askWhichStatement);
        if (source == null) {
            return;
        }

        StatementParserManager manager = new StatementParserManager(file, source.getFormat(), source.getAccount());
        try {
            manager.loadContent();
        } catch (Exception e) {
            Edt.run(() -> ErrorHandler.getThrowableDialog(e).setVisible(true));
            return;
        }

        List<StatementTransactionModel> transactions;
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
                return extension != null && extension.equals("csv");
            }

            @Override
            public String getDescription() {
                return "CSV files";
            }
        });

        int result = fileChooser.showOpenDialog((Frame) getConfiguration());
        return result == JFileChooser.APPROVE_OPTION ? fileChooser.getSelectedFile() : null;
    }

    /**
     * Asks which statement this is, out of the ones configured. Returns null when the user cancels,
     * and says so when none are configured - the app reads formats, the configuration says whose.
     */
    private ConfigModel.Imports.Source askWhichStatement() {
        List<ConfigModel.Imports.Source> sources = Service.CONFIG.getImportSources();
        if (sources.isEmpty()) {
            JOptionPane.showMessageDialog((Frame) getConfiguration(),
                    "No statements are configured to import.", "Importing", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String[] names = sources.stream().map(ConfigModel.Imports.Source::getName).toArray(String[]::new);
        String chosen = (String) JOptionPane.showInputDialog(
                (Frame) getConfiguration(),
                "Select Statement",
                "Importing",
                JOptionPane.PLAIN_MESSAGE,
                null,
                names,
                names[0]);
        return sources.stream().filter(source -> source.getName().equals(chosen)).findFirst().orElse(null);
    }

    /**
     * Books the movement the way a procedure says: every transaction it holds, not only the one that
     * was recognised, because those go together. The amount is the statement's for the transaction
     * that matched - that is the one that really happened - and the procedure's for the rest, which
     * the statement says nothing about.
     */
    private void book(AddTransactionDialog dialog, StatementTransactionModel transaction,
                      ProceduresModel.Group.Procedure procedure) {
        for (int i = 0; i < procedure.getTransaction().size(); i++) {
            ProceduresModel.Group.Procedure.Transaction booked = procedure.getTransaction().get(i);
            boolean isTheOneThatHappened = transaction.getDebit().equals(booked.getDebit())
                    && transaction.getCredit().equals(booked.getCredit());
            String amount = isTheOneThatHappened ? transaction.getAmount() : booked.getAmount();
            // linked to the procedure like any other row booked from one: what the statement shows
            // is what really happened, and a procedure that no longer says so can be corrected here
            dialog.addProcedureTransactionPanel(procedure, i, panel -> {
                if (transaction.getDate() != null) {
                    panel.setDate(transaction.getDate());
                }
                panel.setAmount(amount);
                panel.setDebitCreditDescription(booked.getDebit(), booked.getCredit(), booked.getDescription());
                panel.highlightAsBookedByProcedure(procedure.getName());
            });
        }
    }

    /** Shows the parsed transactions for confirmation, and books the ones that are confirmed. */
    private void showTransactions(List<StatementTransactionModel> transactions,
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
            for (StatementTransactionModel transactionModel : transactions) {
                ProceduresModel.Group.Procedure procedure = Service.PROCEDURES.getProcedureFor(
                        getConfiguration().getSelectedYear(), transactionModel.getDebit(), transactionModel.getCredit());
                if (procedure != null) {
                    book(dialog, transactionModel, procedure);
                    continue;
                }
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
                }, transactionModel.isCounterSideDebit());
            }
            dialog.setVisible(true);
    }
}
