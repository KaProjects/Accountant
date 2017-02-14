package org.kaleta.accountant.frontend.component.year.component;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.configuration.ConfigurationAction;
import org.kaleta.accountant.frontend.action.listener.AccountActionListener;
import org.kaleta.accountant.frontend.action.listener.GroupActionListener;
import org.kaleta.accountant.frontend.common.IconLoader;
import org.kaleta.accountant.frontend.component.year.model.SchemaModel;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Stanislav Kaleta on 14.02.2017.
 */
public class SchemaEditor extends JTabbedPane implements Configurable {
    private Configuration configuration;
    private int[][] classesDef = new int[][]{{0,9},{1,10}}; // [classId,numberOfEditableGroups]
    private Map<Integer, JPanel> editorClassPanels;

    public SchemaEditor(Configuration configuration){
        setConfiguration(configuration);
        editorClassPanels = new HashMap<>();
        for (int[] cidArray : classesDef) {
            JPanel classPanel = new JPanel();
            this.addTab(getConfiguration().getModel().getSchemaModel().getClasses().get(cidArray[0]).getName(), classPanel);
            classPanel.setLayout(new GridBagLayout());
            editorClassPanels.put(cidArray[0], classPanel);
        }

        this.getActionMap().put(Configuration.SCHEMA_UPDATED, new ConfigurationAction(this) {
            @Override
            protected void actionPerformed() {
                SchemaEditor.this.update();
            }
        });
    }

    public void update(){
        for (int[] cIdArray : classesDef) {
            JPanel classPanel = editorClassPanels.get(cIdArray[0]);
            classPanel.removeAll();
            for (int gId = 0; gId < cIdArray[1]; gId++) {
                GridBagConstraints groupConstraints = new GridBagConstraints(gId, 0, 1, 1, 1, 1, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);
                SchemaModel.Clazz.Group group = getConfiguration().getModel().getSchemaModel().getClasses().get(cIdArray[0]).getGroups().get(gId);
                if (group == null) {
                    JButton buttonAddGroup = new JButton(IconLoader.getIcon(IconLoader.ADD, new Dimension(15, 15)));
                    buttonAddGroup.setBackground(Color.LIGHT_GRAY);
                    buttonAddGroup.addActionListener(new GroupActionListener(this, GroupActionListener.CREATE, cIdArray[0], gId));
                    classPanel.add(buttonAddGroup, groupConstraints);
                } else {
                    JPanel groupPanel = new JPanel();
                    groupPanel.setLayout(new GridBagLayout());
                    groupPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), group.getName()));
                    groupPanel.setBackground(Color.GRAY);

                    JButton buttonEditGroup = new JButton(IconLoader.getIcon(IconLoader.EDIT, new Dimension(10, 10)));
                    buttonEditGroup.setBackground(Color.GRAY);
                    buttonEditGroup.setMinimumSize(new Dimension(25, 25));
                    buttonEditGroup.setPreferredSize(new Dimension(25, 25));
                    buttonEditGroup.setMaximumSize(new Dimension(25, 25));
                    buttonEditGroup.addActionListener(new GroupActionListener(this, GroupActionListener.EDIT, cIdArray[0], gId));
                    groupPanel.add(buttonEditGroup, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

                    JButton buttonDelGroup = new JButton(IconLoader.getIcon(IconLoader.DELETE, new Dimension(10, 10)));
                    buttonDelGroup.setBackground(Color.GRAY);
                    buttonDelGroup.setMinimumSize(new Dimension(25, 25));
                    buttonDelGroup.setPreferredSize(new Dimension(25, 25));
                    buttonDelGroup.setMaximumSize(new Dimension(25, 25));
                    buttonDelGroup.addActionListener(new GroupActionListener(this, GroupActionListener.DELETE, cIdArray[0], gId));
                    groupPanel.add(buttonDelGroup, new GridBagConstraints(2, 0, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

                    int width = new JLabel().getFontMetrics(new JLabel().getFont()).stringWidth(group.getName()) + 20;
                    for (int aId = 0; aId < 10; aId++) {
                        GridBagConstraints accConstraints = new GridBagConstraints(0, aId + 1, 3, 1, 1, 1, GridBagConstraints.PAGE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);
                        SchemaModel.Clazz.Group.Account account = group.getAccounts().get(aId);
                        if (account == null) {
                            JButton buttonAddAccount = new JButton(IconLoader.getIcon(IconLoader.ADD, new Dimension(10, 10)));
                            buttonAddAccount.setBackground(Color.LIGHT_GRAY);
                            buttonAddAccount.addActionListener(new AccountActionListener(this, AccountActionListener.CREATE, cIdArray[0],gId,aId));
                            groupPanel.add(buttonAddAccount, accConstraints);
                        } else {
                            String text = " " + account.getType() + " " + account.getName() + " ";
                            JLabel label = new JLabel(text);
                            int labelWidth = label.getFontMetrics(label.getFont()).stringWidth(text) + 10 + 50;
                            width = (labelWidth > width) ? labelWidth : width;

                            JPanel accPanel = new JPanel();
                            accPanel.setLayout(new BoxLayout(accPanel, BoxLayout.X_AXIS));
                            accPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                            accPanel.setBackground(Color.GRAY);
                            accPanel.add(label);
                            accPanel.add(Box.createHorizontalGlue());

                            JButton buttonEdit = new JButton(IconLoader.getIcon(IconLoader.EDIT, new Dimension(10, 10)));
                            buttonEdit.setBackground(Color.GRAY);
                            buttonEdit.setMinimumSize(new Dimension(25, 25));
                            buttonEdit.setPreferredSize(new Dimension(25, 25));
                            buttonEdit.setMaximumSize(new Dimension(25, 25));
                            buttonEdit.addActionListener(new AccountActionListener(this, AccountActionListener.EDIT, cIdArray[0],gId,aId));
                            accPanel.add(buttonEdit);

                            JButton buttonDel = new JButton(IconLoader.getIcon(IconLoader.DELETE, new Dimension(10, 10)));
                            buttonDel.setBackground(Color.GRAY);
                            buttonDel.setMinimumSize(new Dimension(25, 25));
                            buttonDel.setPreferredSize(new Dimension(25, 25));
                            buttonDel.setMaximumSize(new Dimension(25, 25));
                            buttonDel.addActionListener(new AccountActionListener(this, AccountActionListener.DELETE, cIdArray[0],gId,aId));
                            accPanel.add(buttonDel);

                            groupPanel.add(accPanel, accConstraints);
                        }
                    }
                    groupPanel.setMinimumSize(new Dimension(width, 0));
                    groupPanel.setPreferredSize(new Dimension(width, groupPanel.getPreferredSize().height));
                    classPanel.add(groupPanel, groupConstraints);
                }
            }
            classPanel.revalidate();
            classPanel.repaint();
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
