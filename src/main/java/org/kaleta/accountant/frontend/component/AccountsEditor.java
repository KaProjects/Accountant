package org.kaleta.accountant.frontend.component;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.listener.AccountsEditorAccountAction;
import org.kaleta.accountant.frontend.common.IconLoader;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class AccountsEditor extends JPanel implements Configurable {
    private Configuration configuration;

    private List<ContentPanel> contentPanelList;

    public AccountsEditor(Configuration configuration) {
        setConfiguration(configuration);
        contentPanelList = new ArrayList<>();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void resetEditor(SchemaModel.Class clazz){
        this.removeAll();
        for (SchemaModel.Class.Group group : clazz.getGroup()){
            JPanel groupContent = new JPanel();
            groupContent.setLayout(new BoxLayout(groupContent, BoxLayout.Y_AXIS));
            for (SchemaModel.Class.Group.Account schemaAcc : group.getAccount()) {
                String schemaId = String.valueOf(clazz.getId()) + String.valueOf(group.getId()) + String.valueOf(schemaAcc.getId());
                ContentPanel panelContent = new ContentPanel(schemaId);
                panelContent.update();
                groupContent.add(getListPanel(schemaAcc.getName(), panelContent));
                contentPanelList.add(panelContent);
            }
            this.add(getListPanel(group.getName(), groupContent));
        }
        this.revalidate();
        this.repaint();
    }

    public void updateEditor(){
        for (ContentPanel contentPanel : contentPanelList){
            contentPanel.update();
        }
    }

    private JPanel getListPanel(String title, JPanel content){
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
            JButton buttonGeneralAdd = new JButton("General");
            buttonGeneralAdd.setVisible(false);
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
                    .addComponent(buttonGeneralAdd).addGap(5)
                    .addComponent(buttonConfirmAdd).addGap(5)
                    .addComponent(buttonCancelAdd)
                    .addComponent(panelGap));
            layoutAddPanel.setVerticalGroup(layoutAddPanel.createParallelGroup()
                    .addComponent(buttonAdd,25,25,25)
                    .addComponent(textFieldName,25,25,25)
                    .addComponent(buttonGeneralAdd,25,25,25)
                    .addComponent(buttonConfirmAdd,25,25,25)
                    .addComponent(buttonCancelAdd,25,25,25)
                    .addComponent(panelGap,25,25,25));

            buttonAdd.addActionListener(e -> {
                buttonAdd.setVisible(false);
                textFieldName.setText("");
                textFieldName.setVisible(true);
                buttonConfirmAdd.setVisible(true);
                buttonCancelAdd.setVisible(true);
                buttonGeneralAdd.setVisible(true);
            });

            buttonGeneralAdd.addActionListener(e -> {
                textFieldName.setText(Constants.Account.GENERAL_ACCOUNT_NAME);
            });

            buttonConfirmAdd.addActionListener(new AccountsEditorAccountAction(AccountsEditor.this, schemaId, textFieldName));
            buttonConfirmAdd.addActionListener(e -> {
                buttonAdd.setVisible(true);
                textFieldName.setVisible(false);
                buttonConfirmAdd.setVisible(false);
                buttonCancelAdd.setVisible(false);
                buttonGeneralAdd.setVisible(false);
            });

            buttonCancelAdd.addActionListener(e -> {
                buttonAdd.setVisible(true);
                textFieldName.setVisible(false);
                buttonConfirmAdd.setVisible(false);
                buttonCancelAdd.setVisible(false);
                buttonGeneralAdd.setVisible(false);
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
            for (AccountsModel.Account semAcc : Service.ACCOUNT.getAccountsBySchemaId(getConfiguration().getSelectedYear(), schemaId)){
                // TODO post 1.0 : design&impl (edit, close?/del?,...)
                JLabel label = new JLabel(" " + semAcc.getSemanticId() + " " + semAcc.getName());
                label.setFont(new Font(label.getFont().getName(), Font.BOLD, 15));
                panelAccounts.add(label);
            }
            panelAccounts.revalidate();
            panelAccounts.repaint();
        }
    }
}
