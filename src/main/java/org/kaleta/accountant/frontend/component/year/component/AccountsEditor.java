package org.kaleta.accountant.frontend.component.year.component;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.configuration.ConfigurationAction;
import org.kaleta.accountant.frontend.component.year.model.AccountModel;
import org.kaleta.accountant.frontend.component.year.model.SchemaModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by Stanislav Kaleta on 14.02.2017.
 */
public class AccountsEditor extends JPanel implements Configurable {
    private Configuration configuration;
    private JPanel panelClasses;
    private JPanel panelGroups;
    private JPanel panelAccounts;
    private JPanel panelOpenAccounts;

    public AccountsEditor() {
        panelClasses = new JPanel();
        panelClasses.setLayout(new GridBagLayout());
        panelClasses.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2, true));
        panelGroups = new JPanel();
        panelGroups.setLayout(new GridBagLayout());
        panelGroups.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2, true));
        panelAccounts = new JPanel();
        panelAccounts.setLayout(new GridBagLayout());
        panelAccounts.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2, true));
        panelOpenAccounts = new JPanel();
        panelOpenAccounts.setLayout(new BoxLayout(panelOpenAccounts, BoxLayout.Y_AXIS));
        panelOpenAccounts.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2, true));

        this.setLayout(new GridBagLayout());
        this.add(panelClasses, new GridBagConstraints(0,0,1,1,0,1,GridBagConstraints.FIRST_LINE_START,GridBagConstraints.VERTICAL,new Insets(0,0,0,0),0,0));
        this.add(panelGroups, new GridBagConstraints(1,0,1,1,0,1,GridBagConstraints.PAGE_START,GridBagConstraints.VERTICAL,new Insets(0,0,0,0),0,0));
        this.add(panelAccounts, new GridBagConstraints(2,0,1,1,0,1,GridBagConstraints.PAGE_START,GridBagConstraints.VERTICAL,new Insets(0,0,0,0),0,0));
        this.add(panelOpenAccounts, new GridBagConstraints(3,0,1,1,1,1,GridBagConstraints.FIRST_LINE_END,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));

        this.getActionMap().put(Configuration.SCHEMA_UPDATED, new ConfigurationAction(this) {
            @Override
            protected void actionPerformed() {
                AccountsEditor.this.update();
            }
        });

        this.getActionMap().put(Configuration.ACCOUNT_UPDATED, new ConfigurationAction(this) {
            @Override
            protected void actionPerformed() {
                String ttt = panelOpenAccounts.getToolTipText();
                if (ttt.length() == 3){
                    AccountsEditor.this.setUpOpenAccountsEditor(Integer.parseInt(ttt.substring(0,1)),Integer.parseInt(ttt.substring(1,2)),Integer.parseInt(ttt.substring(2,3)));
                    // TODO: 25.2.2017 make this more smoothly...
                }
            }
        });
    }

    public void update(){
        Color bgClicked = Color.LIGHT_GRAY;
        Color bgNotFocused = new JPanel().getBackground();
        Font font = new Font(new JLabel().getFont().getName(),Font.PLAIN,20);
        panelClasses.removeAll();
        panelGroups.removeAll();
        panelAccounts.removeAll();
        panelOpenAccounts.removeAll();panelAccounts.setToolTipText("");
        for (SchemaModel.Clazz clazz : getConfiguration ().getModel().getSchemaModel().getClasses().values()){
            JLabel labelClass = new JLabel(" " + clazz.getName() + " ");
            panelClasses.add(labelClass, new GridBagConstraints(0,clazz.getId(),1,1,0,0,GridBagConstraints.PAGE_START,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
            labelClass.setFont(font);
            labelClass.setOpaque(true);
            labelClass.setBackground(bgNotFocused);
            labelClass.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    for (Component component : panelClasses.getComponents()){
                        component.setBackground(bgNotFocused);
                    }
                    labelClass.setBackground(bgClicked);
                    panelGroups.removeAll();
                    panelAccounts.removeAll();
                    panelOpenAccounts.removeAll();panelAccounts.setToolTipText("");
                    for (SchemaModel.Clazz.Group group : clazz.getGroups().values()){
                        JLabel labelGroup = new JLabel(" " + group.getName() + " ");
                        panelGroups.add(labelGroup, new GridBagConstraints(0,group.getId(),1,1,0,0,GridBagConstraints.PAGE_START,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
                        labelGroup.setFont(font);
                        labelGroup.setOpaque(true);
                        labelGroup.setBackground(bgNotFocused);
                        labelGroup.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseReleased(MouseEvent e) {
                                for (Component component : panelGroups.getComponents()){
                                    component.setBackground(bgNotFocused);
                                }
                                labelGroup.setBackground(bgClicked);
                                panelAccounts.removeAll();
                                panelOpenAccounts.removeAll();panelAccounts.setToolTipText("");
                                for (SchemaModel.Clazz.Group.Account account : group.getAccounts().values()){
                                    JLabel labelAcc = new JLabel(" " + account.getName() + " ");
                                    panelAccounts.add(labelAcc, new GridBagConstraints(0,account.getId(),1,1,0,0,GridBagConstraints.PAGE_START,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
                                    labelAcc.setFont(font);
                                    labelAcc.setOpaque(true);
                                    labelAcc.setBackground(bgNotFocused);
                                    labelAcc.addMouseListener(new MouseAdapter() {
                                        @Override
                                        public void mouseReleased(MouseEvent e) {
                                            for (Component component : panelAccounts.getComponents()){
                                                component.setBackground(bgNotFocused);
                                            }
                                            labelAcc.setBackground(bgClicked);
                                            setUpOpenAccountsEditor(clazz.getId(), group.getId(), account.getId());
                                        }
                                    });
                                }
                                panelAccounts.add(new JPanel(), new GridBagConstraints(0,10,1,1,0,1,GridBagConstraints.PAGE_START,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
                                AccountsEditor.this.revalidate();
                                AccountsEditor.this.repaint();
                            }
                        });
                    }
                    panelGroups.add(new JPanel(), new GridBagConstraints(0,10,1,1,0,1,GridBagConstraints.PAGE_START,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
                    AccountsEditor.this.revalidate();
                    AccountsEditor.this.repaint();
                }
            });
        }
        panelClasses.add(new JPanel(), new GridBagConstraints(0,10,1,1,0,1,GridBagConstraints.PAGE_START,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
        this.revalidate();
        this.repaint();
    }

    private void setUpOpenAccountsEditor(int cId, int gId, int aId){
        panelOpenAccounts.removeAll();
        String schemaId = String.valueOf(cId) + String.valueOf(gId) + String.valueOf(aId);
        panelOpenAccounts.setToolTipText(schemaId);
        for (AccountModel.Account account : getConfiguration().getModel().getAccountModel().getAccountsBySchema(schemaId)) {
            panelOpenAccounts.add(
                    new JLabel(account.getSchemaId() + "." + account.getSemanticId() + " " + account.getName() + " balance="
                            + getConfiguration().getModel().getAccountModel().getAccBalance(account) + " turnover="
                            + getConfiguration().getModel().getAccountModel().getAccTurnover(account))/*,
            new GridBagConstraints(0, 0, 1, 1, 0, 1, GridBagConstraints.PAGE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0)*/);
            // TODO: 2/16/17 update / eventually add func.
        }


        panelOpenAccounts.revalidate();
        panelOpenAccounts.repaint();
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
