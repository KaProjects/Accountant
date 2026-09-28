package org.kaleta.accountant.frontend.component;

import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.common.Utils;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.IconLoader;
import org.kaleta.accountant.frontend.core.accounting.AccountAggregate;
import org.kaleta.accountant.frontend.dialog.AccountingChartDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormatSymbols;
import java.util.Locale;

public class AccountingRowPanel extends JPanel implements Configurable, TableRow {
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

    private AccountAggregate aggregate;
    private String rowType;
    private String title;
    private String initialValue;
    private String[] monthlyBalance;
    private String balance;

    /** Width every value cell asks for; they all share it, and they grow together beyond it. */
    private static final int VALUE_COLUMN_WIDTH = 110;

    private int rowHeight;
    private Font cellValueFont;
    private Color backgroundColor;

    private JPanel panelHeader;
    private JLabel buttonGraph;
    private int titleWidth;

    private Configuration configuration;

    /**
     * Constructor for the table header
     */
    public AccountingRowPanel(int valuesType) {
        this.aggregate = null;
        this.rowType = HEADER;
        this.title = "";
        this.initialValue = valuesType == 2 ? "Initial" : null;
        this.monthlyBalance = valuesType > 0 ? new DateFormatSymbols(Locale.US).getMonths() : null;
        this.balance = "Total";
        initDesign();
        initComponents();
    }

    /**
     * Constructor for positive aggregate.
     */
    public AccountingRowPanel(Configuration configuration, AccountAggregate aggregate, String rowType, int valuesType) {
        setConfiguration(configuration);
        this.aggregate = aggregate;
        this.rowType = rowType;
        this.title = aggregate.getName();
        this.initialValue = valuesType > 1 ? String.valueOf(aggregate.getInitialValue(configuration.getSelectedYear())) : null;
        this.monthlyBalance = valuesType > 0 ? Utils.IntegerToStringArray(aggregate.getMonthlyBalance(configuration.getSelectedYear())) : null;
        this.balance = String.valueOf(aggregate.getBalance(configuration.getSelectedYear()));
        initDesign();
        initComponents();
    }

    /**
     * Constructor for aggregate.
     */
    public AccountingRowPanel(Configuration configuration, AccountAggregate aggregate, String rowType, int valuesType, boolean isPositive) {
        setConfiguration(configuration);
        this.aggregate = aggregate;
        this.rowType = rowType;
        this.title = aggregate.getName();

        int sign = isPositive ? 1 : -1;
        this.initialValue = valuesType > 1 ? String.valueOf(sign * aggregate.getInitialValue(configuration.getSelectedYear())) : null;
        this.monthlyBalance = valuesType > 0 ? Utils.IntegerToStringArray(Utils.multiplyArrayValues(aggregate.getMonthlyBalance(configuration.getSelectedYear()), sign)) : null;
        this.balance = String.valueOf(sign * aggregate.getBalance(configuration.getSelectedYear()));
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

        buttonGraph = new JLabel(IconLoader.getIcon(IconLoader.CHART, new Dimension(20, 20)));
        buttonGraph.setOpaque(true);
        buttonGraph.setBackground(backgroundColor);
        buttonGraph.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                new AccountingChartDialog(getConfiguration(), aggregate).setVisible(true);
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
        if (aggregate == null) buttonGraph.setVisible(false);

        panelHeader = new JPanel();
        panelHeader.setLayout(new BoxLayout(panelHeader, BoxLayout.X_AXIS));
        panelHeader.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        panelHeader.setOpaque(false);
        panelHeader.add(labelName);
        // the column is only as wide as the longest title, so without this the icon would touch it
        panelHeader.add(Box.createHorizontalStrut(10));
        panelHeader.add(Box.createHorizontalGlue());
        panelHeader.add(buttonGraph);
        panelHeader.add(Box.createHorizontalStrut(5));
        // what this row alone would need; the overview widens every row to the longest of them
        titleWidth = panelHeader.getPreferredSize().width;

        // a grid, not a box: every value cell keeps exactly the same width as the others and they
        // share the space the name column does not take
        JPanel panelValues = new JPanel();
        panelValues.setLayout(new GridLayout(1, 0));
        panelValues.setOpaque(false);

        if (initialValue != null) {
            JLabel labelInitBalance = rowType.equals(HEADER)
                    ? new JLabel(initialValue, SwingConstants.CENTER)
                    : new JLabel(initialValue + " ", SwingConstants.RIGHT);
            if (!rowType.equals(HEADER)) labelInitBalance.setToolTipText(initialValue);
            labelInitBalance.setPreferredSize(new Dimension(VALUE_COLUMN_WIDTH, rowHeight));
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
                labelMonthlyBalance.setPreferredSize(new Dimension(VALUE_COLUMN_WIDTH, rowHeight));
                labelMonthlyBalance.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                labelMonthlyBalance.setFont(new Font(cellValueFont.getName(), Font.PLAIN, cellValueFont.getSize()));
                panelValues.add(labelMonthlyBalance);
            }
        }

        JLabel labelFinalBalance = rowType.equals(HEADER)
                ? new JLabel(balance, SwingConstants.CENTER)
                : new JLabel(balance + " ", SwingConstants.RIGHT);
        if (!rowType.equals(HEADER)) labelFinalBalance.setToolTipText(balance);
        labelFinalBalance.setPreferredSize(new Dimension(VALUE_COLUMN_WIDTH, rowHeight));
        labelFinalBalance.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        labelFinalBalance.setFont(cellValueFont);
        panelValues.add(labelFinalBalance);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addComponent(panelHeader, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(panelValues, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(panelHeader, rowHeight, rowHeight, rowHeight)
                .addComponent(panelValues, rowHeight, rowHeight, rowHeight));
    }

    public String getType() {
        return rowType;
    }

    /**
     * Registers a listener that fires wherever the row is clicked, the chart icon aside.
     * <p>
     * Adding it to the row alone is not enough: a click is delivered to the innermost component
     * that is listening, and every value cell has a tooltip, which quietly makes it one. So the
     * listener goes on the cells too, and only the chart icon is left with its own click.
     */
    @Override
    public void addRowMouseListener(java.awt.event.MouseListener listener) {
        attachToRow(this, listener);
    }

    private void attachToRow(Component component, java.awt.event.MouseListener listener) {
        if (component == buttonGraph) {
            return;
        }
        component.addMouseListener(listener);
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                attachToRow(child, listener);
            }
        }
    }

    /** Width this row needs for its own title and chart icon, before any alignment. */
    @Override
    public int getTitleWidth() {
        return titleWidth;
    }

    /**
     * Pins the name column, so that every row of a table lines up and the longest title still fits.
     * Whatever is left over goes to the value cells.
     */
    @Override
    public void setNameColumnWidth(int width) {
        Dimension size = new Dimension(width, rowHeight);
        panelHeader.setMinimumSize(size);
        panelHeader.setPreferredSize(size);
        panelHeader.setMaximumSize(size);
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }
}
