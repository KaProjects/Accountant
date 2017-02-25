package org.kaleta.accountant.frontend.component.year.component;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.listener.OpenAddAssetDialog;
import org.kaleta.accountant.frontend.component.year.model.AccountModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Created by Stanislav Kaleta on 16.02.2017.
 */
public class AssetsEditor extends JPanel implements Configurable {
    private Configuration configuration;
    private JPanel panelItems;
    private String schemaFilter;

    public AssetsEditor() {
        schemaFilter = "0";

        JPanel panelFilter = new JPanel();
        panelFilter.setVisible(false);
        panelFilter.add(new JLabel("asdsad"));
        panelFilter.setBackground(Color.GREEN);


        JToggleButton buttonFilter = new JToggleButton("Filter");
        buttonFilter.addActionListener(e -> {
            panelFilter.setVisible(buttonFilter.isSelected());
        });
        JButton buttonAddItem = new JButton("Add");
        buttonAddItem.addActionListener(new OpenAddAssetDialog(this));
        JButton buttonDepreciateAll = new JButton("Depreciate All");
        buttonDepreciateAll.addActionListener(e -> {
            // TODO: 2/16/17 dialog to dep. all
        });
        JPanel panelButtons = new JPanel();
        panelButtons.setLayout(new BoxLayout(panelButtons, BoxLayout.X_AXIS));
        panelButtons.add(buttonFilter);
        panelButtons.add(buttonAddItem);
        panelButtons.add(buttonDepreciateAll);


        panelItems = new JPanel();
        panelItems.setLayout(new BoxLayout(panelItems, BoxLayout.Y_AXIS));

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(panelButtons);
        this.add(panelFilter);
        this.add(panelItems);

        this.getActionMap().put(Configuration.ACCOUNT_UPDATED, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AssetsEditor.this.update();
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


            JLabel labelInitValue = new JLabel();
            JLabel labelCurrentValue;


            this.add(labelAccountName);
            this.add(labelSchema);
        }
    }
}
