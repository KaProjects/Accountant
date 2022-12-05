package org.kaleta.accountant.frontend.component;

import org.kaleta.accountant.frontend.common.NumberFilter;
import org.kaleta.accountant.frontend.common.Validable;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class HintValidatedTextField extends JTextField implements FocusListener, Validable {
    private boolean showingHint;
    private NumberFilter numberFilter;

    private final String hint;
    private final String label;
    private final boolean isOnlyNumberInput;

    boolean validatorEnabled;

    public HintValidatedTextField(String text, String label, String hint, boolean isOnlyNumberInput, DocumentListener documentListener) {
        super(text);
        this.label = label;
        this.hint = hint;
        validatorEnabled = true;
        this.isOnlyNumberInput = isOnlyNumberInput;
        showingHint = text.trim().isEmpty() && hint != null;
        this.addFocusListener(this);
        this.setToolTipText(label);

        if (isOnlyNumberInput) {
            numberFilter = new NumberFilter();
            ((PlainDocument) this.getDocument()).setDocumentFilter(numberFilter);
            this.setHorizontalAlignment(SwingConstants.RIGHT);
        }
        if (documentListener != null) {
            this.getDocument().addDocumentListener(documentListener);
            this.getDocument().putProperty("owner", this);
        }

        focusLost(null);

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == 1) {
                    HintValidatedTextField c = (HintValidatedTextField) e.getSource();
                    TransferHandler handler = c.getTransferHandler();
                    handler.exportAsDrag(c, e, TransferHandler.COPY);
                }
            }
        });

        this.setDragEnabled(true);
        this.setTransferHandler(new HintValidatedTextFieldTransferHandler());
    }

    public void setValidatorEnabled(boolean enabled){
        this.validatorEnabled = enabled;
    }

    @Override
    public String validator(){
        if (!validatorEnabled) return null;
        return doValidate() ? null : "Value missing at '" + label + "'";
    }

    boolean doValidate() {
        return this.getText() != null && !this.getText().trim().isEmpty();
    }

    @Override
    public void repaint() {
        super.repaint();
        try {
            this.setBackground(doValidate() ? Color.WHITE : Color.getHSBColor(0/360f,0.25f,1));
        } catch (Exception e) {
            // ignore - repaint before full text field initialization
        }
    }


    @Override
    public void focusGained(FocusEvent e) {
        if(hint != null && showingHint) {
            showingHint = false;
            if (isOnlyNumberInput) {
                numberFilter.setSuppressed(false);
                this.setHorizontalAlignment(SwingConstants.RIGHT);
            }
            this.setForeground(Color.BLACK);
            this.setText("");
        }
    }
    @Override
    public void focusLost(FocusEvent e) {
        if(hint != null && !doValidate()) {
            showingHint = true;
            if (isOnlyNumberInput) {
                numberFilter.setSuppressed(true);
                this.setHorizontalAlignment(SwingConstants.LEFT);
            }
            this.setForeground(Color.GRAY);
            this.setText(hint);
        }
    }

    @Override
    public String getText() {
        return (showingHint) ? "" : super.getText();
    }

    private class HintValidatedTextFieldTransferHandler extends TransferHandler {
        public int getSourceActions(JComponent c) {
            return COPY;
        }

        public Transferable createTransferable(JComponent c) {
            return new StringSelection(((HintValidatedTextField) c).getText());
        }

        public void exportDone(JComponent c, Transferable t, int action) {

        }

        public boolean canImport(TransferSupport ts) {
            return ts.getComponent() instanceof HintValidatedTextField;
        }

        public boolean importData(TransferSupport ts) {
            try {
                HintValidatedTextField tf = (HintValidatedTextField) ts.getComponent();
                tf.focusGained(null);
                tf.setText((String) ts.getTransferable().getTransferData(DataFlavor.stringFlavor));

                return true;
            } catch (UnsupportedFlavorException | IOException e) {
                return false;
            }
        }
    }
}
