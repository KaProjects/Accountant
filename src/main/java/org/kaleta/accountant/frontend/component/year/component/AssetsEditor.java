package org.kaleta.accountant.frontend.component.year.component;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.listener.OpenAddAssetDialog;
import org.kaleta.accountant.frontend.action.listener.OpenDepreciateDialog;
import org.kaleta.accountant.frontend.common.constants.ColorConstants;
import org.kaleta.accountant.frontend.component.year.model.AccountModel;
import org.kaleta.accountant.frontend.component.year.model.SchemaModel;

import javax.swing.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stanislav Kaleta on 16.02.2017.
 */
public class AssetsEditor extends JPanel implements Configurable {
    private Configuration configuration;
    private JPanel panelItems;
    private JButton buttonDepreciateAll;
    private String schemaFilter;
    private int activeFilter;

    public AssetsEditor(Configuration configuration) {
        setConfiguration(configuration);

        buttonDepreciateAll = new JButton("Depreciate All");
        buttonDepreciateAll.addActionListener(new OpenDepreciateDialog(this, new ArrayList<AccountModel.Account>()));

        JButton buttonAddItem = new JButton("Add");
        buttonAddItem.addActionListener(new OpenAddAssetDialog(this));

        JCheckBox toggleFilter = new JCheckBox("Toggle Filter");
        JComboBox<String> cbGroups = new JComboBox<>();
        JComboBox<String> cbAccounts = new JComboBox<>();
        JComboBox<String> cbActive = new JComboBox<>();
        panelItems = new JPanel();

        panelItems.setLayout(new BoxLayout(panelItems, BoxLayout.Y_AXIS));

        cbGroups.addItem("All");
        for (SchemaModel.Clazz.Group group : getConfiguration().getModel().getSchemaModel().getClasses().get(0).getGroups().values()) {
            if (group.getId() == 9) continue;
            cbGroups.addItem(group.getName());
        }
        cbGroups.addActionListener(e -> {
            cbAccounts.removeAllItems();
            cbAccounts.repaint();
            cbAccounts.revalidate();
            int groupIndex = cbGroups.getSelectedIndex() - 1;
            if (groupIndex >= 0) {
                int groupId = new ArrayList<>(getConfiguration().getModel().getSchemaModel().getClasses().get(0).getGroups().values()).get(groupIndex).getId();
                cbAccounts.addItem("All");
                for (SchemaModel.Clazz.Group.Account account : getConfiguration().getModel().getSchemaModel().getClasses().get(0).getGroups().get(groupId).getAccounts().values()) {
                    cbAccounts.addItem(account.getName());
                }
                cbAccounts.setSelectedIndex(0);
                schemaFilter = "0" + groupId;
            } else {
                schemaFilter = "0";
            }
            update();
        });
        cbGroups.setSelectedIndex(0);

        cbAccounts.addActionListener(e -> {
            int groupIndex = cbGroups.getSelectedIndex() - 1;
            if (groupIndex >= 0) {
                int groupId = new ArrayList<>(getConfiguration().getModel().getSchemaModel().getClasses().get(0).getGroups().values()).get(groupIndex).getId();
                if (cbAccounts.getSelectedIndex() > 0) {
                    schemaFilter = "0" + groupId + "" + new ArrayList<>(getConfiguration().getModel().getSchemaModel().getClasses().get(0)
                            .getGroups().get(groupId).getAccounts().values()).get(cbAccounts.getSelectedIndex() - 1).getId();
                } else {
                    schemaFilter = "0" + groupId;
                }
                update();
            }
        });

        cbActive.addItem("All");
        cbActive.addItem("Only Active");
        cbActive.addItem("Only Excluded");
        cbActive.addActionListener(e -> {
            activeFilter = cbActive.getSelectedIndex();
            update();
        });
        cbActive.setSelectedIndex(0);

        JPanel panelFilter = new JPanel();
        panelFilter.setPreferredSize(new Dimension(Short.MAX_VALUE,0));
        panelFilter.setMaximumSize(new Dimension(Short.MAX_VALUE,25));
        panelFilter.setLayout(new BoxLayout(panelFilter, BoxLayout.X_AXIS));
        panelFilter.add(cbActive);
        panelFilter.add(cbGroups);
        panelFilter.add(cbAccounts);
        panelFilter.setVisible(false);

        toggleFilter.addActionListener(e -> {
            panelFilter.setVisible(toggleFilter.isSelected());
            cbGroups.setSelectedIndex(0);
            cbActive.setSelectedIndex(0);
        });

        JScrollPane paneItems = new JScrollPane(panelItems);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                        .addComponent(buttonAddItem).addGap(5).addComponent(buttonDepreciateAll).addGap(5).addComponent(toggleFilter))
                .addComponent(panelFilter)
                .addComponent(paneItems));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                        .addComponent(buttonAddItem, 25, 25, 25).addComponent(buttonDepreciateAll, 25, 25, 25).addComponent(toggleFilter, 25, 25, 25))
                .addGap(2).addComponent(panelFilter,25,25,25).addGap(2)
                .addComponent(paneItems));

        this.getActionMap().put(Configuration.ACCOUNT_UPDATED, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AssetsEditor.this.update();
            }
        });
        this.getActionMap().put(Configuration.SCHEMA_UPDATED, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cbGroups.removeAllItems();
                cbGroups.addItem("All");
                for (SchemaModel.Clazz.Group group : getConfiguration().getModel().getSchemaModel().getClasses().get(0).getGroups().values()) {
                    if (group.getId() == 9) continue;
                    cbGroups.addItem(group.getName());
                }
                cbGroups.setSelectedIndex(0);
            }
        });
    }

    public void update(){
        panelItems.removeAll();
        List<AccountModel.Account> filteredAccounts = getConfiguration().getModel().getAccountModel().getAccountsBySchema(schemaFilter);
        List<AccountModel.Account> suitableAccounts = new ArrayList<>();
        for (AccountModel.Account account : filteredAccounts){
            if (account.getSchemaId().startsWith("09")) continue;
            suitableAccounts.add(account);
        }
        for (AccountModel.Account account : suitableAccounts){
            AssetPanel panel = new AssetPanel(account);
            if ((panel.isActive && activeFilter == 1) || (!panel.isActive && activeFilter == 2) || (activeFilter == 0)){
                panelItems.add(panel);
            }
        }
        panelItems.revalidate();
        panelItems.repaint();

        buttonDepreciateAll.removeActionListener(buttonDepreciateAll.getActionListeners()[0]);
        buttonDepreciateAll.addActionListener(new OpenDepreciateDialog(this, suitableAccounts));
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    private class AssetPanel extends JPanel {
        private boolean isActive;
        private AccountModel.Account account;

        public AssetPanel(AccountModel.Account assetAccount) {
            this.account = assetAccount;
            Font boldFont = new Font(new JLabel().getFont().getName(), Font.BOLD, 25);
            AccountModel accModel = getConfiguration().getModel().getAccountModel();
            String assetValue = accModel.getAccBalance(account);
            String accDepValue = accModel.getAccBalance(accModel.getAccount("09" + account.getSchemaId().substring(1, 2), account.getSchemaId().substring(2, 3) + "-" + account.getSemanticId()));
            String currentValue = String.valueOf(Integer.parseInt(assetValue) - Integer.parseInt(accDepValue));

            JLabel labelAccountName = new JLabel(account.getName());
            labelAccountName.setFont(boldFont);
            JLabel labelGroup = new JLabel("> " + getConfiguration().getModel().getSchemaModel().getGroupName(account.getSchemaId()));
            labelGroup.setToolTipText("Group");
            JLabel labelAccType = new JLabel(">> " + getConfiguration().getModel().getSchemaModel().getAccName(account.getSchemaId()));
            labelAccType.setToolTipText("Account Type");
            JLabel labelInitValue;
            JLabel labelCurrentValue = new JLabel(currentValue, SwingConstants.RIGHT);
            labelCurrentValue.setToolTipText("Current Value");
            labelCurrentValue.setFont(boldFont);

            JButton buttonDep = new JButton("Depreciate");
            buttonDep.addActionListener(new OpenDepreciateDialog(AssetsEditor.this, account));
            JButton buttonExclude = new JButton("Exclude");
            buttonExclude.addActionListener(e -> {
                // TODO: 3/17/17 open ex dialog - setup ex. transaction (dar,predaj,vyhodenie, stranie,...)
            });

            JPanel panelActions = new JPanel();
            panelActions.setLayout(new BoxLayout(panelActions, BoxLayout.X_AXIS));
            panelActions.add(buttonDep);
            panelActions.add(buttonExclude);
            panelActions.setBackground(Color.GREEN);

            this.setBorder(BorderFactory.createLineBorder(Color.BLACK));

            JPanel separator = new JPanel();
            separator.setOpaque(false);

            isActive = Integer.parseInt(assetValue) != 0;
            if (isActive) {
                labelInitValue = new JLabel(assetValue, SwingConstants.RIGHT);
                labelInitValue.setToolTipText("Purchasing Value");
                labelInitValue.setFont(boldFont);
            } else {
                labelInitValue = new JLabel("Excluded", SwingConstants.RIGHT);
                labelInitValue.setForeground(Color.RED);
                labelCurrentValue.setVisible(false);
                this.setBackground(ColorConstants.EXPENSE_RED);
                panelActions.setVisible(false);
            }

            GroupLayout layout = new GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(layout.createSequentialGroup().addGap(5)
                    .addGroup(layout.createParallelGroup()
                            .addComponent(labelAccountName,300,300,300)
                            .addComponent(labelGroup,300,300,300)
                            .addComponent(labelAccType,300,300,300))
                    .addGap(20)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addComponent(labelInitValue,150,150,150)
                            .addComponent(labelCurrentValue,150,150,150))
                    .addComponent(separator)
                    .addComponent(panelActions)
                    .addGap(5));
            layout.setVerticalGroup(layout.createSequentialGroup().addGap(5)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                            .addGroup(layout.createSequentialGroup()
                                    .addComponent(labelAccountName)
                                    .addComponent(labelGroup)
                                    .addComponent(labelAccType))
                            .addGroup(layout.createSequentialGroup()
                                    .addComponent(labelInitValue)
                                    .addComponent(labelCurrentValue))
                            .addComponent(separator,50,50,50)
                            .addComponent(panelActions))
                    .addGap(5));
        }

        public AccountModel.Account getAccount() {
            return account;
        }
    }
}
