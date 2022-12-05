package org.kaleta.accountant.frontend.core.accounting;

import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.component.AccountingRowPanel;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class BalanceOverview extends AccountingOverview {

    public BalanceOverview(Configuration configuration) {
        setConfiguration(configuration);
        update();
    }

    public void update() {
        String year = getConfiguration().getSelectedYear();
        int valuesType = AccountingRowPanel.VALUE_BALANCE;

        List<SchemaModel.Class> assetsClasses = Service.SCHEMA.getSchemaClassListByAccountType(year, Constants.AccountType.ASSET);
        assetsClasses.get(0).getGroup().add(Service.SCHEMA.getGroup(year, "0", "9"));
        List<SchemaModel.Class> liabilitiesClasses = Service.SCHEMA.getSchemaClassListByAccountType(year, Constants.AccountType.LIABILITY);
        liabilitiesClasses.remove(0);

        JPanel assetsContent = new JPanel();
        assetsContent.setLayout(new BoxLayout(assetsContent, BoxLayout.Y_AXIS));

        List<String> assetGroupSchemaIds = new ArrayList<>();
        for (SchemaModel.Class clazz : assetsClasses) {
            JPanel classBody = getBodyPanelInstance();

            List<String> groupSchemaIds = new ArrayList<>();

            for (SchemaModel.Class.Group group : clazz.getGroup()) {
                JPanel groupBody = getBodyPanelInstance();
                AccountAggregate groupAggregate = AccountAggregate.create(group.getName()).increasing(clazz.getId() + group.getId());
                if (!groupAggregate.getSchemaId().equals("09")) {
                    groupSchemaIds.add(groupAggregate.getSchemaId());
                }

                for (SchemaModel.Class.Group.Account schemaAccount : group.getAccount()) {
                    AccountAggregate accountAggregate = AccountAggregate.create(schemaAccount.getName()).increasing(groupAggregate.getSchemaId() + schemaAccount.getId());
                    AccountingRowPanel accountHeader = new AccountingRowPanel(getConfiguration(), accountAggregate, AccountingRowPanel.ACCOUNT, valuesType, !groupAggregate.getSchemaId().equals("09"));
                    groupBody.add(accountHeader);
                }
                AccountingRowPanel groupHeader = new AccountingRowPanel(getConfiguration(), groupAggregate, AccountingRowPanel.GROUP, valuesType, !groupAggregate.getSchemaId().equals("09"));
                classBody.add(getListPanel(groupHeader, groupBody));
            }

            AccountAggregate classAgg = AccountAggregate.create(clazz.getName()).increasing(groupSchemaIds.toArray(new String[]{}));
            if (clazz.getId().equals("0")) {
                classAgg.decreasing("09");
            }
            AccountingRowPanel classHeader = new AccountingRowPanel(getConfiguration(), classAgg, AccountingRowPanel.CLASS, valuesType);
            assetsContent.add(getListPanel(classHeader, classBody));
            assetGroupSchemaIds.addAll(groupSchemaIds);
        }

        AccountAggregate assetAgg = AccountAggregate.create("Debit Sum").increasing(assetGroupSchemaIds.toArray(new String[]{})).decreasing("09");
        AccountingRowPanel assetsHeader = new AccountingRowPanel(getConfiguration(), assetAgg, AccountingRowPanel.SUM, valuesType);
        JPanel assetsPanel = getListPanel(assetsHeader, assetsContent);
        assetsPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));


        JPanel liabilitiesContent = new JPanel();
        liabilitiesContent.setLayout(new BoxLayout(liabilitiesContent, BoxLayout.Y_AXIS));

        List<String> liabilitiesGroupSchemaIds = new ArrayList<>();
        for (SchemaModel.Class clazz : liabilitiesClasses) {
            JPanel classBody = getBodyPanelInstance();

            List<String> groupSchemaIds = new ArrayList<>();

            for (SchemaModel.Class.Group group : clazz.getGroup()) {
                JPanel groupBody = getBodyPanelInstance();
                AccountAggregate groupAggregate = AccountAggregate.create(group.getName()).increasing(clazz.getId() + group.getId());
                groupSchemaIds.add(groupAggregate.getSchemaId());
                for (SchemaModel.Class.Group.Account schemaAccount : group.getAccount()) {
                    AccountAggregate accountAggregate = AccountAggregate.create(schemaAccount.getName()).increasing(groupAggregate.getSchemaId() + schemaAccount.getId());
                    AccountingRowPanel accountHeader = new AccountingRowPanel(getConfiguration(), accountAggregate, AccountingRowPanel.ACCOUNT, valuesType);
                    groupBody.add(accountHeader);
                }
                AccountingRowPanel groupHeader = new AccountingRowPanel(getConfiguration(), groupAggregate, AccountingRowPanel.GROUP, valuesType);
                classBody.add(getListPanel(groupHeader, groupBody));
            }
            AccountAggregate classAgg = AccountAggregate.create(clazz.getName()).increasing(groupSchemaIds.toArray(new String[]{}));
            AccountingRowPanel classHeader = new AccountingRowPanel(getConfiguration(), classAgg, AccountingRowPanel.CLASS, valuesType);
            liabilitiesContent.add(getListPanel(classHeader, classBody));
            liabilitiesGroupSchemaIds.addAll(groupSchemaIds);
        }

        AccountAggregate liabilitiesAgg = AccountAggregate.create("Credit Sum").increasing(liabilitiesGroupSchemaIds.toArray(new String[]{}));
        AccountingRowPanel liabilitiesHeader = new AccountingRowPanel(getConfiguration(), liabilitiesAgg, AccountingRowPanel.SUM, valuesType);
        JPanel liabilitiesPanel = getListPanel(liabilitiesHeader, liabilitiesContent);
        liabilitiesPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        liabilitiesGroupSchemaIds.add("09");
        AccountAggregate profitAgg = AccountAggregate.create("Profit").increasing(assetGroupSchemaIds.toArray(new String[]{})).decreasing(liabilitiesGroupSchemaIds.toArray(new String[]{}));
        AccountingRowPanel profitHeader = new AccountingRowPanel(getConfiguration(), profitAgg, AccountingRowPanel.SUM, valuesType);
        liabilitiesContent.add(profitHeader);

        this.removeAll();
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(assetsPanel)
                .addComponent(liabilitiesPanel));
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addComponent(assetsPanel)
                .addComponent(liabilitiesPanel));
        this.repaint();
        this.revalidate();
    }

    private JPanel getListPanel(AccountingRowPanel header, JPanel body) {
        JPanel thisPanel = new JPanel();
        thisPanel.setBackground(Color.white);

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                body.setVisible(!body.isVisible());
            }
        };

        switch (header.getType()) {
            case AccountingRowPanel.SUM: {
                return getSumPanelInstance(header, true, body);
            }
            case AccountingRowPanel.CLASS: {
                header.addMouseListener(mouseAdapter);
                break;
            }
            case AccountingRowPanel.GROUP: {
                header.addMouseListener(mouseAdapter);
                break;
            }
        }
        GroupLayout layout = new GroupLayout(thisPanel);
        thisPanel.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(header)
                .addComponent(body));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(header)
                .addComponent(body));

        return thisPanel;
    }
}
