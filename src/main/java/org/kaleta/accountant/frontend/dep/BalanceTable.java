package org.kaleta.accountant.frontend.dep;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.InitConfigurableAction;
import org.kaleta.accountant.frontend.action.configuration.BalanceTableTransactionsUpdated;
import org.kaleta.accountant.frontend.action.mouse.BalanceTableClicked;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;

/**
 * Created by Stanislav Kaleta on 03.08.2016.
 */
public class BalanceTable extends JTable implements Configurable {
    private Configuration configuration;

    public BalanceTable() {
        initComponents();
    }

    private void initComponents() {
        BalanceTableModel model = new BalanceTableModel();
        this.setModel(model);
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.setRowSelectionAllowed(true);
        this.setColumnSelectionAllowed(true);
        this.getTableHeader().setReorderingAllowed(false);
        this.setPreferredScrollableViewportSize(new Dimension(1200,800));
        this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        this.setRowHeight(30);

        TableColumnModel columnModel = this.getColumnModel();
        columnModel.getColumn(0).setMinWidth(320);
        columnModel.getColumn(1).setMinWidth(140);
        columnModel.getColumn(1).setMaxWidth(140);
        columnModel.getColumn(2).setMinWidth(140);
        columnModel.getColumn(2).setMaxWidth(140);

        columnModel.getColumn(3).setMinWidth(320);
        columnModel.getColumn(4).setMinWidth(140);
        columnModel.getColumn(4).setMaxWidth(140);
        columnModel.getColumn(5).setMinWidth(140);
        columnModel.getColumn(5).setMaxWidth(140);

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String type = ((BalanceTableModel) table.getModel()).getCellType(row, column);
                if (type.equals("")){
                    c.setBackground(null);
                }
                if (type.equals(BalanceRow.SUM) || type.equals(BalanceRow.CLASS)){
                    c.setFont(new Font(c.getFont().getName(), Font.BOLD, 25));
                    c.setBackground(Color.LIGHT_GRAY.darker());
                }
                if (type.equals(BalanceRow.GROUP)){
                    c.setFont(new Font(c.getFont().getName(), Font.PLAIN, 20));
                    c.setBackground(Color.LIGHT_GRAY);
                }
                if (type.equals(BalanceRow.ACCOUNT)){
                    c.setFont(new Font(c.getFont().getName(), Font.PLAIN, 15));
                    c.setBackground(Color.WHITE);
                }
                if (column == 0 || column == 3){
                    setHorizontalAlignment(JLabel.LEFT);
                    setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
                } else {
                    setHorizontalAlignment(JLabel.RIGHT);
                    setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
                }
                return c;
            }
        };
        this.setDefaultRenderer(Object.class, cellRenderer);

        this.addMouseListener(new BalanceTableClicked(this, Boolean.TRUE));

        this.getActionMap().put(Configuration.INIT_CONFIG, new InitConfigurableAction(this));
        this.getActionMap().put(Configuration.TRANSACTION_UPDATED, new BalanceTableTransactionsUpdated(this, model));
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