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
import java.util.List;

public class BalanceOverview extends AccountingOverview {

    public BalanceOverview(Configuration configuration){
        setConfiguration(configuration);
        update();
    }

    public void update() {
        String year = getConfiguration().getSelectedYear();

        List<SchemaModel.Class> assetsClasses = Service.SCHEMA.getSchemaClassListByAccountType(year, Constants.AccountType.ASSET);
        assetsClasses.get(0).getGroup().add(Service.SCHEMA.getGroup(year, "0", "9"));
        List<SchemaModel.Class> liabilitiesClasses = Service.SCHEMA.getSchemaClassListByAccountType(year, Constants.AccountType.LIABILITY);
        liabilitiesClasses.remove(0);

        JPanel assetsContent = new JPanel();
        assetsContent.setLayout(new BoxLayout(assetsContent, BoxLayout.Y_AXIS));
        int assetsBalance = 0;
        for (SchemaModel.Class clazz : assetsClasses) {
            JPanel classBody = getBodyPanelInstance();
            int classBalance = 0;
            for (SchemaModel.Class.Group group : clazz.getGroup()) {
                JPanel groupBody = getBodyPanelInstance();
                String groupSchemaId = clazz.getId() + group.getId();
                Integer groupBalance = Service.TRANSACTIONS.getSchemaIdPrefixBalance(year, groupSchemaId);
                if (groupSchemaId.equals("09")) groupBalance = -groupBalance;
                for (SchemaModel.Class.Group.Account schemaAccount : group.getAccount()) {
                    String accSchemaId = groupSchemaId + schemaAccount.getId();
                    Integer accBalance = Service.TRANSACTIONS.getSchemaIdPrefixBalance(year, accSchemaId);
                    if (groupSchemaId.equals("09")) accBalance = -accBalance;
                    AccountingRowPanel accountHeader = new AccountingRowPanel(accSchemaId, AccountingRowPanel.ACCOUNT, schemaAccount.getName(), accBalance);
                    groupBody.add(accountHeader);
                }
                AccountingRowPanel groupHeader = new AccountingRowPanel(clazz.getId() + group.getId(), AccountingRowPanel.GROUP, group.getName(), groupBalance);
                classBody.add(getListPanel(groupHeader, groupBody));
                classBalance += groupBalance;
            }
            AccountingRowPanel classHeader = new AccountingRowPanel(clazz.getId(), AccountingRowPanel.CLASS, clazz.getName(), classBalance);
            assetsContent.add(getListPanel(classHeader, classBody));
            assetsBalance += classBalance;
        }
        AccountingRowPanel assetsHeader = new AccountingRowPanel("A", AccountingRowPanel.SUM, "Debit Sum", assetsBalance);
        JPanel assetsPanel = getListPanel(assetsHeader, assetsContent);
        assetsPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));


        JPanel liabilitiesContent = new JPanel();
        liabilitiesContent.setLayout(new BoxLayout(liabilitiesContent, BoxLayout.Y_AXIS));
        int liabilitiesBalance = 0;
        for (SchemaModel.Class clazz : liabilitiesClasses) {
            JPanel classBody = getBodyPanelInstance();
            int classBalance = 0;
            for (SchemaModel.Class.Group group : clazz.getGroup()) {
                JPanel groupBody = getBodyPanelInstance();
                String groupSchemaId = clazz.getId() + group.getId();
                Integer groupBalance = Service.TRANSACTIONS.getSchemaIdPrefixBalance(year, groupSchemaId);
                for (SchemaModel.Class.Group.Account schemaAccount : group.getAccount()) {
                    String accSchemaId = groupSchemaId + schemaAccount.getId();
                    Integer accBalance = Service.TRANSACTIONS.getSchemaIdPrefixBalance(year, accSchemaId);
                    AccountingRowPanel accountHeader = new AccountingRowPanel(accSchemaId, AccountingRowPanel.ACCOUNT, schemaAccount.getName(), accBalance);
                    groupBody.add(accountHeader);
                }
                AccountingRowPanel groupHeader = new AccountingRowPanel(clazz.getId() + group.getId(), AccountingRowPanel.GROUP, group.getName(), groupBalance);
                classBody.add(getListPanel(groupHeader, groupBody));
                classBalance += groupBalance;
            }
            AccountingRowPanel classHeader = new AccountingRowPanel(clazz.getId(), AccountingRowPanel.CLASS, clazz.getName(), classBalance);
            liabilitiesContent.add(getListPanel(classHeader, classBody));
            liabilitiesBalance += classBalance;
        }

        int profit = assetsBalance - liabilitiesBalance;
        AccountingRowPanel profitHeader = new AccountingRowPanel("X", AccountingRowPanel.SUM, "Profit", profit);
        liabilitiesContent.add(profitHeader);
        liabilitiesBalance += profit;

        AccountingRowPanel liabilitiesHeader = new AccountingRowPanel("L", AccountingRowPanel.SUM, "Credit Sum", liabilitiesBalance);
        JPanel liabilitiesPanel = getListPanel(liabilitiesHeader, liabilitiesContent);
        liabilitiesPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

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
