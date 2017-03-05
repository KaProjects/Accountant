package org.kaleta.accountant.frontend.component.year.component;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.listener.OpenAddAssetDialog;
import org.kaleta.accountant.frontend.component.year.model.AccountModel;
import org.kaleta.accountant.frontend.component.year.model.SchemaModel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stanislav Kaleta on 16.02.2017.
 */
public class AssetsEditor extends JPanel implements Configurable {
    private Configuration configuration;
    private JPanel panelItems;
    private String schemaFilter;

    public AssetsEditor(Configuration configuration) {
        setConfiguration(configuration);

        JCheckBox toggleFilter = new JCheckBox("Toggle Filter");
        JComboBox<String> cbGroups = new JComboBox<>();
        JComboBox<String> cbAccounts = new JComboBox<>();
        panelItems = new JPanel();

        panelItems.setLayout(new BoxLayout(panelItems, BoxLayout.Y_AXIS));

        cbGroups.setVisible(false);
        cbGroups.addItem("All");
        for (SchemaModel.Clazz.Group group : getConfiguration().getModel().getSchemaModel().getClasses().get(0).getGroups().values()){
            if (group.getId() == 9) continue;
            cbGroups.addItem(group.getName());
        }
        cbGroups.addActionListener(e -> {
            cbAccounts.removeAllItems();
            cbAccounts.repaint();
            cbAccounts.revalidate();
            int groupIndex = cbGroups.getSelectedIndex() - 1;
            if (groupIndex >= 0 ){
                int groupId = new ArrayList<>(getConfiguration().getModel().getSchemaModel().getClasses().get(0).getGroups().values()).get(groupIndex).getId();
                cbAccounts.addItem("All");
                for (SchemaModel.Clazz.Group.Account account : getConfiguration().getModel().getSchemaModel().getClasses().get(0).getGroups().get(groupId).getAccounts().values()){
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

        cbAccounts.setVisible(false);
        cbAccounts.addActionListener(e -> {
            int groupIndex = cbGroups.getSelectedIndex() - 1;
            if (groupIndex >= 0){
                int groupId = new ArrayList<>(getConfiguration().getModel().getSchemaModel().getClasses().get(0).getGroups().values()).get(groupIndex).getId();
                if (cbAccounts.getSelectedIndex() > 0){
                    schemaFilter = "0" + groupId + "" + new ArrayList<>(getConfiguration().getModel().getSchemaModel().getClasses().get(0)
                            .getGroups().get(groupId).getAccounts().values()).get(cbAccounts.getSelectedIndex() - 1).getId();
                } else {
                    schemaFilter = "0" + groupId;
                }
                update();
            }
        });

        toggleFilter.addActionListener(e -> {
            cbAccounts.setVisible(toggleFilter.isSelected());
            cbGroups.setVisible(toggleFilter.isSelected());
            cbGroups.setSelectedIndex(0);
        });

        JPanel panelFilter = new JPanel();
        panelFilter.setLayout(new BoxLayout(panelFilter, BoxLayout.X_AXIS));
        panelFilter.add(toggleFilter);
        panelFilter.add(cbGroups);
        panelFilter.add(cbAccounts);

        JButton buttonAddItem = new JButton("Add");
        buttonAddItem.addActionListener(new OpenAddAssetDialog(this));
        JButton buttonDepreciateAll = new JButton("Depreciate All");
        buttonDepreciateAll.addActionListener(e -> {
            // TODO: 2/16/17 dialog to dep. all
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup()
            .addGroup(layout.createSequentialGroup()
                .addComponent(buttonAddItem).addGap(5).addComponent(buttonDepreciateAll).addGap(5).addComponent(panelFilter))
            .addComponent(panelItems));
        layout.setVerticalGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup()
                .addComponent(buttonAddItem,25,25,25).addComponent(buttonDepreciateAll,25,25,25).addComponent(panelFilter,25,25,25))
            .addComponent(panelItems));

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
                for (SchemaModel.Clazz.Group group : getConfiguration().getModel().getSchemaModel().getClasses().get(0).getGroups().values()){
                    if (group.getId() == 9) continue;
                    cbGroups.addItem(group.getName());
                }
                cbGroups.setSelectedIndex(0);
            }
        });
    }

    public void update(){
        panelItems.removeAll();
        List<AccountModel.Account> accounts = getConfiguration().getModel().getAccountModel().getAccountsBySchema(schemaFilter);
        for (AccountModel.Account account : accounts){
            if (account.getSchemaId().startsWith("09")) continue;
            panelItems.add(new AssetPanel(account));
        }
        panelItems.revalidate();
        panelItems.repaint();
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    private class AssetPanel extends JPanel{

        public AssetPanel(AccountModel.Account account) {
            JLabel labelAccountName = new JLabel(account.getName());
            JLabel labelSchema = new JLabel(getConfiguration().getModel().getSchemaModel().getGroupName(account.getSchemaId())
                    + "-" + getConfiguration().getModel().getSchemaModel().getAccName(account.getSchemaId()));

            String initValue = getConfiguration().getModel().getAccountModel().getAccInitState(account);

            // TODO: 5.3.2017 design&impl

            JLabel labelInitValue = new JLabel();
            JLabel labelCurrentValue;


            this.add(labelAccountName);
            this.add(labelSchema);
        }
    }
}
