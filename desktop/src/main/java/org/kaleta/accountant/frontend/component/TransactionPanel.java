package org.kaleta.accountant.frontend.component;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.ProceduresModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.AccountPairModel;
import org.kaleta.accountant.frontend.common.IconLoader;
import org.kaleta.accountant.frontend.common.SwingWorkerHandler;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TransactionPanel extends JPanel implements DocumentListener {
    /** The tint of a row a procedure was recognised for, and filled in. */
    private static final Color BOOKED_BY_PROCEDURE = new Color(0xE4, 0xF3, 0xE4);

    private final Object lock = new Object();
    private final Map<AccountPairModel, Set<String>> accountPairDescriptionMap;

    private final JButton buttonDelete;
    private final JButton buttonUpdateProcedure;
    private final JButton buttonMapping;
    private final DatePickerTextField tfDate;
    private final JComboBox<String> cbDescription;
    private final HintValidatedTextField tfAmount;
    private final SelectAccountTextField tfDebit;
    private final SelectAccountTextField tfCredit;

    private boolean isSuppressedUpdate = false;

    private String procedureId;
    private int procedureTransactionIndex = -1;
    private String[] asProcedureHasIt;
    private java.util.function.BooleanSupplier mappingWorthSaving;

    public TransactionPanel(Configuration configuration, Map<AccountPairModel, Set<String>> accountPairDescriptionMap,
                            Map<String, List<AccountsModel.Account>> accountMap, List<SchemaModel.Class> classList,
                            DocumentListener documentListener, boolean withDate) {
        this.accountPairDescriptionMap = accountPairDescriptionMap;

        tfDate = new DatePickerTextField("", documentListener, configuration.getSelectedYear());
        if (!withDate){
            tfDate.setVisible(false);
            tfDate.setValidatorEnabled(false);
        }
        cbDescription = new JComboBox<>();
        cbDescription.setEditable(true);

        tfAmount = new HintValidatedTextField("","Transaction Amount", "set amount", true, documentListener);
        tfDebit = new SelectAccountTextField(configuration, accountMap, classList, "Debit",documentListener);
        tfDebit.getDocument().addDocumentListener(this);
        tfCredit = new SelectAccountTextField(configuration, accountMap, classList,"Credit", documentListener);
        tfCredit.getDocument().addDocumentListener(this);

        buttonDelete = new JButton(IconLoader.getIcon(IconLoader.DELETE, new Dimension(10, 10)));
        buttonDelete.setEnabled(false);

        // shown only on a row that came from a procedure, and only worth pressing once that row
        // says something the procedure does not
        buttonUpdateProcedure = new JButton(IconLoader.getIcon(IconLoader.EDIT, new Dimension(10, 10)));
        buttonUpdateProcedure.setToolTipText("Update the procedure with these values");
        buttonUpdateProcedure.setEnabled(false);
        buttonUpdateProcedure.setVisible(false);
        // shown only on a row that came from an imported statement, and only worth pressing once
        // the import would fill that description in differently next time
        buttonMapping = new JButton(IconLoader.getIcon(IconLoader.ADD, new Dimension(10, 10)));
        buttonMapping.setToolTipText("Teach the import what this description is");
        buttonMapping.setEnabled(false);
        buttonMapping.setVisible(false);

        DocumentListener watchesForChanges = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                refreshRowButtons();
            }

            public void removeUpdate(DocumentEvent e) {
                refreshRowButtons();
            }

            public void changedUpdate(DocumentEvent e) {
                refreshRowButtons();
            }
        };
        tfAmount.getDocument().addDocumentListener(watchesForChanges);
        tfDebit.getDocument().addDocumentListener(watchesForChanges);
        tfCredit.getDocument().addDocumentListener(watchesForChanges);
        ((JTextField) cbDescription.getEditor().getEditorComponent()).getDocument().addDocumentListener(watchesForChanges);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addComponent(buttonDelete,10,10,10)
                .addComponent(tfDate,50,50,50)
                .addGap(5)
                .addComponent(tfAmount,75,75,75)
                .addGap(5)
                .addComponent(tfDebit)
                .addGap(5)
                .addComponent(tfCredit)
                .addGap(5)
                .addComponent(cbDescription,200,200,Short.MAX_VALUE)
                .addComponent(buttonUpdateProcedure,20,20,20)
                .addComponent(buttonMapping,20,20,20));
        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(buttonDelete,25,25,25)
                .addComponent(tfDate,25,25,25)
                .addComponent(tfAmount,25,25,25)
                .addComponent(tfDebit,25,25,25)
                .addComponent(tfCredit,25,25,25)
                .addComponent(cbDescription,25,25,25)
                .addComponent(buttonUpdateProcedure,25,25,25)
                .addComponent(buttonMapping,25,25,25));
    }

    /**
     * Records that this row was booked from a procedure, and offers to send a correction back to it.
     * <p>
     * The date is left out of the comparison on purpose: a procedure says what is booked, not when,
     * so entering today's date is not a change to it.
     *
     * @param asProcedureHasIt the procedure's own transaction, which the row is compared against
     * @param onUpdate         what to run when the user asks for the procedure to be brought up to date
     */
    public void bookedFromProcedure(String procedureId, int transactionIndex,
                                    ProceduresModel.Group.Procedure.Transaction asProcedureHasIt, ActionListener onUpdate) {
        this.procedureId = procedureId;
        this.procedureTransactionIndex = transactionIndex;
        // what the procedure says, not what the row says: an imported movement carries the amount
        // that was really paid, and that is exactly the difference worth sending back
        this.asProcedureHasIt = new String[]{asProcedureHasIt.getAmount(), asProcedureHasIt.getDebit(),
                asProcedureHasIt.getCredit(), asProcedureHasIt.getDescription()};
        buttonUpdateProcedure.setVisible(true);
        buttonUpdateProcedure.addActionListener(onUpdate);
        refreshRowButtons();
    }

    /**
     * Marks a row that an imported movement was recognised as: the booking came from a procedure,
     * not from the statement, and it is tinted so that what was filled in for the user stands out
     * from what the statement actually said.
     */
    public void highlightAsBookedByProcedure(String procedureName) {
        setOpaque(true);
        setBackground(BOOKED_BY_PROCEDURE);
        setToolTipText("Booked by the procedure '" + procedureName + "'");
    }

    /**
     * Records that this row came from an imported statement, and offers to teach the import what
     * its description means - which account it is booked against - or to correct what it already
     * thinks it means.
     *
     * @param worthSaving whether the mapping would say something the import does not say already
     */
    public void importedFromStatement(ActionListener onMap, java.util.function.BooleanSupplier worthSaving) {
        this.mappingWorthSaving = worthSaving;
        buttonMapping.setVisible(true);
        buttonMapping.addActionListener(onMap);
        refreshRowButtons();
    }

    /** Called once the mapping has been saved, so the button settles back down. */
    public void mappingSaved() {
        refreshRowButtons();
    }

    /** Called once the procedure has been brought up to date: this row is now what it says. */
    public void procedureUpdated() {
        this.asProcedureHasIt = values();
        refreshRowButtons();
    }

    public String getProcedureId() {
        return procedureId;
    }

    public int getProcedureTransactionIndex() {
        return procedureTransactionIndex;
    }

    private String[] values() {
        return new String[]{getAmount(), getDebit(), getCredit(), getDescription()};
    }

    private void refreshRowButtons() {
        if (asProcedureHasIt != null) {
            buttonUpdateProcedure.setEnabled(!java.util.Arrays.equals(asProcedureHasIt, values()));
        }
        if (mappingWorthSaving != null) {
            buttonMapping.setEnabled(mappingWorthSaving.getAsBoolean());
        }
    }

    public void disableValidators(){
        tfAmount.setValidatorEnabled(false);
        tfDebit.setValidatorEnabled(false);
        tfCredit.setValidatorEnabled(false);
        tfDate.setValidatorEnabled(false);
    }

    public void addDeleteAction(ActionListener action){
        buttonDelete.addActionListener(action);
        buttonDelete.setEnabled(true);
    }

    public String getDate(){
        return tfDate.getText();
    }

    public String getAmount(){
        return tfAmount.getText();
    }

    public String getDebit(){
        return tfDebit.getSelectedAccount();
    }

    public String getCredit(){
        return tfCredit.getSelectedAccount();
    }

    /**
     * The row's fields from left to right, which is the order the arrow keys move through them. The
     * description is reached through the combo box's editor, since that is what takes the focus.
     */
    public List<JComponent> navigableFields() {
        return List.of(tfDate, tfAmount, tfDebit, tfCredit, (JComponent) cbDescription.getEditor().getEditorComponent());
    }

    public String getDescription(){
        return ((JTextField)cbDescription.getEditor().getEditorComponent()).getText();
    }

    public void setDate(String date){
        tfDate.focusGained(null);
        tfDate.setText(date);
    }

    public void setAmount(String amount){
        tfAmount.focusGained(null);
        tfAmount.setText(amount);
    }

    public void setDebit(String debit){
        tfDebit.setSelectedAccount(debit);
    }

    public void setCredit(String credit){
        tfCredit.setSelectedAccount(credit);
    }

    public void setDescription(String description){
        ((JTextField)cbDescription.getEditor().getEditorComponent()).setText(description);
    }

    public void setDebitCreditDescription(String debit, String credit, String description) {
        isSuppressedUpdate = true;
        setDebit(debit);
        setCredit(credit);
        updateDescriptions();
        setDescription(description);
        isSuppressedUpdate = false;
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        new SwingWorkerHandler() {
            @Override
            protected void runInBackground() {
                if (!isSuppressedUpdate) updateDescriptions();
            }
        }.execute();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        new SwingWorkerHandler() {
            @Override
            protected void runInBackground() {
                if (!isSuppressedUpdate) updateDescriptions();
            }
        }.execute();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        new SwingWorkerHandler() {
            @Override
            protected void runInBackground() {
                if (!isSuppressedUpdate) updateDescriptions();
            }
        }.execute();
    }

    private void updateDescriptions() {
        synchronized(lock) {
            String debit = tfDebit.getSelectedAccount();
            String credit = tfCredit.getSelectedAccount();
            if (!debit.trim().isEmpty() && !credit.trim().isEmpty()) {
                Set<String> descList = accountPairDescriptionMap.get(new AccountPairModel(debit, credit));
                if (descList != null) {
                    String cbValue = ((JTextField)cbDescription.getEditor().getEditorComponent()).getText();
                    DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) cbDescription.getModel();
                    model.removeAllElements();
                    descList.forEach(model::addElement);
                    ((JTextField)cbDescription.getEditor().getEditorComponent()).setText(cbValue);
                }
            }
        }
    }
}
