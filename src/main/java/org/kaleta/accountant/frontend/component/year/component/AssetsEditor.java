package org.kaleta.accountant.frontend.component.year.component;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.component.year.model.AccountModel;
import org.kaleta.accountant.frontend.component.year.model.SchemaModel;

import javax.swing.*;
import java.awt.Color;
import java.util.List;

/**
 * Created by Stanislav Kaleta on 16.02.2017.
 */
public class AssetsEditor extends JPanel implements Configurable {
    private Configuration configuration;
    private JPanel panelItems;

    public AssetsEditor() {

        JPanel panelFilter = new JPanel();
        panelFilter.setVisible(false);
        panelFilter.add(new JLabel("asdsad"));
        panelFilter.setBackground(Color.GREEN);


        JToggleButton buttonFilter = new JToggleButton("Filter");
        buttonFilter.addActionListener(e -> {
            panelFilter.setVisible(buttonFilter.isSelected());
        });
        JButton buttonAddItem = new JButton("Add");
        buttonAddItem.addActionListener(e -> {
            // TODO: 2/16/17 dialog to add asset
        });
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

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(panelButtons);
        this.add(panelFilter);
        this.add(panelItems);

    }

    public void update(){
        // TODO: 2/16/17 for schema set - get accs. - set panelItems
        panelItems.removeAll();
        for (SchemaModel.Clazz.Group group : getConfiguration().getModel().getSchemaModel().getClasses().get(0).getGroups().values()){
            for (SchemaModel.Clazz.Group.Account accType : group.getAccounts().values()){
                String schemaId = "0"+String.valueOf(group.getId()+String.valueOf(accType.getId()));
                List<AccountModel.Account> accModel = getConfiguration().getModel().getAccountModel().getAccountsBySchema(schemaId);
                for (AccountModel.Account acc : accModel){
                    String accId = acc.getSchemaId() + "." + acc.getSemanticId();
                    for (AccountModel.Transaction tr : getConfiguration().getModel().getAccountModel().getTransactions()){
                        // TODO: 2/16/17 ... 
                    }
                }
            }
        }

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
