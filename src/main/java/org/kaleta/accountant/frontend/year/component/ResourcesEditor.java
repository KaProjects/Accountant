package org.kaleta.accountant.frontend.year.component;

import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.listener.OpenAddResourcesDialog;
import org.kaleta.accountant.frontend.common.IconLoader;
import org.kaleta.accountant.frontend.year.model.AccountModel;
import org.kaleta.accountant.frontend.year.model.SchemaModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Stanislav Kaleta on 20.04.2017.
 */
public class ResourcesEditor extends JPanel implements Configurable {
    private Configuration configuration;

    private JPanel panelGroups;
    private List<ContentPanel> contentPanelList;

    public ResourcesEditor(Configuration configuration) {
        setConfiguration(configuration);

        JButton buttonAddItem = new JButton("Add Items");
        buttonAddItem.addActionListener(new OpenAddResourcesDialog(this));

        contentPanelList= new ArrayList<>();

        panelGroups = new JPanel();
        panelGroups.setLayout(new BoxLayout(panelGroups, BoxLayout.Y_AXIS));
        JScrollPane paneGroups = new JScrollPane(panelGroups);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                        .addComponent(buttonAddItem))
                .addComponent(paneGroups));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                        .addComponent(buttonAddItem, 25, 25, 25)).addGap(4)
                .addComponent(paneGroups));

        this.getActionMap().put(Configuration.SCHEMA_UPDATED, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ResourcesEditor.this.update();
                for (ContentPanel contentPanel : contentPanelList){
                    contentPanel.update();
                }
            }
        });
        this.getActionMap().put(Configuration.ACCOUNT_UPDATED, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (ContentPanel contentPanel : contentPanelList){
                    contentPanel.update();
                }
            }
        });
    }

    public void update(){
        panelGroups.removeAll();
        contentPanelList.clear();
        SchemaModel.Clazz resourcesClass = getConfiguration().getModel().getSchemaModel().getClasses().get(1);

        for (SchemaModel.Clazz.Group group : resourcesClass.getGroups().values()){

            JPanel groupContent = new JPanel();
            groupContent.setLayout(new BoxLayout(groupContent, BoxLayout.Y_AXIS));

            for (SchemaModel.Clazz.Group.Account account : group.getAccounts().values()){
                String schemaId = String.valueOf(resourcesClass.getId()) + String.valueOf(group.getId()) + String.valueOf(account.getId());
                ContentPanel panelContent = new ContentPanel(schemaId);
                panelContent.update();
                groupContent.add(getListPanel(account.getName(), panelContent));
                contentPanelList.add(panelContent);
            }
            panelGroups.add(getListPanel(group.getName(), groupContent));
        }

        panelGroups.revalidate();
        panelGroups.repaint();
    }

    public JPanel getListPanel(String title, JPanel content){
        content.setVisible(false);

        JPanel panelSide = new JPanel();
        panelSide.setVisible(false);
        panelSide.setOpaque(false);

        JLabel panelToggle = new JLabel(IconLoader.getIcon(IconLoader.TOGGLE_EXPAND, new Dimension(15,30)));

        JPanel panelTitle = new JPanel(new GridLayout(1,1));
        panelTitle.add(new JLabel("   " + title));
        panelTitle.setOpaque(false);

        JPanel thisPanel = new JPanel();
        thisPanel.setBackground(Color.white);
        thisPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        GroupLayout layout = new GroupLayout(thisPanel);
        thisPanel.setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                        .addComponent(panelToggle,15,15,15)
                        .addComponent(panelSide,15,15,15))
                .addGroup(layout.createParallelGroup()
                        .addComponent(panelTitle)
                        .addComponent(content)));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                        .addComponent(panelToggle,30,30,30)
                        .addComponent(panelTitle,30,30,30))
                .addGroup(layout.createParallelGroup()
                        .addComponent(panelSide,20,20,20)
                        .addComponent(content)));

        panelToggle.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                content.setVisible(!content.isVisible());
                panelSide.setVisible(!panelSide.isVisible());
                panelToggle.setIcon(IconLoader.getIcon(content.isVisible() ? IconLoader.TOGGLE_HIDE : IconLoader.TOGGLE_EXPAND, new Dimension(15,30)));
                thisPanel.setBackground(content.isVisible() ? Color.getHSBColor(236/360f,0.27f,0.88f) : Color.WHITE);
            }
        });

        return thisPanel;
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    private class ContentPanel extends JPanel {
        private String schemaId;

        private JPanel panelAccounts;

        private ContentPanel(String schemaId){
            this.schemaId = schemaId;

            JPanel panelAddAcc = new JPanel();
            JButton buttonAdd = new JButton("Add");
            JTextField textFieldName = new JTextField();
            textFieldName.setVisible(false);
            JButton buttonConfirmAdd = new JButton("Confirm");
            buttonConfirmAdd.setVisible(false);
            JButton buttonCancelAdd = new JButton("Cancel");
            buttonCancelAdd.setVisible(false);

            JPanel panelGap = new JPanel();

            GroupLayout layoutAddPanel = new GroupLayout(panelAddAcc);
            panelAddAcc.setLayout(layoutAddPanel);
            layoutAddPanel.setHorizontalGroup(layoutAddPanel.createSequentialGroup()
                    .addComponent(buttonAdd)
                    .addComponent(textFieldName,100,100,100).addGap(5)
                    .addComponent(buttonConfirmAdd).addGap(5)
                    .addComponent(buttonCancelAdd)
                    .addComponent(panelGap));
            layoutAddPanel.setVerticalGroup(layoutAddPanel.createParallelGroup()
                    .addComponent(buttonAdd,25,25,25)
                    .addComponent(textFieldName,25,25,25)
                    .addComponent(buttonConfirmAdd,25,25,25)
                    .addComponent(buttonCancelAdd,25,25,25)
                    .addComponent(panelGap,25,25,25));

            buttonAdd.addActionListener(e -> {
                buttonAdd.setVisible(false);
                textFieldName.setText("");
                textFieldName.setVisible(true);
                buttonConfirmAdd.setVisible(true);
                buttonCancelAdd.setVisible(true);
            });

            buttonConfirmAdd.addActionListener(e -> {
                buttonAdd.setVisible(true);
                textFieldName.setVisible(false);
                buttonConfirmAdd.setVisible(false);
                buttonCancelAdd.setVisible(false);

                AccountModel model = getConfiguration().getModel().getAccountModel();
                String nextSemId = String.valueOf(model.getAccountsBySchema(schemaId).size());
                Calendar calendar = Calendar.getInstance();
                String date = String.format("%1$02d%2$02d%3$04d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));

                model.getAccounts().add(new AccountModel.Account(schemaId,
                        nextSemId, Constants.AccountType.ASSET, textFieldName.getText(), ""));
                model.getTransactions().add(new AccountModel.Transaction(model.getNextTransactionId(), date, "open",
                        "0", schemaId + "." + nextSemId, Constants.Account.INIT_ACC_ID));

                String expenseSemanticId = schemaId.substring(2, 3) + "-" + nextSemId;
                model.getAccounts().add(new AccountModel.Account("58" + schemaId.substring(1, 2),
                        expenseSemanticId, Constants.AccountType.EXPENSE, "Dep. of " + textFieldName.getText(), ""));
                model.getTransactions().add(new AccountModel.Transaction(model.getNextTransactionId(), date, "open",
                        "0", "58" + schemaId.substring(1, 2) + "." + expenseSemanticId, Constants.Account.INIT_ACC_ID));

                getConfiguration().update(Configuration.ACCOUNT_UPDATED);
            });

            buttonCancelAdd.addActionListener(e -> {
                buttonAdd.setVisible(true);
                textFieldName.setVisible(false);
                buttonConfirmAdd.setVisible(false);
                buttonCancelAdd.setVisible(false);
            });

            panelAccounts = new JPanel();
            panelAccounts.setLayout(new BoxLayout(panelAccounts, BoxLayout.Y_AXIS));

            GroupLayout layout = new GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(layout.createParallelGroup().addComponent(panelAccounts).addComponent(panelAddAcc));
            layout.setVerticalGroup(layout.createSequentialGroup().addComponent(panelAccounts).addComponent(panelAddAcc));
        }
        public void update(){
            panelAccounts.removeAll();
            for (AccountModel.Account semAcc : getConfiguration ().getModel().getAccountModel().getAccountsBySchema(schemaId)){
                JLabel label = new JLabel(" " + semAcc.getSemanticId() + " " + semAcc.getName());
                label.setFont(new Font(label.getFont().getName(), Font.BOLD, 15));
                panelAccounts.add(label);
            }
            panelAccounts.revalidate();
            panelAccounts.repaint();
        }
    }
}
