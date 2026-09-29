package org.kaleta.accountant.frontend.dialog;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.ConfigModel;
import org.kaleta.accountant.backend.model.ProceduresModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.menu.OpenAddAssetDialog;
import org.kaleta.accountant.frontend.common.AccountPairModel;
import org.kaleta.accountant.frontend.common.Validable;
import org.kaleta.accountant.frontend.component.DatePickerTextField;
import org.kaleta.accountant.frontend.component.ArrowKeyNavigation;
import org.kaleta.accountant.frontend.component.ProceduresTree;
import org.kaleta.accountant.frontend.component.TransactionPanel;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
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
            book(procedure);
            validateDialog();
        }
        pack();
        this.setSize(new Dimension(this.getWidth() + 500, this.getHeight() + 500));
    }

    private void buildDialogContent() {
        panelTransactions = new JPanel();
        panelTransactions.setLayout(new BoxLayout(panelTransactions, BoxLayout.Y_AXIS));
        // a procedure dragged from the helper lands anywhere on the list of transactions
        panelTransactions.setTransferHandler(new ProcedureDropHandler());
        JScrollPane trPane = new JScrollPane(panelTransactions);
        trPane.setTransferHandler(new ProcedureDropHandler());

        JButton buttonAddTr = new JButton("Add Transaction");
        buttonAddTr.addActionListener(e -> addTransactionPanel());

        JButton buttonAddProcedure = new JButton("Use Procedure");
        buttonAddProcedure.addActionListener(e -> addProcedurePanel());

        JButton buttonAddResource = new JButton("Add Resource");
        buttonAddResource.addActionListener(e -> addResourcePanel());

        // an asset is bought in the middle of booking other things, and the assets tab is a tab away
        JButton buttonAddAsset = new JButton("Add Asset");
        buttonAddAsset.addActionListener(e -> new OpenAddAssetDialog(getConfiguration()).actionPerformed(e));

        JButton buttonShowAccounts = new JButton("DnD Palette");
        buttonShowAccounts.addActionListener(e -> new TransactionHelperDialog(getConfiguration()).setVisible(true));

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
            jPanel.add(buttonAddAsset);
            jPanel.add(buttonShowAccounts);
        });
    }

    public List<TransactionPanel> getTransactionPanelList() {
        return transactionPanelList;
    }

    private void addTransactionPanel(){
        addTransactionPanel(null);
    }

    public void addTransactionPanel(Consumer<TransactionPanel> transactionPanelConsumer){
        TransactionPanel transactionPanel = new TransactionPanel(getConfiguration(), accountPairDescriptionMap, accountMap, classList, this, true);
        // a procedure may be dropped onto a transaction that is already there, not only beside it
        transactionPanel.setTransferHandler(new ProcedureDropHandler());
        ArrowKeyNavigation.install(transactionPanel, () -> transactionPanelList);
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

    /**
     * Adds a row that came from an imported statement. Such a row offers to teach the import what
     * its description means: the account it was booked against, kept as a mapping for next time.
     */
    public void addImportedTransactionPanel(Consumer<TransactionPanel> fill) {
        addImportedTransactionPanel(fill, true);
    }

    /**
     * @param counterSideIsDebit which side the statement left open: the debit for money spent, the
     *                           credit for money received. That is the side a mapping fills in, and
     *                           the side the row can teach the import about
     */
    public void addImportedTransactionPanel(Consumer<TransactionPanel> fill, boolean counterSideIsDebit) {
        addTransactionPanel(panel -> {
            fill.accept(panel);
            panel.importedFromStatement(e -> mapDescriptionOf(panel, counterSideIsDebit),
                    () -> mappingWouldSaySomethingNew(panel, counterSideIsDebit));
        });
    }

    /** Nothing to teach while no account is chosen, or while the import already says the same. */
    private boolean mappingWouldSaySomethingNew(TransactionPanel panel, boolean counterSideIsDebit) {
        String account = counterSideIsDebit ? panel.getDebit() : panel.getCredit();
        if (account == null || account.isEmpty()) {
            return false;
        }
        ConfigModel.Mapping.Entry mapping = Service.CONFIG.getMatchingMapping(panel.getDescription(), counterSideIsDebit);
        return mapping == null || !mapping.getAccount().equals(account);
    }

    private void mapDescriptionOf(TransactionPanel panel, boolean counterSideIsDebit) {
        String description = panel.getDescription();
        String account = counterSideIsDebit ? panel.getDebit() : panel.getCredit();
        ConfigModel.Mapping.Entry mapping = Service.CONFIG.getMatchingMapping(description, counterSideIsDebit);

        MappingDialog dialog = new MappingDialog(getConfiguration(), description,
                mapping == null ? description : mapping.getSubstring(),
                Service.ACCOUNT.getAccountAndGroupName(getConfiguration().getSelectedYear(), account),
                mapping != null);
        dialog.setVisible(true);
        if (!dialog.getResult()) {
            return;
        }

        if (mapping == null) {
            Service.CONFIG.addMapping(dialog.getSubstring(), account, counterSideIsDebit);
        } else {
            Service.CONFIG.updateMapping(mapping.getSubstring(), dialog.getSubstring(), account, counterSideIsDebit);
        }
        panel.mappingSaved();
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
            book(group.getProcedure().get(uiLists.get(pane.getSelectedIndex()).getSelectedIndex()));
        }
    }

    /** Adds one transaction panel per transaction the procedure books, filled in from it. */
    private void book(ProceduresModel.Group.Procedure procedure) {
        for (int i = 0; i < procedure.getTransaction().size(); i++) {
            ProceduresModel.Group.Procedure.Transaction transaction = procedure.getTransaction().get(i);
            int index = i;
            addTransactionPanel(transactionPanel -> {
                transactionPanel.setAmount(transaction.getAmount());
                transactionPanel.setDebitCreditDescription(transaction.getDebit(), transaction.getCredit(), transaction.getDescription());
                transactionPanel.bookedFromProcedure(procedure.getId(), index,
                        e -> updateProcedureFrom(transactionPanel));
            });
        }
    }

    /**
     * Sends what this row now says back to the procedure it was booked from. The correction is made
     * where it was noticed - the amount usually paid has changed, or the account it comes from has -
     * instead of being repeated in the procedure editor afterwards.
     */
    private void updateProcedureFrom(TransactionPanel panel) {
        ProceduresModel.Group.Procedure.Transaction transaction = new ProceduresModel.Group.Procedure.Transaction();
        transaction.setDescription(panel.getDescription());
        transaction.setAmount(panel.getAmount());
        transaction.setDebit(panel.getDebit());
        transaction.setCredit(panel.getCredit());

        Service.PROCEDURES.updateProcedureTransaction(getConfiguration().getSelectedYear(),
                panel.getProcedureId(), panel.getProcedureTransactionIndex(), transaction);
        panel.procedureUpdated();
        getConfiguration().update(Configuration.PROCEDURE_UPDATED);
    }

    /**
     * Accepts a procedure dragged from the helper dialog. Only a procedure: an account dragged onto
     * the list rather than onto one of its fields means nothing, and is refused rather than guessed.
     */
    private class ProcedureDropHandler extends TransferHandler {
        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDataFlavorSupported(DataFlavor.stringFlavor) && procedureId(support) != null;
        }

        @Override
        public boolean importData(TransferSupport support) {
            String id = procedureId(support);
            if (id == null) {
                return false;
            }
            for (ProceduresModel.Group group : Service.PROCEDURES.getProcedureGroupList(getConfiguration().getSelectedYear())) {
                for (ProceduresModel.Group.Procedure procedure : group.getProcedure()) {
                    if (procedure.getId().equals(id)) {
                        book(procedure);
                        return true;
                    }
                }
            }
            return false;
        }

        private String procedureId(TransferSupport support) {
            try {
                String data = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                return data.startsWith(ProceduresTree.PROCEDURE_PREFIX)
                        ? data.substring(ProceduresTree.PROCEDURE_PREFIX.length()) : null;
            } catch (UnsupportedFlavorException | IOException e) {
                return null;
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
