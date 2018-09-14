package org.kaleta.accountant.frontend.core;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.IconLoader;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

public class BalanceOverview extends JPanel implements Configurable {
    private Configuration configuration;

    public BalanceOverview(){
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

    public void update() {
        String year = getConfiguration().getSelectedYear();
        Map<String, java.util.List<AccountsModel.Account>> accountMap =  Service.ACCOUNT.getAccountsViaSchemaMap(year);

        JPanel assetsContent = new JPanel();
        assetsContent.setName("context");
        assetsContent.setLayout(new BoxLayout(assetsContent, BoxLayout.Y_AXIS));
        int assetsBalance = 0;
        for (SchemaModel.Class clazz : Service.SCHEMA.getSchemaClassListByAccountType(year, Constants.AccountType.ASSET)){
            JPanel classContent = new JPanel();
            classContent.setName("context");
            classContent.setLayout(new BoxLayout(classContent, BoxLayout.Y_AXIS));
            int classBalance = 0;
            for (SchemaModel.Class.Group group : clazz.getGroup()) {
                JPanel groupContent = new JPanel();
                groupContent.setName("context");
                groupContent.setLayout(new BoxLayout(groupContent, BoxLayout.Y_AXIS));
                int groupBalance = 0;
                for (SchemaModel.Class.Group.Account schemaAccount : group.getAccount()) {
                    int accBalance = 0;
                    String schemaId = clazz.getId() + group.getId() + schemaAccount.getId();
                    if (accountMap.get(schemaId) != null) {
                        for (AccountsModel.Account account : accountMap.get(schemaId)){
                            accBalance += Integer.parseInt(Service.TRANSACTIONS.getAccountBalance(year, account));
                        }
                    }
                    HeaderPanel accountHeader = new HeaderPanel(schemaAccount.getName(), schemaId, String.valueOf(accBalance), HeaderPanel.ACCOUNT);
                    groupContent.add(accountHeader);
                    groupBalance += accBalance;
                }
                HeaderPanel groupHeader = new HeaderPanel(group.getName(), clazz.getId() + group.getId(), String.valueOf(groupBalance), HeaderPanel.GROUP);
                classContent.add(getListPanel(groupHeader, groupContent));
                classBalance += groupBalance;
            }
            HeaderPanel classHeader = new HeaderPanel(clazz.getName(), clazz.getId(), String.valueOf(classBalance), HeaderPanel.CLASS);
            assetsContent.add(getListPanel(classHeader, classContent));
            assetsBalance += classBalance;
        }
        HeaderPanel assetsHeader = new HeaderPanel("Debit Sum", "A", String.valueOf(assetsBalance), HeaderPanel.SUM);
        JPanel assetsPanel = getListPanel(assetsHeader, assetsContent);
        assetsPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));


        JPanel liabilitiesContent = new JPanel();
        liabilitiesContent.setName("context");
        liabilitiesContent.setLayout(new BoxLayout(liabilitiesContent, BoxLayout.Y_AXIS));
        int liabilitiesBalance = 0;
        for (SchemaModel.Class clazz : Service.SCHEMA.getSchemaClassListByAccountType(year, Constants.AccountType.LIABILITY)){
            JPanel classContent = new JPanel();
            classContent.setName("context");
            classContent.setLayout(new BoxLayout(classContent, BoxLayout.Y_AXIS));
            int classBalance = 0;
            for (SchemaModel.Class.Group group : clazz.getGroup()) {
                JPanel groupContent = new JPanel();
                groupContent.setName("context");
                groupContent.setLayout(new BoxLayout(groupContent, BoxLayout.Y_AXIS));
                int groupBalance = 0;
                for (SchemaModel.Class.Group.Account schemaAccount : group.getAccount()) {
                    int accBalance = 0;
                    String schemaId = clazz.getId() + group.getId() + schemaAccount.getId();
                    if (accountMap.get(schemaId) != null) {
                        for (AccountsModel.Account account : accountMap.get(schemaId)){
                            accBalance += Integer.parseInt(Service.TRANSACTIONS.getAccountBalance(year, account));
                        }
                    }
                    HeaderPanel accountHeader = new HeaderPanel(schemaAccount.getName(), schemaId, String.valueOf(accBalance), HeaderPanel.ACCOUNT);
                    groupContent.add(accountHeader);
                    groupBalance += accBalance;
                }
                HeaderPanel groupHeader = new HeaderPanel(group.getName(), clazz.getId() + group.getId(), String.valueOf(groupBalance), HeaderPanel.GROUP);
                classContent.add(getListPanel(groupHeader, groupContent));
                classBalance += groupBalance;
            }
            HeaderPanel classHeader = new HeaderPanel(clazz.getName(), clazz.getId(), String.valueOf(classBalance), HeaderPanel.CLASS);
            liabilitiesContent.add(getListPanel(classHeader, classContent));
            liabilitiesBalance += classBalance;
        }

        int profit = assetsBalance - liabilitiesBalance;
        HeaderPanel profitHeader = new HeaderPanel("Profit", "P", String.valueOf(profit), HeaderPanel.SUM);
        liabilitiesContent.add(profitHeader);
        liabilitiesBalance += profit;

        HeaderPanel liabilitiesHeader = new HeaderPanel("Credit Sum", "L", String.valueOf(liabilitiesBalance), HeaderPanel.SUM);
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


    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }


    private JPanel getListPanel(HeaderPanel header, JPanel body){
        JPanel thisPanel = new JPanel();
        thisPanel.setBackground(Color.white);

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                body.setVisible(!body.isVisible());}
        };

        switch (header.getType()) {
            case HeaderPanel.SUM: {
                header.addMouseListener(new MouseAdapter() {
                    private boolean toggle = true;
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        for (Component child : body.getComponents()){
                            updateComponentTree((JComponent) child, toggle);
                        }
                        toggle = !toggle;
                    }
                });
                break;
            }
            case HeaderPanel.CLASS: {
                header.addMouseListener(mouseAdapter);
                body.setVisible(false);
                break;
            }
            case HeaderPanel.GROUP: {
                header.addMouseListener(mouseAdapter);
                body.setVisible(false);
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

    private void updateComponentTree(JComponent component, boolean flag) {
        for (Component child : component.getComponents()) {
            if (component.getName() != null && component.getName().equals("context")) component.setVisible(flag);
            try {
                updateComponentTree((JComponent) child, flag);
            } catch (ClassCastException ex){
                // continue
            }
        }
    }

    private class HeaderPanel extends JPanel {
        public static final String SUM = "SUM";
        public static final String CLASS = "CLASS";
        public static final String GROUP = "GROUP";
        public static final String ACCOUNT = "ACC";

        private String schemaId;
        private String type;

        public HeaderPanel(String name, String schemaId, String balance, String type) {
            this.schemaId = schemaId;
            this.type = type;

            int height;
            Font font;
            Color color;
            switch (type) {
                case SUM:
                case CLASS: {
                    font = new Font(new JLabel().getFont().getName(), Font.BOLD, 25);
                    color = Color.LIGHT_GRAY.darker();
                    height = 35;
                    break;
                }
                case GROUP: {
                    font = new Font(new JLabel().getFont().getName(), Font.BOLD, 20);
                    color = Color.LIGHT_GRAY;
                    height = 30;
                    break;
                }
                case ACCOUNT: {
                    font = new Font(new JLabel().getFont().getName(), Font.BOLD, 15);
                    color = Color.WHITE;
                    height = 25;
                    break;
                }
                default: {
                    height = 0;
                    font = new JLabel().getFont();
                    color = new JLabel().getBackground();
                }
            }

            this.setBackground(color);

            JLabel labelName = new JLabel(" " + name);
            labelName.setFont(font);

            JLabel buttonGraph = new JLabel(IconLoader.getIcon(IconLoader.CHART, new Dimension(20,20)));
            buttonGraph.setOpaque(true);
            buttonGraph.setBackground(color);
            buttonGraph.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    // TODO: 6.1.2018 open graph and stuff..
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    buttonGraph.setBackground(color.darker());
                    buttonGraph.repaint();
                    buttonGraph.revalidate();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    buttonGraph.setBackground(color);
                    buttonGraph.repaint();
                    buttonGraph.revalidate();
                }
            });
            if (schemaId.equals("P")) buttonGraph.setVisible(false);

            JPanel panelHeader = new JPanel();
            panelHeader.setLayout(new BoxLayout(panelHeader, BoxLayout.X_AXIS));
            panelHeader.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            panelHeader.setOpaque(false);
            panelHeader.add(labelName);
            panelHeader.add(Box.createHorizontalGlue());
            panelHeader.add(buttonGraph);
            panelHeader.add(Box.createHorizontalStrut(5));

            JLabel labelValue = new JLabel(balance + " ", SwingConstants.RIGHT);
            labelValue.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            labelValue.setFont(font);

            GroupLayout layout = new GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(layout.createSequentialGroup()
                    .addComponent(panelHeader,320,320, Short.MAX_VALUE)
                    .addComponent(labelValue,140,140,140));
            layout.setVerticalGroup(layout.createParallelGroup()
                    .addComponent(panelHeader,height,height,height)
                    .addComponent(labelValue,height,height,height));
        }

        public String getSchemaId() {
            return schemaId;
        }

        public String getType() {
            return type;
        }
    }
}
