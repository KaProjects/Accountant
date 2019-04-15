package org.kaleta.accountant.frontend.core.budgeting;

import org.kaleta.accountant.backend.model.BudgetModel;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.IconLoader;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RowPanel extends JPanel {
    private Configuration configuration;
    private BudgetModel.Row row;
    private Map<String, String> actualValues;
    private List<CellPanel> cellPanelList;

    private JLabel buttonEdit;
    private JLabel buttonConfirm;
    private JLabel buttonDiscard;

    private JPanel panelMonths;

    public static class State {
        public static final int BUDGET = 0;
        public static final int ACTUAL = 1;
        public static final int DIFF = 2;
        public static final int EDIT = 3;
    }

    public RowPanel(Configuration configuration, BudgetModel.Row row, Map<String, String> actualValues){
        this.configuration = configuration;
        this.row = row;
        this.actualValues = actualValues;
        cellPanelList = new ArrayList<>();
        initComponents();

        for (BudgetModel.Row.Month month : row.getMonth()){
            CellPanel panel = new CellPanel(month, actualValues.get(month.getNumber()));
            panelMonths.add(panel);
            cellPanelList.add(panel);
        }

        setState(State.ACTUAL);
    }

    /**
     * sum panel constructor
     */
    public RowPanel(String type, String name){
        row = new BudgetModel.Row("-", type, name, new ArrayList<>());
        cellPanelList = new ArrayList<>();
        initComponents();
        buttonEdit.setEnabled(false);
        setState(State.ACTUAL);
    }

    public void updateSumPanel(List<BudgetModel.Row> rowList, Map<String, String> actualValues){
        this.actualValues = actualValues;

        List<BudgetModel.Row.Month> monthList = new ArrayList<>();
        for (int m=1;m<=12;m++){
            BudgetModel.Row.Month month = new BudgetModel.Row.Month();
            month.setNumber(String.valueOf(m));
            int sum = 0;
            for (BudgetModel.Row row : rowList){
                sum += Integer.parseInt(row.getMonth().get(m - 1).getValue());
            }
            month.setValue(String.valueOf(sum));
            monthList.add(month);
        }
        row.getMonth().clear();
        row.getMonth().addAll(monthList);

        cellPanelList.clear();
        panelMonths.removeAll();
        for (BudgetModel.Row.Month month : row.getMonth()){
            CellPanel panel = new CellPanel(month, actualValues.get(month.getNumber()));
            panel.setState(State.ACTUAL);
            panelMonths.add(panel);
            cellPanelList.add(panel);
        }
    }

    private void  initComponents(){
        int rowHeight = 25;

        JLabel labelName = new JLabel(" " + row.getName());
        labelName.setFont(new Font(labelName.getFont().getName(), Font.BOLD, 15));

        buttonEdit = new JLabel(IconLoader.getIcon(IconLoader.EDIT, new Dimension(20, 20)));
        buttonConfirm = new JLabel(IconLoader.getIcon(IconLoader.SAVE_CHANGES, new Dimension(20, 20)));
        buttonDiscard = new JLabel(IconLoader.getIcon(IconLoader.DISCARD_CHANGES, new Dimension(20, 20)));

        buttonEdit.setOpaque(true);
        buttonEdit.setBackground(this.getBackground());
        buttonEdit.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                setState(State.EDIT);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                buttonEdit.setBackground(RowPanel.this.getBackground().darker());
                buttonEdit.repaint();
                buttonEdit.revalidate();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                buttonEdit.setBackground(RowPanel.this.getBackground());
                buttonEdit.repaint();
                buttonEdit.revalidate();
            }
        });

        buttonConfirm.setOpaque(true);
        buttonConfirm.setBackground(this.getBackground());
        buttonConfirm.setVisible(false);
        buttonConfirm.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                for (CellPanel panel : cellPanelList){
                    panel.updateValues();
                }
                Service.BUDGET.updateRowMonth(configuration.getSelectedYear(), row);
                configuration.update(Configuration.BUDGET_UPDATED);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                buttonConfirm.setBackground(RowPanel.this.getBackground().darker());
                buttonConfirm.repaint();
                buttonConfirm.revalidate();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                buttonConfirm.setBackground(RowPanel.this.getBackground());
                buttonConfirm.repaint();
                buttonConfirm.revalidate();
            }
        });

        buttonDiscard.setOpaque(true);
        buttonDiscard.setBackground(this.getBackground());
        buttonDiscard.setVisible(false);
        buttonDiscard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                setState(State.BUDGET);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                buttonDiscard.setBackground(RowPanel.this.getBackground().darker());
                buttonDiscard.repaint();
                buttonDiscard.revalidate();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                buttonDiscard.setBackground(RowPanel.this.getBackground());
                buttonDiscard.repaint();
                buttonDiscard.revalidate();
            }
        });

        JPanel panelHeader = new JPanel();
        panelHeader.setLayout(new BoxLayout(panelHeader, BoxLayout.X_AXIS));
        panelHeader.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        panelHeader.setOpaque(false);
        panelHeader.add(labelName);
        panelHeader.add(Box.createHorizontalGlue());
        panelHeader.add(buttonEdit);
        panelHeader.add(buttonConfirm);
        panelHeader.add(buttonDiscard);
        panelHeader.add(Box.createHorizontalStrut(5));

        panelMonths = new JPanel();
        panelMonths.setLayout(new BoxLayout(panelMonths, BoxLayout.X_AXIS));
        panelMonths.setOpaque(false);


        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addComponent(panelHeader, 320, 320, 320)
                .addComponent(panelMonths));
        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(panelHeader, rowHeight, rowHeight, rowHeight)
                .addComponent(panelMonths, rowHeight, rowHeight, rowHeight));
    }

    public void setState(int state) {
        for (CellPanel panel : cellPanelList){
            panel.setState(state);
        }
        switch (state){
            case State.BUDGET:{
                if (buttonEdit.isEnabled()) buttonEdit.setVisible(true);
                buttonConfirm.setVisible(false);
                buttonDiscard.setVisible(false);
                break;
            }
            case State.ACTUAL:{
                buttonEdit.setVisible(false);
                buttonConfirm.setVisible(false);
                buttonDiscard.setVisible(false);
                break;
            }
            case State.DIFF:{
                buttonEdit.setVisible(false);
                buttonConfirm.setVisible(false);
                buttonDiscard.setVisible(false);
                break;
            }
            case State.EDIT:{
                buttonEdit.setVisible(false);
                buttonConfirm.setVisible(true);
                buttonDiscard.setVisible(true);
                break;
            }
        }
    }


}
