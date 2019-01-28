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
                if (e.getButton() != 3) {
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
                if (e.getButton() == 3) {
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
        System.out.println("GET " + selectedAccount);
        return selectedAccount;
    }

    public void setSelectedAccount(String selectedAccount) {
        System.out.println("SET " + selectedAccount);
        this.selectedAccount = selectedAccount;
        this.setText(Service.ACCOUNT.getAccountAndGroupName(configuration.getSelectedYear(), selectedAccount));
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

        public boolean canImport(TransferSupport ts) {
            return ts.getComponent() instanceof SelectAccountTextField;
        }

        public boolean importData(TransferSupport ts) {
            try {
                ((SelectAccountTextField) ts.getComponent())
                        .setSelectedAccount((String) ts.getTransferable().getTransferData(DataFlavor.stringFlavor));
                return true;
            } catch (UnsupportedFlavorException | IOException e) {
                return false;
            }
        }
    }
}
