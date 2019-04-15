package org.kaleta.accountant.frontend.core.budgeting;

import org.kaleta.accountant.backend.model.BudgetModel;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class CellPanel extends JPanel {
    private BudgetModel.Row.Month month;
    private String actualValue;

    private JLabel labelBudget;
    private JLabel labelActual;
    private JLabel labelDiff;
    private JTextField textFieldEdit;

    public CellPanel(BudgetModel.Row.Month month, String actualValue) {
        this.month = month;
        this.actualValue = actualValue;
        initComponents();
    }

    private void initComponents(){
        labelBudget = new JLabel(month.getValue());
        this.add(labelBudget);

        if (actualValue != null){
            labelActual = new JLabel(actualValue);
        } else {
            labelActual = new JLabel("-");
        }

        this.add(labelActual);

        if (actualValue != null){
            int diff = Integer.parseInt(month.getValue()) - Integer.parseInt(actualValue);
            labelDiff = new JLabel(((diff >= 0) ? "+ " : "- ") + String.valueOf(Math.abs(diff)));
        } else {
            labelDiff = new JLabel("-");
        }

        this.add(labelDiff);

        textFieldEdit = new JTextField(month.getValue());
        textFieldEdit.setDragEnabled(true);
        textFieldEdit.setTransferHandler(new EditTextFieldTransferHandler());
        textFieldEdit.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == 3) {
                    JTextField c = (JTextField) e.getSource();
                    TransferHandler handler = c.getTransferHandler();
                    handler.exportAsDrag(c, e, TransferHandler.COPY);
                }
            }
        });
        this.add(textFieldEdit);

        this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    }

    public void updateValues(){
        month.setValue(textFieldEdit.getText());
    }

    public void setState(int state){
        switch (state){
            case RowPanel.State.BUDGET:{
                labelBudget.setVisible(true);
                labelActual.setVisible(false);
                labelDiff.setVisible(false);
                textFieldEdit.setVisible(false);
                break;
            }
            case RowPanel.State.ACTUAL:{
                labelBudget.setVisible(false);
                labelActual.setVisible(true);
                labelDiff.setVisible(false);
                textFieldEdit.setVisible(false);
                break;
            }
            case RowPanel.State.DIFF:{
                labelBudget.setVisible(false);
                labelActual.setVisible(false);
                labelDiff.setVisible(true);
                textFieldEdit.setVisible(false);
                break;
            }
            case RowPanel.State.EDIT:{
                labelBudget.setVisible(false);
                labelActual.setVisible(false);
                labelDiff.setVisible(false);
                textFieldEdit.setText(month.getValue());
                textFieldEdit.setVisible(true);
                break;
            }
        }

    }

    private class EditTextFieldTransferHandler extends TransferHandler {
        public int getSourceActions(JComponent c) {
            return COPY;
        }

        public Transferable createTransferable(JComponent c) {
            return new StringSelection(((JTextField) c).getText());
        }

        public void exportDone(JComponent c, Transferable t, int action) {

        }

        public boolean canImport(TransferSupport ts) {
            return ts.getComponent() instanceof JTextField;
        }

        public boolean importData(TransferSupport ts) {
            try {
                ((JTextField) ts.getComponent())
                        .setText((String) ts.getTransferable().getTransferData(DataFlavor.stringFlavor));
                return true;
            } catch (UnsupportedFlavorException | IOException e) {
                return false;
            }
        }
    }
}
