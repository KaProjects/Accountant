package org.kaleta.accountant.frontend.dep;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.InitConfigurableAction;
import org.kaleta.accountant.frontend.action.configuration.JournalTableTransactionsUpdated;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;

/**
 * Created by Stanislav Kaleta on 03.08.2016.
 */
public class JournalTable extends JTable implements Configurable {
    private Configuration configuration;

    public JournalTable() {
        initComponents();
    }

    private void initComponents(){
        JournalTableModel model = new JournalTableModel(Service.ACCOUNT.getSchema(), Service.ACCOUNT.getSemanticAccounts());
        this.setModel(model);
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.setRowSelectionAllowed(true);
        this.setColumnSelectionAllowed(false);
        this.getTableHeader().setReorderingAllowed(false);
        this.setPreferredScrollableViewportSize(new Dimension(1200,800));
        this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        TableColumnModel columnModel = this.getColumnModel();
        columnModel.getColumn(0).setMinWidth(90);
        columnModel.getColumn(0).setMaxWidth(90);
        columnModel.getColumn(0).setCellRenderer(new DefaultTableCellRenderer());
        ((DefaultTableCellRenderer)columnModel.getColumn(0).getCellRenderer()).setHorizontalAlignment(JLabel.CENTER);
        columnModel.getColumn(1).setMinWidth(300);
        columnModel.getColumn(2).setMinWidth(80);
        columnModel.getColumn(2).setMaxWidth(80);
        columnModel.getColumn(2).setCellRenderer(new DefaultTableCellRenderer());
        ((DefaultTableCellRenderer)columnModel.getColumn(2).getCellRenderer()).setHorizontalAlignment(JLabel.RIGHT);
        columnModel.getColumn(3).setMinWidth(300);
        columnModel.getColumn(4).setMinWidth(300);
        // TODO: 8/8/16 play more with column renderers
        // TODO: 8/8/16 add on click listenrers + actions

        this.getActionMap().put(Configuration.INIT_CONFIG, new InitConfigurableAction(this));
        this.getActionMap().put(Configuration.TRANSACTION_UPDATED, new JournalTableTransactionsUpdated(this, model));
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
