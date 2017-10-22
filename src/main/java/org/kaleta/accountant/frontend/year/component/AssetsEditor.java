package org.kaleta.accountant.frontend.year.component;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.listener.OpenAddAssetDialog;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class AssetsEditor extends JPanel implements Configurable {
    private Configuration configuration;
    private JPanel panelItems;
    private JButton buttonDepreciateAll;
    private String schemaFilter;
    private int activeFilter;

    public AssetsEditor(Configuration configuration) {
        setConfiguration(configuration);
        schemaFilter = "0";
        activeFilter = 0;

        buttonDepreciateAll = new JButton("Depreciate All");
        // TODO: 22.10.2017
        //buttonDepreciateAll.addActionListener(new OpenDepreciateDialog(this, new ArrayList<AccountModel.Account>()));

        JButton buttonAddItem = new JButton("Add");
        buttonAddItem.addActionListener(new OpenAddAssetDialog(this));

        JCheckBox toggleFilter = new JCheckBox("Toggle Filter");
        JComboBox<Object> cbGroups = new JComboBox<>();
        JComboBox<Object> cbAccounts = new JComboBox<>();
        JComboBox<String> cbActive = new JComboBox<>();
        panelItems = new JPanel();

        panelItems.setLayout(new BoxLayout(panelItems, BoxLayout.Y_AXIS));

        cbGroups.addItem("All");
        for (SchemaModel.Class.Group group : Service.SCHEMA.getSchemaClassMap(getConfiguration().getSelectedYear()).get(0).getGroup()) {
            if (group.getId().equals(Constants.Schema.ACCUMULATED_DEP_GROUP_ID)) continue;
            cbGroups.addItem(group);
        }
        cbGroups.addActionListener(e -> {
            cbAccounts.removeAllItems();
            cbAccounts.repaint();
            cbAccounts.revalidate();

            if (cbGroups.getSelectedItem() instanceof SchemaModel.Class.Group){
                cbAccounts.addItem("All");
                SchemaModel.Class.Group selectedGroup = (SchemaModel.Class.Group) cbGroups.getSelectedItem();
                for (SchemaModel.Class.Group.Account account : selectedGroup.getAccount()){
                    cbAccounts.addItem(account);
                }
                cbAccounts.setSelectedIndex(0);
            } else {
                schemaFilter = "0";
                update();
            }
        });

        cbAccounts.addActionListener(e -> {
            if (cbGroups.getSelectedItem() instanceof SchemaModel.Class.Group) {
                String groupId = ((SchemaModel.Class.Group)cbGroups.getSelectedItem()).getId();
                if (cbAccounts.getSelectedItem() instanceof SchemaModel.Class.Group.Account) {
                    schemaFilter = "0" + groupId + ((SchemaModel.Class.Group.Account)cbAccounts.getSelectedItem()).getId();
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
        this.getActionMap().put(Configuration.TRANSACTION_UPDATED, new AbstractAction() {
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
                for (SchemaModel.Class.Group group : Service.SCHEMA.getSchemaClassMap(getConfiguration().getSelectedYear()).get(0).getGroup()) {
                    if (group.getId().equals(Constants.Schema.ACCUMULATED_DEP_GROUP_ID)) continue;
                    cbGroups.addItem(group);
                }
                cbGroups.setSelectedIndex(0);
            }
        });
    }

    public void update(){
        panelItems.removeAll();
        List<AccountsModel.Account> filteredAccounts = new ArrayList<>();
        if (schemaFilter.equals("0")){
            for (AccountsModel.Account account : Service.ACCOUNT.getAccountsBySchemaId(getConfiguration().getSelectedYear(),schemaFilter)){
                if (account.getSchemaId().startsWith(schemaFilter + Constants.Schema.ACCUMULATED_DEP_GROUP_ID)) continue;
                filteredAccounts.add(account);
            }
        } else {
            filteredAccounts.addAll(Service.ACCOUNT.getAccountsBySchemaId(getConfiguration().getSelectedYear(),schemaFilter));
        }

        for (AccountsModel.Account account : filteredAccounts){
            AssetPanel panel = new AssetPanel(account);
            if ((panel.isActive && activeFilter == 1) || (!panel.isActive && activeFilter == 2) || (activeFilter == 0)){
                panelItems.add(panel);
            }
        }
        panelItems.revalidate();
        panelItems.repaint();

        // TODO: 22.10.2017
//        buttonDepreciateAll.removeActionListener(buttonDepreciateAll.getActionListeners()[0]);
        //buttonDepreciateAll.addActionListener(new OpenDepreciateDialog(this, filteredAccounts));
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
        private AccountsModel.Account account;

        public AssetPanel(AccountsModel.Account assetAccount) {
            this.account = assetAccount;
            String year = getConfiguration().getSelectedYear();
            String assetValue = Service.ACCOUNT.getAccountBalance(year, account);
            String accDepValue = Service.ACCOUNT.getAccountBalance(year, Service.ACCOUNT.getAccumulatedDepAccount(year, account));
            String currentValue = String.valueOf(Integer.parseInt(assetValue) - Integer.parseInt(accDepValue));

            Font boldFont = new Font(new JLabel().getFont().getName(), Font.BOLD, 25);
            JLabel labelAccountName = new JLabel(account.getName());
            labelAccountName.setFont(boldFont);
            JLabel labelGroup = new JLabel("> " + Service.SCHEMA.getGroupName(year, account.getClassId(), account.getGroupId()));
            labelGroup.setToolTipText("Group");
            JLabel labelAccType = new JLabel(">> " + Service.SCHEMA.getAccountName(year, account.getClassId(), account.getGroupId(), account.getSchemaAccountId()));
            labelAccType.setToolTipText("Account Type");
            JLabel labelInitValue;
            JLabel labelCurrentValue = new JLabel(currentValue, SwingConstants.RIGHT);
            labelCurrentValue.setToolTipText("Current Value");
            labelCurrentValue.setFont(boldFont);

            JButton buttonDep = new JButton("Depreciate");
            // TODO: 22.10.2017
            //buttonDep.addActionListener(new OpenDepreciateDialog(AssetsEditor.this, account));
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
                this.setBackground(Constants.Color.EXPENSE_RED);
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

        public AccountsModel.Account getAccount() {
            return account;
        }
    }
}
