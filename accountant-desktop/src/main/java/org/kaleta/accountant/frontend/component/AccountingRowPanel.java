package org.kaleta.accountant.frontend.component;

import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.common.Utils;
import org.kaleta.accountant.frontend.common.IconLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormatSymbols;
import java.util.Locale;

public class AccountingRowPanel extends JPanel {
    public static final String HEADER = "HEADER";
    public static final String SUM = "SUM";
    public static final String CLASS = "CLASS";
    public static final String GROUP = "GROUP";
    public static final String ACCOUNT = "ACC";
    public static final String REVENUE = "REVENUE";
    public static final String EXPENSE = "EXPENSE";
    public static final String CF = "CF";

    public static final int VALUE_BALANCE = 0;
    public static final int VALUE_MONTHLY_BALANCE = 1;
    public static final int VALUE_INIT_MONTHLY_BALANCE = 2;

    private String schemaId;
    private String rowType;
    private String title;
    private String initialValue;
    private String[] monthlyBalance;
    private String balance;

    private int rowHeight;
    private Font cellValueFont;
    private Color backgroundColor;

    /**
     * Constructor for the table header
     */
    public AccountingRowPanel(int valuesType) {
        this.schemaId = "X";
        this.rowType = HEADER;
        this.title = "";
        this.initialValue = valuesType == 2 ? "Initial" : null;
        this.monthlyBalance = new DateFormatSymbols(Locale.US).getMonths();
        this.balance = "Total";
        initDesign();
        initComponents();
    }

    /**
     * Constructor for the single final balance
     */
    public AccountingRowPanel(String schemaId, String rowType, String title, Integer balance) {
        this.schemaId = schemaId;
        this.rowType = rowType;
        this.title = title;
        this.initialValue = null;
        this.monthlyBalance = null;
        this.balance = String.valueOf(balance);
        initDesign();
        initComponents();
    }

    /**
     * Constructor for the monthly and final balances.
     */
    public AccountingRowPanel(String schemaId, String rowType, String title, Integer balance, Integer[] monthlyBalance) {
        this.schemaId = schemaId;
        this.rowType = rowType;
        this.title = title;
        this.initialValue = null;
        this.monthlyBalance = monthlyBalance != null ? Utils.IntegerToStringArray(monthlyBalance) : null;
        this.balance = String.valueOf(balance);
        initDesign();
        initComponents();
    }

    /**
     * Constructor for the initial, monthly and final balances.
     */
    public AccountingRowPanel(String schemaId, String rowType, String title, Integer balance, Integer[] monthlyBalance, Integer initialValue) {
        this.schemaId = schemaId;
        this.rowType = rowType;
        this.title = title;
        this.initialValue = initialValue != null ? String.valueOf(initialValue) : null;
        this.monthlyBalance = monthlyBalance != null ? Utils.IntegerToStringArray(monthlyBalance) : null;
        this.balance = String.valueOf(balance);
        initDesign();
        initComponents();
    }

    private void initDesign() {
        switch (rowType) {
            case SUM:
            case CLASS: {
                cellValueFont = new Font(new JLabel().getFont().getName(), Font.BOLD, 25);
                backgroundColor = Color.LIGHT_GRAY.darker();
                rowHeight = 35;
                break;
            }
            case GROUP: {
                cellValueFont = new Font(new JLabel().getFont().getName(), Font.BOLD, 20);
                backgroundColor = Color.LIGHT_GRAY;
                rowHeight = 30;
                break;
            }
            case ACCOUNT: {
                cellValueFont = new Font(new JLabel().getFont().getName(), Font.BOLD, 15);
                backgroundColor = Color.WHITE;
                rowHeight = 25;
                break;
            }
            case REVENUE: {
                cellValueFont = new Font(new JLabel().getFont().getName(), Font.BOLD, 20);
                backgroundColor = Constants.Color.INCOME_GREEN;
                rowHeight = 30;
                break;
            }
            case EXPENSE: {
                cellValueFont = new Font(new JLabel().getFont().getName(), Font.BOLD, 20);
                backgroundColor = Constants.Color.EXPENSE_RED;
                rowHeight = 30;
                break;
            }
            case HEADER: {
                cellValueFont = new Font(new JLabel().getFont().getName(), Font.BOLD, 15);
                backgroundColor = Color.LIGHT_GRAY;
                rowHeight = 20;
                break;
            }
            case CF: {
                cellValueFont = new Font(new JLabel().getFont().getName(), Font.BOLD, 20);
                backgroundColor = Constants.Color.CASH_FLOW_PURPLE;
                rowHeight = 30;
                break;
            }
            default:
                throw new IllegalArgumentException("illegal rowType");
        }
    }

    private void initComponents() {
        this.setBackground(backgroundColor);

        JLabel labelName = new JLabel(" " + title);
        labelName.setFont(cellValueFont);

        JLabel buttonGraph = new JLabel(IconLoader.getIcon(IconLoader.CHART, new Dimension(20, 20)));
        buttonGraph.setOpaque(true);
        buttonGraph.setBackground(backgroundColor);
        buttonGraph.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                // TODO: 6.1.2018 open graph and stuff..
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                buttonGraph.setBackground(backgroundColor.darker());
                buttonGraph.repaint();
                buttonGraph.revalidate();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                buttonGraph.setBackground(backgroundColor);
                buttonGraph.repaint();
                buttonGraph.revalidate();
            }
        });
        if (schemaId.equals("X")) buttonGraph.setVisible(false);

        JPanel panelHeader = new JPanel();
        panelHeader.setLayout(new BoxLayout(panelHeader, BoxLayout.X_AXIS));
        panelHeader.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        panelHeader.setOpaque(false);
        panelHeader.add(labelName);
        panelHeader.add(Box.createHorizontalGlue());
        panelHeader.add(buttonGraph);
        panelHeader.add(Box.createHorizontalStrut(5));

        JPanel panelValues = new JPanel();
        panelValues.setLayout(new BoxLayout(panelValues, BoxLayout.X_AXIS));
        panelValues.setOpaque(false);

        if (initialValue != null) {
            JLabel labelInitBalance = rowType.equals(HEADER)
                    ? new JLabel(initialValue, SwingConstants.CENTER)
                    : new JLabel(initialValue + " ", SwingConstants.RIGHT);
            if (!rowType.equals(HEADER)) labelInitBalance.setToolTipText(initialValue);
            labelInitBalance.setMinimumSize(new Dimension(110, rowHeight));
            labelInitBalance.setPreferredSize(new Dimension(110, rowHeight));
            labelInitBalance.setMaximumSize(new Dimension(110, rowHeight));
            labelInitBalance.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            labelInitBalance.setFont(cellValueFont);
            panelValues.add(labelInitBalance);
        }

        if (monthlyBalance != null) {
            for (int m = 0; m < 12; m++) {
                JLabel labelMonthlyBalance = rowType.equals(HEADER)
                        ? new JLabel(monthlyBalance[m], SwingConstants.CENTER)
                        : new JLabel(monthlyBalance[m] + " ", SwingConstants.RIGHT);
                if (!rowType.equals(HEADER)) labelMonthlyBalance.setToolTipText(monthlyBalance[m]);
                labelMonthlyBalance.setMinimumSize(new Dimension(100, rowHeight));
                labelMonthlyBalance.setPreferredSize(new Dimension(100, rowHeight));
                labelMonthlyBalance.setMaximumSize(new Dimension(100, rowHeight));
                labelMonthlyBalance.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                labelMonthlyBalance.setFont(new Font(cellValueFont.getName(), Font.PLAIN, cellValueFont.getSize()));
                panelValues.add(labelMonthlyBalance);
            }
        }

        JLabel labelFinalBalance = rowType.equals(HEADER)
                ? new JLabel(balance, SwingConstants.CENTER)
                : new JLabel(balance + " ", SwingConstants.RIGHT);
        if (!rowType.equals(HEADER)) labelFinalBalance.setToolTipText(balance);
        labelFinalBalance.setMinimumSize(new Dimension(110, rowHeight));
        labelFinalBalance.setPreferredSize(new Dimension(110, rowHeight));
        labelFinalBalance.setMaximumSize(new Dimension(110, rowHeight));
        labelFinalBalance.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        labelFinalBalance.setFont(cellValueFont);
        panelValues.add(labelFinalBalance);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addComponent(panelHeader, 320, 320, Short.MAX_VALUE)
                .addComponent(panelValues));
        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(panelHeader, rowHeight, rowHeight, rowHeight)
                .addComponent(panelValues, rowHeight, rowHeight, rowHeight));
    }

    public String getSchemaId() {
        return schemaId;
    }

    public String getType() {
        return rowType;
    }
}
