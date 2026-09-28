package org.kaleta.accountant.frontend.component;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.Validable;
import org.kaleta.accountant.frontend.dialog.SelectAccountDialog;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SelectAccountTextField extends JTextField implements Validable {
    private final Configuration configuration;

    private final String label;
    private String selectedAccount;

    private boolean validatorEnabled;

    public SelectAccountTextField(Configuration configuration, Map<String, List<AccountsModel.Account>> accountMap, List<SchemaModel.Class> classes,
                                  String label, DocumentListener documentListener) {
        this.configuration = configuration;
        this.label = label;
        init(accountMap, classes, documentListener);
    }

    public SelectAccountTextField(Configuration configuration, Map<String, List<AccountsModel.Account>> accountMap, SchemaModel.Class clazz,
                                  String label, DocumentListener documentListener) {
        this.configuration = configuration;
        this.label = label;
        List<SchemaModel.Class> classes = new ArrayList<>();
        classes.add(clazz);
        init(accountMap, classes, documentListener);
    }

    private void init(Map<String, List<AccountsModel.Account>> accountMap, List<SchemaModel.Class> classes, DocumentListener documentListener) {
        if (documentListener != null) {
            this.getDocument().addDocumentListener(documentListener);
            this.getDocument().putProperty("owner", this);
        }
        validatorEnabled = true;
        selectedAccount = "";
        this.setText(" - - Click to Select - - ");
        this.setForeground(Color.GRAY);
        this.setEditable(false);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == 3) {
                    SelectAccountDialog selectExpenseAccountDialog = new SelectAccountDialog(configuration, accountMap, classes);
                    selectExpenseAccountDialog.setVisible(true);
                    if (selectExpenseAccountDialog.getResult()) {
                        selectedAccount = selectExpenseAccountDialog.getSelectedAccountId();
                        SelectAccountTextField.this.setText(selectExpenseAccountDialog.getSelectedAccountName());
                        SelectAccountTextField.this.setForeground(new JTextField().getForeground());
                        SelectAccountTextField.this.getParent().revalidate();
                        SelectAccountTextField.this.getParent().repaint();
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == 1) {
                    SelectAccountTextField c = (SelectAccountTextField) e.getSource();
                    TransferHandler handler = c.getTransferHandler();
                    handler.exportAsDrag(c, e, TransferHandler.COPY);
                }
            }
        });
        this.setDragEnabled(true);
        this.setTransferHandler(new SelectAccountTextFieldTransferHandler());
    }

    public void setValidatorEnabled(boolean enabled) {
        this.validatorEnabled = enabled;
    }

    public String getSelectedAccount() {
        return selectedAccount;
    }

    public void setSelectedAccount(String selectedAccount) {
        // the name is resolved first: if the id is not one, the field keeps the account it had
        // instead of being left holding an id that never appears in its text
        String name = Service.ACCOUNT.getAccountAndGroupName(configuration.getSelectedYear(), selectedAccount);
        this.selectedAccount = selectedAccount;
        this.setText(name);
    }

    @Override
    public String validator() {
        if (!validatorEnabled) return null;
        return selectedAccount.isEmpty() ? "No account selected at '" + label + "'" : null;
    }

    private class SelectAccountTextFieldTransferHandler extends TransferHandler {
        public int getSourceActions(JComponent c) {
            return COPY;
        }

        public Transferable createTransferable(JComponent c) {
            return new StringSelection(((SelectAccountTextField) c).getSelectedAccount());
        }

        public void exportDone(JComponent c, Transferable t, int action) {

        }

        /**
         * Only an id of an account that actually exists may be dropped here.
         * <p>
         * Checking that the target is one of these fields says nothing about what is being dropped:
         * any text at all was accepted and kept as the selected account. Dragging the amount out of
         * the amount field next door - which is draggable - therefore set the amount as the debit,
         * silently, because the field went on showing the account it had while quietly holding a
         * number, and the validator only asked whether something was selected at all.
         */
        public boolean canImport(TransferSupport ts) {
            return ts.getComponent() instanceof SelectAccountTextField && isAccountId(droppedText(ts));
        }

        public boolean importData(TransferSupport ts) {
            String accountId = droppedText(ts);
            if (!isAccountId(accountId)) {
                return false;
            }
            ((SelectAccountTextField) ts.getComponent()).setSelectedAccount(accountId);
            return true;
        }

        private String droppedText(TransferSupport ts) {
            if (!ts.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return null;
            }
            try {
                return (String) ts.getTransferable().getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException | IOException e) {
                return null;
            }
        }

        private boolean isAccountId(String text) {
            return text != null
                    && text.matches("\\d{3}\\.[\\w-]+")
                    && Service.ACCOUNT.checkAccountExists(configuration.getSelectedYear(), text);
        }
    }
}
