package org.kaleta.accountant.frontend.core.accounting;


import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.component.AccountingRowPanel;
import org.kaleta.accountant.frontend.component.TableRow;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public abstract class AccountingOverview extends JPanel implements Configurable {
    public static final String HIDEABLE = "HIDEABLE";

    private Configuration configuration;

    public AccountingOverview() {
        this.getActionMap().put(Configuration.SCHEMA_UPDATED, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });
        this.getActionMap().put(Configuration.ACCOUNT_UPDATED, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });
        this.getActionMap().put(Configuration.TRANSACTION_UPDATED, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });
    }

    public abstract void update();

    /**
     * Gives every row the same name column, wide enough for the longest title in the view.
     * <p>
     * The name column is what varies between rows, so it is the one that has to be pinned: with it
     * fixed, the value cells line up across every row and take all the width the window has to
     * spare. Call this once the rows have been built.
     */
    protected void alignNameColumn(JComponent... tables) {
        for (JComponent table : tables.length == 0 ? new JComponent[]{this} : tables) {
            List<TableRow> rows = new ArrayList<>();
            collectRows(table, rows);
            int width = 0;
            for (TableRow row : rows) {
                width = Math.max(width, row.getTitleWidth());
            }
            for (TableRow row : rows) {
                row.setNameColumnWidth(width);
            }
        }
    }

    private void collectRows(Component component, List<TableRow> rows) {
        if (component instanceof TableRow) {
            rows.add((TableRow) component);
            return;
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                collectRows(child, rows);
            }
        }
    }

    protected JPanel getBodyPanelInstance() {
        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setName(AccountingOverview.HIDEABLE);
        bodyPanel.setVisible(false);
        return bodyPanel;
    }

    protected <T extends JPanel & TableRow> JPanel getSumPanelInstance(T header, boolean isHeaderOnTop, JPanel... contentPanels) {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        for (JPanel panel : contentPanels) {
            body.add(panel);
        }

        header.addRowMouseListener(new MouseAdapter() {
            private boolean toggle = true;

            @Override
            public void mouseReleased(MouseEvent e) {
                for (Component child : body.getComponents()) {
                    updateComponentTree((JComponent) child, toggle);
                }
                toggle = !toggle;
            }

            private void updateComponentTree(JComponent component, boolean flag) {
                for (Component child : component.getComponents()) {
                    if (component.getName() != null && component.getName().equals(AccountingOverview.HIDEABLE))
                        component.setVisible(flag);
                    try {
                        updateComponentTree((JComponent) child, flag);
                    } catch (ClassCastException ex) {
                        // continue
                    }
                }
            }
        });

        JPanel sumPanel = new JPanel();
        GroupLayout layout = new GroupLayout(sumPanel);
        sumPanel.setLayout(layout);
        if (isHeaderOnTop) {
            layout.setHorizontalGroup(layout.createParallelGroup()
                    .addComponent(header)
                    .addComponent(body));
            layout.setVerticalGroup(layout.createSequentialGroup()
                    .addComponent(header)
                    .addComponent(body));
        } else {
            layout.setHorizontalGroup(layout.createParallelGroup()
                    .addComponent(body)
                    .addComponent(header));
            layout.setVerticalGroup(layout.createSequentialGroup()
                    .addComponent(body)
                    .addComponent(header));
        }
        return sumPanel;
    }

    protected JPanel getSubGroupPanelInstance(String classId, String groupId, List<String> accIds, String groupType, int valuesType){
        String year = getConfiguration().getSelectedYear();

        if (!Service.SCHEMA.checkGroupExists(year, classId, groupId)) return getBodyPanelInstance();

        JPanel groupBody = getBodyPanelInstance();
        String groupSchemaId = classId + groupId;
        java.util.List<String> schemaIds = new ArrayList<>();
        for (String accId : accIds) {
            String schemaId = groupSchemaId + accId;
            if (Service.SCHEMA.checkAccountExists(year, classId, groupId, accId)) {
                schemaIds.add(schemaId);
                String accountName = Service.SCHEMA.getAccountName(year, schemaId);
                AccountAggregate accountAggregate = AccountAggregate.create(accountName).increasing(schemaId);
                AccountingRowPanel accountPanel = new AccountingRowPanel(getConfiguration(), accountAggregate, AccountingRowPanel.ACCOUNT, valuesType, true);
                groupBody.add(accountPanel);
            }
        }

        AccountAggregate groupAggregate = AccountAggregate.create(Service.SCHEMA.getGroupName(year, groupSchemaId)).increasing(schemaIds.toArray(new String[]{}));

        return constructGroupPanel(groupType, valuesType, true, groupBody, groupAggregate);
    }

    protected JPanel getGroupPanelInstance(String classId, String groupId, String groupType, int valuesType, boolean isPositive) {
        String year = getConfiguration().getSelectedYear();

        if (!Service.SCHEMA.checkGroupExists(year, classId, groupId)) return getBodyPanelInstance();

        JPanel groupBody = getBodyPanelInstance();
        String groupSchemaId = classId + groupId;
        for (SchemaModel.Class.Group.Account schemaAccount : Service.SCHEMA.getGroup(year, classId, groupId).getAccount()) {
            AccountAggregate accountAggregate = AccountAggregate.create(schemaAccount.getName()).increasing(groupSchemaId + schemaAccount.getId());
            AccountingRowPanel accountPanel = new AccountingRowPanel(getConfiguration(), accountAggregate, AccountingRowPanel.ACCOUNT, valuesType, isPositive);
            groupBody.add(accountPanel);
        }

        AccountAggregate groupAggregate = AccountAggregate.create(Service.SCHEMA.getGroupName(year, groupSchemaId)).increasing(groupSchemaId);

        return constructGroupPanel(groupType, valuesType, isPositive, groupBody, groupAggregate);
    }

    private JPanel constructGroupPanel(String groupType, int valuesType, boolean isPositive, JPanel groupBody, AccountAggregate groupAggregate) {
        AccountingRowPanel groupHeader = new AccountingRowPanel(getConfiguration(), groupAggregate, groupType, valuesType, isPositive);
        groupHeader.addRowMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                groupBody.setVisible(!groupBody.isVisible());
            }
        });

        JPanel groupPanel = new JPanel();
        GroupLayout layout = new GroupLayout(groupPanel);
        groupPanel.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(groupHeader)
                .addComponent(groupBody));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(groupHeader)
                .addComponent(groupBody));
        return groupPanel;
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
